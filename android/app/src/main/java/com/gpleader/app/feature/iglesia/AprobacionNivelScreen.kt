package com.gpleader.app.feature.iglesia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.NivelActividadDetalle
import com.gpleader.app.core.data.repository.NivelHijoPendiente
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuInsetSm
import com.gpleader.app.feature.miembro.MontoInput
import com.gpleader.app.feature.miembro.miles

@Composable
fun AprobacionNivelScreen(
    onNavigateBack: () -> Unit,
    onVerHistorial: (sourceLevel: String, childId: String, activityId: String, marker: String, titulo: String) -> Unit = { _, _, _, _, _ -> },
    onVerHistorialMiembros: (scope: String, scopeId: String) -> Unit = { _, _ -> },
    viewModel: AprobacionNivelViewModel = hiltViewModel(),
) {
    val uiState      by viewModel.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMsg) {
        uiState.toastMsg?.let {
            snackbarHost.showSnackbar(it)
            viewModel.consumeToast()
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost   = { SnackbarHost(snackbarHost) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding().padding(innerPadding),
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink)
                }
                Column {
                    Text(uiState.nivelTitulo, style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text(uiState.nodeNombre.ifBlank { "Montos por aprobar" }, style = MaterialTheme.typography.bodyMedium, color = Mid)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (uiState.esChurch) {
                com.gpleader.app.feature.actividades.HistorialCard(
                    onClick  = { onVerHistorialMiembros("church", uiState.nodeId) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "MONTOS POR APROBAR",
                    style = MaterialTheme.typography.labelSmall, color = Muted,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(8.dp))
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
                uiState.error != null -> Box(Modifier.fillMaxWidth().padding(24.dp)) {
                    Text(uiState.error!!, color = Blush, style = MaterialTheme.typography.bodyMedium)
                }
                uiState.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sin pendientes", style = MaterialTheme.typography.titleLarge, color = Ink)
                        Spacer(Modifier.height(8.dp))
                        Text("No hay montos por aprobar", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    }
                }
                else -> LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)) {
                    uiState.items.forEach { detalle ->
                        item(key = "act_${detalle.actividad.activityTypeId}") {
                            ActividadHeader(detalle)
                        }
                        items(detalle.hijos, key = { "${detalle.actividad.activityTypeId}_${it.childId}" }) { hijo ->
                            val key = "${hijo.childId}|${detalle.actividad.activityTypeId}"
                            HijoRow(
                                hijo       = hijo,
                                markerType = detalle.actividad.markerType,
                                unitLabel  = detalle.actividad.unitLabel,
                                procesando = key in uiState.procesando,
                                onClick    = { viewModel.onShowApprove(detalle, hijo) },
                                onHistory  = {
                                    onVerHistorial(
                                        uiState.sourceLevel,
                                        hijo.childId,
                                        detalle.actividad.activityTypeId,
                                        detalle.actividad.markerType,
                                        hijo.childNombre,
                                    )
                                },
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }

    val obj = uiState.objetivo
    if (obj != null) {
        AprobarMontoDialog(
            titulo    = obj.childNombre,
            subtitulo = obj.actividadNombre,
            markerType = obj.markerType,
            unitLabel  = obj.unitLabel,
            pendiente  = obj.pendiente.toInt(),
            monto      = uiState.montoInput,
            onMonto    = viewModel::onMontoChange,
            onConfirm  = viewModel::onConfirmApprove,
            onDismiss  = viewModel::onDismiss,
        )
    }
}

private fun fmtMonto(markerType: String, unitLabel: String, v: Double): String = when (markerType) {
    "monetary" -> "₡${miles(v.toLong())}"
    else       -> if (unitLabel.isNotBlank()) "${miles(v.toLong())} $unitLabel" else miles(v.toLong())
}

@Composable
private fun ActividadHeader(detalle: NivelActividadDetalle) {
    val a = detalle.actividad
    Column(modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)) {
        Text(a.actividadNombre, style = MaterialTheme.typography.labelSmall, color = Muted)
        Spacer(Modifier.height(4.dp))
        Row {
            Text("Pendiente ", style = MaterialTheme.typography.bodyMedium, color = Mid)
            Text(fmtMonto(a.markerType, a.unitLabel, a.pendienteTotal), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Blush)
            Text("  ·  Aprobado ", style = MaterialTheme.typography.bodyMedium, color = Mid)
            Text(fmtMonto(a.markerType, a.unitLabel, a.aprobadoTotal), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Sage)
        }
    }
}

@Composable
private fun HijoRow(
    hijo:       NivelHijoPendiente,
    markerType: String,
    unitLabel:  String,
    procesando: Boolean,
    onClick:    () -> Unit,
    onHistory:  () -> Unit,
) {
    NeuCard(modifier = Modifier.fillMaxWidth().clickable(enabled = !procesando && hijo.pendiente > 0, onClick = onClick)) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(hijo.childNombre, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Ink)
                Spacer(Modifier.height(2.dp))
                Text("Aprobado: ${fmtMonto(markerType, unitLabel, hijo.aprobado)}", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
            if (procesando) {
                CircularProgressIndicator(modifier = Modifier.height(22.dp), color = Accent, strokeWidth = 2.dp)
            } else {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pendiente", style = MaterialTheme.typography.labelSmall, color = Muted)
                    Text(
                        fmtMonto(markerType, unitLabel, hijo.pendiente),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (hijo.pendiente > 0) Blush else Muted,
                    )
                }
            }
            IconButton(onClick = onHistory) {
                Icon(Icons.Default.History, contentDescription = "Historial", tint = Muted, modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun AprobarMontoDialog(
    titulo:     String,
    subtitulo:  String,
    markerType: String,
    unitLabel:  String,
    pendiente:  Int,
    monto:      Int,
    onMonto:    (Int) -> Unit,
    onConfirm:  () -> Unit,
    onDismiss:  () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Background,
        title = { Text(titulo, style = MaterialTheme.typography.titleLarge, color = Ink) },
        text = {
            Column {
                Text("$subtitulo · pendiente ${fmtMonto(markerType, unitLabel, pendiente.toDouble())}",
                    style = MaterialTheme.typography.bodyMedium, color = Mid)
                Spacer(Modifier.height(16.dp))
                MontoInput(markerType = markerType, unitLabel = unitLabel, cantidad = monto, onChange = onMonto)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = monto > 0) {
                Text("Aprobar", color = if (monto > 0) Sage else Muted, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Mid) } },
    )
}
