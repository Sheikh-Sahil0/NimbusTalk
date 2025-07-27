package com.example.nimbustalk.enums

enum class ValidationError(val message: String) {
    NONE(""),
    REQUIRED_FIELD("This field is required"),
    INVALID_EMAIL("Please enter a valid email address"),
    PASSWORD_TOO_SHORT("Password must be at least 6 characters"),
    PASSWORDS_DONT_MATCH("Passwords don't match"),
    USERNAME_TOO_SHORT("Username must be at least 3 characters"),
    USERNAME_INVALID("Username can only contain letters, numbers, and underscores"),
    USERNAME_TAKEN("Username is already taken"),
    DISPLAY_NAME_TOO_SHORT("Display name must be at least 2 characters"),
    DISPLAY_NAME_TOO_LONG("Display name cannot exceed 50 characters"),
    INVALID_PHONE("Please enter a valid phone number"),
    FILE_TOO_LARGE("File size cannot exceed 10MB"),
    INVALID_FILE_TYPE("File type not supported"),
    MESSAGE_TOO_LONG("Message cannot exceed 1000 characters"),
    NETWORK_ERROR("Network connection error"),
    SERVER_ERROR("Server error occurred");

    // Check if validation passed
    fun isValid(): Boolean {
        return this == NONE
    }

    // Get error message
    fun getMessage(): String {
        return message
    }

    companion object {
        // Get validation error from string message
        fun fromMessage(message: String): ValidationError {
            return values().find { it.message == message } ?: SERVER_ERROR
        }
    }
}