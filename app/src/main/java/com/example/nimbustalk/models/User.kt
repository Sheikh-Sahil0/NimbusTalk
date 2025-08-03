package com.example.nimbustalk.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("display_name")
    val displayName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("status")
    val status: String = "offline", // online, offline, away

    @SerializedName("last_seen")
    val lastSeen: String? = null,

    @SerializedName("is_verified")
    val isVerified: Boolean = false,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    /**
     * Get display name or fallback to username
     */
    fun getDisplayNameOrUsername(): String {
        return if (displayName.isNotBlank()) displayName else username
    }

    /**
     * Check if user is online
     */
    fun isOnline(): Boolean {
        return status == "online"
    }

    /**
     * Get formatted status text
     */
    fun getStatusText(): String {
        return when (status) {
            "online" -> "Online"
            "away" -> "Away"
            else -> "Offline"
        }
    }
}