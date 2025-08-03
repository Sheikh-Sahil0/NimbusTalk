package com.example.nimbustalk.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nimbustalk.api.AuthApi
import com.example.nimbustalk.api.UserApi
import com.example.nimbustalk.utils.SharedPrefsHelper

class RegisterViewModelFactory(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val sharedPrefsHelper: SharedPrefsHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(authApi, userApi, sharedPrefsHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}