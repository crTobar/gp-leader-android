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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInsetSm
import com.gpleader.app.feature.miembro.MontoInput
import com.gpleader.app.feature.miembro.miles
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LiderAporteDetalleScreen(
    onNavigateBack: () -> Unit,
    viewModel: LiderAporteDetalleViewModel = hiltViewModel(),
) {
    val uiState      by viewModel.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(uiState.done) {
        if (uiState.done) { onNavigateBack(); viewModel.consumeDone() }
    }
    LaunchedEffect(uiState.toastMsg) {
        uiState.toastMsg?.let { snackbarHost.showSnackbar(it); viewModel.consumeToast() }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost   = { SnackbarHost(snackbarHost) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding().padding(innerPadding),
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.neuElevatedSm(cornerRadius = 12.dp).clip(RoundedCornerShape(12.dp))
                        .background(Background).clickable(onClick = onNavigateBack).padding(10.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Aporte a aprobar", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text(uiState.actividadNombre, style = MaterialTheme.typography.bodyMedium, color = Mid)
                }
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
                uiState.error != null -> Box(Modifier.fillMaxWidth().padding(24.dp)) {
                    Text(uiState.error!!, color = Blush, style = MaterialTheme.typography.bodyMedium)
                }
                else -> DetalleContent(uiState, viewModel)
            }
        }
    }

    if (uiState.rechazando) {
        RechazarMotivoDialogDetalle(
            miembro   = uiState.miembroNombre,
            reason    = uiState.rejectReason,
            onChange  = viewModel::onRejectReasonChange,
            onConfirm = viewModel::onConfirmarRechazo,
            onDismiss = viewModel::onDismissReject,
        )
    }
    if (uiState.showEditDialog) {
        EditarTotalDialog(
            markerType = uiState.markerType,
            unitLabel  = uiState.unitLabel,
            value      = uiState.editValue,
            onChange   = viewModel::onEditValueChange,
            onConfirm  = viewModel::onConfirmarEdicion,
            onDismiss  = viewModel::onDismissEdit,
        )
    }
}

private val fmtFecha: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale("es")).withZone(ZoneId.systemDefault())

private fun formatMonto(markerType: String, unitLabel: String, valor: Double): String {
    val n = miles(valor.toLong())
    return when {
        markerType == "monetary" -> "₡$n"
        unitLabel.isNotBlank()   -> "$n $unitLabel"
        else                     -> n
    }
}

private fun formatDelta(markerType: String, unitLabel: String, valor: Double): String {
    val signo = if (valor >= 0) "+" else "−"
    return "$signo${formatMonto(markerType, unitLabel, kotlin.math.abs(valor))}"
}

@Composable
private fun DetalleContent(uiState: LiderAporteDetalleUiState, viewModel: LiderAporteDetalleViewModel) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Total resaltado ────────────────────────────────────────────────────
        item {
            NeuCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("TOTAL A APROBAR", style = MaterialTheme.typography.labelSmall, color = Muted)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text       = formatMonto(uiState.markerType, uiState.unitLabel, uiState.total),
                        style      = MaterialTheme.typography.headlineMedium,
                        color      = Ink,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(uiState.miembroNombre, style = MaterialTheme.typography.bodyLarge, color = Mid)
                }
            }
        }

        item {
            Text(
                text     = "APORTES",
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp),
            )
        }

        items(uiState.lineas, key = { it.entryId }) { linea ->
            AporteLineaRow(linea, uiState.markerType, uiState.unitLabel)
        }

        // ── Acciones ───────────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            if (uiState.procesando) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Accent, strokeWidth = 2.dp)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AccionBtnDetalle("✗ Rechazar", Blush, Modifier.weight(1f)) { viewModel.onShowReject() }
                    Spacer(Modifier.width(8.dp))
                    AccionBtnDetalle("✎ Editar total", Accent, Modifier.weight(1f)) { viewModel.onShowEdit() }
                    Spacer(Modifier.width(8.dp))
                    AccionBtnDetalle("✓ Aprobar", Sage, Modifier.weight(1f)) { viewModel.onAprobar() }
                }
            }
        }
    }
}

@Composable
private fun AporteLineaRow(linea: AporteLinea, markerType: String, unitLabel: String) {
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = if (linea.isAdjustment) "Ajuste del líder" else "Aporte",
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = if (linea.isAdjustment) Accent else Ink,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text  = linea.enteredAt?.let { fmtFecha.format(it) } ?: "—",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
            Text(
                text       = if (linea.isAdjustment) formatDelta(markerType, unitLabel, linea.value)
                             else formatMonto(markerType, unitLabel, linea.value),
                style      = MaterialTheme.typography.bodyLarge,
                color      = if (linea.isAdjustment) Accent else Ink,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AccionBtnDetalle(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EditarTotalDialog(
    markerType: String,
    unitLabel:  String,
    value:      Int,
    onChange:   (Int) -> Unit,
    onConfirm:  () -> Unit,
    onDismiss:  () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Background,
        title = { Text("Editar total", style = MaterialTheme.typography.titleLarge, color = Ink) },
        text = {
            Column {
                Text(
                    "Fija el total a aprobar. Puede ser mayor o menor que la suma de los aportes; la diferencia se registra como \"Ajuste del líder\".",
                    style = MaterialTheme.typography.bodyMedium, color = Mid,
                )
                Spacer(Modifier.height(12.dp))
                MontoInput(markerType = markerType, unitLabel = unitLabel, cantidad = value, onChange = onChange)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Guardar", color = Accent, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Mid) } },
    )
}

@Composable
private fun RechazarMotivoDialogDetalle(
    miembro:   String,
    reason:    String,
    onChange:  (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Background,
        title = { Text("Rechazar aporte", style = MaterialTheme.typography.titleLarge, color = Ink) },
        text = {
            Column {
                Text(
                    "Aporte de $miembro. Puedes indicar un motivo (opcional); quedará en el historial.",
                    style = MaterialTheme.typography.bodyMedium, color = Mid,
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .neuInsetSm(cornerRadius = 12.dp).clip(RoundedCornerShape(12.dp))
                        .background(Background).padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicTextField(
                        value         = reason,
                        onValueChange = onChange,
                        textStyle     = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                        modifier      = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (reason.isEmpty()) {
                                Text("Motivo del rechazo…", style = MaterialTheme.typography.bodyMedium, color = Muted)
                            }
                            inner()
                        },
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Rechazar", color = Blush, fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Mid) } },
    )
}
