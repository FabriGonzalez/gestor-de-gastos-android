package com.example.gestordegastos.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gestordegastos.domain.model.Gasto
import com.example.gestordegastos.domain.model.Historial
import com.example.gestordegastos.domain.model.Persona
import com.example.gestordegastos.domain.model.Transferencia
import com.example.gestordegastos.ui.components.DividerConPunto
import com.example.gestordegastos.utils.formatCentavos
import com.example.gestordegastos.utils.formatearFechaRelativa
import com.example.gestordegastos.viewmodel.HistorialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {

    val historial by viewModel.historial.collectAsState()

    Scaffold(

        modifier = modifier,

        containerColor = MaterialTheme.colorScheme.background,
        topBar = {

            Column{

                CenterAlignedTopAppBar(

                    title = {

                        Text(
                            "Historial",
                            fontWeight = FontWeight.Bold
                        )
                    },


                    navigationIcon = {

                        IconButton(
                            onClick = onVolver
                        ) {

                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver"
                            )

                        }

                    },

                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )

                )

                Spacer(Modifier.height(8.dp))

                DividerConPunto(
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(Modifier.height(20.dp))

            }
        }


    ) { padding ->

        if (historial.isEmpty()) {

            EmptyHistorial(
                Modifier.padding(padding)
            )

        } else {

            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                contentPadding = PaddingValues(16.dp),

                verticalArrangement = Arrangement.spacedBy(16.dp)

            ) {

                items(historial) { liquidacion ->

                    HistorialCard(
                        historial = liquidacion
                    )

                }

            }

        }

    }

}

@Composable
private fun EmptyHistorial(
    modifier: Modifier = Modifier
) {

    Box(

        modifier = modifier.fillMaxSize(),

        contentAlignment = Alignment.Center

    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(

                imageVector = Icons.Default.Payments,

                contentDescription = null,

                tint = MaterialTheme.colorScheme.primary

            )

            Spacer(
                Modifier.height(20.dp)
            )

            Text(

                text = "Todavía no hay\nliquidaciones",

                style = MaterialTheme.typography.headlineSmall,

                fontWeight = FontWeight.Bold

            )

            Spacer(
                Modifier.height(8.dp)
            )

            Text(

                text = "Cuando liquides el grupo aparecerán aquí.",

                color = MaterialTheme.colorScheme.onSurfaceVariant

            )

        }

    }

}

private fun obtenerNombre(
    personas: List<Persona>,
    id: String
): String {

    return personas.firstOrNull {
        it.id == id
    }?.nombre ?: "Desconocido"

}

@Composable
private fun HistorialCard(
    historial: Historial
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable {
                expanded = !expanded
            },

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .8f)
        ),



    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // HEADER

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(

                    modifier = Modifier.size(50.dp),

                    shape = RoundedCornerShape(14.dp),

                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)

                ) {

                    Icon(

                        imageVector = Icons.Default.Payments,

                        contentDescription = null,

                        tint = MaterialTheme.colorScheme.secondary,

                        modifier = Modifier.padding(11.dp)

                    )

                }

                Spacer(
                    Modifier.width(16.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(

                        "Liquidación",

                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,

                        fontWeight = FontWeight.Bold

                    )

                    Text(

                        formatearFechaRelativa(
                            historial.fechaLiquidacion
                        ),

                        style = MaterialTheme.typography.bodySmall,

                        color = MaterialTheme.colorScheme.onSurfaceVariant

                    )

                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {

                    Text(
                        "$${formatCentavos(historial.totalCentavos)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                }

            }

            Spacer(
                Modifier.height(18.dp)
            )

            Row {

                AssistChip(
                    enabled = true,
                    onClick = {},
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .12f),
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        leadingIconContentColor = MaterialTheme.colorScheme.secondary
                    ),
                    label = {
                        Text(
                            if (historial.gastos.size == 1) {
                                "1 gasto"
                            } else {
                                "${historial.gastos.size} gastos"
                            }
                        )                    }
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                AssistChip(
                    enabled = true,
                    onClick = {},
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .12f),
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        leadingIconContentColor = MaterialTheme.colorScheme.secondary
                    ),
                    label = {
                        Text(
                            if (historial.transferencias.size == 1) {
                                "1 transferencia"
                            } else {
                                "${historial.transferencias.size} gastos"
                            }
                        )
                    }
                )

            }

            if (expanded) {

                Spacer(
                    Modifier.height(20.dp)
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = .5f)
                )

                Spacer(
                    Modifier.height(18.dp)
                )

                Text(

                    "TRANSFERENCIAS",

                    style = MaterialTheme.typography.labelLarge,

                    color = MaterialTheme.colorScheme.primary,

                    fontWeight = FontWeight.Bold

                )

                Spacer(Modifier.height(12.dp))

                historial.transferencias.forEach {

                    TransferenciaItem(

                        transferencia = it,

                        personas = historial.personas

                    )

                    Spacer(
                        Modifier.height(10.dp)
                    )

                }

                Spacer(
                    Modifier.height(8.dp)
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = .5f)
                )

                Spacer(
                    Modifier.height(18.dp)
                )

                Text(

                    "GASTOS",

                    style = MaterialTheme.typography.labelLarge,

                    color = MaterialTheme.colorScheme.tertiary,

                    fontWeight = FontWeight.Bold

                )

                Spacer(
                    Modifier.height(12.dp)
                )

                historial.gastos.forEach {

                    GastoResumenItem(
                        gasto = it,
                        personas = historial.personas
                    )

                    Spacer(
                        Modifier.height(10.dp)
                    )

                }

            }

        }

    }

}

