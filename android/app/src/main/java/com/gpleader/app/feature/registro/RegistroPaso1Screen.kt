package com.gpleader.app.feature.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInsetSm
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun RegistroPaso1Screen(
    onNavigateBack:    () -> Unit,
    onNavigateToPaso2: () -> Unit,
    onNavigateToPaso3: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToPaso2) {
        if (uiState.navigateToPaso2) {
            viewModel.consumePaso2Navigation()
            onNavigateToPaso2()
        }
    }
    LaunchedEffect(uiState.navigateToPaso3) {
        if (uiState.navigateToPaso3) {
            viewModel.consumePaso3Navigation()
            onNavigateToPaso3()
        }
    }

    RegistroPaso1Content(
        uiState                          = uiState,
        onNavigateBack                   = onNavigateBack,
        onFechaChange                    = viewModel::onFechaChange,
        onNoHuboReunion                  = viewModel::onNoHuboReunionClick,
        onAsistenciaChange               = viewModel::onAsistenciaChange,
        onSelTodos                       = viewModel::onSelTodos,
        onAgregarVisitaAnterior          = viewModel::onAgregarVisitaAnterior,
        onEliminarVisitaAnterior         = viewModel::onEliminarVisitaAnterior,
        onVisitaAnteriorAsistenciaChange = viewModel::onVisitaAnteriorAsistenciaChange,
        onAgregarNuevaVisita             = viewModel::onAgregarNuevaVisita,
        onVisitaAsistenciaChange         = viewModel::onVisitaAsistenciaChange,
        onToggleVisitas                  = viewModel::onToggleVisitasColapsadas,
        onContinuar                      = viewModel::onContinuarClick,
        onDismissConfirmAusentes         = viewModel::onDismissConfirmTodosAusentes,
        onDismissConfirmNoHubo           = viewModel::onDismissConfirmNoHuboReunion,
        onConfirmNoHubo                  = viewModel::onConfirmNoHuboReunion,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistroPaso1Content(
    uiState:                          RegistroUiState,
    onNavigateBack:                   () -> Unit,
    onFechaChange:                    (LocalDate) -> Unit,
    onNoHuboReunion:                  () -> Unit,
    onAsistenciaChange:               (String, EstadoAsistencia) -> Unit,
    onSelTodos:                       (EstadoAsistencia) -> Unit,
    onAgregarVisitaAnterior:          (String) -> Unit,
    onEliminarVisitaAnterior:         (String) -> Unit,
    onVisitaAnteriorAsistenciaChange: (String, EstadoAsistencia) -> Unit,
    onAgregarNuevaVisita:             (String, String) -> Unit,
    onVisitaAsistenciaChange:         (String, EstadoAsistencia) -> Unit,
    onToggleVisitas:                  () -> Unit,
    onContinuar:                      () -> Unit,
    onDismissConfirmAusentes:         () -> Unit,
    onDismissConfirmNoHubo:           () -> Unit,
    onConfirmNoHubo:                  () -> Unit,
) {
    var showDatePicker         by remember { mutableStateOf(false) }
    var showAgregarVisitaSheet by remember { mutableStateOf(false) }
    var openMiembroId          by remember { mutableStateOf<String?>(null) }
    val listState          = rememberLazyListState()
    val errorShakeOffset   = remember { Animatable(0f) }

    // Al aparecer el error: desplazar al primer miembro sin marcar y sacudir el texto
    LaunchedEffect(uiState.errorSinAsistenciaTrigger) {
        if (uiState.errorSinAsistenciaTrigger > 0) {
            val firstUnset = uiState.miembros.indexOfFirst { it.estado == null }
            val scrollIndex = if (firstUnset >= 0) 7 + firstUnset else 7 + uiState.miembros.size
            listState.animateScrollToItem(scrollIndex)
            repeat(3) {
                errorShakeOffset.animateTo(10f, tween(70))
                errorShakeOffset.animateTo(-10f, tween(70))
            }
            errorShakeOffset.animateTo(0f, tween(70))
        }
    }

    // ── Diálogo: todos ausentes ───────────────────────────────────────────────
    if (uiState.showConfirmTodosAusentes) {
        AlertDialog(
            onDismissRequest = onDismissConfirmAusentes,
            title  = { Text(stringResource(R.string.registro_confirm_ausentes_titulo)) },
            text   = { Text(stringResource(R.string.registro_confirm_ausentes_cuerpo)) },
            confirmButton = {
                TextButton(onClick = onDismissConfirmAusentes) {
                    Text(stringResource(R.string.registro_confirm_entendido))
                }
            },
            containerColor = Background,
        )
    }

    // ── Diálogo: no hubo reunión ──────────────────────────────────────────────
    if (uiState.showConfirmNoHuboReunion) {
        AlertDialog(
            onDismissRequest = onDismissConfirmNoHubo,
            title  = { Text(stringResource(R.string.registro_confirm_no_hubo_titulo)) },
            text   = { Text(stringResource(R.string.registro_confirm_no_hubo_cuerpo)) },
            confirmButton = {
                TextButton(onClick = onConfirmNoHubo) {
                    Text(stringResource(R.string.registro_confirm_confirmar))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissConfirmNoHubo) {
                    Text(stringResource(R.string.registro_confirm_cancelar))
                }
            },
            containerColor = Background,
        )
    }

    // ── DatePicker ────────────────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.fecha
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onFechaChange(
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                        )
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.registro_confirm_confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.registro_confirm_cancelar))
                }
            },
        ) { DatePicker(state = datePickerState) }
    }

    // ── Sheet: agregar nueva visita ───────────────────────────────────────────
    if (showAgregarVisitaSheet) {
        AgregarVisitaSheet(
            visitasAnteriores    = uiState.visitasAnteriores,
            visitasDeHoyIds      = uiState.visitasDeHoy.map { it.id }.toSet(),
            onAgregar            = { nombre, apellido ->
                onAgregarNuevaVisita(nombre, apellido)
                showAgregarVisitaSheet = false
            },
            onAgregarAnterior    = { visitaId ->
                onAgregarVisitaAnterior(visitaId)
                showAgregarVisitaSheet = false
            },
            onCancelar           = { showAgregarVisitaSheet = false },
        )
    }

    // ── Pantalla principal ────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            item { RegistroTopBar(onNavigateBack = onNavigateBack) }

            item { StepperRow(pasoActivo = 1) }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                FechaCard(
                    fecha           = uiState.fecha,
                    onEditarFecha   = { showDatePicker = true },
                    onNoHuboReunion = onNoHuboReunion,
                    modifier        = Modifier.padding(horizontal = 16.dp),
                )
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                val presentes = uiState.miembros.count { it.estado == EstadoAsistencia.PRESENTE }
                MiembrosHeader(
                    presentes  = presentes,
                    total      = uiState.miembros.size,
                    onSelTodos = onSelTodos,
                    modifier   = Modifier.padding(horizontal = 16.dp),
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            items(uiState.miembros, key = { it.id }) { miembro ->
                MiembroRow(
                    miembro          = miembro,
                    onChange         = { estado -> onAsistenciaChange(miembro.id, estado) },
                    showHint         = uiState.showJustificadoHint && miembro == uiState.miembros.firstOrNull(),
                    isOpenExternal   = openMiembroId == miembro.id,
                    onSwipeOpen      = { openMiembroId = miembro.id },
                    onSwipeClosed    = { if (openMiembroId == miembro.id) openMiembroId = null },
                    onAnyTap         = { openMiembroId = null },
                    modifier         = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (uiState.errorSinAsistencia) {
                item {
                    Text(
                        text     = stringResource(R.string.registro_error_sin_asistencia),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = Blush,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .offset { IntOffset(errorShakeOffset.value.roundToInt(), 0) },
                    )
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                VisitasAnterioresHeader(
                    count      = uiState.visitasAnteriores.size,
                    colapsadas = uiState.visitasColapsadas,
                    onToggle   = onToggleVisitas,
                )
            }

            if (!uiState.visitasColapsadas) {
                items(uiState.visitasAnteriores, key = { "ant_${it.id}" }) { visita ->
                    val estadoHoy = uiState.visitasDeHoy.find { it.id == visita.id }?.estado
                    VisitaAnteriorRow(
                        visita          = visita,
                        estadoHoy       = estadoHoy,
                        onChange        = { estado -> onVisitaAnteriorAsistenciaChange(visita.id, estado) },
                        isOpenExternal  = openMiembroId == visita.id,
                        onSwipeOpen     = { openMiembroId = visita.id },
                        onSwipeClosed   = { if (openMiembroId == visita.id) openMiembroId = null },
                        modifier        = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                VisitasHoySection(
                    visitas                  = uiState.visitasDeHoy.filter { it.esNueva },
                    onVisitaAsistenciaChange = onVisitaAsistenciaChange,
                    onAbrirSheet             = { showAgregarVisitaSheet = true },
                    modifier                 = Modifier.padding(horizontal = 16.dp),
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        // ── Botón flotante ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Background)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            NeuButtonPrimary(
                text     = stringResource(R.string.registro_btn_continuar),
                onClick  = onContinuar,
                modifier = Modifier.fillMaxWidth(),
            )
        }

    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun RegistroTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .neuElevatedSm(cornerRadius = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Background)
                .clickable(onClick = onNavigateBack)
                .padding(10.dp),
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint               = Ink,
                modifier           = Modifier.size(20.dp),
            )
        }

        Text(
            text      = stringResource(R.string.registro_titulo),
            style     = MaterialTheme.typography.titleLarge,
            color     = Ink,
            textAlign = TextAlign.Center,
            modifier  = Modifier.weight(1f),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Ink)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text(
                text  = stringResource(R.string.registro_badge_paso),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

// ── Stepper ───────────────────────────────────────────────────────────────────

@Composable
private fun StepperRow(pasoActivo: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        listOf(
            stringResource(R.string.registro_step_asistencia),
            stringResource(R.string.registro_step_actividades),
            stringResource(R.string.registro_step_resumen),
        ).forEachIndexed { idx, label ->
            val activo = idx + 1 == pasoActivo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.weight(1f),
            ) {
                Text(
                    text     = label,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = if (activo) Accent else Muted,
                    modifier = Modifier.padding(vertical = 10.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (activo) Accent else Color.Transparent),
                )
            }
        }
    }
}

// ── Card fecha ────────────────────────────────────────────────────────────────

@Composable
private fun FechaCard(
    fecha:           LocalDate,
    onEditarFecha:   () -> Unit,
    onNoHuboReunion: () -> Unit,
    modifier:        Modifier = Modifier,
) {
    NeuCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text  = stringResource(R.string.registro_label_fecha),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text  = formatFechaReunion(fecha),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .neuElevatedSm(cornerRadius = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Background)
                        .clickable(onClick = onEditarFecha)
                        .padding(8.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = null,
                        tint               = Mid,
                        modifier           = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .neuElevatedSm(cornerRadius = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Background)
                    .clickable(onClick = onNoHuboReunion)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text       = stringResource(R.string.registro_btn_no_hubo),
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = Mid,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ── Miembros header ───────────────────────────────────────────────────────────

@Composable
private fun MiembrosHeader(
    presentes:  Int,
    total:      Int,
    onSelTodos: (EstadoAsistencia) -> Unit,
    modifier:   Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text  = stringResource(R.string.registro_label_miembros),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Spacer(Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Ink)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text  = "$presentes/$total",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }

        Box {
            var expanded by remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .neuElevatedSm(cornerRadius = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Background)
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            ) {
                Text(
                    text  = stringResource(R.string.registro_sel_todos),
                    style = MaterialTheme.typography.labelSmall,
                    color = Mid,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector        = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint               = Muted,
                    modifier           = Modifier.size(14.dp),
                )
            }
            DropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false },
            ) {
                listOf(
                    stringResource(R.string.registro_sel_presentes)   to EstadoAsistencia.PRESENTE,
                    stringResource(R.string.registro_sel_ausentes)     to EstadoAsistencia.AUSENTE,
                    stringResource(R.string.registro_sel_justificados) to EstadoAsistencia.JUSTIFICADO,
                ).forEach { (label, estado) ->
                    DropdownMenuItem(
                        text    = { Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink) },
                        onClick = { onSelTodos(estado); expanded = false },
                    )
                }
            }
        }
    }
}

