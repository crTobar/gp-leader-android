package com.gpleader.app.feature.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.feature.actividades.CrearActividadTipoViewModel
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInsetSm
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun AgregarActividadScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    AgregarActividadContent(
        onNavigateBack = onNavigateBack,
        onAgregar = { nombre, markerType, isMemberAccessible, startDate, endDate ->
            viewModel.onAgregarActividadExtra(nombre, markerType, isMemberAccessible, startDate, endDate)
            onNavigateBack()
        },
    )
}

// ── Entry point standalone (pantalla de actividades) ─────────────────────────
// Usa CrearActividadTipoViewModel para que el coroutine no se cancele al hacer
// popBackStack() — navega de vuelta solo cuando savedOk = true.

@Composable
fun AgregarActividadStandaloneScreen(
    onNavigateBack: () -> Unit,
    viewModel: CrearActividadTipoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedOk) {
        if (uiState.savedOk) onNavigateBack()
    }

    AgregarActividadContent(
        onNavigateBack = onNavigateBack,
        onAgregar = { nombre, markerType, isMemberAccessible, startDate, endDate ->
            viewModel.onNombreChange(nombre)
            viewModel.onMarkerTypeChange(markerType)
            viewModel.onLevelChange("my_group")
            viewModel.onMemberAccessibleChange(isMemberAccessible)
            viewModel.onStartDateChange(startDate)
            viewModel.onEndDateChange(endDate)
            viewModel.onGuardar()
        },
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun AgregarActividadContent(
    onNavigateBack: () -> Unit,
    onAgregar: (nombre: String, markerType: String, isMemberAccessible: Boolean, startDate: LocalDate?, endDate: LocalDate?) -> Unit,
) {
    var nombre              by remember { mutableStateOf("") }
    var tipoMarcador        by remember { mutableStateOf(TipoMarcador.CONTADOR) }
    var visibleParaMiembros by remember { mutableStateOf(false) }
    var startDate           by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var endDate             by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        AgregarActividadTopBar(onNavigateBack = onNavigateBack)

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                // ── Nombre ────────────────────────────────────────────────────
                item {
                    SeccionLabel(stringResource(R.string.agregar_actividad_label_nombre))
                    Spacer(Modifier.height(8.dp))
                    NombreField(
                        value         = nombre,
                        onValueChange = { nombre = it },
                        placeholder   = stringResource(R.string.agregar_actividad_hint_nombre),
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }

                // ── Tipo de marcador + vista previa ───────────────────────────
                item {
                    NeuCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SeccionLabel(stringResource(R.string.agregar_actividad_label_tipo))
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier              = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                TipoMarcadorChip(
                                    texto   = stringResource(R.string.tipo_marcador_contador),
                                    activo  = tipoMarcador == TipoMarcador.CONTADOR,
                                    onClick = { tipoMarcador = TipoMarcador.CONTADOR },
                                )
                                TipoMarcadorChip(
                                    texto   = stringResource(R.string.tipo_marcador_checkbox),
                                    activo  = tipoMarcador == TipoMarcador.CHECKBOX,
                                    onClick = { tipoMarcador = TipoMarcador.CHECKBOX },
                                )
                                TipoMarcadorChip(
                                    texto   = stringResource(R.string.tipo_marcador_monetario),
                                    activo  = tipoMarcador == TipoMarcador.MONETARIO,
                                    onClick = { tipoMarcador = TipoMarcador.MONETARIO },
                                )
                                TipoMarcadorChip(
                                    texto   = stringResource(R.string.tipo_marcador_participantes),
                                    activo  = tipoMarcador == TipoMarcador.PARTICIPANTES,
                                    onClick = { tipoMarcador = TipoMarcador.PARTICIPANTES },
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text  = when (tipoMarcador) {
                                    TipoMarcador.CONTADOR      -> stringResource(R.string.tipo_marcador_contador_desc)
                                    TipoMarcador.CHECKBOX      -> stringResource(R.string.tipo_marcador_checkbox_desc)
                                    TipoMarcador.MONETARIO     -> stringResource(R.string.tipo_marcador_monetario_desc)
                                    TipoMarcador.PARTICIPANTES -> stringResource(R.string.tipo_marcador_participantes_desc)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                            )

                            HorizontalDivider(
                                color    = BackgroundDeep,
                                modifier = Modifier.padding(vertical = 14.dp),
                            )

                            // Vista previa según tipo
                            when (tipoMarcador) {
                                TipoMarcador.CONTADOR, TipoMarcador.PARTICIPANTES -> {
                                    ContadorPreviewRow(
                                        nombre = nombre.ifBlank { stringResource(R.string.agregar_actividad_hint_nombre) },
                                    )
                                }
                                TipoMarcador.CHECKBOX -> {
                                    CheckboxPreview(
                                        nombreActividad = nombre.ifBlank {
                                            stringResource(R.string.agregar_actividad_hint_nombre)
                                        },
                                    )
                                }
                                TipoMarcador.MONETARIO -> {
                                    MonetarioPreviewRow(
                                        nombre = nombre.ifBlank { stringResource(R.string.agregar_actividad_hint_nombre) },
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(10.dp)) }

                // ── Visible para miembros ─────────────────────────────────────
                item {
                    VisibleParaMiembrosToggle(
                        activo   = visibleParaMiembros,
                        onToggle = { visibleParaMiembros = !visibleParaMiembros },
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }

                // ── Período ───────────────────────────────────────────────────
                item {
                    SeccionLabel(stringResource(R.string.agregar_actividad_label_periodo))
                    Spacer(Modifier.height(8.dp))
                    NeuCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            FechaRow(
                                label         = stringResource(R.string.agregar_actividad_fecha_inicio),
                                fecha         = startDate,
                                onFechaChange = { startDate = it ?: LocalDate.now() },
                                puedeEliminar = false,
                                modifier      = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            )
                            HorizontalDivider(
                                color    = BackgroundDeep,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            FechaRow(
                                label         = stringResource(R.string.agregar_actividad_fecha_fin),
                                fecha         = endDate,
                                onFechaChange = { endDate = it },
                                puedeEliminar = true,
                                modifier      = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }

        // ── Botones fijos abajo ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Background)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NeuButtonPrimary(
                text    = stringResource(R.string.agregar_actividad_btn_agregar),
                onClick = {
                    if (nombre.isNotBlank()) {
                        val markerType = when (tipoMarcador) {
                            TipoMarcador.CONTADOR      -> "counter"
                            TipoMarcador.CHECKBOX      -> "checkbox"
                            TipoMarcador.MONETARIO     -> "monetary"
                            TipoMarcador.PARTICIPANTES -> "participants"
                        }
                        onAgregar(nombre, markerType, visibleParaMiembros, startDate, endDate)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            NeuButtonSecondary(
                text     = stringResource(R.string.registro_confirm_cancelar),
                onClick  = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Label de sección ──────────────────────────────────────────────────────────

@Composable
private fun SeccionLabel(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelSmall,
        color = Muted,
    )
}

// ── Campo nombre neumórfico ───────────────────────────────────────────────────

@Composable
private fun NombreField(
    value:         String,
    onValueChange: (String) -> Unit,
    placeholder:   String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .neuInsetSm(cornerRadius = 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        BasicTextField(
            value         = value,
            onValueChange = onValueChange,
            singleLine    = true,
            textStyle     = MaterialTheme.typography.bodyLarge.copy(color = Ink),
            modifier      = Modifier.weight(1f),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text  = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Muted,
                        )
                    }
                    inner()
                }
            },
        )
    }
}

// ── Fila de fecha ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FechaRow(
    label:         String,
    fecha:         LocalDate?,
    onFechaChange: (LocalDate?) -> Unit,
    puedeEliminar: Boolean = true,
    modifier:      Modifier = Modifier,
) {
    val fmt           = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")) }
    var mostrarDialog by remember { mutableStateOf(false) }
    val initialMillis = remember(fecha) {
        fecha?.atStartOfDay(ZoneId.of("UTC"))?.toInstant()?.toEpochMilli()
    }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clickable { mostrarDialog = true },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = Icons.Filled.DateRange,
            contentDescription = null,
            tint               = Muted,
            modifier           = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = Mid,
            modifier = Modifier.width(90.dp),
        )
        Text(
            text     = fecha?.format(fmt) ?: "—",
            style    = MaterialTheme.typography.bodyMedium,
            color    = if (fecha != null) Ink else Muted,
            modifier = Modifier.weight(1f),
        )
        if (puedeEliminar && fecha != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundDeep)
                    .clickable { onFechaChange(null) },
            ) {
                Icon(
                    imageVector        = Icons.Filled.Close,
                    contentDescription = null,
                    tint               = Muted,
                    modifier           = Modifier.size(14.dp),
                )
            }
        } else {
            Spacer(Modifier.size(28.dp))
        }
    }

    if (mostrarDialog) {
        DatePickerDialog(
            onDismissRequest = { mostrarDialog = false },
            confirmButton    = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onFechaChange(
                            Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        )
                    }
                    mostrarDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialog = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

// ── Vista previa del checkbox ─────────────────────────────────────────────────

@Composable
private fun CheckboxPreview(
    nombreActividad: String,
) {
    var marcado by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        SeccionLabel(stringResource(R.string.tipo_marcador_checkbox_preview_label))
        Spacer(Modifier.height(8.dp))
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BackgroundDeep)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Background),
            ) {
                Text(text = "☆", style = MaterialTheme.typography.labelSmall, color = Mid)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text       = nombreActividad,
                style      = MaterialTheme.typography.bodyLarge,
                color      = if (nombreActividad == stringResource(R.string.agregar_actividad_hint_nombre)) Muted else Ink,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.weight(1f),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (marcado) Modifier.background(Sage)
                        else Modifier
                            .background(Background)
                            .border(1.5.dp, Muted.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    )
                    .clickable { marcado = !marcado },
            ) {
                if (marcado) {
                    Icon(
                        imageVector        = Icons.Default.Check,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(18.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text  = stringResource(R.string.tipo_marcador_checkbox_preview_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun AgregarActividadTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
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
            text      = stringResource(R.string.agregar_actividad_titulo),
            style     = MaterialTheme.typography.titleLarge,
            color     = Ink,
            textAlign = TextAlign.Center,
            modifier  = Modifier.weight(1f),
        )
        Box(modifier = Modifier.size(40.dp))
    }
}

// ── Toggle visible para miembros ──────────────────────────────────────────────

@Composable
private fun VisibleParaMiembrosToggle(
    activo:   Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BackgroundDeep)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .then(
                    if (activo) Modifier.background(Accent)
                    else Modifier
                        .background(Background)
                        .border(1.5.dp, Muted.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ),
        ) {
            if (activo) {
                Icon(
                    imageVector        = Icons.Default.Check,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(14.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text       = stringResource(R.string.agregar_actividad_visible_miembros_titulo),
                style      = MaterialTheme.typography.bodyLarge,
                color      = Ink,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text  = stringResource(R.string.agregar_actividad_visible_miembros_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }
    }
}

// ── Vista previa de marcador contador / participantes ─────────────────────────

@Composable
private fun ContadorPreviewRow(nombre: String) {
    val placeholder = stringResource(R.string.agregar_actividad_hint_nombre)
    Column {
        SeccionLabel(stringResource(R.string.tipo_marcador_checkbox_preview_label))
        Spacer(Modifier.height(8.dp))
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BackgroundDeep)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Accent.copy(alpha = 0.5f)),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text       = nombre,
                style      = MaterialTheme.typography.bodyLarge,
                color      = if (nombre == placeholder) Muted else Ink,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(28.dp)
                        .neuElevatedSm(cornerRadius = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Background),
                ) { Text("−", style = MaterialTheme.typography.bodyMedium, color = Muted) }
                Text(
                    text      = "—",
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = Muted,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier
                        .padding(horizontal = 10.dp)
                        .width(32.dp),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(28.dp)
                        .neuElevatedSm(cornerRadius = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Background),
                ) { Text("+", style = MaterialTheme.typography.bodyMedium, color = Accent) }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text  = stringResource(R.string.tipo_marcador_preview_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
    }
}

// ── Vista previa de marcador monetario ────────────────────────────────────────

@Composable
private fun MonetarioPreviewRow(nombre: String) {
    val placeholder = stringResource(R.string.agregar_actividad_hint_nombre)
    Column {
        SeccionLabel(stringResource(R.string.tipo_marcador_checkbox_preview_label))
        Spacer(Modifier.height(8.dp))
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BackgroundDeep)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Accent.copy(alpha = 0.5f)),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text       = nombre,
                style      = MaterialTheme.typography.bodyLarge,
                color      = if (nombre == placeholder) Muted else Ink,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.weight(1f),
            )
            Text(
                text       = "₡ —",
                style      = MaterialTheme.typography.titleMedium,
                color      = Gold,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text  = stringResource(R.string.tipo_marcador_preview_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
    }
}

// ── Chip de tipo de marcador ──────────────────────────────────────────────────

@Composable
private fun TipoMarcadorChip(texto: String, activo: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .then(
                if (activo) Modifier.background(Accent, RoundedCornerShape(8.dp))
                else Modifier.neuElevatedSm(cornerRadius = 8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .then(if (!activo) Modifier.background(Background) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(
            text       = texto,
            style      = MaterialTheme.typography.labelSmall,
            color      = if (activo) Color.White else Ink,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "AgregarActividad")
@Composable
private fun AgregarActividadPreview() {
    GpLeaderTheme {
        AgregarActividadContent(
            onNavigateBack = {},
            onAgregar      = { _, _, _, _, _ -> },
        )
    }
}
