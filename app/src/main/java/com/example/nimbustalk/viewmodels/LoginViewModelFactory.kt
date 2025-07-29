package com.example.nimbustalk.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nimbustalk.api.AuthApi
import com.example.nimbustalk.utils.SharedPrefsHelper

class LoginViewModelFactory(
    private val authApi: AuthApi,
    private val sharedPrefsHelper: SharedPrefsHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authApi, sharedPrefsHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}