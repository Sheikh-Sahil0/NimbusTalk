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
import kotlinx.coroutines.launch

class LoginViewModel(
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

    // Login success
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    // Validation errors
    private val _emailError = MutableLiveData<ValidationError>()
    val emailError: LiveData<ValidationError> = _emailError

    private val _passwordError = MutableLiveData<ValidationError>()
    val passwordError: LiveData<ValidationError> = _passwordError

    // Form validity
    private val _isFormValid = MutableLiveData<Boolean>()
    val isFormValid: LiveData<Boolean> = _isFormValid

    // Current form values for validation
    private var currentEmail = ""
    private var currentPassword = ""

    init {
        _loadingState.value = LoadingState.IDLE
        _emailError.value = ValidationError.NONE
        _passwordError.value = ValidationError.NONE
        _isFormValid.value = false
        _errorMessage.value = ""
        _successMessage.value = ""
        _loginSuccess.value = false
    }

    /**
     * Login user
     */
    fun login(email: String, password: String) {
        if (_loadingState.value == LoadingState.LOADING) return

        viewModelScope.launch {
            try {
                _loadingState.value = LoadingState.LOADING
                _errorMessage.value = ""

                // Validate inputs
                val cleanEmail = ValidationUtils.cleanInput(email)
                if (!validateLoginForm(cleanEmail, password)) {
                    _loadingState.value = LoadingState.ERROR
                    _errorMessage.value = "Please fix the errors before proceeding"
                    return@launch
                }

                // Attempt login
                val response = authApi.login(cleanEmail, password)

                if (response.isSuccess() && response.data != null) {
                    handleLoginSuccess(response.data, cleanEmail)
                } else {
                    handleLoginError(response.getErrorMessage())
                }

            } catch (e: Exception) {
                handleLoginError("Login failed: ${e.message}")
            }
        }
    }

    /**
     * Handle successful login
     */
    private suspend fun handleLoginSuccess(authResponse: AuthResponse, email: String) {
        try {
            val user = authResponse.user
            val accessToken = authResponse.accessToken
            val refreshToken = authResponse.refreshToken

            if (user != null && !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                // Get complete user profile from database
                val userProfileResponse = userApi.getUserProfile(user.id, accessToken)

                if (userProfileResponse.isSuccess() && userProfileResponse.data != null) {
                    val userProfile = userProfileResponse.data

                    // Save authentication data with complete profile
                    sharedPrefsHelper.saveAuthData(
                        userId = userProfile.id,
                        email = userProfile.email,
                        username = userProfile.username,
                        displayName = userProfile.displayName,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        profileImageUrl = userProfile.profileImageUrl
                    )

                    _loadingState.value = LoadingState.SUCCESS
                    _successMessage.value = "Welcome back, ${userProfile.displayName}!"
                    _loginSuccess.value = true

                } else {
                    // Fallback: Save basic auth data from auth response
                    val username = user.userMetadata?.get("username") as? String ?: ""
                    val displayName = user.userMetadata?.get("display_name") as? String ?: ""

                    sharedPrefsHelper.saveAuthData(
                        userId = user.id,
                        email = user.email,
                        username = username,
                        displayName = displayName,
                        accessToken = accessToken,
                        refreshToken = refreshToken
                    )

                    _loadingState.value = LoadingState.SUCCESS
                    _successMessage.value = "Welcome back!"
                    _loginSuccess.value = true
                }

            } else {
                handleLoginError("Login response incomplete")
            }
        } catch (e: Exception) {
            handleLoginError("Failed to process login: ${e.message}")
        }
    }

    /**
     * Handle login error
     */
    private fun handleLoginError(message: String) {
        _loadingState.value = LoadingState.ERROR
        _errorMessage.value = when {
            message.contains("invalid", ignoreCase = true) ||
                    message.contains("credentials", ignoreCase = true) ||
                    message.contains("password", ignoreCase = true) && message.contains("wrong", ignoreCase = true) ->
                "Invalid email or password. Please check your credentials and try again."

            message.contains("email", ignoreCase = true) && message.contains("not found", ignoreCase = true) ->
                "Account not found. Please check your email or create a new account."

            message.contains("email", ignoreCase = true) && message.contains("confirmed", ignoreCase = true) ->
                "Please verify your email address before logging in."

            message.contains("too many", ignoreCase = true) || message.contains("rate limit", ignoreCase = true) ->
                "Too many login attempts. Please wait a few minutes and try again."

            message.contains("network", ignoreCase = true) || message.contains("connection", ignoreCase = true) ->
                "Please check your internet connection and try again."

            message.contains("timeout", ignoreCase = true) ->
                "Login request timed out. Please try again."

            else -> "Login failed. Please try again."
        }
    }

    /**
     * Validate login form
     */
    private fun validateLoginForm(email: String, password: String): Boolean {
        val emailValidation = ValidationUtils.validateEmail(email)
        val passwordValidation = if (password.isEmpty()) ValidationError.PASSWORD_EMPTY else ValidationError.NONE

        _emailError.value = emailValidation
        _passwordError.value = passwordValidation

        updateFormValidity()

        return emailValidation == ValidationError.NONE && passwordValidation == ValidationError.NONE
    }

    /**
     * Set email validation
     */
    fun setEmailValidation(validationError: ValidationError) {
        _emailError.value = validationError
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
     * Update current form values
     */
    fun updateFormValues(email: String, password: String) {
        currentEmail = email
        currentPassword = password
        updateFormValidity()
    }

    /**
     * Update form validity
     */
    private fun updateFormValidity() {
        val isValid = _emailError.value == ValidationError.NONE &&
                _passwordError.value == ValidationError.NONE &&
                currentEmail.isNotBlank() &&
                currentPassword.isNotBlank()

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
     * Reset login success state
     */
    fun resetLoginSuccess() {
        _loginSuccess.value = false
    }

    /**
     * Clear all validation errors
     */
    fun clearValidationErrors() {
        _emailError.value = ValidationError.NONE
        _passwordError.value = ValidationError.NONE
        updateFormValidity()
    }

    /**
     * Auto-fill email (for navigation from forgot password)
     */
    fun setEmail(email: String) {
        currentEmail = email
        val validationError = ValidationUtils.validateEmail(email)
        _emailError.value = validationError
        updateFormValidity()
    }
}