package com.example.gestordegastos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestordegastos.data.datastore.GroupPreferences
import com.example.gestordegastos.data.repository.GrupoRepositoryFirestore

class StartViewModelFactory(
    private val groupPreferences: GroupPreferences,
    private val grupoRepository: GrupoRepositoryFirestore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StartViewModel(groupPreferences, grupoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
