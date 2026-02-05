package com.example.gestordegastos.viewmodel


import Grupo
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.gestordegastos.domain.model.Categoria
import com.example.gestordegastos.domain.model.Gasto
import com.example.gestordegastos.data.repository.GastoRepositoryFirestore
import com.example.gestordegastos.data.repository.PersonaRepositoryFirestore
import com.example.gestordegastos.domain.model.Persona
import com.example.gestordegastos.domain.model.Transferencia
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine


sealed class UiEvent {
    data class Error(val message: String) : UiEvent()
}


class GastoViewModel(
    private val grupo: Grupo,
    private val gastoRepository: GastoRepositoryFirestore,
    private val personaRepository: PersonaRepositoryFirestore
): ViewModel() {

    private val _uiEvent = MutableStateFlow<UiEvent?>(null)
    val uiEvent: StateFlow<UiEvent?> = _uiEvent.asStateFlow()

    private val _gastos = MutableStateFlow<List<Gasto>>(emptyList())
    val gastos: StateFlow<List<Gasto>> = _gastos.asStateFlow()

    private val grupoFirestoreId = grupo.firestoreId

    val codigoGrupo = grupo.codigoGrupo

    private val _personas = MutableStateFlow<List<Persona>>(emptyList())
    val personas: StateFlow<List<Persona>> = _personas.asStateFlow()

    private val _transferencias = MutableStateFlow<List<Transferencia>>(emptyList())
    val transferencias: StateFlow<List<Transferencia>> = _transferencias.asStateFlow()

    init {
        viewModelScope.launch {
            gastoRepository
                .obtenerGastosDelGrupo(grupoFirestoreId)
                .collect { _gastos.value = it }
        }

        viewModelScope.launch {
            personaRepository
                .obtenerPersonasDelGrupo(grupoFirestoreId)
                .collect { _personas.value = it }
        }

        viewModelScope.launch {
            combine(_gastos, _personas) { gastos, personas ->
                calcularTransferencias(gastos, personas)
            }.collect { resultado ->
                _transferencias.value = resultado
            }
        }
    }

    fun agregarGasto(
        categoria: Categoria,
        descripcion: String?,
        montoCentavos: Long,
        pagante: Persona,
        porcentaje: Double = 0.5,
        deudoresIds: List<String> = emptyList()
    ) {
        val gasto = Gasto(
            firestoreId = "",
            grupoId = grupoFirestoreId,
            categoria = categoria,
            descripcion = descripcion,
            montoCentavos = montoCentavos,
            paganteId = pagante.id,
            porcentaje = porcentaje,
            deudoresIds = deudoresIds
        )


        viewModelScope.launch {
            gastoRepository.insertarGasto(gasto)
        }
    }

    fun agregarPersona(nombre: String) {
        viewModelScope.launch {
            val nuevaPersona = Persona(
                nombre = nombre.trim(),
                grupoId = grupoFirestoreId
            )
            personaRepository.insertarPersona(nuevaPersona)
        }
    }


    fun eliminarGasto(id: String) {
        viewModelScope.launch {
            gastoRepository.eliminarGasto(grupoFirestoreId, id)
        }
    }

    suspend fun obtenerPersonaPorId(id: String): Persona? {
        return personaRepository.obtenerPersonaPorId(grupoFirestoreId, id)
    }

    fun editarGasto(
        gasto: Gasto,
        categoria: Categoria,
        descripcion: String?,
        montoCentavos: Long,
        pagante: Persona,
        porcentaje: Double,
        deudoresIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val gastoEditado = gasto.copy(
                categoria = categoria,
                descripcion = descripcion,
                montoCentavos = montoCentavos,
                paganteId = pagante.id,
                porcentaje = porcentaje,
                deudoresIds = deudoresIds
            )
            gastoRepository.actualizarGasto(gastoEditado)
        }
    }

    fun editarPersona(
        personaId: String,
        nuevoNombre: String
    ) {
        if (nuevoNombre.isBlank()) return

        viewModelScope.launch {
            val personaActual = _personas.value.firstOrNull { it.id == personaId }
                ?: return@launch

            val personaEditada = personaActual.copy(
                nombre = nuevoNombre.trim()
            )

            personaRepository.actualizarPersona(personaEditada)
        }
    }

    fun eliminarPersona(personaId: String) {
        val tieneGastos = _gastos.value.any {
            it.paganteId == personaId
        }

        if (tieneGastos) {
            _uiEvent.value = UiEvent.Error(
                "No podés borrar una persona con gastos asociados"
            )
            return
        }

        viewModelScope.launch {
            personaRepository.eliminarPersona(
                grupoId = grupoFirestoreId,
                personaId = personaId
            )
        }
    }

    private fun calcularTransferencias(
        gastos: List<Gasto>,
        personas: List<Persona>
    ): List<Transferencia> {
        if (personas.isEmpty()) return emptyList()

        val saldos = mutableMapOf<String, Long>()
        personas.forEach { saldos[it.id] = 0L }

        gastos.forEach { gasto ->
            val deudoresDelGasto = if (gasto.deudoresIds.isNotEmpty()) {
                personas.filter { it.id in gasto.deudoresIds }
            } else if (personas.size == 2 && gasto.porcentaje == 1.0) {
                personas.filter { it.id != gasto.paganteId }
            } else {
                personas
            }

            if (deudoresDelGasto.isNotEmpty()) {
                val montoPorDeudor = if (personas.size == 2) {
                    if (gasto.porcentaje == 1.0) gasto.montoCentavos else divideHalfUp(gasto.montoCentavos, 2)
                } else {
                    if (gasto.deudoresIds.isNotEmpty()) {
                        divideHalfUp(gasto.montoCentavos, deudoresDelGasto.size + 1)
                    } else {
                        divideHalfUp(gasto.montoCentavos, personas.size)
                    }

                }

                deudoresDelGasto.forEach { persona ->
                    saldos[persona.id] = saldos.getValue(persona.id) - montoPorDeudor
                }

                if (gasto.porcentaje == 1.0){
                    saldos[gasto.paganteId] = saldos.getValue(gasto.paganteId) + gasto.montoCentavos
                } else {
                    saldos[gasto.paganteId] = saldos.getValue(gasto.paganteId) + gasto.montoCentavos - montoPorDeudor
                }
            }
        }

        val acreedores = saldos.filterValues { it > 0 }.toMutableMap()
        val deudores = saldos.filterValues { it < 0 }.mapValues { -it.value }.toMutableMap()

        val transferencias = mutableListOf<Transferencia>()

        for((deudorId, deuda) in deudores){
            var deudaRestante = deuda

            for((acreedorId, credito) in acreedores){
                if(deudaRestante <= 0L) break
                if(credito <= 0L) continue

                val monto = minOf(deudaRestante, credito)

                transferencias += Transferencia(
                    deudorId = deudorId,
                    acreedorId = acreedorId,
                    montoCentavos = monto
                )

                deudaRestante -= monto

                acreedores[acreedorId] = credito - monto            }
        }

        return transferencias
    }


    fun calcularDeudaPorGasto(
        gasto: Gasto,
        personas: List<Persona>
    ): List<Transferencia> {

        if (personas.size <= 1) return emptyList()

        val deudores = if (gasto.deudoresIds.isNotEmpty()) {
            personas.filter { it.id in gasto.deudoresIds }
        } else {
            personas
        }

        if (deudores.isEmpty()) return emptyList()

        val montoAPagar = if (personas.size == 2) {
            if (gasto.porcentaje == 1.0) gasto.montoCentavos else divideHalfUp(gasto.montoCentavos, 2)
        } else {
            if (gasto.deudoresIds.isNotEmpty()) {
                divideHalfUp(gasto.montoCentavos, deudores.size + 1)
            } else {
                divideHalfUp(gasto.montoCentavos, personas.size)
            }
        }

        return deudores
            .filter { it.id != gasto.paganteId }
            .map { persona ->
                Transferencia(
                    deudorId = persona.id,
                    acreedorId = gasto.paganteId,
                    montoCentavos = montoAPagar
                )
            }
    }

    private fun divideHalfUp(montoCentavos: Long, divisor: Int): Long {
        if (divisor <= 0) return 0L
        return (montoCentavos + (divisor / 2)) / divisor
    }

    fun pagarTodosLosGastos() {
        viewModelScope.launch {
            gastos.value.forEach { gasto ->
                eliminarGasto(gasto.firestoreId)
            }
        }
    }

    fun limpiarUiEvent() {
        _uiEvent.value = null
    }

}