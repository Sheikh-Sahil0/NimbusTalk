package com.example.nimbustalk.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nimbustalk.api.AuthApi
import com.example.nimbustalk.enums.LoadingState
import com.example.nimbustalk.enums.ValidationError
import com.example.nimbustalk.models.AuthResponse
import com.example.nimbustalk.utils.SharedPrefsHelper
import com.example.nimbustalk.utils.ValidationUtils
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authApi: AuthApi,
    private val sharedPrefsHelper: SharedPrefsHelper
) : ViewModel() {

    // Loading state
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState

    // Error message
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Success message
    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    // Login success event
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    // Email validation
    private val _emailError = MutableLiveData<ValidationError>()
    val emailError: LiveData<ValidationError> = _emailError

    // Password validation
    private val _passwordError = MutableLiveData<ValidationError>()
    val passwordError: LiveData<ValidationError> = _passwordError

    // Form validity
    private val _isFormValid = MutableLiveData<Boolean>()
    val isFormValid: LiveData<Boolean> = _isFormValid

    init {
        _loadingState.value = LoadingState.IDLE
        _emailError.value = ValidationError.NONE
        _passwordError.value = ValidationError.NONE
        _isFormValid.value = false
    }

    /**
     * Set email validation result from Activity
     */
    fun setEmailValidation(validationError: ValidationError) {
        _emailError.value = validationError
        updateFormValidity()
    }

    /**
     * Set password validation result from Activity
     */
    fun setPasswordValidation(validationError: ValidationError) {
        _passwordError.value = validationError
        updateFormValidity()
    }

    /**
     * Validate email input using ValidationUtils
     */
    fun validateEmail(email: String): ValidationError {
        val error = ValidationUtils.validateEmail(email)
        _emailError.value = error
        updateFormValidity()
        return error
    }

    /**
     * Validate password input using ValidationUtils
     */
    fun validatePassword(password: String): ValidationError {
        val error = ValidationUtils.validatePassword(password)
        _passwordError.value = error
        updateFormValidity()
        return error
    }

    /**
     * Update form validity based on all validations
     */
    private fun updateFormValidity() {
        val emailValid = _emailError.value?.isValid() == true
        val passwordValid = _passwordError.value?.isValid() == true
        _isFormValid.value = emailValid && passwordValid
    }

    /**
     * Perform login with comprehensive validation
     */
    fun login(email: String, password: String) {
        // Clear previous messages
        _errorMessage.value = ""
        _successMessage.value = ""

        // Clean and validate inputs using ValidationUtils
        val cleanEmail = ValidationUtils.cleanInput(email)
        val cleanPassword = password.trim()

        // Validate login form
        val validationResults = ValidationUtils.validateLoginForm(cleanEmail, cleanPassword)

        // Check if form is valid
        if (!ValidationUtils.isFormValid(validationResults)) {
            val firstError = ValidationUtils.getFirstErrorMessage(validationResults)
            _errorMessage.value = firstError ?: "Please fix the errors above"

            // Update individual field errors
            _emailError.value = validationResults["email"] ?: ValidationError.NONE
            _passwordError.value = validationResults["password"] ?: ValidationError.NONE
            return
        }

        // Start loading
        _loadingState.value = LoadingState.LOADING

        // Perform login API call
        viewModelScope.launch {
            try {
                val response = authApi.login(cleanEmail, cleanPassword)

                when {
                    response.success && response.data != null -> {
                        val authResponse = response.data

                        if (authResponse.hasValidTokens() && authResponse.hasUserData()) {
                            // Save auth data
                            saveAuthData(authResponse)

                            // Update state
                            _loadingState.value = LoadingState.SUCCESS
                            _successMessage.value = response.message
                            _loginSuccess.value = true
                        } else {
                            _loadingState.value = LoadingState.ERROR
                            _errorMessage.value = "Invalid response from server"
                        }
                    }
                    else -> {
                        _loadingState.value = LoadingState.ERROR
                        _errorMessage.value = response.getErrorMessage()
                    }
                }
            } catch (e: Exception) {
                _loadingState.value = LoadingState.ERROR
                _errorMessage.value = "Login failed: ${e.message}"
            }
        }
    }

    /**
     * Save authentication data to SharedPreferences
     */
    private fun saveAuthData(authResponse: AuthResponse) {
        try {
            // Save tokens
            sharedPrefsHelper.saveAuthTokens(
                accessToken = authResponse.accessToken ?: "",
                refreshToken = authResponse.refreshToken ?: ""
            )

            // Save user profile
            authResponse.user?.let { user ->
                sharedPrefsHelper.saveUserProfile(
                    userId = user.id,
                    email = user.email,
                    name = user.displayName.ifBlank { user.username },
                    avatar = user.avatarUrl
                )
            }

            // Mark user as logged in
            sharedPrefsHelper.setUserLoggedIn(true)

        } catch (e: Exception) {
            // Handle save error
            _errorMessage.value = "Failed to save login data: ${e.message}"
        }
    }

    /**
     * Clear error messages
     */
    fun clearError() {
        _errorMessage.value = ""
    }

    /**
     * Clear success messages
     */
    fun clearSuccess() {
        _successMessage.value = ""
    }

    /**
     * Reset login success event
     */
    fun resetLoginSuccess() {
        _loginSuccess.value = false
    }

    /**
     * Check if currently loading
     */
    fun isLoading(): Boolean {
        return _loadingState.value == LoadingState.LOADING
    }

    /**
     * Get current loading state
     */
    fun getCurrentLoadingState(): LoadingState {
        return _loadingState.value ?: LoadingState.IDLE
    }

    /**
     * Check if email format is valid (quick check)
     */
    fun isEmailFormatValid(email: String): Boolean {
        return ValidationUtils.isValidEmailFormat(email)
    }

    /**
     * Check if password is strong enough
     */
    fun isPasswordStrong(password: String): Boolean {
        return ValidationUtils.isPasswordStrong(password)
    }

    /**
     * Get password strength score for UI feedback
     */
    fun getPasswordStrength(password: String): Int {
        return ValidationUtils.getPasswordStrength(password)
    }

    /**
     * Manual validation trigger (for submit button)
     */
    fun validateForm(email: String, password: String): Boolean {
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        return emailError.isValid() && passwordError.isValid()
    }

    companion object {
        const val TAG = "LoginViewModel"
    }
}