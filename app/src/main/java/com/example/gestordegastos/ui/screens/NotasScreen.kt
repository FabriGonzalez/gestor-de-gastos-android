package com.example.gestordegastos.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gestordegastos.domain.model.Nota
import com.example.gestordegastos.ui.components.AppTopBar
import com.example.gestordegastos.ui.components.DividerConPunto
import com.example.gestordegastos.utils.formatearFechaRelativa
import com.example.gestordegastos.viewmodel.NotaViewModel
import com.example.gestordegastos.viewmodel.NotasUiEvent
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(
    viewModel: NotaViewModel,
    codigoGrupo: String,
    onVerPersonas: () -> Unit,
    onSalirGrupo: () -> Unit,
    modifier: Modifier,
    personas: Int
){
    val notas by viewModel.notas.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState()
    var showDialogAgregarNota by remember { mutableStateOf(false) }
    var notaAEditar by remember { mutableStateOf<Nota?>(null) }
    var showDialogEliminarNota by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiEvent) {
        when (uiEvent) {
            is NotasUiEvent.Error -> {
                snackbarHostState.showSnackbar(
                    message = (uiEvent as NotasUiEvent.Error).message
                )
                viewModel.limpiarUiEvent()
            }
            is NotasUiEvent.Success -> {
                snackbarHostState.showSnackbar(
                    message = (uiEvent as NotasUiEvent.Success).message
                )
                viewModel.limpiarUiEvent()
            }
            null -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize()){
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,

            topBar = {
                AppTopBar(
                    titulo = "Notas",
                    personasCount = personas,
                    codigoGrupo = codigoGrupo,
                    onVerPersonas = onVerPersonas,
                    onSalirGrupo = onSalirGrupo
                )
            },

            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        notaAEditar = null
                        showDialogAgregarNota = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar nota",
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            }
        ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(Modifier.height(16.dp))
            DividerConPunto()
            Spacer(Modifier.height(16.dp))
            if (notas.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No hay notas aún",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Crea una nota para el grupo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notas) { nota ->
                        NotaItem(
                            nota = nota,
                            onEdit = {
                                notaAEditar = nota
                                showDialogAgregarNota = true
                            },
                            onDelete = {
                                notaAEditar = nota
                                showDialogEliminarNota = true
                            }
                        )
                    }
                }
            }
        }
    }

        if (showDialogAgregarNota) {
            DialogAgregarEditarNota(
                nota = notaAEditar,
                onDismiss = {
                    showDialogAgregarNota = false
                    notaAEditar = null
                },
                onGuardar = { titulo, contenido ->
                    if (notaAEditar != null) {
                        viewModel.actualizarNota(notaAEditar!!.firestoreId, titulo, contenido)
                    } else {
                        viewModel.agregarNota(titulo, contenido)
                    }
                    showDialogAgregarNota = false
                    notaAEditar = null
                }
            )
        }

        if (showDialogEliminarNota && notaAEditar != null) {
            AlertDialog(
                onDismissRequest = { showDialogEliminarNota = false },
                title = { Text("Eliminar nota") },
                text = { Text("¿Estás seguro de que deseas eliminar esta nota? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.eliminarNota(notaAEditar!!.firestoreId)
                            showDialogEliminarNota = false
                            notaAEditar = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialogEliminarNota = false }
                    ) {
                        Text("Cancelar")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotaItem(
    nota: Nota,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onEdit,
                onLongClick = onDelete
            ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Nota",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = nota.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp
                    )
                }
                Text(
                    text = formatearFechaRelativa(nota.fechaCreacion),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val richTextState = rememberRichTextState()

            LaunchedEffect(nota.contenido) {
                richTextState.config.unorderedListIndent = 8
                richTextState.config.orderedListIndent = 8
                richTextState.setHtml(nota.contenido)
            }

            RichText(
                state = richTextState,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarEditarNota(
    nota: Nota?,
    onDismiss: () -> Unit,
    onGuardar: (String, String) -> Unit
) {
    val richTextState = rememberRichTextState()
    var titulo by remember { mutableStateOf(nota?.titulo ?: "") }

    LaunchedEffect(nota) {
        richTextState.config.unorderedListIndent = 8
        richTextState.config.orderedListIndent = 8
        richTextState.setHtml(nota?.contenido ?: "")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (nota != null) "Editar nota" else "Agregar nota",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 250.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    RichTextEditor(
                        state = richTextState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 48.dp)
                            .padding(8.dp),
                        colors = RichTextEditorDefaults.richTextEditorColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                richTextState.toggleSpanStyle(
                                    androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)
                                )
                            }) {
                                Icon(
                                    Icons.Default.FormatBold,
                                    contentDescription = "Negrita",
                                    tint = if (richTextState.currentSpanStyle.fontWeight == FontWeight.Bold)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = {
                                richTextState.toggleSpanStyle(
                                    androidx.compose.ui.text.SpanStyle(
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                )
                            }) {
                                Icon(
                                    Icons.Default.FormatItalic,
                                    contentDescription = "Cursiva",
                                    tint = if (richTextState.currentSpanStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = {
                                richTextState.toggleSpanStyle(
                                    androidx.compose.ui.text.SpanStyle(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                    )
                                )
                            }) {
                                Icon(
                                    Icons.Default.FormatUnderlined,
                                    contentDescription = "Subrayado",
                                    tint = if (richTextState.currentSpanStyle.textDecoration?.contains(
                                            androidx.compose.ui.text.style.TextDecoration.Underline) == true)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Divider(
                                modifier = Modifier.height(24.dp).width(1.dp),
                                color = MaterialTheme.colorScheme.outline
                            )

                            IconButton(onClick = { richTextState.toggleUnorderedList() }) {
                                Icon(
                                    Icons.Default.FormatListBulleted,
                                    contentDescription = "Lista",
                                    tint = if (richTextState.isUnorderedList)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onGuardar(titulo, richTextState.toHtml()) },
                        enabled = titulo.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}



