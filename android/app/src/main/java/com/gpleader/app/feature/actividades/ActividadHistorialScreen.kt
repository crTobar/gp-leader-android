package com.gpleader.app.feature.actividades

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.RegistroSemanalData
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.OnResumeEffect
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun ActividadHistorialScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActividadHistorialViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }
    ActividadHistorialContent(
        uiState           = uiState,
        onNavigateBack    = onNavigateBack,
        onEditarClick     = viewModel::onEditarClick,
        onCancelarEdicion = viewModel::onCancelarEdicion,
        onGuardarEdicion  = viewModel::onGuardarEdicion,
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun ActividadHistorialContent(
    uiState:          ActividadHistorialUiState,
    onNavigateBack:   () -> Unit,
    onEditarClick:    (String) -> Unit,
    onCancelarEdicion: () -> Unit,
    onGuardarEdicion: (String, Int?, Double?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        // Top bar
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
                text      = uiState.actividadNombre.ifBlank { "Historial" },
                style     = MaterialTheme.typography.titleLarge,
                color     = Ink,
                textAlign = TextAlign.Center,
                modifier  = Modifier.weight(1f),
            )
            Box(modifier = Modifier.size(40.dp))
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
            }
            return@Column
        }

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Resumen ───────────────────────────────────────────────────────
            item {
                ResumenCard(uiState = uiState)
            }

            // ── Registros semanales ───────────────────────────────────────────
            if (uiState.registros.isNotEmpty()) {
                item {
                    NeuCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            // Header de la lista
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BackgroundDeep)
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text  = "REGISTROS SEMANALES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Muted,
                                )
                                Text(
                                    text  = "${uiState.registros.size} semanas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Muted,
                                )
                            }

                            uiState.registros.forEachIndexed { idx, registro ->
                                RegistroRow(
                                    registro      = registro,
                                    markerType    = uiState.markerType,
                                    unidad        = uiState.actividadUnidad,
                                    editando      = uiState.editandoId == registro.recordId,
                                    isSaving      = uiState.isSaving,
                                    onEditarClick = { onEditarClick(registro.recordId) },
                                    onCancelar    = onCancelarEdicion,
                                    onGuardar     = { cantidad, monto ->
                                        onGuardarEdicion(registro.recordId, cantidad, monto)
                                    },
                                    modifier      = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                )
                                if (idx < uiState.registros.lastIndex) {
                                    HorizontalDivider(
                                        color    = BackgroundDeep,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = "Sin registros",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )
                    }
                }
            }
        }
    }
}

// ── Card de resumen ───────────────────────────────────────────────────────────

