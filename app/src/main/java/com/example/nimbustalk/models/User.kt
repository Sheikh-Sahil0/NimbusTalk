package com.example.nimbustalk.models

import com.example.nimbustalk.enums.UserStatus

data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val status: UserStatus = UserStatus.OFFLINE,
    val lastSeen: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val deviceToken: String? = null // For push notifications
) {
    // Helper function to get display name or fallback to username
    fun getDisplayNameOrUsername(): String {
        return if (displayName.isNotBlank()) displayName else username
    }

    // Helper function to check if user was recently online (within 5 minutes)
    fun isRecentlyOnline(): Boolean {
        val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
        return lastSeen > fiveMinutesAgo
    }

    // Helper function to get status text for UI
    fun getStatusText(): String {
        return when {
            isOnline -> "Online"
            isRecentlyOnline() -> "Recently online"
            else -> "Last seen ${getFormattedLastSeen()}"
        }
    }

    private fun getFormattedLastSeen(): String {
        val now = System.currentTimeMillis()
        val diff = now - lastSeen

        return when {
            diff < 60 * 1000 -> "just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            else -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        }
    }
}