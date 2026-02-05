package com.example.gestordegastos.utils

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

fun formatearFechaRelativa(fecha: Date): String {
    val zona = ZoneId.systemDefault()

    val fechaGasto = fecha.toInstant()
        .atZone(zona)
        .toLocalDate()

    val hoy = LocalDate.now(zona)

    return when {
        fechaGasto.isEqual(hoy) -> "Hoy"
        fechaGasto.isEqual(hoy.minusDays(1)) -> "Ayer"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
            fechaGasto.format(formatter)
        }
    }
}
