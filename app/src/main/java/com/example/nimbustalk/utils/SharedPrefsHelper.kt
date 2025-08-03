package com.example.nimbustalk.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.nimbustalk.models.User

class SharedPrefsHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    /**
     * Save user authentication data
     */
    fun saveAuthData(
        userId: String,
        email: String,
        username: String,
        displayName: String,
        accessToken: String,
        refreshToken: String,
        profileImageUrl: String? = null
    ) {
        editor.apply {
            putString(Constants.PREF_USER_ID, userId)
            putString(Constants.PREF_EMAIL, email)
            putString(Constants.PREF_USERNAME, username)
            putString(Constants.PREF_DISPLAY_NAME, displayName)
            putString(Constants.PREF_ACCESS_TOKEN, accessToken)
            putString(Constants.PREF_REFRESH_TOKEN, refreshToken)
            putString(Constants.PREF_PROFILE_IMAGE, profileImageUrl)
            putBoolean(Constants.PREF_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Save user profile data
     */
    fun saveUserProfile(user: User) {
        editor.apply {
            putString(Constants.PREF_USER_ID, user.id)
            putString(Constants.PREF_EMAIL, user.email)
            putString(Constants.PREF_USERNAME, user.username)
            putString(Constants.PREF_DISPLAY_NAME, user.displayName)
            putString(Constants.PREF_PROFILE_IMAGE, user.profileImageUrl)
            apply()
        }
    }

    /**
     * Update access token
     */
    fun updateAccessToken(accessToken: String) {
        editor.putString(Constants.PREF_ACCESS_TOKEN, accessToken).apply()
    }

    /**
     * Update refresh token
     */
    fun updateRefreshToken(refreshToken: String) {
        editor.putString(Constants.PREF_REFRESH_TOKEN, refreshToken).apply()
    }

    /**
     * Get stored user ID
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(Constants.PREF_USER_ID, null)
    }

    /**
     * Get stored email
     */
    fun getEmail(): String? {
        return sharedPreferences.getString(Constants.PREF_EMAIL, null)
    }

    /**
     * Get stored username
     */
    fun getUsername(): String? {
        return sharedPreferences.getString(Constants.PREF_USERNAME, null)
    }

    /**
     * Get stored display name
     */
    fun getDisplayName(): String? {
        return sharedPreferences.getString(Constants.PREF_DISPLAY_NAME, null)
    }

    /**
     * Get stored profile image URL
     */
    fun getProfileImageUrl(): String? {
        return sharedPreferences.getString(Constants.PREF_PROFILE_IMAGE, null)
    }

    /**
     * Get stored access token
     */
    fun getAccessToken(): String? {
        return sharedPreferences.getString(Constants.PREF_ACCESS_TOKEN, null)
    }

    /**
     * Get stored refresh token
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(Constants.PREF_REFRESH_TOKEN, null)
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false) &&
                !getAccessToken().isNullOrBlank()
    }

    /**
     * Get current user as User object
     */
    fun getCurrentUser(): User? {
        val userId = getUserId()
        val email = getEmail()
        val username = getUsername()
        val displayName = getDisplayName()

        return if (userId != null && email != null && username != null && displayName != null) {
            User(
                id = userId,
                email = email,
                username = username,
                displayName = displayName,
                profileImageUrl = getProfileImageUrl(),
                bio = null,
                phoneNumber = null,
                status = "offline",
                lastSeen = null,
                isVerified = false,
                createdAt = null,
                updatedAt = null
            )
        } else {
            null
        }
    }

    /**
     * Clear all authentication data
     */
    fun clearAuthData() {
        editor.apply {
            remove(Constants.PREF_USER_ID)
            remove(Constants.PREF_EMAIL)
            remove(Constants.PREF_USERNAME)
            remove(Constants.PREF_DISPLAY_NAME)
            remove(Constants.PREF_PROFILE_IMAGE)
            remove(Constants.PREF_ACCESS_TOKEN)
            remove(Constants.PREF_REFRESH_TOKEN)
            putBoolean(Constants.PREF_IS_LOGGED_IN, false)
            apply()
        }
    }

    /**
     * Save theme preference
     */
    fun saveThemeMode(themeMode: Int) {
        editor.putInt(Constants.PREF_THEME_MODE, themeMode).apply()
    }

    /**
     * Get saved theme preference
     */
    fun getThemeMode(): Int {
        return sharedPreferences.getInt(Constants.PREF_THEME_MODE, Constants.THEME_SYSTEM)
    }

    /**
     * Clear all preferences
     */
    fun clearAll() {
        editor.clear().apply()
    }
}