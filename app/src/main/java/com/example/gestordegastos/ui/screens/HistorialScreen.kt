package com.example.gestordegastos.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestordegastos.domain.model.Historial
import com.example.gestordegastos.utils.formatCentavos
import com.example.gestordegastos.utils.formatearFechaRelativa
import com.example.gestordegastos.viewmodel.HistorialViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    onVolver: () -> Unit,
    onVerDetalle: (Historial) -> Unit,
    modifier: Modifier = Modifier
){

    val historial by viewModel.historial.collectAsState()


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }

    ) { padding ->


        if (historial.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    "No hay liquidaciones",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    "Cuando liquides el grupo aparecerá aquí",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


        } else {


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)

            ) {

                items(historial) { item ->


                    HistorialItem(
                        historial = item,
                        onClick = {
                            onVerDetalle(item)
                        }
                    )

                }
            }
        }
    }
}


@Composable
fun HistorialItem(
    historial: Historial,
    onClick: () -> Unit
) {

    Surface(

        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(12.dp)
            ),

        color = MaterialTheme.colorScheme.surfaceVariant,

        onClick = onClick

    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {


            Text(
                text = "Liquidación",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )


            Spacer(
                modifier = Modifier.height(6.dp)
            )


            Text(
                text = formatearFechaRelativa(
                    historial.fechaLiquidacion
                ),

                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            Text(
                text = "Gastos: ${historial.gastos.size}"
            )


            Text(
                text = "Total: $${formatCentavos(historial.totalCentavos)}",
                fontWeight = FontWeight.Bold
            )

        }
    }
}