// ── Fila de miembro ───────────────────────────────────────────────────────────
// Interacción:
//   • Tap en checkbox   → alterna PRESENTE / AUSENTE
//   • Swipe izquierda   → revela panel J; tap en "Justificado" lo aplica
//   • Long press en row → menú contextual P / A / J
//   • showHint = true   → animación peek al abrir la pantalla (hasta 5 usos de J)

@Composable
private fun MiembroRow(
    miembro:        MiembroAsistencia,
    onChange:       (EstadoAsistencia) -> Unit,
    showHint:       Boolean = false,
    isOpenExternal: Boolean = false,
    onSwipeOpen:    () -> Unit = {},
    onSwipeClosed:  () -> Unit = {},
    onAnyTap:       () -> Unit = {},
    modifier:       Modifier = Modifier,
) {
    val density   = LocalDensity.current
    val revealDp  = 88.dp
    val revealPx  = with(density) { revealDp.toPx() }
    val offsetX   = remember(miembro.id) { Animatable(0f) }
    val scope     = rememberCoroutineScope()
    var showMenu  by remember { mutableStateOf(false) }

    // Cerrar externamente cuando otro ítem se abre
    LaunchedEffect(isOpenExternal) {
        if (!isOpenExternal && offsetX.value < -1f) {
            offsetX.animateTo(0f, tween(220))
        }
    }

    // ── Hint: peek al entrar, luego cada 15s sin interacción ──────────────────
    LaunchedEffect(showHint) {
        if (!showHint) return@LaunchedEffect
        kotlinx.coroutines.delay(1800)
        while (true) {
            if (offsetX.value >= -1f) {
                offsetX.animateTo(-revealPx * 0.22f, tween(280))
                kotlinx.coroutines.delay(220)
                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }
            kotlinx.coroutines.delay(15_000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(14.dp)),
    ) {
        // ── Panel J (tarjeta completa oculta detrás, se revela al deslizar) ──
        Box(modifier = Modifier.matchParentSize()) {
            // Fondo completo dorado suave
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Gold.copy(alpha = 0.15f)),
            )
            // Texto centrado en la zona revelable (derecha revealDp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(revealDp)
                    .fillMaxHeight()
                    .clickable {
                        onChange(EstadoAsistencia.JUSTIFICADO)
                        scope.launch { offsetX.animateTo(0f, tween(200)) }
                    },
            ) {
                Text(
                    text       = "Justificar",
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = Gold,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // ── Menú contextual (long press) ──────────────────────────────────────
        DropdownMenu(
            expanded         = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            listOf(
                stringResource(R.string.registro_menu_presente)    to EstadoAsistencia.PRESENTE,
                stringResource(R.string.registro_menu_ausente)     to EstadoAsistencia.AUSENTE,
                stringResource(R.string.registro_menu_justificado) to EstadoAsistencia.JUSTIFICADO,
            ).forEach { (label, estado) ->
                DropdownMenuItem(
                    text    = { Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink) },
                    onClick = {
                        onChange(estado)
                        showMenu = false
                        scope.launch { offsetX.animateTo(0f, tween(200)) }
                    },
                )
            }
        }

        // ── Card principal ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(RoundedCornerShape(14.dp))
                .background(Background)
                .pointerInput("lp_${miembro.id}") {
                    detectTapGestures(
                        onLongPress = {
                            scope.launch { offsetX.animateTo(0f, tween(200)) }
                            showMenu = true
                        },
                        onTap = { onAnyTap() },
                    )
                }
                .pointerInput(miembro.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < -(revealPx * 0.4f)) {
                                    offsetX.animateTo(
                                        -revealPx,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    )
                                    onSwipeOpen()
                                } else {
                                    offsetX.animateTo(0f, tween(220))
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, tween(220)) }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo(
                                    (offsetX.value + dragAmount).coerceIn(-revealPx, 0f)
                                )
                            }
                        },
                    )
                },
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InitialsAvatar(iniciales = miembro.iniciales)
                Spacer(Modifier.width(12.dp))
                Text(
                    text     = miembro.nombre,
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = Ink,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                AsistenciaCheckbox(
                    estado   = miembro.estado,
                    onChange = onChange,
                )
            }
            // Overlay gris progresivo (mismo efecto que las tarjetas de miembros)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        alpha = (-offsetX.value / revealPx).coerceIn(0f, 1f) * 0.30f
                    }
                    .background(Shadow),
            )
        }

        // Overlay: intercepta gestos cuando el panel J está visible.
        // Tap en cualquier parte de la card → cierra. Deslizar derecha → cierra.
        // Usa mismo patrón que SwipeableItem: drag (inner) procesa antes que tap (outer).
        if (offsetX.value < -1f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .pointerInput("tap_close_${miembro.id}") {
                        detectTapGestures(
                            onTap = {
                                scope.launch { offsetX.animateTo(0f, tween(220)) }
                                onSwipeClosed()
                            }
                        )
                    }
                    .pointerInput("drag_close_${miembro.id}") {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (offsetX.value < -(revealPx * 0.6f))
                                        offsetX.animateTo(
                                            -revealPx,
                                            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        )
                                    else {
                                        offsetX.animateTo(0f, tween(220))
                                        onSwipeClosed()
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch { offsetX.animateTo(0f, tween(220)) }
                                onSwipeClosed()
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    offsetX.snapTo(
                                        (offsetX.value + dragAmount).coerceIn(-revealPx, 0f)
                                    )
                                }
                            },
                        )
                    },
            )
        }
    }
}

