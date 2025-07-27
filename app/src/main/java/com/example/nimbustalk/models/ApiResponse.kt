package com.example.nimbustalk.models

data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T? = null,
    val error: String? = null,
    val statusCode: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    // Check if response is successful with data
    fun isSuccessWithData(): Boolean {
        return success && data != null
    }

    // Get error message from error field or message field
    fun getErrorMessage(): String {
        return when {
            !error.isNullOrBlank() -> error
            !message.isBlank() && !success -> message
            else -> "Unknown error occurred"
        }
    }

    // Get success message
    fun getSuccessMessage(): String {
        return if (success && message.isNotBlank()) message else "Operation successful"
    }

    companion object {
        // Helper function to create success response
        fun <T> success(data: T? = null, message: String = "Success"): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data,
                statusCode = 200
            )
        }

        // Helper function to create error response
        fun <T> error(message: String, statusCode: Int = 400): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                error = message,
                statusCode = statusCode
            )
        }

        // Helper function to create network error response
        fun <T> networkError(): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = "Network connection error",
                error = "No internet connection",
                statusCode = -1
            )
        }
    }
}