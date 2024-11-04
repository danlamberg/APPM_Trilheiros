package com.example.appm_trilheiros.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appm_trilheiros.repositories.ItemLocalRepository
import com.example.appm_trilheiros.repositories.ItemRemoteRepository

class ItensViewModelFactory(
    private val localRepository: ItemLocalRepository,
    private val remoteRepository: ItemRemoteRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItensViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItensViewModel(localRepository, remoteRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
