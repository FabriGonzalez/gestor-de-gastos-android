package com.example.gestordegastos.domain.model

import java.util.Date

data class Nota(
    var firestoreId: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val grupoId: String = "",
    val fechaCreacion: Date = Date()
)

