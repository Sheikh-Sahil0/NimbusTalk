package com.example.nimbustalk.utils

import com.example.nimbustalk.enums.ValidationError
import java.util.regex.Pattern

object ValidationUtils {

    // Email validation regex pattern
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )

    // Username validation regex (letters, numbers, underscores only)
    private val USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]+$"
    )

    // Phone pattern removed - not used in this project

    /**
     * Validate email address
     */
    fun validateEmail(email: String): ValidationError {
        return when {
            email.isBlank() -> ValidationError.REQUIRED_FIELD
            !EMAIL_PATTERN.matcher(email.trim()).matches() -> ValidationError.INVALID_EMAIL
            else -> ValidationError.NONE
        }
    }

    /**
     * Validate password
     */
    fun validatePassword(password: String): ValidationError {
        return when {
            password.isBlank() -> ValidationError.REQUIRED_FIELD
            password.length < 6 -> ValidationError.PASSWORD_TOO_SHORT
            else -> ValidationError.NONE
        }
    }

    /**
     * Validate password confirmation
     */
    fun validatePasswordConfirmation(password: String, confirmPassword: String): ValidationError {
        return when {
            confirmPassword.isBlank() -> ValidationError.REQUIRED_FIELD
            password != confirmPassword -> ValidationError.PASSWORDS_DONT_MATCH
            else -> ValidationError.NONE
        }
    }

    /**
     * Validate username
     */
    fun validateUsername(username: String): ValidationError {
        return when {
            username.isBlank() -> ValidationError.REQUIRED_FIELD
            username.trim().length < 3 -> ValidationError.USERNAME_TOO_SHORT
            !USERNAME_PATTERN.matcher(username.trim()).matches() -> ValidationError.USERNAME_INVALID
            else -> ValidationError.NONE
        }
    }

    /**
     * Validate display name
     */
    fun validateDisplayName(displayName: String): ValidationError {
        return when {
            displayName.isBlank() -> ValidationError.REQUIRED_FIELD
            displayName.trim().length < 2 -> ValidationError.DISPLAY_NAME_TOO_SHORT
            displayName.trim().length > 50 -> ValidationError.DISPLAY_NAME_TOO_LONG
            else -> ValidationError.NONE
        }
    }

    // Phone validation removed - not used in this project architecture

    /**
     * Validate message content
     */
    fun validateMessage(message: String): ValidationError {
        return when {
            message.isBlank() -> ValidationError.REQUIRED_FIELD
            message.length > 1000 -> ValidationError.MESSAGE_TOO_LONG
            else -> ValidationError.NONE
        }
    }

    /**
     * Validate file size (in bytes)
     */
    fun validateFileSize(fileSizeBytes: Long): ValidationError {
        return when {
            fileSizeBytes > Constants.MAX_IMAGE_SIZE -> ValidationError.FILE_TOO_LARGE
            else -> ValidationError.NONE
        }
    }

    /**
     * Validate file type for images
     */
    fun validateImageFileType(fileName: String): ValidationError {
        val allowedExtensions = listOf("jpg", "jpeg", "png", "gif", "webp")
        val extension = fileName.substringAfterLast(".", "").lowercase()

        return when {
            extension.isEmpty() -> ValidationError.INVALID_FILE_TYPE
            !allowedExtensions.contains(extension) -> ValidationError.INVALID_FILE_TYPE
            else -> ValidationError.NONE
        }
    }

    /**
     * Validate required field (generic)
     */
    fun validateRequiredField(value: String): ValidationError {
        return if (value.isBlank()) ValidationError.REQUIRED_FIELD else ValidationError.NONE
    }

    /**
     * Validate login form - returns map of field errors
     */
    fun validateLoginForm(email: String, password: String): Map<String, ValidationError> {
        return mapOf(
            "email" to validateEmail(email),
            "password" to validatePassword(password)
        )
    }

    /**
     * Validate registration form - returns map of field errors
     */
    fun validateRegistrationForm(
        email: String,
        password: String,
        confirmPassword: String,
        username: String,
        displayName: String
    ): Map<String, ValidationError> {
        return mapOf(
            "email" to validateEmail(email),
            "password" to validatePassword(password),
            "confirmPassword" to validatePasswordConfirmation(password, confirmPassword),
            "username" to validateUsername(username),
            "displayName" to validateDisplayName(displayName)
        )
    }

    /**
     * Validate forgot password form
     */
    fun validateForgotPasswordForm(email: String): Map<String, ValidationError> {
        return mapOf(
            "email" to validateEmail(email)
        )
    }

    /**
     * Validate reset password form
     */
    fun validateResetPasswordForm(
        password: String,
        confirmPassword: String
    ): Map<String, ValidationError> {
        return mapOf(
            "password" to validatePassword(password),
            "confirmPassword" to validatePasswordConfirmation(password, confirmPassword)
        )
    }

    /**
     * Check if all validations in a map passed
     */
    fun isFormValid(validationResults: Map<String, ValidationError>): Boolean {
        return validationResults.values.all { it == ValidationError.NONE }
    }

    /**
     * Get first error message from validation results
     */
    fun getFirstErrorMessage(validationResults: Map<String, ValidationError>): String? {
        return validationResults.values.firstOrNull { it != ValidationError.NONE }?.message
    }

    /**
     * Clean and trim input string
     */
    fun cleanInput(input: String): String {
        return input.trim()
    }

    /**
     * Check if email format is valid (quick check without trimming)
     */
    fun isValidEmailFormat(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }

    /**
     * Check if username format is valid (quick check)
     */
    fun isValidUsernameFormat(username: String): Boolean {
        return username.length >= 3 && USERNAME_PATTERN.matcher(username).matches()
    }

    /**
     * Check if password is strong enough (minimum requirements)
     */
    fun isPasswordStrong(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Get password strength score (0-4)
     * 0 = Very weak, 4 = Very strong
     */
    fun getPasswordStrength(password: String): Int {
        var score = 0

        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return minOf(score, 4)
    }

    /**
     * Get password strength text
     */
    fun getPasswordStrengthText(password: String): String {
        return when (getPasswordStrength(password)) {
            0, 1 -> "Very Weak"
            2 -> "Weak"
            3 -> "Good"
            4 -> "Strong"
            else -> "Very Weak"
        }
    }

    /**
     * Sanitize user input to prevent basic injection attempts
     */
    fun sanitizeInput(input: String): String {
        return input.trim()
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
    }

    /**
     * Validate search query
     */
    fun validateSearchQuery(query: String): ValidationError {
        return when {
            query.trim().length < 2 -> ValidationError.REQUIRED_FIELD
            query.trim().length > 50 -> ValidationError.DISPLAY_NAME_TOO_LONG
            else -> ValidationError.NONE
        }
    }

    /**
     * Check if string contains only alphanumeric characters and allowed symbols
     */
    fun isAlphanumericWithSymbols(input: String, allowedSymbols: String = "_-."): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9${Pattern.quote(allowedSymbols)}]+$")
        return pattern.matcher(input).matches()
    }

    /**
     * Validate URL format (basic validation)
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            val urlPattern = Pattern.compile(
                "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/.*)?$"
            )
            urlPattern.matcher(url).matches()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Real-time validation helper for EditText fields
     */
    fun validateField(fieldType: String, value: String): ValidationError {
        return when (fieldType.lowercase()) {
            "email" -> validateEmail(value)
            "password" -> validatePassword(value)
            "username" -> validateUsername(value)
            "displayname", "display_name" -> validateDisplayName(value)
            "message" -> validateMessage(value)
            "required" -> validateRequiredField(value)
            else -> ValidationError.NONE
        }
    }

    // Constants for validation limits
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_USERNAME_LENGTH = 3
    const val MIN_DISPLAY_NAME_LENGTH = 2
    const val MAX_DISPLAY_NAME_LENGTH = 50
    const val MAX_MESSAGE_LENGTH = 1000
    const val MIN_SEARCH_QUERY_LENGTH = 2

    // Common validation patterns
    const val EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    const val USERNAME_REGEX = "^[a-zA-Z0-9_]+$"
}