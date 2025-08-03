package com.example.nimbustalk.api

import com.example.nimbustalk.models.*
import com.example.nimbustalk.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthApi(private val supabaseClient: SupabaseClient) {

    private val gson = Gson()

    /**
     * Register new user
     */
    suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): ApiResponse<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = RegisterRequest(
                email = email,
                password = password,
                data = RegisterUserMetadata(
                    username = username,
                    displayName = displayName
                )
            )

            val response = supabaseClient.post(Constants.AUTH_SIGNUP, requestBody)

            if (response.isSuccess()) {
                val authResponse = gson.fromJson(response.data, AuthResponse::class.java)
                ApiResponse(
                    data = authResponse,
                    error = null,
                    message = "Registration successful",
                    status = response.status
                )
            } else {
                ApiResponse(
                    data = null,
                    error = response.error,
                    message = response.getErrorMessage(),
                    status = response.status
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                data = null,
                error = ApiError("Registration failed: ${e.message}"),
                message = "Registration failed",
                status = 0
            )
        }
    }

    /**
     * Login user
     */
    suspend fun login(
        email: String,
        password: String
    ): ApiResponse<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = LoginRequest(
                email = email,
                password = password
            )

            val response = supabaseClient.post(Constants.AUTH_LOGIN, requestBody)

            if (response.isSuccess()) {
                val authResponse = gson.fromJson(response.data, AuthResponse::class.java)
                ApiResponse(
                    data = authResponse,
                    error = null,
                    message = "Login successful",
                    status = response.status
                )
            } else {
                ApiResponse(
                    data = null,
                    error = response.error,
                    message = response.getErrorMessage(),
                    status = response.status
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                data = null,
                error = ApiError("Login failed: ${e.message}"),
                message = "Login failed",
                status = 0
            )
        }
    }

    /**
     * Logout user
     */
    suspend fun logout(accessToken: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val response = supabaseClient.post(Constants.AUTH_LOGOUT, emptyMap<String, String>(), accessToken)

            if (response.isSuccess()) {
                ApiResponse(
                    data = "Logout successful",
                    error = null,
                    message = "Logout successful",
                    status = response.status
                )
            } else {
                ApiResponse(
                    data = null,
                    error = response.error,
                    message = response.getErrorMessage(),
                    status = response.status
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                data = null,
                error = ApiError("Logout failed: ${e.message}"),
                message = "Logout failed",
                status = 0
            )
        }
    }

    /**
     * Refresh access token
     */
    suspend fun refreshToken(refreshToken: String): ApiResponse<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = RefreshTokenRequest(refreshToken = refreshToken)

            val response = supabaseClient.post(Constants.AUTH_REFRESH, requestBody)

            if (response.isSuccess()) {
                val authResponse = gson.fromJson(response.data, AuthResponse::class.java)
                ApiResponse(
                    data = authResponse,
                    error = null,
                    message = "Token refreshed successfully",
                    status = response.status
                )
            } else {
                ApiResponse(
                    data = null,
                    error = response.error,
                    message = response.getErrorMessage(),
                    status = response.status
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                data = null,
                error = ApiError("Token refresh failed: ${e.message}"),
                message = "Token refresh failed",
                status = 0
            )
        }
    }

    /**
     * Send password reset email
     */
    suspend fun forgotPassword(email: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = PasswordResetRequest(email = email)

            val response = supabaseClient.post(Constants.AUTH_FORGOT_PASSWORD, requestBody)

            if (response.isSuccess()) {
                ApiResponse(
                    data = "Password reset email sent",
                    error = null,
                    message = "Password reset email sent successfully",
                    status = response.status
                )
            } else {
                ApiResponse(
                    data = null,
                    error = response.error,
                    message = response.getErrorMessage(),
                    status = response.status
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                data = null,
                error = ApiError("Password reset failed: ${e.message}"),
                message = "Password reset failed",
                status = 0
            )
        }
    }
}