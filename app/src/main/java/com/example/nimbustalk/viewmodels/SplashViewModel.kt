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
import android.util.Log

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

                // Check authentication status
                val isLoggedIn = sharedPrefs.isLoggedIn()
                val hasValidTokens = !sharedPrefs.getAccessToken().isNullOrBlank()
                val userId = sharedPrefs.getUserId()

                Log.d("SplashViewModel", "Auth check - isLoggedIn: $isLoggedIn, hasTokens: $hasValidTokens, userId: $userId")

                when {
                    !isLoggedIn -> {
                        // User never logged in or logged out
                        Log.d("SplashViewModel", "User not logged in - going to login")
                        _loadingState.value = LoadingState.SUCCESS
                        _authState.value = false
                    }

                    !hasValidTokens || userId.isNullOrBlank() -> {
                        // User was logged in but tokens are missing - clear data
                        Log.d("SplashViewModel", "Invalid tokens - clearing auth data")
                        sharedPrefs.clearAuthData()
                        _loadingState.value = LoadingState.SUCCESS
                        _authState.value = false
                    }

                    else -> {
                        // User has valid authentication data
                        Log.d("SplashViewModel", "User authenticated - going to home")
                        _loadingState.value = LoadingState.SUCCESS
                        _authState.value = true
                    }
                }

            } catch (e: Exception) {
                Log.e("SplashViewModel", "Auth check error", e)
                _errorMessage.value = "Initialization error: ${e.localizedMessage}"
                _loadingState.value = LoadingState.ERROR

                // Still allow app to continue after error - default to not authenticated
                delay(1000)
                _authState.value = false
            }
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