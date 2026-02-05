package com.example.gestordegastos.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

private const val CENTAVOS_POR_UNIDAD = 100

fun parseMonedaToCentavos(input: String): Long? {
    val limpio = input.trim().replace("$", "")
    if (limpio.isBlank()) return null

    val normalizado = if (limpio.contains(',')) {
        val sinMiles = limpio.replace(".", "")
        sinMiles.replace(',', '.')
    } else {
        limpio.replace(",", "")
    }

    val valor = normalizado.toBigDecimalOrNull() ?: return null
    return valor
        .multiply(BigDecimal(CENTAVOS_POR_UNIDAD))
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact()
}

fun formatCentavos(
    centavos: Long,
    locale: Locale = Locale.getDefault(),
    withDecimals: Boolean = true
): String {
    val formato = NumberFormat.getNumberInstance(locale)
    formato.minimumFractionDigits = if (withDecimals) 2 else 0
    formato.maximumFractionDigits = if (withDecimals) 2 else 0
    return formato.format(centavos.toDouble() / CENTAVOS_POR_UNIDAD)
}

