package com.example.nimbustalk.enums

enum class UserStatus(val value: String) {
    ONLINE("online"),
    OFFLINE("offline"),
    AWAY("away"),
    BUSY("busy"),
    INVISIBLE("invisible");

    companion object {
        // Convert string to enum
        fun fromString(value: String): UserStatus {
            return values().find { it.value == value } ?: OFFLINE
        }
    }

    // Get display name for UI
    fun getDisplayName(): String {
        return when (this) {
            ONLINE -> "Online"
            OFFLINE -> "Offline"
            AWAY -> "Away"
            BUSY -> "Busy"
            INVISIBLE -> "Invisible"
        }
    }

    // Get status color resource name
    fun getColorName(): String {
        return when (this) {
            ONLINE -> "online_green"
            OFFLINE -> "offline_grey"
            AWAY -> "warning_color"
            BUSY -> "error_color"
            INVISIBLE -> "offline_grey"
        }
    }

    // Get status icon resource name
    fun getIconName(): String {
        return when (this) {
            ONLINE -> "ic_online"
            OFFLINE -> "ic_offline"
            AWAY -> "ic_away"
            BUSY -> "ic_busy"
            INVISIBLE -> "ic_invisible"
        }
    }

    // Check if user is available for chat
    fun isAvailable(): Boolean {
        return when (this) {
            ONLINE, AWAY -> true
            OFFLINE, BUSY, INVISIBLE -> false
        }
    }

    // Check if status should be shown to other users
    fun isVisible(): Boolean {
        return this != INVISIBLE
    }
}