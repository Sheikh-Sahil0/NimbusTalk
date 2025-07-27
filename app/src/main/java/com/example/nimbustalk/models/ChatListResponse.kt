package com.example.nimbustalk.models

data class ChatListResponse(
    val chats: List<Chat> = emptyList(),
    val users: Map<String, User> = emptyMap(), // User cache for chat participants
    val totalCount: Int = 0,
    val hasMore: Boolean = false,
    val nextOffset: Int = 0
) {
    // Get user by ID from cached users
    fun getUserById(userId: String): User? {
        return users[userId]
    }

    // Check if there are more chats to load
    fun canLoadMore(): Boolean {
        return hasMore && chats.size < totalCount
    }

    // Check if response has chats
    fun hasChats(): Boolean {
        return chats.isNotEmpty()
    }
}