// ── Checkbox de asistencia ────────────────────────────────────────────────────
// Vacío = AUSENTE · Check verde = PRESENTE · J dorado = JUSTIFICADO
// Tap: alterna PRESENTE ↔ AUSENTE (incluyendo desde JUSTIFICADO → AUSENTE)

@Composable
private fun AsistenciaCheckbox(
    estado:   EstadoAsistencia?,
    onChange: (EstadoAsistencia) -> Unit,
) {
    val esPresente    = estado == EstadoAsistencia.PRESENTE
    val esJustificado = estado == EstadoAsistencia.JUSTIFICADO
    val nudgeX = remember { Animatable(0f) }
    val scope  = rememberCoroutineScope()

    // Nudge horizontal al cambiar estado — no desborda los límites del item
    LaunchedEffect(estado) {
        if (estado != null) {
            nudgeX.animateTo( 5f, spring(Spring.DampingRatioHighBouncy,   Spring.StiffnessMediumLow))
            nudgeX.animateTo(-3f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
            nudgeX.animateTo( 0f, spring(Spring.DampingRatioMediumBouncy))
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset { IntOffset(nudgeX.value.roundToInt(), 0) }
            .size(40.dp)
            .then(
                if (esPresente || esJustificado) Modifier.neuInsetSm(cornerRadius = 10.dp)
                else Modifier.neuElevatedSm(cornerRadius = 10.dp)
            )
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    esPresente    -> Sage.copy(alpha = 0.15f)
                    esJustificado -> Gold.copy(alpha = 0.15f)
                    else          -> Background
                }
            )
            .border(
                width = 1.5.dp,
                color = when {
                    esPresente    -> Sage
                    esJustificado -> Gold
                    else          -> Muted.copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(10.dp),
            )
            .clickable {
                onChange(
                    if (esPresente) EstadoAsistencia.AUSENTE else EstadoAsistencia.PRESENTE
                )
            },
    ) {
        when {
            esPresente -> Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = null,
                tint               = Sage,
                modifier           = Modifier.size(20.dp),
            )
            esJustificado -> Text(
                text       = "J",
                style      = MaterialTheme.typography.labelSmall,
                color      = Gold,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ── Avatar con iniciales ──────────────────────────────────────────────────────

@Composable
private fun InitialsAvatar(iniciales: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .neuElevatedSm(cornerRadius = 20.dp)
            .clip(CircleShape)
            .background(BackgroundDeep),
    ) {
        Text(
            text  = iniciales,
            style = MaterialTheme.typography.labelSmall,
            color = Mid,
        )
    }
}

// ── Toggle P/A/J ─────────────────────────────────────────────────────────────
// Tocar el estado activo lo deselecciona (→ null), comportamiento manejado en VM.

@Composable
private fun PajToggle(
    estado:   EstadoAsistencia?,
    onChange: (EstadoAsistencia) -> Unit,
) {
    Row(
        modifier = Modifier
            .neuInsetSm(cornerRadius = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundDeep),
    ) {
        listOf(
            "P" to EstadoAsistencia.PRESENTE,
            "A" to EstadoAsistencia.AUSENTE,
            "J" to EstadoAsistencia.JUSTIFICADO,
        ).forEach { (label, opcion) ->
            val activo = estado == opcion
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (activo) Ink else Color.Transparent)
                    .clickable { onChange(opcion) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (activo) Color.White else Muted,
                )
            }
        }
    }
}

// ── Fila punteada: agregar miembro al grupo ───────────────────────────────────

@Composable
private fun AgregarMiembroRow(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        DashedDivider()
        Spacer(Modifier.height(8.dp))
        Text(
            text  = stringResource(R.string.registro_agregar_miembro),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
    }
}

// ── Visitas anteriores header ─────────────────────────────────────────────────

@Composable
private fun VisitasAnterioresHeader(
    count:      Int,
    colapsadas: Boolean,
    onToggle:   () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink)
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text  = stringResource(R.string.registro_visitas_anteriores, count),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
        // ▶ cuando colapsado, ▼ cuando expandido
        Icon(
            imageVector        = if (colapsadas) Icons.AutoMirrored.Filled.KeyboardArrowRight
                                 else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint               = Color.White,
            modifier           = Modifier.size(18.dp),
        )
    }
}

