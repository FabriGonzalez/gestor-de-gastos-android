package com.example.gestordegastos.domain.model

import java.util.Date

data class Gasto(
    var firestoreId: String = "",
    val descripcion: String? = null,
    val montoCentavos: Long = 0L,
    val categoria: Categoria = Categoria.OTROS,
    val paganteId: String = "",
    val grupoId: String = "",
    val fecha: Date = Date(),
    val porcentaje: Double = 0.5,
    val deudoresIds: List<String> = emptyList()
)
