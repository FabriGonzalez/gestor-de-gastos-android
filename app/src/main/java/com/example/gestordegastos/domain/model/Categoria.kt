package com.example.gestordegastos.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Categoria(
    val icono: ImageVector,
    val color: Color
) {
    SUPERMERCADO(
        icono = Icons.Outlined.ShoppingCart,
        color = Color(0xFF2ED573)
    ),
    COMIDA(
        icono = Icons.Outlined.Restaurant,
        color = Color(0xFFFFC312)
    ),
    KIOSCO(
        icono = Icons.Outlined.Store,
        color = Color(0xFFFF9F1A)
    ),
    AGUA(
        icono = Icons.Outlined.WaterDrop,
        color = Color(0xFF1E90FF)
    ),
    CARNICERIA(
        icono = Icons.Outlined.SetMeal,
        color = Color(0xFFFF4757)
    ),
    VERDULERIA(
        icono = Icons.Outlined.Spa,
        color = Color(0xFF20BF6B)
    ),
    PANADERIA(
        icono = Icons.Outlined.BakeryDining,
        color = Color(0xFFFFA502)
    ),
    TRANSPORTE(
        icono = Icons.Outlined.DirectionsCar,
        color = Color(0xFF3742FA)
    ),
    VESTIMENTA(
        icono = Icons.Outlined.Checkroom,
        color = Color(0xFF9B59B6)
    ),
    GAS(
        icono = Icons.Outlined.PropaneTank,
        color = Color(0xFFFF5722)
    ),
    LUZ(
        icono = Icons.Outlined.ElectricBolt,
        color = Color(0xFFFFC107)
    ),
    OTROS(
        icono = Icons.Outlined.ReceiptLong,
        color = Color(0xFFE0E2E3)
    )
}

