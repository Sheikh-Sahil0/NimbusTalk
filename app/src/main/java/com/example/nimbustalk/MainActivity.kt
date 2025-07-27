package com.example.nimbustalk

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nimbustalk.activities.SettingsActivity
//import com.example.nimbustalk.activities.HomeActivity
//import com.example.nimbustalk.activities.SplashActivity
import com.example.nimbustalk.utils.SharedPrefsHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Don't set any content view - this is just a router

        // Check if user is logged in
        val sharedPrefs = SharedPrefsHelper(this)
        val isLoggedIn = sharedPrefs.isUserLoggedIn()

        if (true) {
            // User is logged in, go directly to HomeActivity
//            startActivity(Intent(this, HomeActivity::class.java))
            startActivity(Intent(this, SettingsActivity::class.java))
        } else {
            // User not logged in, show splash screen
//            startActivity(Intent(this, SplashActivity::class.java))
        }

        // Close this activity
        finish()
    }
}