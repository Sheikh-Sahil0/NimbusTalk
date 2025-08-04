package com.example.nimbustalk.api

import com.example.nimbustalk.models.*
import com.example.nimbustalk.utils.Constants
import com.example.nimbustalk.utils.NetworkUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.util.Log
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class SupabaseClient(private val networkUtils: NetworkUtils) {

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")

            chain.proceed(requestBuilder.build())
        }
        .addInterceptor { chain ->
            // Log requests for debugging
            val request = chain.request()
            Log.d("SupabaseClient", "Request: ${request.method} ${request.url}")
            Log.d("SupabaseClient", "Headers: ${request.headers}")

            val response = chain.proceed(request)
            Log.d("SupabaseClient", "Response: ${response.code} ${response.message}")

            response.peekBody(2048).let { body ->
                Log.d("SupabaseClient", "Response body preview: ${body.string()}")
            }

            response
        }
        .build()

    /**
     * Make authenticated request with bearer token
     */
    private fun makeAuthenticatedRequest(
        request: Request,
        accessToken: String
    ): Request {
        return request.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
    }

    /**
     * Execute HTTP request asynchronously
     */
    private suspend fun executeRequest(request: Request): Response = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val call = httpClient.newCall(request)

            continuation.invokeOnCancellation {
                call.cancel()
            }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("SupabaseClient", "Request failed", e)
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d("SupabaseClient", "Response received: ${response.code}")
                    continuation.resume(response)
                }
            })
        }
    }

    /**
     * Check network connectivity
     */
    private fun checkNetworkConnection() {
        if (!networkUtils.isConnected()) {
            throw IOException("No internet connection")
        }
    }

    /**
     * Parse error response with better error message extraction
     */
    private fun parseErrorResponse(responseBody: String?, statusCode: Int): String {
        return try {
            if (responseBody.isNullOrBlank()) {
                return getDefaultErrorMessage(statusCode)
            }

            Log.d("SupabaseClient", "Parsing error response: $responseBody")

            // Try parsing as SupabaseError first
            val supabaseError = gson.fromJson(responseBody, SupabaseError::class.java)

            when {
                // Handle specific auth errors
                supabaseError.error != null -> {
                    when {
                        supabaseError.error.contains("invalid_grant", ignoreCase = true) ||
                                supabaseError.error.contains("invalid_credentials", ignoreCase = true) ->
                            "Invalid email or password"

                        supabaseError.error.contains("user_not_found", ignoreCase = true) ->
                            "Account not found"

                        supabaseError.error.contains("email_not_confirmed", ignoreCase = true) ->
                            "Please verify your email address"

                        supabaseError.error.contains("signup_disabled", ignoreCase = true) ->
                            "Registration is temporarily disabled"

                        supabaseError.error.contains("email_address_invalid", ignoreCase = true) ->
                            "Please enter a valid email address"

                        supabaseError.error.contains("password_is_too_weak", ignoreCase = true) ->
                            "Password is too weak"

                        supabaseError.error.contains("user_already_registered", ignoreCase = true) ->
                            "Email is already registered"

                        else -> supabaseError.error
                    }
                }

                supabaseError.errorDescription != null -> {
                    when {
                        supabaseError.errorDescription.contains("Invalid login credentials", ignoreCase = true) ->
                            "Invalid email or password"

                        supabaseError.errorDescription.contains("User already registered", ignoreCase = true) ->
                            "Email is already registered"

                        supabaseError.errorDescription.contains("Password should be at least", ignoreCase = true) ->
                            "Password is too weak"

                        supabaseError.errorDescription.contains("Unable to validate email address", ignoreCase = true) ->
                            "Please enter a valid email address"

                        supabaseError.errorDescription.contains("Email not confirmed", ignoreCase = true) ->
                            "Please verify your email address"

                        else -> supabaseError.errorDescription
                    }
                }

                supabaseError.message != null -> {
                    when {
                        supabaseError.message.contains("duplicate key value", ignoreCase = true) &&
                                supabaseError.message.contains("email", ignoreCase = true) ->
                            "Email is already registered"

                        supabaseError.message.contains("duplicate key value", ignoreCase = true) &&
                                supabaseError.message.contains("username", ignoreCase = true) ->
                            "Username is already taken"

                        else -> supabaseError.message
                    }
                }

                else -> {
                    // Try to parse as a generic error object
                    try {
                        val errorMap = gson.fromJson(responseBody, Map::class.java) as? Map<String, Any>
                        when {
                            errorMap?.containsKey("msg") == true -> errorMap["msg"].toString()
                            errorMap?.containsKey("message") == true -> errorMap["message"].toString()
                            errorMap?.containsKey("error") == true -> errorMap["error"].toString()
                            else -> getDefaultErrorMessage(statusCode)
                        }
                    } catch (e: Exception) {
                        Log.e("SupabaseClient", "Failed to parse as generic error", e)
                        getDefaultErrorMessage(statusCode)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Error parsing error response", e)
            // If JSON parsing fails, try to extract meaningful error from raw response
            when {
                responseBody?.contains("Invalid login credentials", ignoreCase = true) == true ->
                    "Invalid email or password"
                responseBody?.contains("User already registered", ignoreCase = true) == true ->
                    "Email is already registered"
                responseBody?.contains("duplicate key value", ignoreCase = true) == true -> {
                    when {
                        responseBody.contains("email", ignoreCase = true) -> "Email is already registered"
                        responseBody.contains("username", ignoreCase = true) -> "Username is already taken"
                        else -> "This information is already in use"
                    }
                }
                else -> responseBody?.take(200) ?: getDefaultErrorMessage(statusCode)
            }
        }
    }

    /**
     * Get default error message based on status code
     */
    private fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Invalid request"
            401 -> "Invalid email or password"
            403 -> "Access denied"
            404 -> "Not found"
            409 -> "Data conflict - information already exists"
            422 -> "Invalid data provided"
            429 -> "Too many requests - please try again later"
            500 -> "Server error - please try again"
            else -> "Request failed"
        }
    }

    /**
     * POST request - Enhanced error handling
     */
    suspend fun post(
        endpoint: String,
        body: Any,
        accessToken: String? = null
    ): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            checkNetworkConnection()

            val jsonBody = gson.toJson(body)
            Log.d("SupabaseClient", "POST body: $jsonBody")

            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

            // Build URL properly
            val url = if (Constants.SUPABASE_URL.endsWith("/")) {
                "${Constants.SUPABASE_URL.dropLast(1)}$endpoint"
            } else {
                "${Constants.SUPABASE_URL}$endpoint"
            }

            Log.d("SupabaseClient", "POST URL: $url")

            var request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            // Add authentication if token provided
            if (!accessToken.isNullOrBlank()) {
                request = makeAuthenticatedRequest(request, accessToken)
            }

            val response = executeRequest(request)
            val responseBody = response.body?.string()

            Log.d("SupabaseClient", "Response code: ${response.code}")
            Log.d("SupabaseClient", "Response body: $responseBody")

            if (response.isSuccessful) {
                ApiResponse(
                    data = responseBody,
                    error = null,
                    message = "Success",
                    status = response.code
                )
            } else {
                val errorMessage = parseErrorResponse(responseBody, response.code)
                Log.e("SupabaseClient", "Request failed with error: $errorMessage")

                ApiResponse(
                    data = null,
                    error = ApiError(errorMessage),
                    message = errorMessage,
                    status = response.code
                )
            }
        } catch (e: IOException) {
            Log.e("SupabaseClient", "Network error", e)
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Request timed out - please check your connection"
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                    "Unable to connect - please check your internet connection"
                else -> "Network error - please check your connection"
            }

            ApiResponse(
                data = null,
                error = ApiError(errorMessage),
                message = errorMessage,
                status = 0
            )
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Unexpected error", e)
            ApiResponse(
                data = null,
                error = ApiError("An unexpected error occurred"),
                message = "An unexpected error occurred",
                status = 0
            )
        }
    }

    /**
     * GET request - Enhanced error handling
     */
    suspend fun get(
        endpoint: String,
        accessToken: String? = null,
        queryParams: Map<String, String>? = null
    ): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            checkNetworkConnection()

            // Build URL properly
            val baseUrl = if (Constants.SUPABASE_URL.endsWith("/")) {
                Constants.SUPABASE_URL.dropLast(1)
            } else {
                Constants.SUPABASE_URL
            }

            val parsedUrl = "$baseUrl$endpoint".toHttpUrlOrNull()

            if (parsedUrl == null) {
                throw IllegalArgumentException("Invalid URL: $baseUrl$endpoint")
            }

            val finalUrlBuilder = parsedUrl.newBuilder()

            // Add query parameters
            queryParams?.forEach { (key, value) ->
                finalUrlBuilder.addQueryParameter(key, value)
            }

            val finalUrl = finalUrlBuilder.build()
            Log.d("SupabaseClient", "GET URL: $finalUrl")

            var request = Request.Builder()
                .url(finalUrl)
                .get()
                .build()

            // Add authentication if token provided
            if (!accessToken.isNullOrBlank()) {
                request = makeAuthenticatedRequest(request, accessToken)
            }

            val response = executeRequest(request)
            val responseBody = response.body?.string()

            Log.d("SupabaseClient", "GET Response code: ${response.code}")
            Log.d("SupabaseClient", "GET Response body: $responseBody")

            if (response.isSuccessful) {
                ApiResponse(
                    data = responseBody,
                    error = null,
                    message = "Success",
                    status = response.code
                )
            } else {
                val errorMessage = parseErrorResponse(responseBody, response.code)
                ApiResponse(
                    data = null,
                    error = ApiError(errorMessage),
                    message = errorMessage,
                    status = response.code
                )
            }
        } catch (e: IOException) {
            Log.e("SupabaseClient", "Network error in GET", e)
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Request timed out - please check your connection"
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                    "Unable to connect - please check your internet connection"
                else -> "Network error - please check your connection"
            }

            ApiResponse(
                data = null,
                error = ApiError(errorMessage),
                message = errorMessage,
                status = 0
            )
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Unexpected error in GET", e)
            ApiResponse(
                data = null,
                error = ApiError("An unexpected error occurred"),
                message = "An unexpected error occurred",
                status = 0
            )
        }
    }

    /**
     * INSERT request to database table
     */
    suspend fun insert(
        table: String,
        data: Any,
        accessToken: String? = null
    ): ApiResponse<String> {
        val endpoint = "/rest/v1/$table"
        return post(endpoint, data, accessToken)
    }

    /**
     * GET request to database table
     */
    suspend fun select(
        table: String,
        select: String = "*",
        filter: String? = null,
        accessToken: String? = null
    ): ApiResponse<String> {
        val endpoint = "/rest/v1/$table"
        val queryParams = mutableMapOf<String, String>()

        queryParams["select"] = select
        filter?.let { queryParams["filter"] = it }

        return get(endpoint, accessToken, queryParams)
    }
}