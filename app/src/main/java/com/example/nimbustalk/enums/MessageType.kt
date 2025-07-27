package com.example.nimbustalk.enums

enum class MessageType(val value: String) {
    TEXT("text"),
    IMAGE("image"),
    FILE("file");

    companion object {
        // Convert string to enum
        fun fromString(value: String): MessageType {
            return values().find { it.value == value } ?: TEXT
        }
    }

    // Get display name for UI
    fun getDisplayName(): String {
        return when (this) {
            TEXT -> "Text"
            IMAGE -> "Image"
            FILE -> "File"
        }
    }

    // Get icon resource name (you can use these with drawable resources)
    fun getIconName(): String {
        return when (this) {
            TEXT -> "ic_message_text"
            IMAGE -> "ic_image"
            FILE -> "ic_attach_file"
        }
    }

    // Check if message type supports preview
    fun supportsPreview(): Boolean {
        return when (this) {
            TEXT -> true
            IMAGE -> true
            FILE -> false
        }
    }
}