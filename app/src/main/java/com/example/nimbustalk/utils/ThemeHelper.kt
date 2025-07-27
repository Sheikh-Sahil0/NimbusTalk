package com.example.nimbustalk.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    private const val THEME_PREFS = "theme_preferences"
    private const val THEME_MODE_KEY = "theme_mode"

    // Theme modes
    const val MODE_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    // Apply the saved theme mode
    fun applyTheme(context: Context) {
        val mode = getSavedThemeMode(context)
        applyTheme(mode)
    }

    // Apply specific theme mode
    fun applyTheme(mode: Int) {
        when (mode) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MODE_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    // Save theme mode preference
    fun saveThemeMode(context: Context, mode: Int) {
        val prefs = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(THEME_MODE_KEY, mode).apply()
    }

    // Get saved theme mode (default: follow system)
    fun getSavedThemeMode(context: Context): Int {
        val prefs = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(THEME_MODE_KEY, MODE_SYSTEM)
    }

    // Get current theme mode name for display
    fun getThemeModeName(context: Context, mode: Int): String {
        return when (mode) {
            MODE_LIGHT -> "Light"
            MODE_DARK -> "Dark"
            MODE_SYSTEM -> "System Default"
            else -> "System Default"
        }
    }

     // Check if current theme is dark
    fun isDarkTheme(context: Context): Boolean {
        val currentMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentMode == Configuration.UI_MODE_NIGHT_YES
    }

     // Toggle between light and dark (skips system mode)
    fun toggleTheme(context: Context) {
        val currentMode = getSavedThemeMode(context)
        val newMode = when (currentMode) {
            MODE_LIGHT -> MODE_DARK
            MODE_DARK -> MODE_LIGHT
            MODE_SYSTEM -> if (isDarkTheme(context)) MODE_LIGHT else MODE_DARK
            else -> MODE_LIGHT
        }

        saveThemeMode(context, newMode)
        applyTheme(newMode)
    }
}