package com.example.nimbustalk.utils

import com.example.nimbustalk.BuildConfig

object Constants {
    // Supabase Configuration
    const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    const val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY

    // Storage buckets
    const val PROFILE_IMAGES_BUCKET = "profile-images"
    const val CHAT_FILES_BUCKET = "chat-files"

    // Database tables
    const val USERS_TABLE = "users"
    const val CHATS_TABLE = "chats"
    const val MESSAGES_TABLE = "messages"
    const val CHAT_PARTICIPANTS_TABLE = "chat_participants"
    const val MESSAGE_STATUS_TABLE = "message_status"

    // API Endpoints
    const val AUTH_SIGNUP = "/auth/v1/signup"
    const val AUTH_LOGIN = "/auth/v1/token?grant_type=password"
    const val AUTH_LOGOUT = "/auth/v1/logout"
    const val AUTH_REFRESH = "/auth/v1/token?grant_type=refresh_token"
    const val AUTH_FORGOT_PASSWORD = "/auth/v1/recover"
    const val AUTH_RESET_PASSWORD = "/auth/v1/token?grant_type=password"

    // Shared Preferences Keys
    const val PREF_NAME = "NimbusTalk_Prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_EMAIL = "email"
    const val PREF_USERNAME = "username"
    const val PREF_DISPLAY_NAME = "display_name"
    const val PREF_PROFILE_IMAGE = "profile_image"
    const val PREF_ACCESS_TOKEN = "access_token"
    const val PREF_REFRESH_TOKEN = "refresh_token"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    const val PREF_THEME_MODE = "theme_mode"

    // Validation Constants
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 128
    const val MIN_USERNAME_LENGTH = 3
    const val MAX_USERNAME_LENGTH = 50
    const val MIN_DISPLAY_NAME_LENGTH = 1
    const val MAX_DISPLAY_NAME_LENGTH = 100

    // Network Constants
    const val NETWORK_TIMEOUT = 30L
    const val MAX_RETRY_ATTEMPTS = 3
    const val RETRY_DELAY_MS = 1000L

    // File Upload Constants
    const val MAX_IMAGE_SIZE_MB = 5
    const val MAX_FILE_SIZE_MB = 10
    const val IMAGE_QUALITY = 80

    // Chat Constants
    const val MESSAGES_PER_PAGE = 50
    const val TYPING_TIMEOUT_MS = 3000L

    // Theme Constants
    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2
}