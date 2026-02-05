package com.example.gestordegastos.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarPersona(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var nombre by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cs.surface,
        titleContentColor = cs.onSurface,
        textContentColor = cs.onSurfaceVariant,
        title = { Text("Agregar persona") },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { input ->
                    nombre = input.replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                    }
                },
                placeholder = { Text("Nombre") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.outline,
                    cursorColor = cs.primary
                )
            )
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank(),
                onClick = { onConfirm(nombre.trim()) }
            ) {
                Text(
                    "Agregar",
                    color = cs.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancelar",
                    color = cs.onSurfaceVariant
                )
            }
        }
    )
}
