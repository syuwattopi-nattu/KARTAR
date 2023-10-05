package com.example.kartar.controller.viewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kartar.controller.CreateViewModel

class CreateViewModelFactory (private val context: Context): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateViewModel(context = context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}