package com.example.kartar.controller.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kartar.controller.AuthViewModel
import com.example.kartar.controller.ProfileViewModel

class AuthViewModelFactory (private val profileViewModel: ProfileViewModel): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(profileViewModel = profileViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}