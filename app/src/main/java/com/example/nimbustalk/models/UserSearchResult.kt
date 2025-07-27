package com.example.nimbustalk.models

data class UserSearchResult(
    val users: List<User> = emptyList(),
    val totalCount: Int = 0,
    val hasMore: Boolean = false,
    val nextOffset: Int = 0
) {
    // Check if search returned any results
    fun hasResults(): Boolean {
        return users.isNotEmpty()
    }

    // Check if there are more users to load
    fun canLoadMore(): Boolean {
        return hasMore && users.size < totalCount
    }
}