// ── Fila de visita anterior ───────────────────────────────────────────────────

@Composable
private fun VisitaAnteriorRow(
    visita:         VisitaAnterior,
    estadoHoy:      EstadoAsistencia?,
    onChange:       (EstadoAsistencia) -> Unit,
    isOpenExternal: Boolean = false,
    onSwipeOpen:    () -> Unit = {},
    onSwipeClosed:  () -> Unit = {},
    modifier:       Modifier = Modifier,
) {
    val density  = LocalDensity.current
    val revealDp = 88.dp
    val revealPx = with(density) { revealDp.toPx() }
    val offsetX  = remember(visita.id) { Animatable(0f) }
    val scope    = rememberCoroutineScope()

    LaunchedEffect(isOpenExternal) {
        if (!isOpenExternal && offsetX.value < -1f) {
            offsetX.animateTo(0f, tween(220))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(14.dp)),
    ) {
        // ── Panel J (igual que MiembroRow) ────────────────────────────────────
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Gold.copy(alpha = 0.15f)),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(revealDp)
                    .fillMaxHeight()
                    .clickable {
                        onChange(EstadoAsistencia.JUSTIFICADO)
                        scope.launch { offsetX.animateTo(0f, tween(200)) }
                    },
            ) {
                Text(
                    text       = "Justificar",
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = Gold,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // ── Card principal deslizable ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(RoundedCornerShape(14.dp))
                .background(Background)
                .pointerInput(visita.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < -(revealPx * 0.4f)) {
                                    offsetX.animateTo(
                                        -revealPx,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    )
                                    onSwipeOpen()
                                } else {
                                    offsetX.animateTo(0f, tween(220))
                                }
                            }
                        },
                        onDragCancel = { scope.launch { offsetX.animateTo(0f, tween(220)) } },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo(
                                    (offsetX.value + dragAmount).coerceIn(-revealPx, 0f)
                                )
                            }
                        },
                    )
                },
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val iniciales = visita.nombre.split(" ")
                    .filter { it.isNotBlank() }.take(2)
                    .joinToString("") { it.first().uppercaseChar().toString() }
                InitialsAvatar(iniciales = iniciales)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = visita.nombre,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink,
                    )
                    Text(
                        text  = "${visita.fechaUltimaVisita.dayOfMonth} ${MESES_ES_CORTO[visita.fechaUltimaVisita.monthValue - 1]}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
                Spacer(Modifier.width(12.dp))
                AsistenciaCheckbox(
                    estado   = estadoHoy,
                    onChange = onChange,
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        alpha = (-offsetX.value / revealPx).coerceIn(0f, 1f) * 0.30f
                    }
                    .background(Shadow),
            )
        }

        // ── Overlay: tap o deslizar derecha para cerrar ───────────────────────
        if (offsetX.value < -1f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .pointerInput("tap_close_${visita.id}") {
                        detectTapGestures(
                            onTap = {
                                scope.launch { offsetX.animateTo(0f, tween(220)) }
                                onSwipeClosed()
                            }
                        )
                    }
                    .pointerInput("drag_close_${visita.id}") {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (offsetX.value < -(revealPx * 0.6f))
                                        offsetX.animateTo(
                                            -revealPx,
                                            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        )
                                    else {
                                        offsetX.animateTo(0f, tween(220))
                                        onSwipeClosed()
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch { offsetX.animateTo(0f, tween(220)) }
                                onSwipeClosed()
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    offsetX.snapTo(
                                        (offsetX.value + dragAmount).coerceIn(-revealPx, 0f)
                                    )
                                }
                            },
                        )
                    },
            )
        }
    }
}

