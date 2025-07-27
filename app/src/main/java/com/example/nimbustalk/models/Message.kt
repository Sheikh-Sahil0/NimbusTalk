package com.example.nimbustalk.models

import com.example.nimbustalk.enums.MessageType

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val readAt: Long? = null,
    val imageUrl: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val isDelivered: Boolean = false,
    val deliveredAt: Long? = null,
    val isEdited: Boolean = false,
    val editedAt: Long? = null,
    val replyToMessageId: String? = null // For future reply feature
) {
    // Get preview text for chat list
    fun getPreviewText(): String {
        return when (type) {
            MessageType.TEXT -> content
            MessageType.IMAGE -> "📷 Image"
            MessageType.FILE -> "📎 ${fileName ?: "File"}"
        }
    }

    // Get formatted timestamp for message display
    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
            diff < 24 * 60 * 60 * 1000 -> {
                val hour = (timestamp / (60 * 60 * 1000)) % 24
                val minute = (timestamp / (60 * 1000)) % 60
                String.format("%02d:%02d", hour, minute)
            }
            else -> {
                val day = (timestamp / (24 * 60 * 60 * 1000)) % 31
                val month = (timestamp / (30 * 24 * 60 * 60 * 1000)) % 12
                "${day}/${month}"
            }
        }
    }

    // Check if message is from current user
    fun isSentByUser(currentUserId: String): Boolean {
        return senderId == currentUserId
    }

    // Get message status text
    fun getStatusText(): String {
        return when {
            !isDelivered -> "Sending..."
            !isRead -> "Delivered"
            isRead -> "Read"
            else -> ""
        }
    }

    // Check if message contains media
    fun hasMedia(): Boolean {
        return type != MessageType.TEXT
    }

    // Get file size in readable format
    fun getFormattedFileSize(): String {
        if (fileSize == null) return ""

        return when {
            fileSize < 1024 -> "${fileSize} B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> "${fileSize / (1024 * 1024)} MB"
        }
    }
}