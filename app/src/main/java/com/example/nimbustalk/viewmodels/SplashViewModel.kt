package com.example.nimbustalk.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.nimbustalk.utils.SharedPrefsHelper
import com.example.nimbustalk.utils.NetworkUtils
import com.example.nimbustalk.enums.LoadingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = SharedPrefsHelper(application.applicationContext)
    private val networkUtils = NetworkUtils(application.applicationContext)

    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> = _authState

    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _networkStatus = MutableLiveData<String>()
    val networkStatus: LiveData<String> = _networkStatus

    private var authCheckJob: Job? = null

    init {
        _loadingState.value = LoadingState.IDLE
    }

    fun checkAuthenticationStatus() {
        authCheckJob = viewModelScope.launch {
            try {
                _loadingState.value = LoadingState.LOADING
                _errorMessage.value = "" // Clear previous errors

                // Check network status
                val networkType = networkUtils.getNetworkType()
                _networkStatus.value = networkType

                // Add minimum loading time for better UX
                delay(1000)

                // Check if user is logged in
                val isLoggedIn = sharedPrefs.isUserLoggedIn()
                val hasValidTokens = !sharedPrefs.getAccessToken().isNullOrBlank()
                val userId = sharedPrefs.getUserId()

                when {
                    !isLoggedIn -> {
                        // User never logged in
                        _loadingState.value = LoadingState.SUCCESS
                        _authState.value = false
                    }

                    !hasValidTokens || userId.isNullOrBlank() -> {
                        // User was logged in but tokens are missing
                        sharedPrefs.clearAuthData()
                        _loadingState.value = LoadingState.SUCCESS
                        _authState.value = false
                    }

                    !networkUtils.isNetworkAvailable() -> {
                        // No network but user has valid local data
                        _loadingState.value = LoadingState.SUCCESS
                        _authState.value = true
                    }

                    else -> {
                        // Validate tokens with server
                        validateTokenWithServer()
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "Initialization error: ${e.localizedMessage}"
                _loadingState.value = LoadingState.ERROR

                // Still allow app to continue after error
                delay(1000)
                _authState.value = false
            }
        }
    }

    private suspend fun validateTokenWithServer() {
        try {
            // Simulate server validation
            delay(800)

            val accessToken = sharedPrefs.getAccessToken()
            val refreshToken = sharedPrefs.getRefreshToken()

            if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                // In a real app, make API call to validate token
                // For now, assume token is valid
                _loadingState.value = LoadingState.SUCCESS
                _authState.value = true
            } else {
                // Invalid tokens
                sharedPrefs.clearAuthData()
                _loadingState.value = LoadingState.SUCCESS
                _authState.value = false
            }

        } catch (e: Exception) {
            // Server validation failed - still allow offline access
            _loadingState.value = LoadingState.SUCCESS
            _authState.value = true
        }
    }

    fun cleanup() {
        authCheckJob?.cancel()
    }

    // Function for other parts of app to trigger logout
    fun logout() {
        viewModelScope.launch {
            sharedPrefs.clearAuthData()
            _authState.value = false
        }
    }

    // Function to refresh auth state after login
    fun refreshAuthState() {
        checkAuthenticationStatus()
    }
}