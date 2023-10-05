package com.example.kartar.controller.viewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kartar.controller.RoomListViewModel

class RoomListViewModelFactory (private val context: Context): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomListViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}