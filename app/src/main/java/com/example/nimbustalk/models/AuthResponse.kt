package com.example.nimbustalk.models

data class AuthResponse(
    val success: Boolean = false,
    val message: String = "",
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: User? = null,
    val expiresIn: Long = 0L,
    val tokenType: String = "Bearer"
) {
    // Check if tokens are valid
    fun hasValidTokens(): Boolean {
        return !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
    }

    // Check if response contains user data
    fun hasUserData(): Boolean {
        return user != null && user.id.isNotBlank()
    }

    // Get error message or default
    fun getErrorMessage(): String {
        return if (message.isNotBlank()) message else "An error occurred"
    }
}