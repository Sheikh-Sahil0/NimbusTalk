package com.example.nimbustalk.api

import com.example.nimbustalk.models.ApiResponse
import com.example.nimbustalk.utils.Constants
import com.example.nimbustalk.utils.NetworkUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class SupabaseClient(private val networkUtils: NetworkUtils) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Make authenticated POST request
     */
    suspend fun makeAuthenticatedRequest(
        endpoint: String,
        requestBody: Map<String, Any>,
        accessToken: String? = null
    ): ApiResponse<Map<String, Any>> {

        // Check network connectivity
        if (!networkUtils.isNetworkAvailable()) {
            return ApiResponse.networkError()
        }

        return try {
            val json = gson.toJson(requestBody)
            val body = json.toRequestBody(jsonMediaType)

            val requestBuilder = Request.Builder()
                .url(Constants.SUPABASE_URL + endpoint)
                .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)

            // Add auth token if provided
            accessToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            val request = requestBuilder.build()
            val response = client.newCall(request).execute()

            parseResponse(response)

        } catch (e: IOException) {
            ApiResponse.error("Network connection error: ${e.message}", -1)
        } catch (e: Exception) {
            ApiResponse.error("Unexpected error: ${e.message}", -1)
        }
    }

    /**
     * Make GET request with authentication
     */
    suspend fun makeGetRequest(
        endpoint: String,
        accessToken: String? = null,
        queryParams: Map<String, String>? = null
    ): ApiResponse<List<Map<String, Any>>> {

        if (!networkUtils.isNetworkAvailable()) {
            return ApiResponse.networkError()
        }

        return try {
            var url = Constants.SUPABASE_URL + endpoint

            // Add query parameters
            queryParams?.let { params ->
                if (params.isNotEmpty()) {
                    val queryString = params.map { "${it.key}=${it.value}" }.joinToString("&")
                    url += "?$queryString"
                }
            }

            val requestBuilder = Request.Builder()
                .url(url)
                .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .get()

            accessToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            val request = requestBuilder.build()
            val response = client.newCall(request).execute()

            parseListResponse(response)

        } catch (e: IOException) {
            ApiResponse.error("Network connection error: ${e.message}", -1)
        } catch (e: Exception) {
            ApiResponse.error("Unexpected error: ${e.message}", -1)
        }
    }

    /**
     * Parse single object response
     */
    private fun parseResponse(response: Response): ApiResponse<Map<String, Any>> {
        val responseBody = response.body?.string() ?: ""

        return when (response.code) {
            200, 201 -> {
                try {
                    val data: Map<String, Any> = gson.fromJson(
                        responseBody,
                        object : TypeToken<Map<String, Any>>() {}.type
                    )
                    ApiResponse.success(data, "Success")
                } catch (e: Exception) {
                    ApiResponse.error("Failed to parse response: ${e.message}", response.code)
                }
            }
            400 -> {
                val errorMessage = parseErrorMessage(responseBody)
                ApiResponse.error(errorMessage, 400)
            }
            401 -> {
                ApiResponse.error("Invalid credentials", 401)
            }
            422 -> {
                val errorMessage = parseErrorMessage(responseBody)
                ApiResponse.error(errorMessage, 422)
            }
            500 -> {
                ApiResponse.error("Server error occurred", 500)
            }
            else -> {
                val errorMessage = parseErrorMessage(responseBody)
                ApiResponse.error(errorMessage, response.code)
            }
        }
    }

    /**
     * Parse list response
     */
    private fun parseListResponse(response: Response): ApiResponse<List<Map<String, Any>>> {
        val responseBody = response.body?.string() ?: ""

        return when (response.code) {
            200 -> {
                try {
                    val data: List<Map<String, Any>> = gson.fromJson(
                        responseBody,
                        object : TypeToken<List<Map<String, Any>>>() {}.type
                    )
                    ApiResponse.success(data, "Success")
                } catch (e: Exception) {
                    ApiResponse.error("Failed to parse response: ${e.message}", response.code)
                }
            }
            400 -> {
                val errorMessage = parseErrorMessage(responseBody)
                ApiResponse.error(errorMessage, 400)
            }
            401 -> {
                ApiResponse.error("Unauthorized access", 401)
            }
            else -> {
                val errorMessage = parseErrorMessage(responseBody)
                ApiResponse.error(errorMessage, response.code)
            }
        }
    }

    /**
     * Parse error message from response body
     */
    private fun parseErrorMessage(responseBody: String): String {
        return try {
            val errorData: Map<String, Any> = gson.fromJson(
                responseBody,
                object : TypeToken<Map<String, Any>>() {}.type
            )

            when {
                errorData.containsKey("error_description") -> {
                    errorData["error_description"] as? String ?: "Unknown error"
                }
                errorData.containsKey("message") -> {
                    errorData["message"] as? String ?: "Unknown error"
                }
                errorData.containsKey("msg") -> {
                    errorData["msg"] as? String ?: "Unknown error"
                }
                else -> "An error occurred"
            }
        } catch (e: Exception) {
            if (responseBody.isNotBlank()) responseBody else "Unknown error occurred"
        }
    }

    companion object {
        const val TAG = "SupabaseClient"
    }
}