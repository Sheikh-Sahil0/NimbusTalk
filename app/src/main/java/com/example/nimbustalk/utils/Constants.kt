package com.example.nimbustalk.utils

object Constants {

    // Supabase Configuration (You'll replace these with your actual Supabase details)
    const val SUPABASE_URL = "https://your-project.supabase.co"
    const val SUPABASE_ANON_KEY = "your-anon-key-here"

    // API Endpoints
    const val AUTH_SIGNUP = "/auth/v1/signup"
    const val AUTH_LOGIN = "/auth/v1/token?grant_type=password"
    const val AUTH_REFRESH = "/auth/v1/token?grant_type=refresh_token"
    const val AUTH_LOGOUT = "/auth/v1/logout"
    const val AUTH_RESET_PASSWORD = "/auth/v1/recover"

    // Database Tables
    const val TABLE_USERS = "users"
    const val TABLE_CHATS = "chats"
    const val TABLE_MESSAGES = "messages"
    const val TABLE_USER_CHATS = "user_chats"

    // SharedPreferences Keys
    const val PREFS_NAME = "nimbus_talk_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_AVATAR = "user_avatar"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_FIRST_TIME = "first_time"

    // Request Codes
    const val REQUEST_CODE_CAMERA = 100
    const val REQUEST_CODE_GALLERY = 101
    const val REQUEST_CODE_PERMISSIONS = 102

    // Network
    const val NETWORK_TIMEOUT = 30L // seconds
    const val MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5MB
    const val IMAGE_QUALITY = 80 // 0-100

    // UI
    const val SPLASH_DELAY = 2000L // milliseconds
    const val TYPING_DELAY = 1000L // milliseconds
    const val MESSAGE_PAGE_SIZE = 20
    const val CHAT_PAGE_SIZE = 15

    // Message Types
    const val MESSAGE_TYPE_TEXT = "text"
    const val MESSAGE_TYPE_IMAGE = "image"
    const val MESSAGE_TYPE_FILE = "file"

    // User Status
    const val STATUS_ONLINE = "online"
    const val STATUS_OFFLINE = "offline"
    const val STATUS_AWAY = "away"

    // Error Messages
    const val ERROR_NETWORK = "Network connection error"
    const val ERROR_SERVER = "Server error occurred"
    const val ERROR_UNAUTHORIZED = "Unauthorized access"
    const val ERROR_NOT_FOUND = "Resource not found"
    const val ERROR_VALIDATION = "Validation error"

    // Success Messages
    const val SUCCESS_LOGIN = "Login successful"
    const val SUCCESS_REGISTER = "Registration successful"
    const val SUCCESS_LOGOUT = "Logged out successfully"
    const val SUCCESS_PASSWORD_RESET = "Password reset email sent"
}