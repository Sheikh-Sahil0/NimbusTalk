package com.example.nimbustalk.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.example.nimbustalk.R
import com.example.nimbustalk.viewmodels.SplashViewModel
import com.example.nimbustalk.enums.LoadingState

class SplashActivity : AppCompatActivity() {

    private lateinit var viewModel: SplashViewModel

    // UI Components
    private lateinit var logoImageView: ImageView
    private lateinit var appNameText: TextView
    private lateinit var taglineText: TextView
    private lateinit var loadingProgress: CircularProgressIndicator
    private lateinit var statusText: TextView
    private lateinit var versionText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize views
        initViews()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]

        // Start animations
        startEntranceAnimations()

        // Observe states
        observeViewModel()

        // Start the authentication check after animations
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.checkAuthenticationStatus()
        }, 1000) // Wait for entrance animations to complete
    }

    private fun initViews() {
        logoImageView = findViewById(R.id.logoImageView)
        appNameText = findViewById(R.id.appNameText)
        taglineText = findViewById(R.id.taglineText)
        loadingProgress = findViewById(R.id.loadingProgress)
        statusText = findViewById(R.id.statusText)
        versionText = findViewById(R.id.versionText)

        // Set version text
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionText.text = "v${packageInfo.versionName}"
        } catch (e: Exception) {
            versionText.text = "v1.0.0"
        }

        // Initially hide all views for animation
        logoImageView.alpha = 0f
        appNameText.alpha = 0f
        taglineText.alpha = 0f
        loadingProgress.alpha = 0f
        statusText.alpha = 0f
        versionText.alpha = 0f
    }

    private fun startEntranceAnimations() {
        // Logo animation
        val logoAnimator = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        // App name animation
        val nameAnimator = ObjectAnimator.ofFloat(appNameText, "alpha", 0f, 1f).apply {
            duration = 600
            startDelay = 300
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Tagline animation
        val taglineAnimator = ObjectAnimator.ofFloat(taglineText, "alpha", 0f, 1f).apply {
            duration = 600
            startDelay = 600
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Loading progress animation
        val progressAnimator = ObjectAnimator.ofFloat(loadingProgress, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 1000
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Status text animation
        val statusAnimator = ObjectAnimator.ofFloat(statusText, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 1200
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Version text animation
        val versionAnimator = ObjectAnimator.ofFloat(versionText, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 1400
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Logo scale animation for extra effect
        val logoScaleX = ObjectAnimator.ofFloat(logoImageView, "scaleX", 0.3f, 1f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        val logoScaleY = ObjectAnimator.ofFloat(logoImageView, "scaleY", 0.3f, 1f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Play all animations together
        AnimatorSet().apply {
            playTogether(
                logoAnimator, logoScaleX, logoScaleY,
                nameAnimator, taglineAnimator,
                progressAnimator, statusAnimator, versionAnimator
            )
            start()
        }
    }

    private fun observeViewModel() {
        // Authentication state
        viewModel.authState.observe(this) { isAuthenticated ->
            updateStatusText("Authentication complete")
            Handler(Looper.getMainLooper()).postDelayed({
                navigateToNextScreen(isAuthenticated)
            }, 500)
        }

        // Loading state
        viewModel.loadingState.observe(this) { state ->
            when (state) {
                LoadingState.LOADING -> {
                    updateStatusText("Checking authentication...")
                    showLoading(true)
                }
                LoadingState.SUCCESS -> {
                    showLoading(false)
                }
                LoadingState.ERROR -> {
                    updateStatusText("Connection error")
                    showLoading(false)
                    // Still proceed to app after error
                    Handler(Looper.getMainLooper()).postDelayed({
                        navigateToLogin()
                    }, 1500)
                }
                else -> {
                    showLoading(false)
                }
            }
        }

        // Error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                updateStatusText("Error: $errorMessage")
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateToLogin()
                }, 2000)
            }
        }

        // Network status
        viewModel.networkStatus.observe(this) { networkType ->
            if (networkType.isNotBlank()) {
                updateStatusText("Connected via $networkType")
            }
        }
    }

    private fun updateStatusText(message: String) {
        statusText.text = message

        // Animate text change
        ObjectAnimator.ofFloat(statusText, "alpha", 0.5f, 1f).apply {
            duration = 300
            start()
        }
    }

    private fun showLoading(show: Boolean) {
        loadingProgress.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    private fun navigateToNextScreen(isAuthenticated: Boolean) {
        startExitAnimations {
            val intent = if (isAuthenticated) {
                // User is authenticated - go to HomeActivity
                Intent(this, HomeActivity::class.java)
            } else {
                // User is not authenticated - go to LoginActivity
                Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun navigateToLogin() {
        startExitAnimations {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun startExitAnimations(onComplete: () -> Unit) {
        // Fade out all views
        val fadeOutDuration = 500L

        val logoFadeOut = ObjectAnimator.ofFloat(logoImageView, "alpha", 1f, 0f).apply {
            duration = fadeOutDuration
        }

        val nameFadeOut = ObjectAnimator.ofFloat(appNameText, "alpha", 1f, 0f).apply {
            duration = fadeOutDuration
        }

        val taglineFadeOut = ObjectAnimator.ofFloat(taglineText, "alpha", 1f, 0f).apply {
            duration = fadeOutDuration
        }

        val progressFadeOut = ObjectAnimator.ofFloat(loadingProgress, "alpha", 1f, 0f).apply {
            duration = fadeOutDuration
        }

        val statusFadeOut = ObjectAnimator.ofFloat(statusText, "alpha", 1f, 0f).apply {
            duration = fadeOutDuration
        }

        val versionFadeOut = ObjectAnimator.ofFloat(versionText, "alpha", 1f, 0f).apply {
            duration = fadeOutDuration
        }

        AnimatorSet().apply {
            playTogether(
                logoFadeOut, nameFadeOut, taglineFadeOut,
                progressFadeOut, statusFadeOut, versionFadeOut
            )
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete()
                }
            })
            start()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Disable back button on splash screen
        // Do nothing - user must wait for app to load
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up any ongoing operations
        viewModel.cleanup()
    }
}