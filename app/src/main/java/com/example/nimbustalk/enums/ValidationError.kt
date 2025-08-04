package com.example.nimbustalk.enums

enum class ValidationError(val message: String) {
    NONE(""),

    // Email validation errors
    EMAIL_EMPTY("Email is required"),
    EMAIL_INVALID("Please enter a valid email address"),
    EMAIL_ALREADY_EXISTS("Email is already registered"),

    // Password validation errors
    PASSWORD_EMPTY("Password is required"),
    PASSWORD_TOO_SHORT("Password must be at least 6 characters"),
    PASSWORD_TOO_LONG("Password must be less than 128 characters"),
    PASSWORD_WEAK("Password should contain letters and numbers"),

    // Confirm password validation errors
    CONFIRM_PASSWORD_EMPTY("Please confirm your password"),
    PASSWORDS_DO_NOT_MATCH("Passwords do not match"),

    // Username validation errors
    USERNAME_EMPTY("Username is required"),
    USERNAME_TOO_SHORT("Username must be at least 3 characters"),
    USERNAME_TOO_LONG("Username must be less than 50 characters"),
    USERNAME_INVALID_CHARACTERS("Username can only contain letters, numbers, and underscores"),
    USERNAME_ALREADY_EXISTS("Username is already taken"),

    // Display name validation errors
    DISPLAY_NAME_EMPTY("Display name is required"),
    DISPLAY_NAME_TOO_SHORT("Display name must be at least 1 character"),
    DISPLAY_NAME_TOO_LONG("Display name must be less than 100 characters"),

    // Network errors
    NETWORK_ERROR("Please check your internet connection"),
    SERVER_ERROR("Server error occurred. Please try again"),
    TIMEOUT_ERROR("Request timed out. Please try again"),

    // Auth errors
    INVALID_CREDENTIALS("Invalid email or password"),
    USER_NOT_FOUND("User not found"),
    WEAK_PASSWORD("Password is too weak"),
    EMAIL_NOT_CONFIRMED("Please verify your email address"),

    // General errors
    UNKNOWN_ERROR("An unknown error occurred")
}