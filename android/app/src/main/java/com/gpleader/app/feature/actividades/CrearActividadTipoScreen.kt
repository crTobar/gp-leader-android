package com.gpleader.app.feature.actividades

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuInsetSm
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CrearActividadTipoScreen(
    onNavigateBack: () -> Unit,
    viewModel: CrearActividadTipoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedOk) {
        if (uiState.savedOk) onNavigateBack()
    }

    CrearActividadTipoContent(
        uiState                  = uiState,
        onNavigateBack           = onNavigateBack,
        onNombreChange           = viewModel::onNombreChange,
        onLevelChange            = viewModel::onLevelChange,
        onMarkerTypeChange       = viewModel::onMarkerTypeChange,
        onFrecuenciaChange       = viewModel::onFrecuenciaChange,
        onUnitLabelChange        = viewModel::onUnitLabelChange,
        onMemberAccessibleChange = viewModel::onMemberAccessibleChange,
        onStartDateChange        = viewModel::onStartDateChange,
        onEndDateChange          = viewModel::onEndDateChange,
        onGuardar                = viewModel::onGuardar,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrearActividadTipoContent(
    uiState: CrearActividadTipoUiState,
    onNavigateBack: () -> Unit = {},
    onNombreChange: (String) -> Unit = {},
    onLevelChange: (String) -> Unit = {},
    onMarkerTypeChange: (String) -> Unit = {},
    onFrecuenciaChange: (String) -> Unit = {},
    onUnitLabelChange: (String) -> Unit = {},
    onMemberAccessibleChange: (Boolean) -> Unit = {},
    onStartDateChange: (LocalDate?) -> Unit = {},
    onEndDateChange: (LocalDate?) -> Unit = {},
    onGuardar: () -> Unit = {},
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }

    val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // ── TopBar ────────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink)
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text       = "Nueva Actividad",
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )
        }

        LazyColumn(
            modifier        = Modifier.weight(1f),
            contentPadding  = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // ── Nombre ────────────────────────────────────────────────────────
            item {
                SeccionLabel("NOMBRE")
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neuInsetSm()
                        .background(Background, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicTextField(
                        value         = uiState.nombre,
                        onValueChange = onNombreChange,
                        textStyle     = MaterialTheme.typography.bodyLarge.copy(color = Ink),
                        cursorBrush   = SolidColor(Accent),
                        modifier      = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (uiState.nombre.isEmpty()) {
                                Text("Ej: Semana de Oración", style = MaterialTheme.typography.bodyLarge, color = Muted)
                            }
                            inner()
                        },
                    )
                }
                if (uiState.error != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(uiState.error, style = MaterialTheme.typography.labelSmall, color = Blush)
                }
            }

            // ── Nivel ─────────────────────────────────────────────────────────
            item {
                SeccionLabel("NIVEL")
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("my_group" to "Mi GP", "pastor" to "Pastor", "union" to "Unión").forEach { (valor, label) ->
                        SeleccionChip(
                            label    = label,
                            selected = uiState.level == valor,
                            color    = when (valor) { "union" -> Gold; "pastor" -> Ink; else -> Accent },
                            onClick  = { onLevelChange(valor) },
                        )
                    }
                }
            }

            // ── Tipo de marcador ──────────────────────────────────────────────
            item {
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        SeccionLabel("TIPO DE MARCADOR")
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier              = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                "counter"  to "Contador",
                                "realizado" to "Realizado",
                                "monetary" to "Monetario",
                            ).forEach { (valor, label) ->
                                SeleccionChip(
                                    label    = label,
                                    selected = uiState.markerType == valor,
                                    onClick  = { onMarkerTypeChange(valor) },
                                )
                            }
                        }

                        if (uiState.markerType == "counter") {
                            HorizontalDivider(color = BackgroundDeep, modifier = Modifier.padding(vertical = 10.dp))
                            SeccionLabel("UNIDAD")
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neuInsetSm()
                                    .background(Background, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                BasicTextField(
                                    value         = uiState.unitLabel,
                                    onValueChange = onUnitLabelChange,
                                    textStyle     = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                                    cursorBrush   = SolidColor(Accent),
                                    modifier      = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        if (uiState.unitLabel.isEmpty()) {
                                            Text("Ej: personas, libros…", style = MaterialTheme.typography.bodyMedium, color = Muted)
                                        }
                                        inner()
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // ── Opciones de tarea ─────────────────────────────────────────────
            item {
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Marcar diario
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .clickable { onFrecuenciaChange(if (uiState.frecuencia == "diaria") "semanal" else "diaria") },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Marcar diario", style = MaterialTheme.typography.bodyLarge, color = Ink)
                                Text(
                                    "Cada miembro puede marcarla como hecha cada día",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Mid,
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            ToggleBox(checked = uiState.frecuencia == "diaria")
                        }

                        HorizontalDivider(color = BackgroundDeep, modifier = Modifier.padding(vertical = 10.dp))

                        // Visible para miembros
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .clickable { onMemberAccessibleChange(!uiState.isMemberAccessible) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Visible para miembros", style = MaterialTheme.typography.bodyLarge, color = Ink)
                                Text(
                                    "Los miembros del grupo pueden ver y marcar esta tarea",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Mid,
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            ToggleBox(checked = uiState.isMemberAccessible)
                        }
                    }
                }
            }

            // ── Período ───────────────────────────────────────────────────────
            item {
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        SeccionLabel("PERÍODO")
                        Spacer(Modifier.height(8.dp))

                        FechaRow(
                            label          = "Inicio",
                            fecha          = uiState.startDate,
                            puedeEliminar  = false,
                            onPicker       = { showStartPicker = true },
                            onEliminar     = {},
                            fmt            = fmt,
                        )
                        Spacer(Modifier.height(8.dp))
                        FechaRow(
                            label          = "Vencimiento",
                            fecha          = uiState.endDate,
                            puedeEliminar  = true,
                            onPicker       = { showEndPicker = true },
                            onEliminar     = { onEndDateChange(null) },
                            fmt            = fmt,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        // ── Botones fijos ─────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            if (uiState.isSaving) {
                Box(Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(28.dp))
                }
            } else {
                NeuButtonPrimary(
                    text     = "Guardar actividad",
                    onClick  = onGuardar,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(8.dp))
            NeuButtonSecondary(
                text     = "Cancelar",
                onClick  = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // ── DatePicker dialogs ────────────────────────────────────────────────────
    if (showStartPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = uiState.startDate?.let { localDateToMillis(it) }
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onStartDateChange(millisToLocalDate(it)) }
                    showStartPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Cancelar") }
            },
        ) { DatePicker(state) }
    }

    if (showEndPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = uiState.endDate?.let { localDateToMillis(it) }
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onEndDateChange(millisToLocalDate(it)) }
                    showEndPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Cancelar") }
            },
        ) { DatePicker(state) }
    }
}

