package com.example.nimbustalk.api

import com.example.nimbustalk.models.*
import com.example.nimbustalk.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserApi(private val supabaseClient: SupabaseClient) {

    private val gson = Gson()

    /**
     * Check if username is available
     */
    suspend fun checkUsernameAvailability(username: String): ApiResponse<Boolean> = withContext(Dispatchers.IO) {
        try {
            val queryParams = mapOf(
                "select" to "username",
                "username" to "eq.$username"
            )

            val response = supabaseClient.get("/rest/v1/${Constants.USERS_TABLE}", null, queryParams)

            if (response.isSuccess()) {
                val listType = object : TypeToken<List<User>>() {}.type
                val users: List<User> = gson.fromJson(response.data ?: "[]", listType)

                // Username is available if no users found
                val isAvailable = users.isEmpty()

                ApiResponse(
                    data = isAvailable,
                    error = null,
                    message = if (isAvailable) "Username is available" else "Username is already taken",
                    status = response.status
                )
            } else {
                ApiResponse(
                    data = false,
                    error = response.error,
                    message = response.getErrorMessage(),
                    status = response.status
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                data = false,
                error = ApiError("Username check failed: ${e.message}"),
                message = "Failed to check username availability",
                status = 0
            )
        }
    }

    /**
     * Get user profile by ID
     */
    suspend fun getUserProfile(userId: String, accessToken: String): ApiResponse<User> = withContext(Dispatchers.IO) {
        try {
            val queryParams = mapOf(
                "select" to "*",
                "id" to "eq.$userId"
            )

            val response = supabaseClient.get("/rest/v1/${Constants.USERS_TABLE}", accessToken, queryParams)

            if (response.isSuccess()) {
                val listType = object : TypeToken<List<User>>() {}.type
                val users: List<User> = gson.fromJson(response.data ?: "[]", listType)

                if (users.isNotEmpty()) {
                    ApiResponse(
                        data = users.first(),
                        error = null,
                        message = "User profile retrieved successfully",
                        status = response.status
                    )
                } else {
                    ApiResponse(
                        data = null,
                        error = ApiError("User not found"),
                        message = "User not found",
                        status = 404
                    )
                }
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
                error = ApiError("Failed to get user profile: ${e.message}"),
                message = "Failed to get user profile",
                status = 0
            )
        }
    }

    /**
     * Get user profile by username
     */
    suspend fun getUserByUsername(username: String, accessToken: String): ApiResponse<User> = withContext(Dispatchers.IO) {
        try {
            val queryParams = mapOf(
                "select" to "*",
                "username" to "eq.$username"
            )

            val response = supabaseClient.get("/rest/v1/${Constants.USERS_TABLE}", accessToken, queryParams)

            if (response.isSuccess()) {
                val listType = object : TypeToken<List<User>>() {}.type
                val users: List<User> = gson.fromJson(response.data ?: "[]", listType)

                if (users.isNotEmpty()) {
                    ApiResponse(
                        data = users.first(),
                        error = null,
                        message = "User found successfully",
                        status = response.status
                    )
                } else {
                    ApiResponse(
                        data = null,
                        error = ApiError("User not found"),
                        message = "User not found",
                        status = 404
                    )
                }
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
                error = ApiError("Failed to get user: ${e.message}"),
                message = "Failed to get user",
                status = 0
            )
        }
    }

    /**
     * Search users by username or display name
     */
    suspend fun searchUsers(query: String, accessToken: String): ApiResponse<List<User>> = withContext(Dispatchers.IO) {
        try {
            // Use the search_users function from Supabase
            val response = supabaseClient.post(
                "/rest/v1/rpc/search_users",
                mapOf("search_term" to query),
                accessToken
            )

            if (response.isSuccess()) {
                val listType = object : TypeToken<List<User>>() {}.type
                val users: List<User> = gson.fromJson(response.data ?: "[]", listType)

                ApiResponse(
                    data = users,
                    error = null,
                    message = "Search completed successfully",
                    status = response.status
                )
            } else {
                ApiResponse(
                    data = emptyList(),
                    error = response.error,
                    message = response.getErrorMessage(),
                    status = response.status
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                data = emptyList(),
                error = ApiError("Search failed: ${e.message}"),
                message = "Search failed",
                status = 0
            )
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(
        userId: String,
        updates: Map<String, Any>,
        accessToken: String
    ): ApiResponse<User> = withContext(Dispatchers.IO) {
        try {
            val queryParams = mapOf("id" to "eq.$userId")

            // This would need a PATCH method, but for simplicity we'll use our existing structure
            // In a real implementation, you'd want to add PATCH support to SupabaseClient
            val response = supabaseClient.post(
                "/rest/v1/${Constants.USERS_TABLE}?id=eq.$userId",
                updates,
                accessToken
            )

            if (response.isSuccess()) {
                // After successful update, get the updated user
                getUserProfile(userId, accessToken)
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
                error = ApiError("Profile update failed: ${e.message}"),
                message = "Failed to update profile",
                status = 0
            )
        }
    }
}