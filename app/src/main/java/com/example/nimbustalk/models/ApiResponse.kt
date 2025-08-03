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
     * Get error message or default message
     */
    fun getErrorMessage(): String {
        return error?.message
            ?: message
            ?: "An unknown error occurred"
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
 * Supabase error response
 */
data class SupabaseError(
    @SerializedName("error")
    val error: String?,

    @SerializedName("error_description")
    val errorDescription: String?,

    @SerializedName("message")
    val message: String?
) {
    fun getDisplayMessage(): String {
        return when {
            !message.isNullOrBlank() -> message
            !errorDescription.isNullOrBlank() -> errorDescription
            !error.isNullOrBlank() -> error
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