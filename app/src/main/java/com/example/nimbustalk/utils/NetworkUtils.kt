package com.example.nimbustalk.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkUtils(private val context: Context) {

    // Check if network is available
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo?.isConnected == true
        }
    }

    /**
     * Check if connected to WiFi
     */
    fun isWifiConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            @Suppress("DEPRECATION")
            networkInfo?.isConnected == true
        }
    }

    /**
     * Check if connected to mobile data
     */
    fun isMobileDataConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            @Suppress("DEPRECATION")
            networkInfo?.isConnected == true
        }
    }

    /**
     * Get network type as string
     */
    fun getNetworkType(): String {
        return when {
            isWifiConnected() -> "WiFi"
            isMobileDataConnected() -> "Mobile Data"
            else -> "No Connection"
        }
    }

    /**
     * Check if network is metered (limited data)
     */
    fun isNetworkMetered(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.isActiveNetworkMetered
    }

    /**
     * Get connection strength (0-4 scale)
     */
    fun getConnectionStrength(): Int {
        // This is a simplified implementation
        // In a real app, you might use signal strength APIs
        return when {
            !isNetworkAvailable() -> 0
            isWifiConnected() -> 4
            isMobileDataConnected() -> 3
            else -> 1
        }
    }

    companion object {
        const val NETWORK_TIMEOUT_SECONDS = 30L
        const val RETRY_DELAY_MS = 2000L
        const val MAX_RETRY_ATTEMPTS = 3
    }
}