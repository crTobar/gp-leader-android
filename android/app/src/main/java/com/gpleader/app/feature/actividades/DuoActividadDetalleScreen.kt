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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.DuoActividadRecord
import com.gpleader.app.core.data.repository.DuoActividadTipo
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FMT_DETALLE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))
private val DIAS_CORTOS_DET = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuoActividadDetalleScreen(
    onNavigateBack: () -> Unit,
    viewModel: DuoActividadDetalleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ───────────────────────────────────────────────────────
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
                    text      = uiState.tipo?.nombre ?: "Actividad",
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Ink,
                    textAlign = TextAlign.Center,
                    maxLines  = 1,
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

            val tipo = uiState.tipo ?: return@Column

            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── Tarjeta resumen ───────────────────────────────────────────
                item {
                    TarjetaResumen(tipo = tipo, registroHoy = uiState.registroHoy)
                }

                // ── Tarjeta registrar ─────────────────────────────────────────
                item {
                    TarjetaRegistrar(
                        tipo        = tipo,
                        registroHoy = uiState.registroHoy,
                        isGuardando = uiState.isGuardando,
                        onClick     = viewModel::onRegistrarClick,
                    )
                }

                // ── Error ─────────────────────────────────────────────────────
                uiState.error?.let { err ->
                    item { Text(err, style = MaterialTheme.typography.bodyMedium, color = Blush) }
                }

                // ── Historial ─────────────────────────────────────────────────
                if (uiState.historial.isNotEmpty()) {
                    item {
                        Text(
                            "Historial",
                            style      = MaterialTheme.typography.titleLarge,
                            color      = Ink,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    items(uiState.historial, key = { it.id }) { registro ->
                        RegistroCard(
                            registro       = registro,
                            tipo           = tipo,
                            miembroNombres = uiState.miembroNombres,
                        )
                    }
                }
            }
        }
    }

    // ── Sheet de registro ─────────────────────────────────────────────────────
    val tipoSheet = uiState.tipo
    if (uiState.showSheet && tipoSheet != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissSheet,
            sheetState       = sheetState,
            containerColor   = Background,
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Registrar reporte",
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Ink,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Un aporte del dúo cuenta para ambos miembros",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )

                when (tipoSheet.markerType) {
                    "counter" -> NeuTextField(
                        value            = uiState.inputValue,
                        onValueChange    = viewModel::onInputChange,
                        label            = "Cantidad (${tipoSheet.unitLabel})",
                        keyboardOptions  = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    "monetary" -> NeuTextField(
                        value            = uiState.inputValue,
                        onValueChange    = viewModel::onInputChange,
                        label            = "Monto (₡)",
                        keyboardOptions  = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    "checkbox" -> Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Marcar como completado", style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
                        Switch(
                            checked         = uiState.checkboxValue,
                            onCheckedChange = viewModel::onCheckboxChange,
                            colors          = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Accent),
                        )
                    }
                }

                NeuButtonPrimary(
                    text     = if (uiState.isGuardando) "Guardando…" else "Guardar",
                    onClick  = viewModel::onGuardar,
                    modifier = Modifier.fillMaxWidth(),
                    enabled  = !uiState.isGuardando,
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TarjetaResumen(
    tipo:        DuoActividadTipo,
    registroHoy: DuoActividadRecord?,
) {
    val totalStr = when (tipo.markerType) {
        "monetary" -> if (registroHoy?.count != null) "₡${registroHoy.count}" else "—"
        "checkbox" -> if (registroHoy?.isDone == true) "✓ Completado hoy" else "Pendiente"
        else       -> if (registroHoy?.count != null) "${registroHoy.count} ${tipo.unitLabel}" else "—"
    }
    val totalColor = when {
        registroHoy == null -> Muted
        tipo.markerType == "checkbox" && registroHoy.isDone == true -> Sage
        else -> Accent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Background)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "HOY",
            style         = MaterialTheme.typography.labelSmall,
            color         = Accent,
            fontWeight    = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            totalStr,
            style      = MaterialTheme.typography.headlineMedium,
            color      = totalColor,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
        )
        if (tipo.unitLabel.isNotBlank() && tipo.markerType !in listOf("checkbox", "monetary")) {
            Text(tipo.unitLabel, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

@Composable
private fun TarjetaRegistrar(
    tipo:        DuoActividadTipo,
    registroHoy: DuoActividadRecord?,
    isGuardando: Boolean,
    onClick:     () -> Unit,
) {
    val yaRegistrado = when (tipo.markerType) {
        "checkbox" -> registroHoy?.isDone == true
        else       -> registroHoy?.count != null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Background)
            .clickable(enabled = !isGuardando, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .neuElevatedSm(cornerRadius = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Background),
        ) {
            Icon(Icons.Default.Edit, null, tint = Accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (isGuardando) "Guardando…" else "Registrar reporte",
                style      = MaterialTheme.typography.bodyLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )
            if (yaRegistrado) {
                val valorHoy = when (tipo.markerType) {
                    "monetary" -> "₡${registroHoy?.count ?: 0} registrado hoy"
                    "checkbox" -> "Completado hoy"
                    else       -> "${registroHoy?.count ?: 0} ${tipo.unitLabel} hoy"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Sage.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(valorHoy, style = MaterialTheme.typography.labelSmall, color = Sage, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Muted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun RegistroCard(
    registro:       DuoActividadRecord,
    tipo:           DuoActividadTipo,
    miembroNombres: Map<String, String>,
) {
    val quien  = miembroNombres[registro.updatedBy] ?: "Dúo"
    val diaIdx = if (registro.recordDate.dayOfWeek == DayOfWeek.SUNDAY) 0 else registro.recordDate.dayOfWeek.value
    val valorStr = when (tipo.markerType) {
        "monetary" -> if (registro.count != null) "₡${registro.count}" else "—"
        "checkbox" -> if (registro.isDone) "✓" else "—"
        else       -> if (registro.count != null) "${registro.count} ${tipo.unitLabel}" else "—"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevatedSm(cornerRadius = 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Background)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${DIAS_CORTOS_DET[diaIdx]}, ${registro.recordDate.format(FMT_DETALLE)}",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Text(quien, style = MaterialTheme.typography.bodyMedium, color = Ink, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Accent.copy(alpha = 0.10f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text(valorStr, style = MaterialTheme.typography.bodyMedium, color = Accent, fontWeight = FontWeight.Bold)
        }
    }
}
