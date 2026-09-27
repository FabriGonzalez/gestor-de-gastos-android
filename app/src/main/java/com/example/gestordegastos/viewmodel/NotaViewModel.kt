package com.example.gestordegastos.viewmodel

import Grupo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestordegastos.data.repository.NotaRepositoryFirestore
import com.example.gestordegastos.domain.model.Nota
import com.example.gestordegastos.domain.model.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

sealed class NotasUiEvent {
    data class Error(val message: String) : NotasUiEvent()
    data class Success(val message: String) : NotasUiEvent()
}

class NotaViewModel(
    private val grupo: Grupo,
    private val notaRepository: NotaRepositoryFirestore
) : ViewModel() {

    private val _uiEvent = MutableStateFlow<NotasUiEvent?>(null)
    val uiEvent: StateFlow<NotasUiEvent?> = _uiEvent.asStateFlow()

    private val _notas = MutableStateFlow<List<Nota>>(emptyList())
    val notas: StateFlow<List<Nota>> = _notas.asStateFlow()

    private val grupoFirestoreId = grupo.firestoreId

    val codigoGrupo = grupo.codigoGrupo

    // NUEVO: estado para el pull-to-refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            // NUEVO: primero traemos una foto fresca del servidor,
            // así evitamos arrancar mostrando datos viejos de la cache local
            try {
                refrescarSync()
            } catch (e: Exception) {
                _uiEvent.value = NotasUiEvent.Error("No se pudieron cargar las notas más recientes")
            }

            // Recién ahora nos suscribimos al listener en tiempo real
            notaRepository
                .obtenerNotasDelGrupo(grupoFirestoreId)
                .collect { _notas.value = it }
        }
    }

    // NUEVO: para el pull-to-refresh de la pantalla de notas
    fun refrescar() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                refrescarSync()
            } catch (e: Exception) {
                _uiEvent.value = NotasUiEvent.Error("No se pudo actualizar. Revisá tu conexión.")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // NUEVO: lectura forzada al servidor, sin depender de la cache local
    private suspend fun refrescarSync() {
        val notasFrescas = notaRepository.refrescarNotasDesdeServidor(grupoFirestoreId)
        _notas.value = notasFrescas
    }

    fun agregarNota(titulo: String, contenido: String) {
        if (titulo.isBlank()) {
            _uiEvent.value = NotasUiEvent.Error("El título no puede estar vacío")
            return
        }

        if (contenido.isBlank()) {
            _uiEvent.value = NotasUiEvent.Error("El contenido no puede estar vacío")
            return
        }

        viewModelScope.launch {
            try {
                val nota = Nota(
                    titulo = titulo,
                    contenido = contenido,
                    grupoId = grupoFirestoreId,
                    fechaCreacion = Date()
                )
                notaRepository.insertarNota(nota)
                _uiEvent.value = NotasUiEvent.Success("Nota agregada correctamente")
            } catch (e: Exception) {
                _uiEvent.value = NotasUiEvent.Error("Error al agregar la nota: ${e.message}")
            }
        }
    }

    fun eliminarNota(notaId: String) {
        viewModelScope.launch {
            try {
                notaRepository.eliminarNota(grupoFirestoreId, notaId)
                _uiEvent.value = NotasUiEvent.Success("Nota eliminada correctamente")
            } catch (e: Exception) {
                _uiEvent.value = NotasUiEvent.Error("Error al eliminar la nota: ${e.message}")
            }
        }
    }

    fun actualizarNota(notaId: String, titulo: String, contenido: String) {
        if (titulo.isBlank()) {
            _uiEvent.value = NotasUiEvent.Error("El título no puede estar vacío")
            return
        }

        if (contenido.isBlank()) {
            _uiEvent.value = NotasUiEvent.Error("El contenido no puede estar vacío")
            return
        }

        viewModelScope.launch {
            try {
                val notaActual = _notas.value.find { it.firestoreId == notaId }
                if (notaActual != null) {
                    val notaActualizada = notaActual.copy(
                        titulo = titulo,
                        contenido = contenido
                    )
                    notaRepository.actualizarNota(notaActualizada)
                    _uiEvent.value = NotasUiEvent.Success("Nota actualizada correctamente")
                }
            } catch (e: Exception) {
                _uiEvent.value = NotasUiEvent.Error("Error al actualizar la nota: ${e.message}")
            }
        }
    }

    fun limpiarUiEvent() {
        _uiEvent.value = null
    }
}