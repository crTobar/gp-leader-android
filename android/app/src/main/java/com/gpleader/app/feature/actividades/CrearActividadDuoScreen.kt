package com.gpleader.app.feature.actividades

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInset
import com.gpleader.app.core.ui.theme.neuInsetSm
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FMT_FECHA_DUO = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearActividadDuoScreen(
    onNavigateBack: () -> Unit,
    viewModel: CrearActividadDuoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.savedOk) {
        if (uiState.savedOk) onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink, modifier = Modifier.size(20.dp))
            }
            Text(
                text      = "Nueva Actividad del Dúo",
                style     = MaterialTheme.typography.titleLarge,
                color     = Ink,
                textAlign = TextAlign.Center,
                modifier  = Modifier.weight(1f),
            )
            Box(modifier = Modifier.size(40.dp))
        }

        Column(
            modifier            = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Nombre ────────────────────────────────────────────────────────
            NeuTextField(
                value         = uiState.nombre,
                onValueChange = viewModel::onNombreChange,
                label         = "Nombre de la actividad",
            )

            // ── Tipo de marcador ──────────────────────────────────────────────
            Text("Tipo", style = MaterialTheme.typography.bodyMedium, color = Mid, fontWeight = FontWeight.SemiBold)
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "counter"  to "Contador",
                    "checkbox" to "Verificación",
                    "monetary" to "Monetario",
                ).forEach { (value, label) ->
                    val selected = uiState.markerType == value && !uiState.esDiario
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .alpha(if (uiState.esDiario) 0.4f else 1f)
                            .then(
                                if (selected) Modifier.neuGlow(cornerRadius = 20.dp)
                                else Modifier.neuElevatedSm(cornerRadius = 20.dp)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) Accent else Background)
                            .then(
                                if (!uiState.esDiario) Modifier.clickable { viewModel.onMarkerTypeChange(value) }
                                else Modifier
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text       = label,
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = if (selected) Color.White else Ink,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Campo unidad
            if (!uiState.esDiario) {
                when (uiState.markerType) {
                    "counter" -> NeuTextField(
                        value         = uiState.unitLabel,
                        onValueChange = viewModel::onUnitLabelChange,
                        label         = "Unidad (ej: visitas, oraciones)",
                    )
                    "monetary" -> Text(
                        text  = "Registra montos en colones (₡)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            }

            // ── Toggle marcador diario ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuElevated(cornerRadius = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Background)
                    .clickable { viewModel.onEsDiarioChange(!uiState.esDiario) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Marcador diario", style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
                    Text("Compartido · ambos miembros pueden marcar", style = MaterialTheme.typography.bodyMedium, color = Muted)
                }
                Switch(
                    checked         = uiState.esDiario,
                    onCheckedChange = viewModel::onEsDiarioChange,
                    colors          = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Accent),
                )
            }

            // ── Preview tarjeta diaria ────────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.esDiario,
                enter   = expandVertically(),
                exit    = shrinkVertically(),
            ) {
                DailyMarkerPreview(nombre = uiState.nombre.ifBlank { "Vista previa" })
            }

            // ── Período ───────────────────────────────────────────────────────
            NeuCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("PERÍODO", style = MaterialTheme.typography.labelSmall, color = Muted)
                    Spacer(Modifier.height(10.dp))
                    PeriodoFechaRow(
                        label         = "Inicio",
                        fecha         = uiState.startDate,
                        puedeEliminar = false,
                        onPicker      = { showStartPicker = true },
                        onEliminar    = {},
                    )
                    Spacer(Modifier.height(8.dp))
                    PeriodoFechaRow(
                        label         = "Vencimiento",
                        fecha         = uiState.endDate,
                        puedeEliminar = true,
                        onPicker      = { showEndPicker = true },
                        onEliminar    = { viewModel.onEndDateChange(null) },
                    )
                }
            }

            uiState.error?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Blush)
            }

            Spacer(Modifier.height(8.dp))
        }

        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            NeuButtonPrimary(
                text     = if (uiState.isGuardando) "Guardando…" else "Crear actividad",
                onClick  = viewModel::onGuardar,
                modifier = Modifier.fillMaxWidth(),
                enabled  = uiState.nombre.isNotBlank() && !uiState.isGuardando,
            )
        }
    }

    // ── DatePicker dialogs ────────────────────────────────────────────────────
    if (showStartPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = uiState.startDate?.let { duoLocalDateToMillis(it) }
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { viewModel.onStartDateChange(duoMillisToLocalDate(it)) }
                    showStartPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state) }
    }

    if (showEndPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = uiState.endDate?.let { duoLocalDateToMillis(it) }
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { viewModel.onEndDateChange(duoMillisToLocalDate(it)) }
                    showEndPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state) }
    }
}

@Composable
private fun PeriodoFechaRow(
    label:         String,
    fecha:         LocalDate?,
    puedeEliminar: Boolean,
    onPicker:      () -> Unit,
    onEliminar:    () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                text  = fecha?.format(FMT_FECHA_DUO) ?: "Sin fecha",
                style = MaterialTheme.typography.bodyMedium,
                color = if (fecha != null) Ink else Muted,
            )
        }
        if (puedeEliminar && fecha != null) {
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onEliminar) { Text("✕", color = Blush) }
        }
    }
}

@Composable
private fun DailyMarkerPreview(nombre: String) {
    val dias = listOf("D", "L", "M", "M", "J", "V", "S")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .neuInset(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(nombre, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Accent.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text("DIARIO", style = MaterialTheme.typography.labelSmall, color = Accent, fontWeight = FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            dias.forEach { dia ->
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Background)) {
                    Text(dia, style = MaterialTheme.typography.labelSmall, color = Muted, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text("Cualquier miembro del dúo puede marcar cada día", style = MaterialTheme.typography.labelSmall, color = Muted)
    }
}

private fun duoLocalDateToMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

private fun duoMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
