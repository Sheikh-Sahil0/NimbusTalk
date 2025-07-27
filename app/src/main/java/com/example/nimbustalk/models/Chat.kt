package com.example.nimbustalk.models

import com.example.nimbustalk.enums.ChatStatus

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(), // User IDs
    val lastMessage: Message? = null,
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0,
    val status: ChatStatus = ChatStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val chatName: String? = null, // For group chats (future feature)
    val isGroupChat: Boolean = false
) {
    // Get the other participant's ID (for one-on-one chats)
    fun getOtherParticipantId(currentUserId: String): String? {
        return participants.find { it != currentUserId }
    }

    // Check if chat has unread messages
    fun hasUnreadMessages(): Boolean {
        return unreadCount > 0
    }

    // Get last message preview text
    fun getLastMessagePreview(): String {
        return lastMessage?.getPreviewText() ?: "No messages yet"
    }

    // Get formatted time for chat list
    fun getFormattedTime(): String {
        if (lastMessageTime == 0L) return ""

        val now = System.currentTimeMillis()
        val diff = now - lastMessageTime

        return when {
            diff < 24 * 60 * 60 * 1000 -> {
                // Today - show time
                val hour = (lastMessageTime / (60 * 60 * 1000)) % 24
                val minute = (lastMessageTime / (60 * 1000)) % 60
                String.format("%02d:%02d", hour, minute)
            }
            diff < 7 * 24 * 60 * 60 * 1000 -> {
                // This week - show day
                val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                val dayOfWeek = ((lastMessageTime / (24 * 60 * 60 * 1000)) % 7).toInt()
                dayNames[dayOfWeek]
            }
            else -> {
                // Older - show date
                val day = (lastMessageTime / (24 * 60 * 60 * 1000)) % 31
                val month = (lastMessageTime / (30 * 24 * 60 * 60 * 1000)) % 12
                "${day}/${month}"
            }
        }
    }
}