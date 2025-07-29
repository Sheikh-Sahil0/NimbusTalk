package com.example.nimbustalk.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.nimbustalk.R
import com.example.nimbustalk.api.AuthApi
import com.example.nimbustalk.api.SupabaseClient
import com.example.nimbustalk.enums.LoadingState
import com.example.nimbustalk.enums.ValidationError
import com.example.nimbustalk.utils.NetworkUtils
import com.example.nimbustalk.utils.SharedPrefsHelper
import com.example.nimbustalk.utils.ValidationUtils
import com.example.nimbustalk.viewmodels.LoginViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.TextView
import com.example.nimbustalk.viewmodels.LoginViewModelFactory

class LoginActivity : AppCompatActivity() {

    // UI Components
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var loadingProgress: CircularProgressIndicator
    private lateinit var errorText: TextView
    private lateinit var forgotPasswordText: TextView
    private lateinit var registerText: TextView

    // ViewModel
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initViewModel()
        setupObservers()
        setupClickListeners()
        setupTextWatchers()
    }

    // Initialize UI components
    private fun initViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout)
        emailEditText = findViewById(R.id.emailEditText)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        loadingProgress = findViewById(R.id.loadingProgress)
        errorText = findViewById(R.id.errorText)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        registerText = findViewById(R.id.registerText)
    }

    // Initialize ViewModel
    private fun initViewModel() {
        // Create dependencies
        val networkUtils = NetworkUtils(this)
        val supabaseClient = SupabaseClient(networkUtils)
        val authApi = AuthApi(supabaseClient)
        val sharedPrefsHelper = SharedPrefsHelper(this)

        // Create ViewModel using factory
        val factory = LoginViewModelFactory(authApi, sharedPrefsHelper)
        loginViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    /**
     * Setup LiveData observers
     */
    private fun setupObservers() {
        // Loading state observer
        loginViewModel.loadingState.observe(this) { loadingState ->
            when (loadingState) {
                LoadingState.LOADING -> {
                    showLoading(true)
                    hideError()
                }
                LoadingState.SUCCESS -> {
                    showLoading(false)
                    hideError()
                }
                LoadingState.ERROR -> {
                    showLoading(false)
                }
                else -> {
                    showLoading(false)
                    hideError()
                }
            }
        }

        // Error message observer
        loginViewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                showError(errorMessage)
            } else {
                hideError()
            }
        }

        // Success message observer
        loginViewModel.successMessage.observe(this) { successMessage ->
            if (successMessage.isNotBlank()) {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
            }
        }

        // Login success observer
        loginViewModel.loginSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                navigateToHome()
                loginViewModel.resetLoginSuccess()
            }
        }

        // Email validation observer
        loginViewModel.emailError.observe(this) { validationError ->
            showEmailError(validationError)
        }

        // Password validation observer
        loginViewModel.passwordError.observe(this) { validationError ->
            showPasswordError(validationError)
        }

        // Form validity observer
        loginViewModel.isFormValid.observe(this) { isValid ->
            loginButton.isEnabled = isValid && !loginViewModel.isLoading()
        }
    }

    /**
     * Setup click listeners
     */
    private fun setupClickListeners() {
        // Login button click
        loginButton.setOnClickListener {
            if (!loginViewModel.isLoading()) {
                val email = ValidationUtils.cleanInput(emailEditText.text.toString())
                val password = passwordEditText.text.toString()
                loginViewModel.login(email, password)
            }
        }

        // Forgot password click
        forgotPasswordText.setOnClickListener {
            // TODO: Navigate to ForgotPasswordActivity
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Register click
        registerText.setOnClickListener {
            navigateToRegister()
        }
    }

    /**
     * Setup text watchers for real-time validation using ValidationUtils
     */
    private fun setupTextWatchers() {
        // Email text watcher with ValidationUtils
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = ValidationUtils.cleanInput(s.toString())
                val validationError = ValidationUtils.validateEmail(email)
                loginViewModel.setEmailValidation(validationError)

                // Real-time UI feedback
                if (email.isNotBlank()) {
                    showEmailError(validationError)
                } else {
                    clearEmailError()
                }
            }
        })

        // Password text watcher with ValidationUtils
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                val validationError = ValidationUtils.validatePassword(password)
                loginViewModel.setPasswordValidation(validationError)

                // Real-time UI feedback
                if (password.isNotBlank()) {
                    showPasswordError(validationError)
                } else {
                    clearPasswordError()
                }
            }
        })
    }

    /**
     * Show loading state
     */
    private fun showLoading(show: Boolean) {
        if (show) {
            loadingProgress.visibility = View.VISIBLE
            loginButton.isEnabled = false
            loginButton.text = "Signing in..."
        } else {
            loadingProgress.visibility = View.GONE
            loginButton.text = getString(R.string.login)
            // Button enabled state is handled by form validity observer
        }
    }

    /**
     * Show error message
     */
    private fun showError(message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
    }

    /**
     * Hide error message
     */
    private fun hideError() {
        errorText.visibility = View.GONE
    }

    /**
     * Show email validation error
     */
    private fun showEmailError(validationError: ValidationError) {
        if (validationError != ValidationError.NONE) {
            emailInputLayout.error = validationError.message
            emailInputLayout.isErrorEnabled = true
        } else {
            clearEmailError()
        }
    }

    /**
     * Clear email validation error
     */
    private fun clearEmailError() {
        emailInputLayout.isErrorEnabled = false
        emailInputLayout.error = null
    }

    /**
     * Show password validation error
     */
    private fun showPasswordError(validationError: ValidationError) {
        if (validationError != ValidationError.NONE) {
            passwordInputLayout.error = validationError.message
            passwordInputLayout.isErrorEnabled = true
        } else {
            clearPasswordError()
        }
    }

    /**
     * Clear password validation error
     */
    private fun clearPasswordError() {
        passwordInputLayout.isErrorEnabled = false
        passwordInputLayout.error = null
    }

    /**
     * Navigate to home screen
     */
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Navigate to register screen
     */
    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear any pending operations
        loginViewModel.clearError()
        loginViewModel.clearSuccess()
    }
}