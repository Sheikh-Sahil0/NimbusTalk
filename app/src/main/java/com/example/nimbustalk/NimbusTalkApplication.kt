package com.example.nimbustalk

import android.app.Application
import com.example.nimbustalk.utils.ThemeHelper

class NimbusTalkApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Apply saved theme on app startup
        ThemeHelper.applyTheme(this)

        // Any other global initialization can go here
        // Like crash reporting, analytics, etc.
    }
}