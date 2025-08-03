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
import com.example.nimbustalk.api.UserApi
import com.example.nimbustalk.enums.LoadingState
import com.example.nimbustalk.enums.ValidationError
import com.example.nimbustalk.utils.NetworkUtils
import com.example.nimbustalk.utils.SharedPrefsHelper
import com.example.nimbustalk.utils.ValidationUtils
import com.example.nimbustalk.viewmodels.RegisterViewModel
import com.example.nimbustalk.viewmodels.RegisterViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.TextView

class RegisterActivity : AppCompatActivity() {

    // UI Components
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var usernameInputLayout: TextInputLayout
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var displayNameInputLayout: TextInputLayout
    private lateinit var displayNameEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var loadingProgress: CircularProgressIndicator
    private lateinit var errorText: TextView
    private lateinit var alreadyHaveAccountText: TextView

    // ViewModel
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

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

    /**
     * Initialize UI components
     */
    private fun initViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout)
        emailEditText = findViewById(R.id.emailEditText)
        usernameInputLayout = findViewById(R.id.usernameInputLayout)
        usernameEditText = findViewById(R.id.usernameEditText)
        displayNameInputLayout = findViewById(R.id.displayNameInputLayout)
        displayNameEditText = findViewById(R.id.displayNameEditText)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        loadingProgress = findViewById(R.id.loadingProgress)
        errorText = findViewById(R.id.errorText)
        alreadyHaveAccountText = findViewById(R.id.alreadyHaveAccountText)
    }

    /**
     * Initialize ViewModel
     */
    private fun initViewModel() {
        // Create dependencies
        val networkUtils = NetworkUtils(this)
        val supabaseClient = SupabaseClient(networkUtils)
        val authApi = AuthApi(supabaseClient)
        val userApi = UserApi(supabaseClient)
        val sharedPrefsHelper = SharedPrefsHelper(this)

        // Create ViewModel using factory
        val factory = RegisterViewModelFactory(authApi, userApi, sharedPrefsHelper)
        registerViewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]
    }

    /**
     * Setup LiveData observers
     */
    private fun setupObservers() {
        // Loading state observer
        registerViewModel.loadingState.observe(this) { loadingState ->
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
        registerViewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                showError(errorMessage)
            } else {
                hideError()
            }
        }

        // Success message observer
        registerViewModel.successMessage.observe(this) { successMessage ->
            if (successMessage.isNotBlank()) {
                Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
            }
        }

        // Registration success observer
        registerViewModel.registrationSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                navigateToHome()
                registerViewModel.resetRegistrationSuccess()
            }
        }

        // Email validation observer
        registerViewModel.emailError.observe(this) { validationError ->
            showEmailError(validationError)
        }

        // Username validation observer
        registerViewModel.usernameError.observe(this) { validationError ->
            showUsernameError(validationError)
        }

        // Display name validation observer
        registerViewModel.displayNameError.observe(this) { validationError ->
            showDisplayNameError(validationError)
        }

        // Password validation observer
        registerViewModel.passwordError.observe(this) { validationError ->
            showPasswordError(validationError)
        }

        // Confirm password validation observer
        registerViewModel.confirmPasswordError.observe(this) { validationError ->
            showConfirmPasswordError(validationError)
        }

        // Form validity observer
        registerViewModel.isFormValid.observe(this) { isValid ->
            registerButton.isEnabled = isValid && !registerViewModel.isLoading()
        }

        // Username availability observer
        registerViewModel.usernameAvailable.observe(this) { isAvailable ->
            when (isAvailable) {
                true -> {
                    usernameInputLayout.helperText = "✓ Username is available"
                    usernameInputLayout.setHelperTextColor(getColorStateList(R.color.success_color))
                }
                false -> {
                    usernameInputLayout.helperText = "✗ Username is already taken"
                    usernameInputLayout.setHelperTextColor(getColorStateList(R.color.error_color))
                }
                null -> {
                    usernameInputLayout.helperText = "Choose a unique username"
                    usernameInputLayout.setHelperTextColor(getColorStateList(R.color.text_hint))
                }
            }
        }

        // Username check loading observer
        registerViewModel.usernameCheckLoading.observe(this) { isLoading ->
            if (isLoading) {
                usernameInputLayout.helperText = "Checking availability..."
                usernameInputLayout.setHelperTextColor(getColorStateList(R.color.text_hint))
            }
        }
    }

    /**
     * Setup click listeners
     */
    private fun setupClickListeners() {
        // Register button click
        registerButton.setOnClickListener {
            if (!registerViewModel.isLoading()) {
                val email = ValidationUtils.cleanInput(emailEditText.text.toString())
                val username = ValidationUtils.cleanInput(usernameEditText.text.toString())
                val displayName = ValidationUtils.cleanInput(displayNameEditText.text.toString())
                val password = passwordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                registerViewModel.register(email, username, displayName, password, confirmPassword)
            }
        }

        // Already have account click
        alreadyHaveAccountText.setOnClickListener {
            navigateToLogin()
        }
    }

    /**
     * Setup text watchers for real-time validation
     */
    private fun setupTextWatchers() {
        // Email text watcher
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = ValidationUtils.cleanInput(s.toString())
                val validationError = ValidationUtils.validateEmail(email)
                registerViewModel.setEmailValidation(validationError)
                updateFormValues()

                // Real-time UI feedback
                if (email.isNotBlank()) {
                    showEmailError(validationError)
                } else {
                    clearEmailError()
                }
            }
        })

        // Username text watcher
        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val username = ValidationUtils.cleanInput(s.toString())
                val validationError = ValidationUtils.validateUsername(username)
                registerViewModel.setUsernameValidation(validationError)
                updateFormValues()

                // Real-time UI feedback
                if (username.isNotBlank()) {
                    showUsernameError(validationError)
                    if (validationError == ValidationError.NONE) {
                        // Check availability
                        registerViewModel.checkUsernameAvailability(username)
                    }
                } else {
                    clearUsernameError()
                }
            }
        })

        // Display name text watcher
        displayNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val displayName = ValidationUtils.cleanInput(s.toString())
                val validationError = ValidationUtils.validateDisplayName(displayName)
                registerViewModel.setDisplayNameValidation(validationError)
                updateFormValues()

                // Real-time UI feedback
                if (displayName.isNotBlank()) {
                    showDisplayNameError(validationError)
                } else {
                    clearDisplayNameError()
                }
            }
        })

        // Password text watcher
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                val validationError = ValidationUtils.validatePassword(password)
                registerViewModel.setPasswordValidation(validationError)
                updateFormValues()

                // Real-time UI feedback
                if (password.isNotBlank()) {
                    showPasswordError(validationError)
                } else {
                    clearPasswordError()
                }

                // Also revalidate confirm password
                val confirmPassword = confirmPasswordEditText.text.toString()
                if (confirmPassword.isNotBlank()) {
                    val confirmValidationError = ValidationUtils.validateConfirmPassword(password, confirmPassword)
                    registerViewModel.setConfirmPasswordValidation(confirmValidationError)
                    showConfirmPasswordError(confirmValidationError)
                }
            }
        })

        // Confirm password text watcher
        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString()
                val password = passwordEditText.text.toString()
                val validationError = ValidationUtils.validateConfirmPassword(password, confirmPassword)
                registerViewModel.setConfirmPasswordValidation(validationError)
                updateFormValues()

                // Real-time UI feedback
                if (confirmPassword.isNotBlank()) {
                    showConfirmPasswordError(validationError)
                } else {
                    clearConfirmPasswordError()
                }
            }
        })
    }

    /**
     * Update form values in ViewModel
     */
    private fun updateFormValues() {
        registerViewModel.updateFormValues(
            email = emailEditText.text.toString(),
            username = usernameEditText.text.toString(),
            displayName = displayNameEditText.text.toString(),
            password = passwordEditText.text.toString(),
            confirmPassword = confirmPasswordEditText.text.toString()
        )
    }

    /**
     * Show loading state
     */
    private fun showLoading(show: Boolean) {
        if (show) {
            loadingProgress.visibility = View.VISIBLE
            registerButton.isEnabled = false
            registerButton.text = "Creating account..."
        } else {
            loadingProgress.visibility = View.GONE
            registerButton.text = getString(R.string.register)
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

    // Validation error display methods
    private fun showEmailError(validationError: ValidationError) {
        if (validationError != ValidationError.NONE) {
            emailInputLayout.error = validationError.message
            emailInputLayout.isErrorEnabled = true
        } else {
            clearEmailError()
        }
    }

    private fun clearEmailError() {
        emailInputLayout.isErrorEnabled = false
        emailInputLayout.error = null
    }

    private fun showUsernameError(validationError: ValidationError) {
        if (validationError != ValidationError.NONE) {
            usernameInputLayout.error = validationError.message
            usernameInputLayout.isErrorEnabled = true
        } else {
            clearUsernameError()
        }
    }

    private fun clearUsernameError() {
        usernameInputLayout.isErrorEnabled = false
        usernameInputLayout.error = null
    }

    private fun showDisplayNameError(validationError: ValidationError) {
        if (validationError != ValidationError.NONE) {
            displayNameInputLayout.error = validationError.message
            displayNameInputLayout.isErrorEnabled = true
        } else {
            clearDisplayNameError()
        }
    }

    private fun clearDisplayNameError() {
        displayNameInputLayout.isErrorEnabled = false
        displayNameInputLayout.error = null
    }

    private fun showPasswordError(validationError: ValidationError) {
        if (validationError != ValidationError.NONE) {
            passwordInputLayout.error = validationError.message
            passwordInputLayout.isErrorEnabled = true
        } else {
            clearPasswordError()
        }
    }

    private fun clearPasswordError() {
        passwordInputLayout.isErrorEnabled = false
        passwordInputLayout.error = null
    }

    private fun showConfirmPasswordError(validationError: ValidationError) {
        if (validationError != ValidationError.NONE) {
            confirmPasswordInputLayout.error = validationError.message
            confirmPasswordInputLayout.isErrorEnabled = true
        } else {
            clearConfirmPasswordError()
        }
    }

    private fun clearConfirmPasswordError() {
        confirmPasswordInputLayout.isErrorEnabled = false
        confirmPasswordInputLayout.error = null
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
     * Navigate to login screen
     */
    private fun navigateToLogin() {
        finish() // Just finish this activity to go back to login
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear any pending operations
        registerViewModel.clearError()
        registerViewModel.clearSuccess()
    }
}