@Composable
private fun ResumenCard(uiState: ActividadHistorialUiState) {
    val count = uiState.registros.size

    val promedio = when {
        count == 0                     -> "—"
        uiState.markerType == "monetary" -> "₡${(uiState.montoTotal / count).toLong()}"
        else                           -> "${if (count > 0) uiState.totalCantidad / count else 0}"
    }
    val (maxLabel, maxValor) = when (uiState.markerType) {
        "monetary" -> "MÁXIMO" to "₡${(uiState.registros.maxOfOrNull { (it.monto ?: 0.0) + it.aportesMiembros } ?: 0.0).toLong()}"
        "checkbox" -> "REALIZADAS" to "${uiState.registros.count { (it.cantidad ?: 0) > 0 }}"
        else       -> "MÁXIMO" to "${uiState.registros.maxOfOrNull { (it.cantidad ?: 0) + it.aportesMiembros } ?: 0}"
    }

    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text  = "RESUMEN DEL PERIODO",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )

            Spacer(Modifier.height(14.dp))

            // Número grande + unidad
            when (uiState.markerType) {
                "monetary" -> {
                    Text(
                        text  = "₡${uiState.montoTotal.toLong()}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                    )
                }
                "checkbox" -> {
                    val realizadas = uiState.registros.count { (it.cantidad ?: 0) > 0 }
                    Row(
                        verticalAlignment     = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text  = "$realizadas",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Ink,
                        )
                        Text(
                            text     = "semanas realizadas",
                            style    = MaterialTheme.typography.bodyLarge,
                            color    = Mid,
                            modifier = Modifier.padding(bottom = 3.dp),
                        )
                    }
                }
                else -> {
                    Row(
                        verticalAlignment     = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text  = "${uiState.totalCantidad}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Ink,
                        )
                        Text(
                            text     = uiState.actividadUnidad,
                            style    = MaterialTheme.typography.bodyLarge,
                            color    = Mid,
                            modifier = Modifier.padding(bottom = 3.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = BackgroundDeep)
            Spacer(Modifier.height(14.dp))

            // Stats row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                StatItem(label = "REGISTROS", value = "$count")
                StatDivider()
                StatItem(label = "PROMEDIO",  value = promedio)
                StatDivider()
                StatItem(label = maxLabel,    value = maxValor)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleLarge,
            color      = Ink,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .width(1.dp)
            .background(BackgroundDeep),
    )
}

// ── Fila de registro semanal ──────────────────────────────────────────────────

@Composable
private fun RegistroRow(
    registro:      RegistroSemanalData,
    markerType:    String,
    unidad:        String,
    editando:      Boolean,
    isSaving:      Boolean,
    onEditarClick: () -> Unit,
    onCancelar:    () -> Unit,
    onGuardar:     (Int?, Double?) -> Unit,
    modifier:      Modifier = Modifier,
) {
    val dayFmt   = DateTimeFormatter.ofPattern("d",        Locale("es"))
    val monthFmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale("es"))

    var editCantidad by remember(editando) { mutableStateOf(registro.cantidad?.toString() ?: "") }
    var editMonto    by remember(editando) { mutableStateOf(
        registro.monto?.let {
            if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
        } ?: ""
    ) }

    val combinado = when (markerType) {
        "monetary" -> (registro.monto ?: 0.0) + registro.aportesMiembros
        else       -> ((registro.cantidad ?: 0) + registro.aportesMiembros).toDouble()
    }
    val esNonZero = when (markerType) {
        "checkbox" -> (registro.cantidad ?: 0) > 0
        else       -> combinado > 0.0
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (!editando) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Bloque de fecha
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier            = Modifier.width(56.dp),
                ) {
                    Text(
                        text       = registro.meetingDate.format(dayFmt),
                        style      = MaterialTheme.typography.titleLarge,
                        color      = Ink,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text  = registro.meetingDate.format(monthFmt)
                            .lowercase()
                            .replace(".", ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }

                Spacer(Modifier.weight(1f))

                // Chip de valor + nota de miembros
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    markerType == "checkbox" && esNonZero -> Sage.copy(alpha = 0.12f)
                                    esNonZero                             -> Accent.copy(alpha = 0.1f)
                                    else                                  -> BackgroundDeep
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text       = formatRegistroValor(registro, markerType, unidad, combinado),
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = when {
                                markerType == "checkbox" && esNonZero -> Sage
                                esNonZero                             -> Accent
                                else                                  -> Muted
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (registro.aportesMiembros > 0) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text  = "+${registro.aportesMiembros} miembros",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }

                // Botón editar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundDeep)
                        .clickable(onClick = onEditarClick),
                ) {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint               = Muted,
                        modifier           = Modifier.size(15.dp),
                    )
                }
            }
        } else {
            // ── Modo edición ──────────────────────────────────────────────────
            Text(
                text  = "${registro.meetingDate.format(dayFmt)} ${registro.meetingDate.format(monthFmt).lowercase().replace(".", "")}",
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (markerType) {
                    "monetary" -> BasicTextField(
                        value           = editMonto,
                        onValueChange   = { editMonto = it.filter { c -> c.isDigit() || c == '.' } },
                        singleLine      = true,
                        textStyle       = MaterialTheme.typography.bodyLarge.copy(color = Ink),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        decorationBox   = { inner ->
                            Row(
                                modifier = Modifier
                                    .background(BackgroundDeep, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("₡ ", style = MaterialTheme.typography.bodyMedium, color = Gold)
                                Box(modifier = Modifier.widthIn(min = 80.dp)) { inner() }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    else -> BasicTextField(
                        value           = editCantidad,
                        onValueChange   = { editCantidad = it.filter { c -> c.isDigit() } },
                        singleLine      = true,
                        textStyle       = MaterialTheme.typography.bodyLarge.copy(color = Ink),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        decorationBox   = { inner ->
                            Box(
                                modifier = Modifier
                                    .background(BackgroundDeep, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .widthIn(min = 80.dp),
                            ) { inner() }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundDeep)
                        .clickable(enabled = !isSaving, onClick = onCancelar),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Blush, modifier = Modifier.size(18.dp))
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Sage)
                        .clickable(enabled = !isSaving) {
                            onGuardar(editCantidad.toIntOrNull(), editMonto.toDoubleOrNull())
                        },
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun formatRegistroValor(registro: RegistroSemanalData, markerType: String, unidad: String, combinado: Double): String =
    when (markerType) {
        "monetary"     -> "₡${combinado.toLong()}"
        "checkbox"     -> if ((registro.cantidad ?: 0) > 0) "Realizada" else "No realizada"
        else           -> "${combinado.toInt()} $unidad"
    }
