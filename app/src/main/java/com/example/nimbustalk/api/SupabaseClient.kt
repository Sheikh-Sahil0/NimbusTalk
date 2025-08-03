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
     * Parse error response
     */
    private fun parseErrorResponse(responseBody: String?): String {
        return try {
            if (responseBody.isNullOrBlank()) {
                "Unknown error occurred"
            } else {
                Log.d("SupabaseClient", "Error response body: $responseBody")
                val supabaseError = gson.fromJson(responseBody, SupabaseError::class.java)
                supabaseError.getDisplayMessage()
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Error parsing error response", e)
            responseBody ?: "Unknown error occurred"
        }
    }

    /**
     * POST request - FIXED
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

            // Build URL properly - FIXED
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

            Log.d("SupabaseClient", "Response body: $responseBody")

            if (response.isSuccessful) {
                ApiResponse(
                    data = responseBody,
                    error = null,
                    message = "Success",
                    status = response.code
                )
            } else {
                val errorMessage = parseErrorResponse(responseBody)
                ApiResponse(
                    data = null,
                    error = ApiError(errorMessage),
                    message = errorMessage,
                    status = response.code
                )
            }
        } catch (e: IOException) {
            Log.e("SupabaseClient", "Network error", e)
            ApiResponse(
                data = null,
                error = ApiError("Network error: ${e.message}"),
                message = "Network error occurred",
                status = 0
            )
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Unexpected error", e)
            ApiResponse(
                data = null,
                error = ApiError("Unexpected error: ${e.message}"),
                message = "An unexpected error occurred",
                status = 0
            )
        }
    }

    /**
     * GET request - FIXED
     */
    suspend fun get(
        endpoint: String,
        accessToken: String? = null,
        queryParams: Map<String, String>? = null
    ): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            checkNetworkConnection()

            // Build URL properly - FIXED
            val baseUrl = if (Constants.SUPABASE_URL.endsWith("/")) {
                Constants.SUPABASE_URL.dropLast(1)
            } else {
                Constants.SUPABASE_URL
            }

            val urlBuilder = HttpUrl.Builder()
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

            Log.d("SupabaseClient", "GET Response body: $responseBody")

            if (response.isSuccessful) {
                ApiResponse(
                    data = responseBody,
                    error = null,
                    message = "Success",
                    status = response.code
                )
            } else {
                val errorMessage = parseErrorResponse(responseBody)
                ApiResponse(
                    data = null,
                    error = ApiError(errorMessage),
                    message = errorMessage,
                    status = response.code
                )
            }
        } catch (e: IOException) {
            Log.e("SupabaseClient", "Network error in GET", e)
            ApiResponse(
                data = null,
                error = ApiError("Network error: ${e.message}"),
                message = "Network error occurred",
                status = 0
            )
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Unexpected error in GET", e)
            ApiResponse(
                data = null,
                error = ApiError("Unexpected error: ${e.message}"),
                message = "An unexpected error occurred",
                status = 0
            )
        }
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
}