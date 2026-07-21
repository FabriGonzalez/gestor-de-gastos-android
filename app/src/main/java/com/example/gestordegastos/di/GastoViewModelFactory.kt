package com.example.gestordegastos.di


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestordegastos.data.repository.GastoRepositoryFirestore
import com.example.gestordegastos.data.repository.HistorialRepositoryFirestore
import com.example.gestordegastos.data.repository.PersonaRepositoryFirestore
import Grupo
import com.example.gestordegastos.viewmodel.GastoViewModel

class GastoViewModelFactory(
    private val grupo : Grupo,
    private val gastoRepository: GastoRepositoryFirestore,
    private val personaRepository: PersonaRepositoryFirestore,
    private val historialRepository: HistorialRepositoryFirestore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GastoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GastoViewModel(grupo, gastoRepository, personaRepository, historialRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
