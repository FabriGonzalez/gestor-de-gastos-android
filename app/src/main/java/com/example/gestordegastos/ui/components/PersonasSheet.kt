package com.example.gestordegastos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestordegastos.viewmodel.GastoViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.gestordegastos.domain.model.Persona

@Composable
fun PersonasSheet(
    viewModel: GastoViewModel,
    personas: List<Persona>,
    onAgregarPersona: () -> Unit
) {

    var personaEditandoId by remember { mutableStateOf<String?>(null) }
    var nombreEditando by remember { mutableStateOf("") }

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {

        Text(
            "Personas del grupo",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        personas.forEach { persona ->

            val editando = personaEditandoId == persona.id

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (editando) {

                    OutlinedTextField(
                        value = nombreEditando,
                        onValueChange = { nombreEditando = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                } else {

                    Text(
                        persona.nombre,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (editando) {

                    IconButton(
                        onClick = {
                            viewModel.editarPersona(
                                persona.id,
                                nombreEditando.trim()
                            )
                            personaEditandoId = null
                        }
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Guardar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { personaEditandoId = null }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancelar"
                        )
                    }

                } else {

                    IconButton(
                        onClick = {
                            personaEditandoId = persona.id
                            nombreEditando = persona.nombre
                        }
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar"
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.eliminarPersona(persona.id)
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onAgregarPersona,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar persona")
        }
    }
}