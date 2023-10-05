package com.example.kartar.controller.viewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.kartar.controller.ProfileViewModel

class ProfileViewModelFactory(private val context: Context, private val navController: NavController): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(context, navController = navController) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}