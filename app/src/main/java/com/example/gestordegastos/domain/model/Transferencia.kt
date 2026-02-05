package com.example.gestordegastos.domain.model

data class Transferencia(
    val deudorId: String,
    val acreedorId: String,
    val montoCentavos: Long
)