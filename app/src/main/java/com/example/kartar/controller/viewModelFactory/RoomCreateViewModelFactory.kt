package com.example.kartar.controller.viewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kartar.controller.RoomCreateViewModel

class RoomCreateViewModelFactory (private val context: Context): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomCreateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomCreateViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}