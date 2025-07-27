package com.example.nimbustalk.enums

enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR;

    // Get display message for UI
    fun getDisplayMessage(): String {
        return when (this) {
            CONNECTED -> "Connected"
            DISCONNECTED -> "No internet connection"
            CONNECTING -> "Connecting..."
            ERROR -> "Connection error"
        }
    }

    // Check if network is available for API calls
    fun isAvailable(): Boolean {
        return this == CONNECTED
    }

    // Get color resource name for status indicator
    fun getColorName(): String {
        return when (this) {
            CONNECTED -> "success_color"
            DISCONNECTED -> "offline_grey"
            CONNECTING -> "warning_color"
            ERROR -> "error_color"
        }
    }
}