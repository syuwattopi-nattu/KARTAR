package com.example.kartar.controller.viewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kartar.controller.CreateViewModel
import com.example.kartar.controller.KartaSearchViewModel

class KartaSearchViewModelFactory (private val createViewModel: CreateViewModel): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KartaSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KartaSearchViewModel(createViewModel = createViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}