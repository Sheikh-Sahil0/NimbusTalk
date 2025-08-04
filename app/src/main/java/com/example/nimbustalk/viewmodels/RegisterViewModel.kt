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
import android.util.Log

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

                Log.d("RegisterViewModel", "Attempting registration for email: $cleanEmail, username: $cleanUsername")

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

                Log.d("RegisterViewModel", "Registration response status: ${response.status}")
                Log.d("RegisterViewModel", "Registration response success: ${response.isSuccess()}")

                if (response.isSuccess() && response.data != null) {
                    Log.d("RegisterViewModel", "Registration successful, processing auth data")
                    handleRegistrationSuccess(response.data)
                } else {
                    val errorMessage = response.getUserFriendlyErrorMessage()
                    Log.e("RegisterViewModel", "Registration failed: $errorMessage")
                    handleRegistrationError(errorMessage)
                }

            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Registration exception", e)
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

            Log.d("RegisterViewModel", "Auth response - User: ${user?.id}, Token: ${!accessToken.isNullOrBlank()}")

            if (user != null && !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                // Extract user metadata
                val username = user.userMetadata?.get("username") as? String ?: ""
                val displayName = user.userMetadata?.get("display_name") as? String ?: ""

                Log.d("RegisterViewModel", "Saving auth data for user: $username")

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
                Log.e("RegisterViewModel", "Incomplete registration response")
                handleRegistrationError("Registration response incomplete. Please try again.")
            }
        } catch (e: Exception) {
            Log.e("RegisterViewModel", "Error processing registration success", e)
            handleRegistrationError("Failed to complete registration: ${e.message}")
        }
    }

    /**
     * Handle registration error with specific error messages
     */
    private fun handleRegistrationError(message: String) {
        _loadingState.value = LoadingState.ERROR

        // The message should already be user-friendly from getUserFriendlyErrorMessage()
        // But we can add additional context and handle validation errors
        _errorMessage.value = when {
            message.contains("email is already registered", ignoreCase = true) ||
                    message.contains("User already registered", ignoreCase = true) -> {
                // Also set email validation error
                _emailError.value = ValidationError.EMAIL_ALREADY_EXISTS
                updateFormValidity()
                "This email is already registered. Please try logging in instead."
            }

            message.contains("username is already taken", ignoreCase = true) ||
                    (message.contains("duplicate", ignoreCase = true) && message.contains("username", ignoreCase = true)) -> {
                // Also set username validation error
                _usernameError.value = ValidationError.USERNAME_ALREADY_EXISTS
                _usernameAvailable.value = false
                updateFormValidity()
                "This username is already taken. Please choose a different one."
            }

            message.contains("Password is too weak", ignoreCase = true) ||
                    message.contains("password should be at least", ignoreCase = true) -> {
                // Also set password validation error
                _passwordError.value = ValidationError.PASSWORD_WEAK
                updateFormValidity()
                "Password is too weak. Please include letters, numbers, and special characters."
            }

            message.contains("valid email", ignoreCase = true) -> {
                // Also set email validation error
                _emailError.value = ValidationError.EMAIL_INVALID
                updateFormValidity()
                "Please enter a valid email address."
            }

            message.contains("network", ignoreCase = true) ||
                    message.contains("connection", ignoreCase = true) ->
                "Please check your internet connection and try again."

            message.contains("timeout", ignoreCase = true) ->
                "Registration request timed out. Please try again."

            message.contains("server error", ignoreCase = true) ->
                "Server temporarily unavailable. Please try again in a moment."

            message.contains("too many", ignoreCase = true) ||
                    message.contains("rate limit", ignoreCase = true) ->
                "Too many attempts. Please wait a moment and try again."

            else -> message
        }

        Log.e("RegisterViewModel", "Registration error: ${_errorMessage.value}")
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
                Log.d("RegisterViewModel", "Checking username availability: $cleanUsername")

                val response = userApi.checkUsernameAvailability(cleanUsername)

                if (response.isSuccess()) {
                    val isAvailable = response.data ?: false
                    _usernameAvailable.value = isAvailable

                    Log.d("RegisterViewModel", "Username $cleanUsername availability: $isAvailable")

                    if (!isAvailable) {
                        _usernameError.value = ValidationError.USERNAME_ALREADY_EXISTS
                    } else {
                        _usernameError.value = ValidationError.NONE
                    }
                } else {
                    Log.e("RegisterViewModel", "Username check failed: ${response.getErrorMessage()}")
                    _usernameAvailable.value = null
                }

            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Username check exception", e)
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