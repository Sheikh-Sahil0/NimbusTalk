package com.example.nimbustalk.utils

import com.example.nimbustalk.enums.ValidationError
import java.util.regex.Pattern

object ValidationUtils {

    // Email validation pattern
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )

    // Username validation pattern (letters, numbers, underscores only)
    private val USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]+$"
    )

    // Password validation pattern (at least one letter and one number)
    private val PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-zA-Z])(?=.*\\d).+$"
    )

    /**
     * Clean input by trimming whitespace
     */
    fun cleanInput(input: String): String {
        return input.trim()
    }

    /**
     * Validate email address
     */
    fun validateEmail(email: String): ValidationError {
        val cleanEmail = cleanInput(email)

        return when {
            cleanEmail.isEmpty() -> ValidationError.EMAIL_EMPTY
            !EMAIL_PATTERN.matcher(cleanEmail).matches() -> ValidationError.EMAIL_INVALID
            else -> ValidationError.NONE
        }
    }

    /**
     * Validate password
     */
    fun validatePassword(password: String): ValidationError {
        return when {
            password.isEmpty() -> ValidationError.PASSWORD_EMPTY
            password.length < Constants.MIN_PASSWORD_LENGTH -> ValidationError.PASSWORD_TOO_SHORT
            password.length > Constants.MAX_PASSWORD_LENGTH -> ValidationError.PASSWORD_TOO_LONG
            !PASSWORD_PATTERN.matcher(password).matches() -> ValidationError.PASSWORD_WEAK
            else -> ValidationError.NONE
        }
    }

    /**
     * Validate confirm password
     */
    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationError {
        return when {
            confirmPassword.isEmpty() -> ValidationError.CONFIRM_PASSWORD_EMPTY
            password != confirmPassword -> ValidationError.PASSWORDS_DO_NOT_MATCH
            else -> ValidationError.NONE
        }
    }

    /**
     * Validate username
     */
    fun validateUsername(username: String): ValidationError {
        val cleanUsername = cleanInput(username).lowercase()

        return when {
            cleanUsername.isEmpty() -> ValidationError.USERNAME_EMPTY
            cleanUsername.length < Constants.MIN_USERNAME_LENGTH -> ValidationError.USERNAME_TOO_SHORT
            cleanUsername.length > Constants.MAX_USERNAME_LENGTH -> ValidationError.USERNAME_TOO_LONG
            !USERNAME_PATTERN.matcher(cleanUsername).matches() -> ValidationError.USERNAME_INVALID_CHARACTERS
            else -> ValidationError.NONE
        }
    }

    /**
     * Validate display name
     */
    fun validateDisplayName(displayName: String): ValidationError {
        val cleanDisplayName = cleanInput(displayName)

        return when {
            cleanDisplayName.isEmpty() -> ValidationError.DISPLAY_NAME_EMPTY
            cleanDisplayName.length < Constants.MIN_DISPLAY_NAME_LENGTH -> ValidationError.DISPLAY_NAME_TOO_SHORT
            cleanDisplayName.length > Constants.MAX_DISPLAY_NAME_LENGTH -> ValidationError.DISPLAY_NAME_TOO_LONG
            else -> ValidationError.NONE
        }
    }

    /**
     * Check if all validations pass
     */
    fun isValidRegistrationForm(
        email: String,
        username: String,
        displayName: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return validateEmail(email) == ValidationError.NONE &&
                validateUsername(username) == ValidationError.NONE &&
                validateDisplayName(displayName) == ValidationError.NONE &&
                validatePassword(password) == ValidationError.NONE &&
                validateConfirmPassword(password, confirmPassword) == ValidationError.NONE
    }

    /**
     * Check if login form is valid
     */
    fun isValidLoginForm(email: String, password: String): Boolean {
        return validateEmail(email) == ValidationError.NONE &&
                validatePassword(password) == ValidationError.NONE
    }

    /**
     * Sanitize username for database storage
     */
    fun sanitizeUsername(username: String): String {
        return cleanInput(username).lowercase()
    }

    /**
     * Sanitize display name for database storage
     */
    fun sanitizeDisplayName(displayName: String): String {
        return cleanInput(displayName)
    }
}