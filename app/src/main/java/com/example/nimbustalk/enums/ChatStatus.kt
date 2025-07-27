package com.example.nimbustalk.enums

enum class ChatStatus(val value: String) {
    ACTIVE("active"),
    ARCHIVED("archived"),
    BLOCKED("blocked"),
    DELETED("deleted"),
    MUTED("muted");

    companion object {
        // Convert string to enum
        fun fromString(value: String): ChatStatus {
            return values().find { it.value == value } ?: ACTIVE
        }
    }

    // Get display name for UI
    fun getDisplayName(): String {
        return when (this) {
            ACTIVE -> "Active"
            ARCHIVED -> "Archived"
            BLOCKED -> "Blocked"
            DELETED -> "Deleted"
            MUTED -> "Muted"
        }
    }

    // Get status icon resource name
    fun getIconName(): String {
        return when (this) {
            ACTIVE -> "ic_chat_active"
            ARCHIVED -> "ic_archive"
            BLOCKED -> "ic_block"
            DELETED -> "ic_delete"
            MUTED -> "ic_volume_off"
        }
    }

    // Check if chat can receive new messages
    fun canReceiveMessages(): Boolean {
        return when (this) {
            ACTIVE, MUTED -> true
            ARCHIVED, BLOCKED, DELETED -> false
        }
    }

    // Check if chat should show notifications
    fun shouldShowNotifications(): Boolean {
        return when (this) {
            ACTIVE -> true
            ARCHIVED, BLOCKED, DELETED, MUTED -> false
        }
    }

    // Check if chat is visible in chat list
    fun isVisibleInList(): Boolean {
        return when (this) {
            ACTIVE, MUTED -> true
            ARCHIVED, BLOCKED, DELETED -> false
        }
    }

    // Get status priority for sorting (lower number = higher priority)
    fun getPriority(): Int {
        return when (this) {
            ACTIVE -> 1
            MUTED -> 2
            ARCHIVED -> 3
            BLOCKED -> 4
            DELETED -> 5
        }
    }
}