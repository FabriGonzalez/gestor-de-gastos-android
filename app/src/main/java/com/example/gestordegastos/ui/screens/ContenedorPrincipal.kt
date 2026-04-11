package com.example.gestordegastos.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.gestordegastos.viewmodel.GastoViewModel
import com.example.gestordegastos.viewmodel.NotaViewModel
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import com.example.gestordegastos.ui.components.DialogAgregarPersona
import com.example.gestordegastos.ui.components.PersonasSheet

enum class PantallaNavegacion {
    GASTOS,
    NOTAS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenedorPrincipal(
    gastoViewModel: GastoViewModel,
    notaViewModel: NotaViewModel,
    onSalirDelGrupo: () -> Unit
) {
    var pantallaActual by remember { mutableStateOf(PantallaNavegacion.GASTOS) }
    var showPersonaSheet by remember { mutableStateOf(false) }
    var showAgregarPersonaDialog by remember { mutableStateOf(false) }
    var showSalirGrupoDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Gastos") },
                    label = { Text("Gastos") },
                    selected = pantallaActual == PantallaNavegacion.GASTOS,
                    onClick = { pantallaActual = PantallaNavegacion.GASTOS }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Note, contentDescription = "Notas") },
                    label = { Text("Notas") },
                    selected = pantallaActual == PantallaNavegacion.NOTAS,
                    onClick = { pantallaActual = PantallaNavegacion.NOTAS }
                )
            }
        }
    ) { paddingValues ->

        when (pantallaActual) {
            PantallaNavegacion.GASTOS -> {
                MainScreen(
                    viewModel = gastoViewModel,
                    onSalirDelGrupo = { showSalirGrupoDialog = true },
                    onVerPersonas = { showPersonaSheet = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            PantallaNavegacion.NOTAS -> {
                val personas by gastoViewModel.personas.collectAsState()
                NotasScreen(
                    viewModel = notaViewModel,
                    codigoGrupo = gastoViewModel.codigoGrupo,
                    personas = personas.size,
                    onVerPersonas = { showPersonaSheet = true },
                    onSalirGrupo = { showSalirGrupoDialog = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
    if (showPersonaSheet) {

        val personas by gastoViewModel.personas.collectAsState()

        ModalBottomSheet(
            onDismissRequest = { showPersonaSheet = false }
        ) {

            PersonasSheet(
                viewModel = gastoViewModel,
                personas = personas,
                onAgregarPersona = { showAgregarPersonaDialog = true }
            )

        }
    }

    if (showAgregarPersonaDialog) {
        DialogAgregarPersona(
            onDismiss = { showAgregarPersonaDialog = false },
            onConfirm = { nombre ->
                gastoViewModel.agregarPersona(nombre)
                showAgregarPersonaDialog = false
            }
        )
    }

    if (showSalirGrupoDialog) {
        AlertDialog(
            onDismissRequest = { showSalirGrupoDialog = false },
            title = { Text("Salir del grupo") },
            text = { Text("¿Estás seguro de que querés salir del grupo?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSalirDelGrupo()
                        showSalirGrupoDialog = false
                    }
                ) {
                    Text("Salir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSalirGrupoDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

