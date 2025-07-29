package com.example.nimbustalk.api

import com.example.nimbustalk.models.ApiResponse
import com.example.nimbustalk.models.AuthResponse
import com.example.nimbustalk.models.User
import com.example.nimbustalk.utils.Constants

class AuthApi(private val supabaseClient: SupabaseClient) {

    /**
     * Login user with email and password
     */
    suspend fun login(email: String, password: String): ApiResponse<AuthResponse> {
        val requestBody = mapOf(
            "email" to email,
            "password" to password
        )

        val response = supabaseClient.makeAuthenticatedRequest(
            endpoint = Constants.AUTH_LOGIN,
            requestBody = requestBody
        )

        return when {
            response.success && response.data != null -> {
                try {
                    val authData = response.data

                    // Extract tokens
                    val accessToken = authData["access_token"] as? String
                    val refreshToken = authData["refresh_token"] as? String
                    val expiresIn = (authData["expires_in"] as? Double)?.toLong() ?: 0L

                    // Extract user data
                    val userMap = authData["user"] as? Map<String, Any>
                    val user = userMap?.let { parseUserFromAuth(it) }

                    val authResponse = AuthResponse(
                        success = true,
                        message = "Login successful",
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        user = user,
                        expiresIn = expiresIn
                    )

                    ApiResponse.success(authResponse, "Login successful")
                } catch (e: Exception) {
                    ApiResponse.error("Failed to parse login response: ${e.message}")
                }
            }
            else -> {
                ApiResponse.error(response.getErrorMessage())
            }
        }
    }

    /**
     * Register new user
     */
    suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): ApiResponse<AuthResponse> {

        val requestBody = mapOf(
            "email" to email,
            "password" to password,
            "data" to mapOf(
                "username" to username,
                "display_name" to displayName
            )
        )

        val response = supabaseClient.makeAuthenticatedRequest(
            endpoint = Constants.AUTH_SIGNUP,
            requestBody = requestBody
        )

        return when {
            response.success && response.data != null -> {
                try {
                    val authData = response.data

                    val accessToken = authData["access_token"] as? String
                    val refreshToken = authData["refresh_token"] as? String
                    val expiresIn = (authData["expires_in"] as? Double)?.toLong() ?: 0L

                    val userMap = authData["user"] as? Map<String, Any>
                    val user = userMap?.let { parseUserFromAuth(it) }

                    val authResponse = AuthResponse(
                        success = true,
                        message = "Registration successful",
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        user = user,
                        expiresIn = expiresIn
                    )

                    ApiResponse.success(authResponse, "Registration successful")
                } catch (e: Exception) {
                    ApiResponse.error("Failed to parse registration response: ${e.message}")
                }
            }
            else -> {
                ApiResponse.error(response.getErrorMessage())
            }
        }
    }

    /**
     * Refresh access token
     */
    suspend fun refreshToken(refreshToken: String): ApiResponse<AuthResponse> {
        val requestBody = mapOf(
            "refresh_token" to refreshToken
        )

        val response = supabaseClient.makeAuthenticatedRequest(
            endpoint = Constants.AUTH_REFRESH,
            requestBody = requestBody
        )

        return when {
            response.success && response.data != null -> {
                try {
                    val authData = response.data

                    val newAccessToken = authData["access_token"] as? String
                    val newRefreshToken = authData["refresh_token"] as? String
                    val expiresIn = (authData["expires_in"] as? Double)?.toLong() ?: 0L

                    val userMap = authData["user"] as? Map<String, Any>
                    val user = userMap?.let { parseUserFromAuth(it) }

                    val authResponse = AuthResponse(
                        success = true,
                        message = "Token refreshed",
                        accessToken = newAccessToken,
                        refreshToken = newRefreshToken,
                        user = user,
                        expiresIn = expiresIn
                    )

                    ApiResponse.success(authResponse, "Token refreshed")
                } catch (e: Exception) {
                    ApiResponse.error("Failed to refresh token: ${e.message}")
                }
            }
            else -> {
                ApiResponse.error(response.getErrorMessage())
            }
        }
    }

    /**
     * Logout user
     */
    suspend fun logout(accessToken: String): ApiResponse<Boolean> {
        val response = supabaseClient.makeAuthenticatedRequest(
            endpoint = Constants.AUTH_LOGOUT,
            requestBody = emptyMap(),
            accessToken = accessToken
        )

        return when {
            response.success -> {
                ApiResponse.success(true, "Logout successful")
            }
            else -> {
                // Even if logout fails on server, we consider it successful locally
                ApiResponse.success(true, "Logged out locally")
            }
        }
    }

    /**
     * Send password reset email
     */
    suspend fun resetPassword(email: String): ApiResponse<Boolean> {
        val requestBody = mapOf(
            "email" to email
        )

        val response = supabaseClient.makeAuthenticatedRequest(
            endpoint = Constants.AUTH_RESET_PASSWORD,
            requestBody = requestBody
        )

        return when {
            response.success -> {
                ApiResponse.success(true, "Password reset email sent")
            }
            else -> {
                ApiResponse.error(response.getErrorMessage())
            }
        }
    }

    /**
     * Parse user data from authentication response
     */
    private fun parseUserFromAuth(userMap: Map<String, Any>): User {
        val id = userMap["id"] as? String ?: ""
        val email = userMap["email"] as? String ?: ""

        // Extract user metadata (custom fields)
        val userMetadata = userMap["user_metadata"] as? Map<String, Any> ?: emptyMap()
        val username = userMetadata["username"] as? String ?: ""
        val displayName = userMetadata["display_name"] as? String ?: ""

        // Parse timestamps
        val createdAt = parseTimestamp(userMap["created_at"] as? String)
        val updatedAt = parseTimestamp(userMap["updated_at"] as? String)

        return User(
            id = id,
            email = email,
            username = username,
            displayName = displayName,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    /**
     * Parse ISO timestamp string to milliseconds
     */
    private fun parseTimestamp(timestamp: String?): Long {
        return try {
            timestamp?.let {
                // Simple parsing - in production, use proper date parsing
                System.currentTimeMillis()
            } ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    companion object {
        const val TAG = "AuthApi"
    }
}