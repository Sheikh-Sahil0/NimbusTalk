package com.example.nimbustalk.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nimbustalk.R
import com.example.nimbustalk.utils.SharedPrefsHelper
import com.example.nimbustalk.utils.ThemeHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPrefsHelper
    private lateinit var currentThemeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPrefs = SharedPrefsHelper(this)

        setupViews()
        updateThemeDisplay()
    }

    private fun setupViews() {
        // Toolbar back button
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }

        // Theme selector
        currentThemeText = findViewById(R.id.currentTheme)
        findViewById<android.view.View>(R.id.themeSelector).setOnClickListener {
            showThemeDialog()
        }

        // Logout button
        findViewById<android.view.View>(R.id.logoutButton).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf("System Default", "Light", "Dark")
        val currentMode = ThemeHelper.getSavedThemeMode(this)

        AlertDialog.Builder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, currentMode) { dialog, which ->
                ThemeHelper.saveThemeMode(this, which)
                ThemeHelper.applyTheme(which)
                updateThemeDisplay()
                dialog.dismiss()

                // Recreate activity to apply theme immediately
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateThemeDisplay() {
        val currentMode = ThemeHelper.getSavedThemeMode(this)
        currentThemeText.text = ThemeHelper.getThemeModeName(this, currentMode)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage(getString(R.string.logout_confirmation))
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
//        // Clear user data
//        sharedPrefs.clearAuthData()
//
//        // Go back to MainActivity (which will redirect to splash/login)
//        val intent = Intent(this, MainActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//        finish()
    }
}