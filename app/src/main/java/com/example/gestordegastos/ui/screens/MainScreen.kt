package com.example.gestordegastos.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.example.gestordegastos.domain.model.Gasto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestordegastos.viewmodel.GastoViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import java.util.Locale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Dialog
import com.example.gestordegastos.R
import com.example.gestordegastos.domain.model.Categoria
import com.example.gestordegastos.domain.model.Persona
import com.example.gestordegastos.ui.components.DialogAgregarPersona
import com.example.gestordegastos.utils.formatearFechaRelativa
import com.example.gestordegastos.utils.formatCentavos
import com.example.gestordegastos.utils.parseMonedaToCentavos
import com.example.gestordegastos.viewmodel.UiEvent
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GastoViewModel,
    onSalirDelGrupo: () -> Unit
) {
    val gastos by viewModel.gastos.collectAsState()
    val personas by viewModel.personas.collectAsState()
    var showDialogAgregarGasto by remember{ mutableStateOf(false) }
    var gastoAPagar by remember { mutableStateOf<Gasto?>(null) }
    var gastoAEliminar by remember { mutableStateOf<Gasto?>(null) }
    var showDialogPagarTodo by remember { mutableStateOf(false) }
    val transferencias by viewModel.transferencias.collectAsState()
    var showPagoAnimacion by remember {mutableStateOf(false)}
    var showDetalleDeudas by remember { mutableStateOf(false) }
    var showMenu by remember {mutableStateOf(false)}
    var showPersonaSheet by remember{mutableStateOf(false)}
    var showAgregarPersonaDialog by remember{mutableStateOf(false)}
    var showSalirGrupoDialog by remember { mutableStateOf(false) }
    val uiEvent by viewModel.uiEvent.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiEvent) {
        when (uiEvent) {
            is UiEvent.Error -> {
                showPersonaSheet = false
                snackbarHostState.showSnackbar(
                    message = (uiEvent as UiEvent.Error).message
                )
                viewModel.limpiarUiEvent()
            }
            null -> Unit
        }
    }

    LaunchedEffect(showPagoAnimacion) {
        if (showPagoAnimacion) {
            kotlinx.coroutines.delay(1000)
            showPagoAnimacion = false
        }
    }


    Box(modifier = Modifier.fillMaxSize()){
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Column {
                            Text(
                                "Gestor de gastos",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "● ${personas.size} integrantes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        val clipboardManager = LocalClipboardManager.current

                        Surface(
                            onClick = { clipboardManager.setText(AnnotatedString(viewModel.codigoGrupo)) },
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Copiar código",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(32.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreHoriz,
                                    contentDescription = "Menú",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false}
                            ){
                                DropdownMenuItem(
                                    text = { Text("Ver personas")},
                                    onClick = {
                                        showMenu = false
                                        showPersonaSheet = true
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(
                                        "Salir del grupo",
                                        color = MaterialTheme.colorScheme.error
                                        )},
                                    onClick = {
                                        showMenu = false
                                        showSalirGrupoDialog = true
                                    }
                                )
                            }

                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialogAgregarGasto = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Agregar gasto", tint = MaterialTheme.colorScheme.background)
                }
            },
            bottomBar = {
                if (transferencias.isNotEmpty()) {
                    Button(
                        onClick = { showDetalleDeudas = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                    Text("Ver Deudas")
                    }
                } else {
                    Text("No hay deudas pendientes",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(40.dp)
                        ,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Total del grupo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$${formatCentavos(gastos.sumOf { it.montoCentavos }, Locale.getDefault())}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                DividerConPunto()
                Spacer(Modifier.height(16.dp))

                if (gastos.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay gastos cargados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(gastos) { gasto ->
                            GastoItem(
                                gasto = gasto,
                                onMarcarPagado = { gastoAPagar = gasto },
                                onEliminar = { gastoAEliminar = gasto },
                                viewModel = viewModel,
                                personas = personas,
                                onAgregarPersona = { showAgregarPersonaDialog = true }
                            )
                        }
                    }
                }
            }
        }

        if (showDetalleDeudas) {
            ModalBottomSheet(
                onDismissRequest = { showDetalleDeudas = false },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Detalle de deudas", style = MaterialTheme.typography.titleLarge)

                    Spacer(Modifier.height(8.dp))


                    transferencias.forEach { transferenca ->
                        val montoFormateado = formatCentavos(transferenca.montoCentavos)
                        Text(
                            "• ${obtenerNombre(personas, transferenca.deudorId)} le debe a " +
                                    "${obtenerNombre(personas, transferenca.acreedorId)}: " +
                                    "" +
                                    "$${montoFormateado}"
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showDetalleDeudas = false
                            showDialogPagarTodo = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Pagar todo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }


        if (showDialogPagarTodo) {
            AlertDialog(
                onDismissRequest = { showDialogPagarTodo = false },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = {
                    Text("Confirmar pago total", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("¿Deseas marcar todos los gastos como pagados?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.pagarTodosLosGastos()
                            showDialogPagarTodo = false
                            showPagoAnimacion = true
                        }
                    ) {
                        Text("Sí, pagar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialogPagarTodo = false }) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = showPagoAnimacion,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500)) + scaleOut(animationSpec = tween(500)),
            modifier = Modifier
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash_logo),
                contentDescription = "Pago completo",
                modifier = Modifier.size(150.dp)
            )
        }

        if(showPersonaSheet){
            ModalBottomSheet(
                onDismissRequest = {showPersonaSheet = false},
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
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
                                        viewModel.editarPersona(persona.id, nombreEditando.trim())
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
                                    onClick = {
                                        personaEditandoId = null
                                    }
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
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
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
                        onClick = { showAgregarPersonaDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar persona")
                    }
                }
            }
        }

        if (showSalirGrupoDialog) {
            AlertDialog(
                onDismissRequest = { showSalirGrupoDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = { Text("Salir del grupo") },
                text = {
                    Text(
                        "¿Estás seguro de que querés salir del grupo?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onSalirDelGrupo()
                            showSalirGrupoDialog = false
                        }
                    ){
                        Text(
                            "Salir",
                            color = MaterialTheme.colorScheme.error
                        )
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


        if (showAgregarPersonaDialog) {
            DialogAgregarPersona(
                onDismiss = { showAgregarPersonaDialog = false },
                onConfirm = { nombre ->
                    viewModel.agregarPersona(nombre)
                    showAgregarPersonaDialog = false
                }
            )
        }

        if(showDialogAgregarGasto){
            AgregarGastoDialog(
                onDismiss = {showDialogAgregarGasto = false},
                onGuardar = { categoria, descripcion, monto, pagante, porcentaje, deudoresIds ->
                    viewModel.agregarGasto(categoria, descripcion, monto, pagante, porcentaje, deudoresIds)
                    showDialogAgregarGasto = false
                },
                personas,
                onAgregarPersona = { showAgregarPersonaDialog = true }
            )
        }

        if (gastoAPagar != null) {
            AlertDialog(
                onDismissRequest = { gastoAPagar = null },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = { Text("Confirmar pago") },
                text = { Text("¿Deseas marcar este gasto como pagado?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.eliminarGasto(gastoAPagar!!.firestoreId)
                        gastoAPagar = null
                    }) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { gastoAPagar = null }) {
                        Text("No")
                    }
                }
            )
        }


        if (gastoAEliminar != null) {
            AlertDialog(
                onDismissRequest = { gastoAEliminar = null },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Estás seguro que quieres eliminar el gasto?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.eliminarGasto(gastoAEliminar!!.firestoreId)
                        gastoAEliminar = null
                    }) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { gastoAEliminar = null }) {
                        Text("No")
                    }
                }
            )
        }
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun GastoItem(
    gasto: Gasto,
    onMarcarPagado: () -> Unit,
    onEliminar: (Gasto) -> Unit,
    viewModel: GastoViewModel,
    personas: List<Persona>,
    onAgregarPersona: () -> Unit
) {
    val pagante by produceState<Persona?>(initialValue = null, gasto.paganteId) {
        value = viewModel.obtenerPersonaPorId(gasto.paganteId)
    }

    var expanded by remember { mutableStateOf(false) }
    var showEditarDialog by remember { mutableStateOf(false) }

    val deudasPorGasto = remember(gasto, personas) {
        viewModel.calcularDeudaPorGasto(gasto, personas)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = gasto.categoria.color.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = gasto.categoria.icono,
                        contentDescription = null,
                        tint = gasto.categoria.color,
                        modifier = Modifier.padding(10.dp)
                    )
                }


                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gasto.categoria.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                append("Pagó: ")
                            }

                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface,fontWeight = FontWeight.Medium)) {
                                append(pagante?.nombre ?: "...")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${formatCentavos(gasto.montoCentavos, Locale.getDefault())}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = formatearFechaRelativa(gasto.fecha),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(12.dp))

                if (!gasto.descripcion.isNullOrBlank()) {
                    Text(
                        text = "Descripción: ${gasto.descripcion}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (personas.size == 2) {
                    val etiqueta = if (gasto.porcentaje == 1.0) "Prestamo" else "Gasto compartido"
                    Text(
                        text = etiqueta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (deudasPorGasto.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Detalle de este gasto",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        deudasPorGasto.forEach { deuda ->
                            Text(
                                text = "• ${
                                    obtenerNombre(personas, deuda.deudorId)
                                } le debe a ${
                                    obtenerNombre(personas, deuda.acreedorId)
                                }: $${formatCentavos(deuda.montoCentavos)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showEditarDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Editar")
                    }
                    TextButton(onClick = onMarcarPagado) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Saldar")
                    }
                    TextButton(onClick = { onEliminar(gasto) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("Borrar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showEditarDialog) {
        EditarGastoDialog(gasto = gasto, personas = personas, viewModel = viewModel, onAgregarPersona = onAgregarPersona ,onDismiss = { showEditarDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarGastoDialog(
    onDismiss: () -> Unit,
    onGuardar: (Categoria, String?, Long, Persona, Double, List<String>) -> Unit,
    personas: List<Persona>,
    onAgregarPersona: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    var categoria by remember { mutableStateOf(Categoria.SUPERMERCADO) }
    var descripcion by remember { mutableStateOf("") }
    var montoTexto by remember { mutableStateOf("") }
    var pagante by remember { mutableStateOf<Persona?>(null) }
    var porcentaje by remember { mutableDoubleStateOf(0.5) }
    var deudoresSeleccionados by remember { mutableStateOf<Set<String>>(emptySet()) }

    val montoCentavos = remember(montoTexto) { parseMonedaToCentavos(montoTexto) }

    LaunchedEffect(pagante) {
        pagante?.let { p ->
            deudoresSeleccionados = personas.filter { it.id != p.id }.map { it.id }.toSet()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = cs.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nuevo gasto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = cs.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it },
                    placeholder = {
                        Text(
                            "0",
                            style = MaterialTheme.typography.headlineMedium,
                            color = cs.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Text(
                            "$",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = cs.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cs.surfaceVariant,
                        unfocusedContainerColor = cs.surfaceVariant,
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.outline,
                        cursorColor = cs.primary,
                        focusedTextColor = cs.onSurface,
                        unfocusedTextColor = cs.onSurface
                    )
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "CATEGORÍA",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(Categoria.entries) { cat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { categoria = cat }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = cat.color.copy(
                                    alpha = if (categoria == cat) 0.35f else 0.18f
                                ),
                                shadowElevation = if (categoria == cat) 6.dp else 0.dp,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = cat.icono,
                                    contentDescription = cat.name,
                                    tint = cat.color,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = cat.name
                                    .lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "DESCRIPCIÓN (opcional)",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cs.surfaceVariant,
                        unfocusedContainerColor = cs.surfaceVariant,
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.outline,
                        focusedTextColor = cs.onSurface,
                        unfocusedTextColor = cs.onSurface,
                        cursorColor = cs.primary
                    )
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "PAGÓ",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                PaganteDropdown(
                    personas = personas,
                    pagante = pagante,
                    onSeleccionar = { pagante = it },
                    onAgregarPersona = onAgregarPersona
                )

                if (personas.size > 2 && pagante != null) {
                    Spacer(Modifier.height(24.dp))

                    Text(
                        "¿QUIÉN DEBE?",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(personas.filter { it.id != pagante?.id }) { persona ->
                            val seleccionado = persona.id in deudoresSeleccionados
                            val personaColor = remember(persona.id) {
                                personaColorFromId(persona.id)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    deudoresSeleccionados = if (seleccionado) {
                                        deudoresSeleccionados - persona.id
                                    } else {
                                        deudoresSeleccionados + persona.id
                                    }
                                }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (seleccionado)
                                        personaColor.copy(alpha = 0.35f)
                                    else
                                        cs.surfaceVariant,
                                    shadowElevation = if (seleccionado) 6.dp else 0.dp,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    val backgroundColor =
                                        if (seleccionado) personaColor else cs.surfaceVariant

                                    val iconColor =
                                        if (seleccionado) Color.White else cs.onSurfaceVariant

                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(backgroundColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = persona.nombre,
                                            tint = iconColor,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = persona.nombre,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                if (personas.size == 2) {
                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        FilterChip(
                            selected = porcentaje == 0.5,
                            onClick = { porcentaje = 0.5 },
                            label = { Text("Compartido") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cs.primary,
                                selectedLabelColor = cs.onPrimary
                            )
                        )

                        FilterChip(
                            selected = porcentaje == 1.0,
                            onClick = { porcentaje = 1.0 },
                            label = { Text("Préstamo") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cs.primary,
                                selectedLabelColor = cs.onPrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val monto = montoCentavos
                        if (monto != null && monto > 0 && pagante != null) {
                            onGuardar(
                                categoria,
                                descripcion.ifBlank { null },
                                monto,
                                pagante!!,
                                porcentaje,
                                deudoresSeleccionados.toList()
                            )
                            onDismiss()
                        }
                    },
                    enabled = (montoCentavos?.let { it > 0 } == true) &&
                            pagante != null && personas.size >= 2 &&
                            (personas.size <= 2 || deudoresSeleccionados.isNotEmpty()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        "Guardar gasto",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (personas.size < 2) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Se necesitan 2 o mas personas para agregar un nuevo gasto",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarGastoDialog(
    gasto: Gasto,
    personas: List<Persona>,
    viewModel: GastoViewModel,
    onAgregarPersona: () -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    var categoria by remember { mutableStateOf(gasto.categoria) }
    var descripcion by remember { mutableStateOf(gasto.descripcion ?: "") }
    var montoTexto by remember { mutableStateOf(formatCentavos(gasto.montoCentavos)) }

    val paganteInicial = personas.firstOrNull { it.id == gasto.paganteId }
    var pagante by remember { mutableStateOf(paganteInicial) }
    var porcentaje by remember { mutableStateOf(gasto.porcentaje) }

    var deudoresSeleccionados by remember {
        mutableStateOf(
            if (gasto.deudoresIds.isNotEmpty()) {
                gasto.deudoresIds.toSet()
            } else {
                personas.filter { it.id != gasto.paganteId }.map { it.id }.toSet()
            }
        )
    }

    val montoCentavos = remember(montoTexto) { parseMonedaToCentavos(montoTexto) }

    LaunchedEffect(pagante) {
        pagante?.let { p ->
            deudoresSeleccionados = deudoresSeleccionados - p.id
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = cs.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar gasto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = cs.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it },
                    leadingIcon = {
                        Text(
                            "$",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = cs.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cs.surfaceVariant,
                        unfocusedContainerColor = cs.surfaceVariant,
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.outline,
                        cursorColor = cs.primary
                    )
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "CATEGORÍA",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(Categoria.entries) { cat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { categoria = cat }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = cat.color.copy(
                                    alpha = if (categoria == cat) 0.6f else 0.15f
                                ),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = cat.icono,
                                    contentDescription = cat.name,
                                    tint = cat.color,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            Text(
                                cat.name.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "DESCRIPCIÓN (opcional)",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cs.surfaceVariant,
                        unfocusedContainerColor = cs.surfaceVariant
                    )
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "PAGÓ",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                PaganteDropdown(
                    personas = personas,
                    pagante = pagante,
                    onSeleccionar = { pagante = it },
                    onAgregarPersona = onAgregarPersona
                )

                if (personas.size > 2 && pagante != null) {
                    Spacer(Modifier.height(24.dp))

                    Text(
                        "¿QUIÉN DEBE?",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(personas.filter { it.id != pagante?.id }) { persona ->
                            val seleccionado = persona.id in deudoresSeleccionados
                            val personaColor = remember(persona.id) {
                                personaColorFromId(persona.id)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    deudoresSeleccionados = if (seleccionado) {
                                        deudoresSeleccionados - persona.id
                                    } else {
                                        deudoresSeleccionados + persona.id
                                    }
                                }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (seleccionado)
                                        personaColor.copy(alpha = 0.35f)
                                    else
                                        cs.surfaceVariant,
                                    shadowElevation = if (seleccionado) 6.dp else 0.dp,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    val backgroundColor =
                                        if (seleccionado) personaColor else cs.surfaceVariant

                                    val iconColor =
                                        if (seleccionado) Color.White else cs.onSurfaceVariant

                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(backgroundColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = persona.nombre,
                                            tint = iconColor,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = persona.nombre,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                if (personas.size == 2) {
                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        FilterChip(
                            selected = porcentaje == 0.5,
                            onClick = { porcentaje = 0.5 },
                            label = { Text("Compartido") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cs.primary,
                                selectedLabelColor = cs.onPrimary
                            )
                        )

                        FilterChip(
                            selected = porcentaje == 1.0,
                            onClick = { porcentaje = 1.0 },
                            label = { Text("Préstamo") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cs.primary,
                                selectedLabelColor = cs.onPrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        val monto = montoCentavos
                        if (monto != null && monto > 0 && pagante != null) {
                            viewModel.editarGasto(
                                gasto,
                                categoria,
                                descripcion.ifBlank { null },
                                monto,
                                pagante!!,
                                porcentaje,
                                deudoresSeleccionados.toList()
                            )
                            onDismiss()
                        }
                    },
                    enabled =
                        (montoCentavos?.let { it > 0 } == true) &&
                                pagante != null &&
                                (personas.size <= 2 || deudoresSeleccionados.isNotEmpty()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Guardar cambios", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DividerConPunto(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )


        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(4.dp)
                .background(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    CircleShape
                )
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    }
}

private fun obtenerNombre(
    personas: List<Persona>,
    id: String
): String {
    return personas.firstOrNull { it.id == id }?.nombre ?: "Desconocido"
}

fun personaColorFromId(id: Any): Color {
    val colors = listOf(
        Color(0xFF60A5FA),
        Color(0xFF9800C2),
        Color(0xFFFBBF24),
        Color(0xFFF87171),
        Color(0xFFA78BFA),
        Color(0xFFFB7185),
        Color(0xFF34D399),
        Color(0xFFFF9F43),
        Color(0xFF00CEC9),
        Color(0xFFE17055),
        Color(0xFF6C5CE7),
        Color(0xFF00B894),
        Color(0xFFED4C67),
        Color(0xFF05A8AA),
        Color(0xFFF368E0),
        Color(0xFF1ABC9C)
    )
    return colors[id.hashCode().absoluteValue % colors.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaganteDropdown(
    personas: List<Persona>,
    pagante: Persona?,
    onSeleccionar: (Persona) -> Unit,
    onAgregarPersona: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Surface(
            modifier = Modifier
                .menuAnchor(
                type = MenuAnchorType.PrimaryNotEditable,
                enabled = true)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = cs.surfaceVariant,
            border = BorderStroke(
                1.dp,
                if (pagante != null) cs.primary else cs.outline
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = cs.onSurfaceVariant
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = pagante?.nombre ?: "¿Quién pagó?",
                    modifier = Modifier.weight(1f),
                    color = if (pagante == null)
                        cs.onSurfaceVariant
                    else
                        cs.onSurface
                )

                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = cs.onSurfaceVariant
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(cs.surface)
        ) {

            personas.forEach { persona ->
                val personaColor = remember(persona.id) {
                    personaColorFromId(persona.id)
                }

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(personaColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    persona.nombre.take(1).uppercase(),
                                    color = cs.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.width(12.dp))
                            Text(persona.nombre)
                        }
                    },
                    onClick = {
                        onSeleccionar(persona)
                        expanded = false
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = cs.outline.copy(alpha = 0.3f)
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = cs.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Agregar persona",
                            color = cs.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                onClick = {
                    expanded = false
                    onAgregarPersona()
                }
            )
        }
    }
}
