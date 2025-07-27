package com.example.nimbustalk.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsHelper(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // User Login State
    fun setUserLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false)
    }

    // User Authentication Data
    fun saveAuthTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(Constants.KEY_ACCESS_TOKEN, accessToken)
            .putString(Constants.KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(Constants.KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(Constants.KEY_REFRESH_TOKEN, null)
    }

    // User Profile Data
    fun saveUserProfile(userId: String, email: String, name: String, avatar: String? = null) {
        prefs.edit()
            .putString(Constants.KEY_USER_ID, userId)
            .putString(Constants.KEY_USER_EMAIL, email)
            .putString(Constants.KEY_USER_NAME, name)
            .putString(Constants.KEY_USER_AVATAR, avatar)
            .apply()
    }

    fun getUserId(): String? {
        return prefs.getString(Constants.KEY_USER_ID, null)
    }

    fun getUserEmail(): String? {
        return prefs.getString(Constants.KEY_USER_EMAIL, null)
    }

    fun getUserName(): String? {
        return prefs.getString(Constants.KEY_USER_NAME, null)
    }

    fun getUserAvatar(): String? {
        return prefs.getString(Constants.KEY_USER_AVATAR, null)
    }

    // App Settings
    fun setFirstTime(isFirstTime: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_FIRST_TIME, isFirstTime).apply()
    }

    fun isFirstTime(): Boolean {
        return prefs.getBoolean(Constants.KEY_FIRST_TIME, true)
    }

    // Clear All Data (for logout)
    fun clearAllData() {
        prefs.edit().clear().apply()
    }

    // Clear only auth data (keep app settings)
    fun clearAuthData() {
        prefs.edit()
            .remove(Constants.KEY_ACCESS_TOKEN)
            .remove(Constants.KEY_REFRESH_TOKEN)
            .remove(Constants.KEY_USER_ID)
            .remove(Constants.KEY_USER_EMAIL)
            .remove(Constants.KEY_USER_NAME)
            .remove(Constants.KEY_USER_AVATAR)
            .putBoolean(Constants.KEY_IS_LOGGED_IN, false)
            .apply()
    }
}