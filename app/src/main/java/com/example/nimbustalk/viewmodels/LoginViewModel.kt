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
import android.util.Log

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

                Log.d("LoginViewModel", "Attempting login for email: $cleanEmail")

                // Attempt login
                val response = authApi.login(cleanEmail, password)

                Log.d("LoginViewModel", "Login response status: ${response.status}")
                Log.d("LoginViewModel", "Login response success: ${response.isSuccess()}")

                if (response.isSuccess() && response.data != null) {
                    Log.d("LoginViewModel", "Login successful, processing auth data")
                    handleLoginSuccess(response.data, cleanEmail)
                } else {
                    val errorMessage = response.getUserFriendlyErrorMessage()
                    Log.e("LoginViewModel", "Login failed: $errorMessage")
                    handleLoginError(errorMessage)
                }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login exception", e)
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

            Log.d("LoginViewModel", "Auth response - User: ${user?.id}, Token: ${!accessToken.isNullOrBlank()}")

            if (user != null && !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                // Get complete user profile from database
                Log.d("LoginViewModel", "Fetching user profile for: ${user.id}")
                val userProfileResponse = userApi.getUserProfile(user.id, accessToken)

                if (userProfileResponse.isSuccess() && userProfileResponse.data != null) {
                    val userProfile = userProfileResponse.data
                    Log.d("LoginViewModel", "User profile retrieved: ${userProfile.username}")

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
                    Log.d("LoginViewModel", "Using fallback auth data save")
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
                Log.e("LoginViewModel", "Incomplete auth response")
                handleLoginError("Login response incomplete. Please try again.")
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error processing login success", e)
            handleLoginError("Failed to complete login: ${e.message}")
        }
    }

    /**
     * Handle login error with specific error messages
     */
    private fun handleLoginError(message: String) {
        _loadingState.value = LoadingState.ERROR

        // The message should already be user-friendly from getUserFriendlyErrorMessage()
        // But we can add additional context for specific cases
        _errorMessage.value = when {
            message.contains("Invalid email or password", ignoreCase = true) ->
                "Invalid email or password. Please check your credentials and try again."

            message.contains("email is already registered", ignoreCase = true) ->
                "This account exists. Please try logging in instead."

            message.contains("verify your email", ignoreCase = true) ->
                "Please verify your email address before logging in. Check your inbox for a verification link."

            message.contains("too many", ignoreCase = true) ||
                    message.contains("rate limit", ignoreCase = true) ->
                "Too many login attempts. Please wait a few minutes and try again."

            message.contains("network", ignoreCase = true) ||
                    message.contains("connection", ignoreCase = true) ->
                "Please check your internet connection and try again."

            message.contains("timeout", ignoreCase = true) ->
                "Login request timed out. Please try again."

            message.contains("server error", ignoreCase = true) ->
                "Server temporarily unavailable. Please try again in a moment."

            else -> message
        }

        Log.e("LoginViewModel", "Login error: ${_errorMessage.value}")
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