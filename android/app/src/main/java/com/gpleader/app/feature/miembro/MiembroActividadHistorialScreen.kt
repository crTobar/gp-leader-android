package com.gpleader.app.feature.miembro

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.gpleader.app.core.ui.theme.neuInsetSm
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        // ── Back button ───────────────────────────────────────────────────────
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
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint               = Ink,
                    modifier           = Modifier.size(20.dp),
                )
            }
            Text(
                text      = "Detalle",
                style     = MaterialTheme.typography.titleLarge,
                color     = Ink,
                textAlign = TextAlign.Center,
                modifier  = Modifier.weight(1f),
            )
            Box(modifier = Modifier.size(40.dp))
        }

        // ── Card nombre + total ───────────────────────────────────────────────
        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text       = uiState.nombreActividad.ifBlank { "Actividad" },
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f),
                )
                val totalLabel = when (uiState.markerType) {
                    "monetary"              -> "₡${uiState.totalHistorico}"
                    "realizado", "checkbox" -> if (uiState.totalHistorico > 0) "${uiState.totalHistorico}×" else "—"
                    else                    -> if (uiState.totalHistorico > 0) "${uiState.totalHistorico} ${uiState.unitLabel}" else "—"
                }
                Text(
                    text       = totalLabel,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = if (uiState.totalHistorico > 0) Accent else Muted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Card "Registrar aporte" (solo para no-monetario) ─────────────────
        if (uiState.markerType != "monetary") {
            NeuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Text(
                        text       = "Registrar aporte",
                        style      = MaterialTheme.typography.titleLarge,
                        color      = Ink,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "Registra tu aporte de hoy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                    Spacer(Modifier.height(16.dp))
                    NeuButtonPrimary(
                        text     = "Agregar",
                        onClick  = viewModel::onShowAddDialog,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Lista de registros ────────────────────────────────────────────────
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
                            text  = "Toca \"Agregar\" para registrar tu primer aporte.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 24.dp),
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
                if (markerType == "monetary") {
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
                var editando  by remember { mutableStateOf(false) }
                var inputText by remember { mutableStateOf(cantidad.toString()) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = unitLabel.ifBlank { "Cantidad" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mid,
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        // − button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .neuElevatedSm(cornerRadius = 14.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Background)
                                .clickable(enabled = cantidad > 0) {
                                    editando = false
                                    onCantidadChange((cantidad - 1).coerceAtLeast(0))
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                        ) {
                            Text(
                                text  = "−",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (cantidad > 0) Ink else Muted,
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        // Valor central — tappable para input directo
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .neuInsetSm(cornerRadius = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Background)
                                .clickable {
                                    inputText = if (cantidad > 0) cantidad.toString() else ""
                                    editando = true
                                }
                                .padding(horizontal = 24.dp, vertical = 14.dp)
                                .widthIn(min = 64.dp),
                        ) {
                            if (editando) {
                                BasicTextField(
                                    value         = inputText,
                                    onValueChange = {
                                        inputText = it.filter { c -> c.isDigit() }
                                        onCantidadChange(inputText.toIntOrNull() ?: 0)
                                    },
                                    singleLine    = true,
                                    textStyle     = MaterialTheme.typography.headlineMedium.copy(
                                        color     = Accent,
                                        textAlign = TextAlign.Center,
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction    = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            editando = false
                                            onCantidadChange(inputText.toIntOrNull() ?: 0)
                                        },
                                    ),
                                )
                            } else {
                                Text(
                                    text      = cantidad.toString(),
                                    style     = MaterialTheme.typography.headlineMedium,
                                    color     = if (cantidad > 0) Accent else Muted,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        // + button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .neuGlow(cornerRadius = 14.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Accent)
                                .clickable {
                                    editando = false
                                    onCantidadChange(cantidad + 1)
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                        ) {
                            Text(
                                text  = "+",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Accent)
            } else {
                TextButton(onClick = onConfirmar, enabled = cantidad > 0) {
                    Text("Guardar", color = if (cantidad > 0) Accent else Muted, fontWeight = FontWeight.SemiBold)
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