@Composable
private fun TransferenciaItem(
    transferencia: Transferencia,
    personas: List<Persona>
) {

    val deudores = transferencia.deudorId
        .split(",")
        .joinToString(", ") { id ->
            obtenerNombre(personas, id.trim())
        }

    val acreedores = transferencia.acreedorId
        .split(",")
        .joinToString(", ") { id ->
            obtenerNombre(personas, id.trim())
        }

    Surface(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(14.dp),

        color = MaterialTheme.colorScheme.surface,

        tonalElevation = 1.dp

    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            Row(

                modifier = Modifier.fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically

            ) {

                Text(

                    deudores,

                    modifier = Modifier.weight(1f),

                    textAlign = TextAlign.End,

                    fontWeight = FontWeight.SemiBold

                )

                Spacer(Modifier.width(12.dp))

                Icon(

                    imageVector = Icons.Default.ArrowForward,

                    contentDescription = null,

                    tint = MaterialTheme.colorScheme.secondary

                )

                Spacer(Modifier.width(12.dp))

                Text(

                    acreedores,

                    modifier = Modifier.weight(1f),

                    textAlign = TextAlign.Start,

                    fontWeight = FontWeight.SemiBold

                )

            }

            Spacer(Modifier.height(14.dp))

            Text(

                "$${formatCentavos(transferencia.montoCentavos)}",

                style = MaterialTheme.typography.titleLarge,

                fontWeight = FontWeight.Bold,

                color = MaterialTheme.colorScheme.primary

            )

        }

    }

}

@Composable
private fun GastoResumenItem(
    gasto: Gasto,
    personas: List<Persona>
) {

    Surface(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(14.dp),

        color = gasto.categoria.color.copy(alpha = .08f)

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            Surface(

                modifier = Modifier.size(42.dp),

                shape = RoundedCornerShape(12.dp),

                color = gasto.categoria.color.copy(alpha = .18f)

            ) {

                Icon(

                    imageVector = gasto.categoria.icono,

                    contentDescription = null,

                    tint = gasto.categoria.color,

                    modifier = Modifier.padding(9.dp)

                )

            }

            Spacer(
                Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(

                    gasto.categoria.name
                        .lowercase()
                        .replaceFirstChar { it.uppercase() },

                    fontWeight = FontWeight.Bold

                )

                Text(
                    text = "Pagó: ${obtenerNombre(personas, gasto.paganteId)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                gasto.descripcion?.takeIf {

                    it.isNotBlank()

                }?.let {

                    Text(

                        text = "Descripción: ${it}",

                        style = MaterialTheme.typography.bodySmall,

                        color = MaterialTheme.colorScheme.onSurfaceVariant

                    )

                }

            }

            Text(

                "$${formatCentavos(gasto.montoCentavos)}",

                style = MaterialTheme.typography.titleSmall,

                fontWeight = FontWeight.Bold

            )

        }

    }

}