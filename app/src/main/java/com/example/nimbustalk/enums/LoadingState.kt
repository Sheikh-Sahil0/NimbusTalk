package com.example.nimbustalk.enums

enum class LoadingState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR,
    EMPTY;

    // Check if currently loading
    fun isLoading(): Boolean {
        return this == LOADING
    }

    // Check if operation was successful
    fun isSuccess(): Boolean {
        return this == SUCCESS
    }

    // Check if there was an error
    fun isError(): Boolean {
        return this == ERROR
    }

    // Check if result is empty
    fun isEmpty(): Boolean {
        return this == EMPTY
    }

    // Check if should show loading indicator
    fun shouldShowLoading(): Boolean {
        return this == LOADING
    }

    // Check if should show error message
    fun shouldShowError(): Boolean {
        return this == ERROR
    }

    // Check if should show empty state
    fun shouldShowEmpty(): Boolean {
        return this == EMPTY
    }
}