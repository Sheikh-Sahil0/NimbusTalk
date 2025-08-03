package com.example.nimbustalk.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nimbustalk.api.AuthApi
import com.example.nimbustalk.api.UserApi
import com.example.nimbustalk.enums.LoadingState
import com.example.nimbustalk.enums.ValidationError
import com.example.nimbustalk.models.AuthResponse
import com.example.nimbustalk.utils.SharedPrefsHelper
import com.example.nimbustalk.utils.ValidationUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val sharedPrefsHelper: SharedPrefsHelper
) : ViewModel() {

    // Loading state
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState

    // Error and success messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    // Registration success
    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> = _registrationSuccess

    // Validation errors
    private val _emailError = MutableLiveData<ValidationError>()
    val emailError: LiveData<ValidationError> = _emailError

    private val _usernameError = MutableLiveData<ValidationError>()
    val usernameError: LiveData<ValidationError> = _usernameError

    private val _displayNameError = MutableLiveData<ValidationError>()
    val displayNameError: LiveData<ValidationError> = _displayNameError

    private val _passwordError = MutableLiveData<ValidationError>()
    val passwordError: LiveData<ValidationError> = _passwordError

    private val _confirmPasswordError = MutableLiveData<ValidationError>()
    val confirmPasswordError: LiveData<ValidationError> = _confirmPasswordError

    // Form validity
    private val _isFormValid = MutableLiveData<Boolean>()
    val isFormValid: LiveData<Boolean> = _isFormValid

    // Username availability
    private val _usernameAvailable = MutableLiveData<Boolean?>()
    val usernameAvailable: LiveData<Boolean?> = _usernameAvailable

    private val _usernameCheckLoading = MutableLiveData<Boolean>()
    val usernameCheckLoading: LiveData<Boolean> = _usernameCheckLoading

    // Current form values for validation
    private var currentEmail = ""
    private var currentUsername = ""
    private var currentDisplayName = ""
    private var currentPassword = ""
    private var currentConfirmPassword = ""

    // Username check job for debouncing
    private var usernameCheckJob: Job? = null

    init {
        _loadingState.value = LoadingState.IDLE
        _emailError.value = ValidationError.NONE
        _usernameError.value = ValidationError.NONE
        _displayNameError.value = ValidationError.NONE
        _passwordError.value = ValidationError.NONE
        _confirmPasswordError.value = ValidationError.NONE
        _isFormValid.value = false
        _errorMessage.value = ""
        _successMessage.value = ""
        _registrationSuccess.value = false
    }

    /**
     * Register new user
     */
    fun register(email: String, username: String, displayName: String, password: String, confirmPassword: String) {
        if (_loadingState.value == LoadingState.LOADING) return

        viewModelScope.launch {
            try {
                _loadingState.value = LoadingState.LOADING
                _errorMessage.value = ""

                // Final validation
                if (!validateAllFields(email, username, displayName, password, confirmPassword)) {
                    _loadingState.value = LoadingState.ERROR
                    _errorMessage.value = "Please fix the errors before proceeding"
                    return@launch
                }

                // Clean inputs
                val cleanEmail = ValidationUtils.cleanInput(email)
                val cleanUsername = ValidationUtils.sanitizeUsername(username)
                val cleanDisplayName = ValidationUtils.sanitizeDisplayName(displayName)

                // Double-check username availability
                val usernameCheckResponse = userApi.checkUsernameAvailability(cleanUsername)
                if (!usernameCheckResponse.isSuccess() || usernameCheckResponse.data != true) {
                    _loadingState.value = LoadingState.ERROR
                    _usernameError.value = ValidationError.USERNAME_ALREADY_EXISTS
                    _errorMessage.value = "Username is not available"
                    updateFormValidity()
                    return@launch
                }

                // Attempt registration
                val response = authApi.register(cleanEmail, password, cleanUsername, cleanDisplayName)

                if (response.isSuccess() && response.data != null) {
                    handleRegistrationSuccess(response.data)
                } else {
                    handleRegistrationError(response.getErrorMessage())
                }

            } catch (e: Exception) {
                handleRegistrationError("Registration failed: ${e.message}")
            }
        }
    }

    /**
     * Handle successful registration
     */
    private fun handleRegistrationSuccess(authResponse: AuthResponse) {
        try {
            val user = authResponse.user
            val accessToken = authResponse.accessToken
            val refreshToken = authResponse.refreshToken

            if (user != null && !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                // Extract user metadata
                val username = user.userMetadata?.get("username") as? String ?: ""
                val displayName = user.userMetadata?.get("display_name") as? String ?: ""

                // Save authentication data
                sharedPrefsHelper.saveAuthData(
                    userId = user.id,
                    email = user.email,
                    username = username,
                    displayName = displayName,
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )

                _loadingState.value = LoadingState.SUCCESS
                _successMessage.value = "Registration successful! Welcome to NimbusTalk!"
                _registrationSuccess.value = true

            } else {
                handleRegistrationError("Registration response incomplete")
            }
        } catch (e: Exception) {
            handleRegistrationError("Failed to process registration: ${e.message}")
        }
    }

    /**
     * Handle registration error
     */
    private fun handleRegistrationError(message: String) {
        _loadingState.value = LoadingState.ERROR
        _errorMessage.value = when {
            message.contains("email", ignoreCase = true) && message.contains("already", ignoreCase = true) ->
                "Email is already registered. Please try logging in instead."
            message.contains("password", ignoreCase = true) && message.contains("weak", ignoreCase = true) ->
                "Password is too weak. Please use a stronger password."
            message.contains("network", ignoreCase = true) || message.contains("connection", ignoreCase = true) ->
                "Please check your internet connection and try again."
            else -> message
        }
    }

    /**
     * Check username availability with debouncing
     */
    fun checkUsernameAvailability(username: String) {
        // Cancel previous job
        usernameCheckJob?.cancel()

        val cleanUsername = ValidationUtils.sanitizeUsername(username)

        // Validate username format first
        val usernameValidation = ValidationUtils.validateUsername(cleanUsername)
        if (usernameValidation != ValidationError.NONE) {
            _usernameAvailable.value = null
            return
        }

        // Debounce username check
        usernameCheckJob = viewModelScope.launch {
            delay(500) // Wait 500ms before checking

            try {
                _usernameCheckLoading.value = true

                val response = userApi.checkUsernameAvailability(cleanUsername)

                if (response.isSuccess()) {
                    _usernameAvailable.value = response.data
                    if (response.data == false) {
                        _usernameError.value = ValidationError.USERNAME_ALREADY_EXISTS
                    } else {
                        _usernameError.value = ValidationError.NONE
                    }
                } else {
                    _usernameAvailable.value = null
                }

            } catch (e: Exception) {
                _usernameAvailable.value = null
            } finally {
                _usernameCheckLoading.value = false
                updateFormValidity()
            }
        }
    }

    /**
     * Set email validation
     */
    fun setEmailValidation(validationError: ValidationError) {
        _emailError.value = validationError
        updateFormValidity()
    }

    /**
     * Set username validation
     */
    fun setUsernameValidation(validationError: ValidationError) {
        _usernameError.value = validationError
        updateFormValidity()
    }

    /**
     * Set display name validation
     */
    fun setDisplayNameValidation(validationError: ValidationError) {
        _displayNameError.value = validationError
        updateFormValidity()
    }

    /**
     * Set password validation
     */
    fun setPasswordValidation(validationError: ValidationError) {
        _passwordError.value = validationError
        updateFormValidity()
    }

    /**
     * Set confirm password validation
     */
    fun setConfirmPasswordValidation(validationError: ValidationError) {
        _confirmPasswordError.value = validationError
        updateFormValidity()
    }

    /**
     * Update current form values
     */
    fun updateFormValues(email: String, username: String, displayName: String, password: String, confirmPassword: String) {
        currentEmail = email
        currentUsername = username
        currentDisplayName = displayName
        currentPassword = password
        currentConfirmPassword = confirmPassword
        updateFormValidity()
    }

    /**
     * Validate all fields
     */
    private fun validateAllFields(email: String, username: String, displayName: String, password: String, confirmPassword: String): Boolean {
        val emailValid = ValidationUtils.validateEmail(email) == ValidationError.NONE
        val usernameValid = ValidationUtils.validateUsername(username) == ValidationError.NONE
        val displayNameValid = ValidationUtils.validateDisplayName(displayName) == ValidationError.NONE
        val passwordValid = ValidationUtils.validatePassword(password) == ValidationError.NONE
        val confirmPasswordValid = ValidationUtils.validateConfirmPassword(password, confirmPassword) == ValidationError.NONE
        val usernameAvailable = _usernameAvailable.value == true

        return emailValid && usernameValid && displayNameValid && passwordValid && confirmPasswordValid && usernameAvailable
    }

    /**
     * Update form validity
     */
    private fun updateFormValidity() {
        val isValid = _emailError.value == ValidationError.NONE &&
                _usernameError.value == ValidationError.NONE &&
                _displayNameError.value == ValidationError.NONE &&
                _passwordError.value == ValidationError.NONE &&
                _confirmPasswordError.value == ValidationError.NONE &&
                _usernameAvailable.value == true &&
                currentEmail.isNotBlank() &&
                currentUsername.isNotBlank() &&
                currentDisplayName.isNotBlank() &&
                currentPassword.isNotBlank() &&
                currentConfirmPassword.isNotBlank()

        _isFormValid.value = isValid
    }

    /**
     * Check if currently loading
     */
    fun isLoading(): Boolean {
        return _loadingState.value == LoadingState.LOADING
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = ""
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        _successMessage.value = ""
    }

    /**
     * Reset registration success state
     */
    fun resetRegistrationSuccess() {
        _registrationSuccess.value = false
    }

    /**
     * Clear all validation errors
     */
    fun clearValidationErrors() {
        _emailError.value = ValidationError.NONE
        _usernameError.value = ValidationError.NONE
        _displayNameError.value = ValidationError.NONE
        _passwordError.value = ValidationError.NONE
        _confirmPasswordError.value = ValidationError.NONE
        updateFormValidity()
    }

    override fun onCleared() {
        super.onCleared()
        usernameCheckJob?.cancel()
    }
}