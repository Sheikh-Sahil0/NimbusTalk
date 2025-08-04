package com.example.nimbustalk.models

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("data")
    val data: T?,

    @SerializedName("error")
    val error: ApiError?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("status")
    val status: Int = 200
) {
    /**
     * Check if the response is successful
     */
    fun isSuccess(): Boolean = error == null && status in 200..299

    /**
     * Get error message with priority: error.message > message > default
     */
    fun getErrorMessage(): String {
        return when {
            error?.message?.isNotBlank() == true -> error.message
            message?.isNotBlank() == true -> message
            else -> "An unknown error occurred"
        }
    }

    /**
     * Get user-friendly error message based on status code and error content
     */
    fun getUserFriendlyErrorMessage(): String {
        val errorMsg = getErrorMessage()

        return when {
            // Authentication errors
            errorMsg.contains("Invalid login credentials", ignoreCase = true) ||
                    errorMsg.contains("invalid_grant", ignoreCase = true) ||
                    errorMsg.contains("invalid_credentials", ignoreCase = true) ->
                "Invalid email or password"

            // Registration errors
            errorMsg.contains("User already registered", ignoreCase = true) ||
                    errorMsg.contains("email is already registered", ignoreCase = true) ||
                    (status == 409 && errorMsg.contains("email", ignoreCase = true)) ->
                "This email is already registered"

            errorMsg.contains("username is already taken", ignoreCase = true) ||
                    errorMsg.contains("duplicate key value", ignoreCase = true) && errorMsg.contains("username", ignoreCase = true) ->
                "This username is already taken"

            // Password errors
            errorMsg.contains("Password should be at least", ignoreCase = true) ||
                    errorMsg.contains("password_is_too_weak", ignoreCase = true) ||
                    errorMsg.contains("password is too weak", ignoreCase = true) ->
                "Password is too weak. Please use a stronger password."

            // Email verification errors
            errorMsg.contains("Email not confirmed", ignoreCase = true) ||
                    errorMsg.contains("email_not_confirmed", ignoreCase = true) ->
                "Please verify your email address before logging in"

            // Network errors
            errorMsg.contains("network error", ignoreCase = true) ||
                    errorMsg.contains("connection", ignoreCase = true) ||
                    status == 0 ->
                "Please check your internet connection and try again"

            // Rate limiting
            status == 429 ||
                    errorMsg.contains("too many", ignoreCase = true) ||
                    errorMsg.contains("rate limit", ignoreCase = true) ->
                "Too many attempts. Please wait a moment and try again."

            // Server errors
            status >= 500 ->
                "Server error. Please try again later."

            // Client errors
            status == 400 ->
                "Invalid request. Please check your information."

            status == 404 ->
                "The requested resource was not found"

            status == 403 ->
                "Access denied"

            // Return original message if no specific handling
            else -> errorMsg
        }
    }
}

/**
 * API error details
 */
data class ApiError(
    @SerializedName("message")
    val message: String,

    @SerializedName("code")
    val code: String? = null,

    @SerializedName("details")
    val details: String? = null,

    @SerializedName("hint")
    val hint: String? = null
)

/**
 * Enhanced Supabase error response with multiple possible formats
 */
data class SupabaseError(
    @SerializedName("error")
    val error: String? = null,

    @SerializedName("error_description")
    val errorDescription: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("msg")
    val msg: String? = null,

    @SerializedName("error_code")
    val errorCode: String? = null,

    @SerializedName("code")
    val code: String? = null,

    @SerializedName("details")
    val details: String? = null,

    @SerializedName("hint")
    val hint: String? = null
) {
    fun getDisplayMessage(): String {
        return when {
            !message.isNullOrBlank() -> message
            !errorDescription.isNullOrBlank() -> errorDescription
            !error.isNullOrBlank() -> error
            !msg.isNullOrBlank() -> msg
            !details.isNullOrBlank() -> details
            !hint.isNullOrBlank() -> hint
            else -> "An unknown error occurred"
        }
    }
}

/**
 * Username availability response
 */
data class UsernameCheckResponse(
    @SerializedName("available")
    val available: Boolean,

    @SerializedName("message")
    val message: String?
)