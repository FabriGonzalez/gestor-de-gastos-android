package com.example.gestordegastos.di

import Grupo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestordegastos.data.repository.HistorialRepositoryFirestore
import com.example.gestordegastos.viewmodel.HistorialViewModel

class HistorialViewModelFactory(
    private val grupo: Grupo,
    private val historialRepository: HistorialRepositoryFirestore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(HistorialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistorialViewModel(
                grupo,
                historialRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}