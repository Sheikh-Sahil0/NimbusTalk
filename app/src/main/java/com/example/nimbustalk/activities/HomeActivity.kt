package com.example.nimbustalk.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nimbustalk.R
import com.example.nimbustalk.utils.SharedPrefsHelper
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    private lateinit var sharedPrefsHelper: SharedPrefsHelper
    private lateinit var welcomeText: TextView
    private lateinit var settingActivityButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPrefsHelper = SharedPrefsHelper(this)

        initViews()
        setupUI()
        setupClickListeners()
    }

    private fun initViews() {
        welcomeText = findViewById(R.id.welcomeText)
        settingActivityButton = findViewById(R.id.settingActivity)
    }

    private fun setupUI() {
        // Get current user info
        val currentUser = sharedPrefsHelper.getCurrentUser()
        val displayName = currentUser?.displayName ?: "User"

        welcomeText.text = "Welcome, $displayName!"
    }

    private fun setupClickListeners() {
        settingActivityButton.setOnClickListener {
            startSettingActivity()
        }
    }

    private fun startSettingActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        // Prevent going back to login screen
        // Do nothing or show exit confirmation
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                // Close the activity
                finish()
            }
            .setNegativeButton("No", null) // Dismiss the dialog
            .show()
    }
}