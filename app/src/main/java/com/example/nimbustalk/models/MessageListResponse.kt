package com.example.nimbustalk.models

data class MessageListResponse(
    val messages: List<Message> = emptyList(),
    val totalCount: Int = 0,
    val hasMore: Boolean = false,
    val nextOffset: Int = 0,
    val chatInfo: Chat? = null
) {
    // Check if there are more messages to load
    fun canLoadMore(): Boolean {
        return hasMore && messages.size < totalCount
    }

    // Check if response has messages
    fun hasMessages(): Boolean {
        return messages.isNotEmpty()
    }

    // Get messages sorted by timestamp (oldest first)
    fun getSortedMessages(): List<Message> {
        return messages.sortedBy { it.timestamp }
    }
}