package com.example.nimbustalk

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nimbustalk.activities.SplashActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MainActivity is now just a launcher - redirect to SplashActivity
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()

        // Smooth transition
        overridePendingTransition(0, 0)
    }
}