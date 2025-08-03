package com.example.nimbustalk.enums

enum class LoadingState {
    IDLE,       // Initial state, no operation
    LOADING,    // Operation in progress
    SUCCESS,    // Operation completed successfully
    ERROR       // Operation failed
}