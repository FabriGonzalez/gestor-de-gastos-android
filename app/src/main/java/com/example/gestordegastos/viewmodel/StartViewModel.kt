package com.example.gestordegastos.viewmodel

import Grupo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestordegastos.data.datastore.GroupPreferences
import com.example.gestordegastos.data.repository.GrupoRepositoryFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class StartViewModel(
    private val groupPreferences: GroupPreferences,
    private val grupoRepository: GrupoRepositoryFirestore
) : ViewModel() {

    private val _grupo = MutableStateFlow<Grupo?>(null)
    val grupo: StateFlow<Grupo?> = _grupo

    private val _inicializado = MutableStateFlow(false)
    val inicializado: StateFlow<Boolean> = _inicializado

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        viewModelScope.launch {
            groupPreferences.grupoFlow.collect { cache ->
                if (cache != null) {
                    _grupo.value = Grupo(
                        firestoreId = cache.firestoreId,
                        codigoGrupo = cache.codigoGrupo
                    )

                    sincronizarGrupo(cache.codigoGrupo)
                }
                _inicializado.value = true
            }
        }
    }

    private fun sincronizarGrupo(codigo: String) {
        viewModelScope.launch {
            try {
                val remoto = grupoRepository.obtenerGrupoPorCodigo(codigo)
                if (remoto != null) {
                    _grupo.value = remoto
                    groupPreferences.guardarGrupo(remoto)
                }
            } catch (_: Exception) { }
        }
    }

    fun crearGrupo() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val codigo = generarGrupoId()
                val grupo = grupoRepository.crearGrupo(codigo)
                groupPreferences.guardarGrupo(grupo)
                _grupo.value = grupo
            } catch (e: Exception) {
                _error.value = "Error al crear el grupo"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun unirseAGrupo(codigo: String) {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                val grupo = grupoRepository.unirseAGrupo(codigo)

                if (grupo == null) {
                    _error.value = "El grupo no existe"
                    return@launch
                }

                groupPreferences.guardarGrupo(grupo)
                _grupo.value = grupo

            } catch (e: Exception) {
                _error.value = "No se pudo unir al grupo"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun salirDelGrupo() {
        viewModelScope.launch {
            groupPreferences.clearGrupo()
            _grupo.value = null
        }
    }

    private fun generarGrupoId(): String =
        UUID.randomUUID().toString().substring(0, 6).uppercase()
}