// ── Visitas de hoy ────────────────────────────────────────────────────────────

@Composable
private fun VisitasHoySection(
    visitas:                  List<VisitaHoy>,
    onVisitaAsistenciaChange: (String, EstadoAsistencia) -> Unit,
    onAbrirSheet:             () -> Unit,
    modifier:                 Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text     = stringResource(R.string.registro_visitas_hoy),
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        DashedDivider()
        Spacer(Modifier.height(4.dp))

        visitas.forEach { visita ->
            VisitaHoyRow(
                visita    = visita,
                onChange  = { estado -> onVisitaAsistenciaChange(visita.id, estado) },
                modifier  = Modifier.padding(vertical = 4.dp),
            )
        }

        Spacer(Modifier.height(8.dp))

        // Fila "+ Agregar visita" — abre el Sheet
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .neuElevatedSm(cornerRadius = 10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Background)
                .clickable(onClick = onAbrirSheet)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text  = "+",
                style = MaterialTheme.typography.titleLarge,
                color = Accent,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text  = stringResource(R.string.registro_placeholder_visita),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }
    }
}

@Composable
private fun VisitaHoyRow(
    visita:   VisitaHoy,
    onChange: (EstadoAsistencia) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density  = LocalDensity.current
    val revealDp = 88.dp
    val revealPx = with(density) { revealDp.toPx() }
    val offsetX  = remember(visita.id) { Animatable(0f) }
    val scope    = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(14.dp)),
    ) {
        // ── Panel J (tarjeta completa oculta detrás) ──────────────────────────
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Gold.copy(alpha = 0.15f)),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(revealDp)
                    .fillMaxHeight()
                    .clickable {
                        onChange(EstadoAsistencia.JUSTIFICADO)
                        scope.launch { offsetX.animateTo(0f, tween(200)) }
                    },
            ) {
                Text(
                    text       = "Justificado",
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = Gold,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // ── Card principal deslizable ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(RoundedCornerShape(14.dp))
                .background(Background)
                .pointerInput(visita.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < -(revealPx * 0.4f))
                                    offsetX.animateTo(
                                        -revealPx,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    )
                                else
                                    offsetX.animateTo(0f, tween(220))
                            }
                        },
                        onDragCancel = { scope.launch { offsetX.animateTo(0f, tween(220)) } },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo(
                                    (offsetX.value + dragAmount).coerceIn(-revealPx, 0f)
                                )
                            }
                        },
                    )
                },
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val iniciales = visita.nombre.split(" ")
                    .filter { it.isNotBlank() }.take(2)
                    .joinToString("") { it.first().uppercaseChar().toString() }
                InitialsAvatar(iniciales = iniciales)
                Spacer(Modifier.width(12.dp))
                Text(
                    text     = visita.nombre,
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = Ink,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                AsistenciaCheckbox(
                    estado   = visita.estado,
                    onChange = onChange,
                )
            }
            // Overlay gris progresivo al deslizar
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        alpha = (-offsetX.value / revealPx).coerceIn(0f, 1f) * 0.30f
                    }
                    .background(Shadow),
            )
        }

        // ── Overlay: tap o deslizar derecha para cerrar ───────────────────────
        if (offsetX.value < -1f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .pointerInput("tap_close_${visita.id}") {
                        detectTapGestures(
                            onTap = { scope.launch { offsetX.animateTo(0f, tween(220)) } }
                        )
                    }
                    .pointerInput("drag_close_${visita.id}") {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (offsetX.value < -(revealPx * 0.6f))
                                        offsetX.animateTo(
                                            -revealPx,
                                            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        )
                                    else
                                        offsetX.animateTo(0f, tween(220))
                                }
                            },
                            onDragCancel = { scope.launch { offsetX.animateTo(0f, tween(220)) } },
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    offsetX.snapTo(
                                        (offsetX.value + dragAmount).coerceIn(-revealPx, 0f)
                                    )
                                }
                            },
                        )
                    },
            )
        }
    }
}