// ── Helpers UI ────────────────────────────────────────────────────────────────

@Composable
private fun SeccionLabel(texto: String) {
    Text(texto, style = MaterialTheme.typography.labelSmall, color = Muted)
}

@Composable
private fun SeleccionChip(
    label: String,
    selected: Boolean,
    color: Color = Accent,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) color else BackgroundDeep)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = if (selected) Color.White else Mid,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ToggleBox(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) Sage else BackgroundDeep),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun FechaRow(
    label: String,
    fecha: LocalDate?,
    puedeEliminar: Boolean,
    onPicker: () -> Unit,
    onEliminar: () -> Unit,
    fmt: DateTimeFormatter,
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Mid, modifier = Modifier.width(90.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .neuInsetSm()
                .background(Background, RoundedCornerShape(8.dp))
                .clickable(onClick = onPicker)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text  = fecha?.format(fmt) ?: "Sin fecha",
                style = MaterialTheme.typography.bodyMedium,
                color = if (fecha != null) Ink else Muted,
            )
        }
        if (puedeEliminar && fecha != null) {
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onEliminar) {
                Text("✕", color = Blush)
            }
        }
    }
}

// ── Date conversions ──────────────────────────────────────────────────────────

private fun localDateToMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

private fun millisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun CrearActividadPreview() {
    GpLeaderTheme {
        CrearActividadTipoContent(uiState = CrearActividadTipoUiState())
    }
}
