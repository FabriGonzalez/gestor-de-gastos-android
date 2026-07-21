package com.example.gestordegastos.viewmodel

import Grupo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestordegastos.data.repository.HistorialRepositoryFirestore
import com.example.gestordegastos.domain.model.Historial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HistorialUiEvent {
    data class Error(val message: String) : HistorialUiEvent()
}

class HistorialViewModel(
    grupo: Grupo,
    private val historialRepository: HistorialRepositoryFirestore
) : ViewModel() {

    private val _uiEvent = MutableStateFlow<HistorialUiEvent?>(null)
    val uiEvent: StateFlow<HistorialUiEvent?> = _uiEvent.asStateFlow()

    private val _historial = MutableStateFlow<List<Historial>>(emptyList())
    val historial: StateFlow<List<Historial>> = _historial.asStateFlow()

    private val grupoFirestoreId = grupo.firestoreId

    init {
        viewModelScope.launch {
            try {
                historialRepository
                    .obtenerHistorialDelGrupo(grupoFirestoreId)
                    .collect {
                        _historial.value = it
                    }
            } catch (e: Exception) {
                _uiEvent.value = HistorialUiEvent.Error(
                    e.message ?: "Error al obtener el historial"
                )
            }
        }
    }

    fun limpiarUiEvent() {
        _uiEvent.value = null
    }
}