// ── Sheet: agregar nueva visita ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgregarVisitaSheet(
    visitasAnteriores: List<VisitaAnterior>,
    visitasDeHoyIds:   Set<String>,
    onAgregar:         (primerNombre: String, primerApellido: String) -> Unit,
    onAgregarAnterior: (visitaId: String) -> Unit,
    onCancelar:        () -> Unit,
) {
    val sheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    var nombre   by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }

    val sugerencias = remember(nombre, apellido, visitasAnteriores) {
        val query = "${nombre.trim()} ${apellido.trim()}".trim()
        if (query.length < 2) emptyList()
        else visitasAnteriores.filter { it.nombre.contains(query, ignoreCase = true) }
    }
    var showDropdown by remember { mutableStateOf(false) }

    // Mostrar dropdown cuando hay sugerencias y algún campo tiene texto
    val mostrarDropdown = showDropdown && sugerencias.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest    = onCancelar,
        sheetState          = sheetState,
        containerColor      = Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text     = stringResource(R.string.registro_sheet_titulo),
                style    = MaterialTheme.typography.titleLarge,
                color    = Ink,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            // ── Dropdown hacia arriba (aparece antes de los campos) ───────────
            if (mostrarDropdown) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(BackgroundDeep)
                        .border(1.dp, Muted.copy(alpha = 0.3f),
                            RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                ) {
                    sugerencias.forEach { sugerencia ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val ini = sugerencia.nombre.split(" ")
                                .filter { it.isNotBlank() }.take(2)
                                .joinToString("") { it.first().uppercaseChar().toString() }
                            InitialsAvatar(iniciales = ini, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text  = sugerencia.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Ink,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text  = stringResource(
                                        R.string.registro_sheet_ultima_visita,
                                        sugerencia.fechaUltimaVisita.dayOfMonth,
                                        MESES_ES_CORTO[sugerencia.fechaUltimaVisita.monthValue - 1],
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Muted,
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .neuElevatedSm(cornerRadius = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Background)
                                    .clickable(enabled = sugerencia.id !in visitasDeHoyIds) {
                                        // Usa el ID original → evita duplicados y muestra ✓ en visitas anteriores
                                        onAgregarAnterior(sugerencia.id)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                val yaAgregada = sugerencia.id in visitasDeHoyIds
                                Text(
                                    text  = if (yaAgregada) "✓" else stringResource(R.string.registro_sheet_seleccionar),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (yaAgregada) Sage else Accent,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // ── Campo NOMBRE ──────────────────────────────────────────────────
            Text(
                text     = stringResource(R.string.registro_sheet_label_nombre),
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            OutlinedTextField(
                value         = nombre,
                onValueChange = {
                    nombre       = it
                    showDropdown = it.length >= 2 || apellido.length >= 2
                },
                placeholder   = {
                    Text(
                        text  = stringResource(R.string.registro_sheet_hint_nombre),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction      = ImeAction.Next,
                ),
                singleLine = true,
                shape      = RoundedCornerShape(12.dp),
                colors     = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Background,
                    focusedContainerColor   = Background,
                    unfocusedBorderColor    = Muted.copy(alpha = 0.4f),
                    focusedBorderColor      = Accent,
                    cursorColor             = Accent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            // ── Campo APELLIDO ────────────────────────────────────────────────
            Text(
                text     = stringResource(R.string.registro_sheet_label_apellido),
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            OutlinedTextField(
                value         = apellido,
                onValueChange = {
                    apellido     = it
                    showDropdown = it.length >= 2 || nombre.length >= 2
                },
                placeholder   = {
                    Text(
                        text  = stringResource(R.string.registro_sheet_hint_apellido),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction      = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                singleLine = true,
                shape      = RoundedCornerShape(12.dp),
                colors     = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Background,
                    focusedContainerColor   = Background,
                    unfocusedBorderColor    = Muted.copy(alpha = 0.4f),
                    focusedBorderColor      = Accent,
                    cursorColor             = Accent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            // ── Nota punteada ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Muted.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "•", style = MaterialTheme.typography.bodyMedium, color = Sage)
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = stringResource(R.string.registro_sheet_nota_presente),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Botones ───────────────────────────────────────────────────────
            NeuButtonPrimary(
                text     = stringResource(R.string.registro_sheet_btn_agregar),
                onClick  = {
                    if (nombre.isNotBlank() || apellido.isNotBlank()) {
                        onAgregar(nombre, apellido)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            NeuButtonSecondary(
                text     = stringResource(R.string.registro_confirm_cancelar),
                onClick  = onCancelar,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun DashedDivider(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color       = Muted.copy(alpha = 0.4f),
            start       = androidx.compose.ui.geometry.Offset(0f, 0f),
            end         = androidx.compose.ui.geometry.Offset(size.width, 0f),
            strokeWidth = 2f,
            pathEffect  = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
        )
    }
}

private val DIAS_ES       = arrayOf("Dom","Lun","Mar","Mié","Jue","Vie","Sáb")
private val MESES_ES      = arrayOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
private val MESES_ES_CORTO = MESES_ES

private fun formatFechaReunion(fecha: LocalDate): String {
    val hoy       = LocalDate.now()
    val diaSemana = DIAS_ES[fecha.dayOfWeek.value % 7]
    val mes       = MESES_ES[fecha.monthValue - 1]
    val base      = "$diaSemana ${fecha.dayOfMonth} $mes ${fecha.year}"
    return if (fecha == hoy) "Hoy, $base" else base
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewState = RegistroUiState(
    fecha = LocalDate.of(2026, 3, 4),
    miembros = listOf(
        MiembroAsistencia("m1", "Ana Castillo",   "AC", EstadoAsistencia.PRESENTE),
        MiembroAsistencia("m2", "Jose Rodriguez", "JR", EstadoAsistencia.PRESENTE),
        MiembroAsistencia("m3", "Lucia Martinez", "LM", EstadoAsistencia.AUSENTE),
        MiembroAsistencia("m4", "Carlos Perez",   "CP", null),
        MiembroAsistencia("m5", "Rosa Torres",    "RT", EstadoAsistencia.JUSTIFICADO),
    ),
    visitasAnteriores = listOf(
        VisitaAnterior("v1", "Juan Lopez",    LocalDate.of(2026, 2, 26), visitasCount = 5),
        VisitaAnterior("v2", "Maria Solano",  LocalDate.of(2026, 2, 19), visitasCount = 3),
        VisitaAnterior("v3", "Roberto Gomez", LocalDate.of(2026, 2, 12), visitasCount = 1),
    ),
    visitasDeHoy = listOf(
        VisitaHoy("v1", "Juan Lopez", esNueva = false, EstadoAsistencia.PRESENTE),
    ),
    visitasColapsadas = true,
)

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "Paso1 — visitas colapsadas")
@Composable
private fun Paso1Preview() {
    GpLeaderTheme {
        RegistroPaso1Content(
            uiState                  = previewState,
            onNavigateBack           = {},
            onFechaChange            = {},
            onNoHuboReunion          = {},
            onAsistenciaChange       = { _, _ -> },
            onSelTodos               = {},
            onAgregarVisitaAnterior          = {},
            onEliminarVisitaAnterior         = {},
            onVisitaAnteriorAsistenciaChange = { _, _ -> },
            onAgregarNuevaVisita             = { _, _ -> },
            onVisitaAsistenciaChange         = { _, _ -> },
            onToggleVisitas          = {},
            onContinuar              = {},
            onDismissConfirmAusentes = {},
            onDismissConfirmNoHubo   = {},
            onConfirmNoHubo          = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "Paso1 — visitas expandidas")
@Composable
private fun Paso1ExpandidasPreview() {
    GpLeaderTheme {
        RegistroPaso1Content(
            uiState                  = previewState.copy(visitasColapsadas = false),
            onNavigateBack           = {},
            onFechaChange            = {},
            onNoHuboReunion          = {},
            onAsistenciaChange       = { _, _ -> },
            onSelTodos               = {},
            onAgregarVisitaAnterior          = {},
            onEliminarVisitaAnterior         = {},
            onVisitaAnteriorAsistenciaChange = { _, _ -> },
            onAgregarNuevaVisita             = { _, _ -> },
            onVisitaAsistenciaChange         = { _, _ -> },
            onToggleVisitas          = {},
            onContinuar              = {},
            onDismissConfirmAusentes = {},
            onDismissConfirmNoHubo   = {},
            onConfirmNoHubo          = {},
        )
    }
}

