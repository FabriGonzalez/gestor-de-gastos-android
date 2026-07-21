package com.example.gestordegastos.domain.model

import java.util.Date

data class Historial(
    var firestoreId: String = "",
    val grupoId: String = "",

    val fechaLiquidacion: Date = Date(),

    val totalCentavos: Long = 0L,

    val gastos: List<Gasto> = emptyList(),

    val personas: List<Persona> = emptyList(),

    val transferencias: List<Transferencia> = emptyList()
)