package com.gpleader.app.feature.miembro

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.RegistroHistorial
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.OnResumeEffect
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuGlow
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiembroActividadHistorialScreen(
    onNavigateBack: () -> Unit,
    viewModel: MiembroActividadHistorialViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── TopBar ────────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink)
                }
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = uiState.nombreActividad.ifBlank { "Actividad" },
                        style      = MaterialTheme.typography.titleLarge,
                        color      = Ink,
                        fontWeight = FontWeight.SemiBold,
                    )
                    // Total acumulado
                    val totalLabel = when (uiState.markerType) {
                        "monetary"              -> "₡${uiState.totalHistorico} acumulado"
                        "realizado", "checkbox" -> if (uiState.totalHistorico > 0) "Realizado ${uiState.totalHistorico} veces" else "Aún no realizado"
                        else                    -> "${uiState.totalHistorico} ${uiState.unitLabel} acumulados"
                    }
                    Text(
                        text  = totalLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.totalHistorico > 0) Accent else Muted,
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh    = viewModel::onRefresh,
                modifier     = Modifier.fillMaxSize(),
                indicator    = {},
            ) {
                when {
                    uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent)
                    }

                    uiState.error != null -> Box(
                        Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(uiState.error!!, color = Blush, textAlign = TextAlign.Center)
                    }

                    uiState.registros.isEmpty() -> Box(
                        Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sin registros aún", style = MaterialTheme.typography.bodyLarge, color = Muted)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text  = "Usa el botón + para registrar tu primer aporte.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    else -> LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(uiState.registros, key = { it.id }) { reg ->
                            RegistroHistorialRow(
                                reg        = reg,
                                markerType = uiState.markerType,
                                unitLabel  = uiState.unitLabel,
                            )
                        }
                    }
                }
            }
        }

        // ── FAB agregar (solo para counter y realizado, no monetary) ─────────
        if (uiState.markerType != "monetary") {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(24.dp)
                    .size(56.dp)
                    .neuGlow(cornerRadius = 28.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Accent),
            ) {
                IconButton(onClick = viewModel::onShowAddDialog) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }

    // ── Diálogo agregar ───────────────────────────────────────────────────────
    if (uiState.showAddDialog) {
        AddRegistroDialog(
            markerType = uiState.markerType,
            unitLabel  = uiState.unitLabel,
            cantidad   = uiState.nuevaCantidad,
            isLoading  = uiState.isGuardando,
            onCantidadChange = viewModel::onCantidadChange,
            onConfirmar      = viewModel::onGuardar,
            onDismiss        = viewModel::onDismissDialog,
        )
    }
}

@Composable
private fun RegistroHistorialRow(
    reg:        RegistroHistorial,
    markerType: String,
    unitLabel:  String,
) {
    val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es"))
    val statusColor = when (reg.status) {
        "approved", "pending_board" -> Sage
        "rejected"                  -> Blush
        else                        -> Muted
    }
    val statusLabel = when (reg.status) {
        "approved"      -> "Aprobado"
        "pending_board" -> "Pend. junta"
        "rejected"      -> "Rechazado"
        else            -> "Pendiente"
    }

    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = reg.recordDate.format(fmt),
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text  = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                    )
                }
            }
            when (markerType) {
                "realizado", "checkbox" -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint     = if (reg.isDone) Sage else Muted,
                    modifier = Modifier.size(24.dp),
                )
                "monetary" -> Text(
                    text       = "₡${reg.count ?: 0}",
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Accent,
                    fontWeight = FontWeight.Bold,
                )
                else -> {
                    Text(
                        text       = "${reg.count ?: 0}",
                        style      = MaterialTheme.typography.titleLarge,
                        color      = if ((reg.count ?: 0) > 0) Accent else Muted,
                        fontWeight = FontWeight.Bold,
                    )
                    if (unitLabel.isNotBlank()) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = unitLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = Mid,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddRegistroDialog(
    markerType:      String,
    unitLabel:       String,
    cantidad:        Int,
    isLoading:       Boolean,
    onCantidadChange: (Int) -> Unit,
    onConfirmar:     () -> Unit,
    onDismiss:       () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Background,
        title = {
            Text(
                text  = if (markerType == "realizado" || markerType == "checkbox") "Marcar como realizado" else "Agregar registro",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
        },
        text = {
            if (markerType == "realizado" || markerType == "checkbox") {
                Text(
                    text  = "Se registrará que realizaste esta actividad hoy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
            } else {
                var texto by remember { mutableStateOf(if (cantidad > 0) cantidad.toString() else "") }
                Column {
                    Text(
                        text  = "¿Cuánto ${unitLabel.ifBlank { "deseas agregar" }}?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mid,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = texto,
                        onValueChange = { v ->
                            texto = v.filter { it.isDigit() }
                            onCantidadChange(texto.toIntOrNull() ?: 1)
                        },
                        label         = { Text(unitLabel.ifBlank { "Cantidad" }) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Accent)
            } else {
                TextButton(onClick = onConfirmar) {
                    Text("Guardar", color = Accent, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Mid)
            }
        },
    )
}
