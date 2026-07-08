package com.gpleader.app.feature.actividades

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Leaderboard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.feature.miembro.miles
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuInsetInner
import com.gpleader.app.core.ui.theme.neuInsetSm

@Composable
fun LiderAprobacionesScreen(
    onNavigateBack:  () -> Unit,
    onVerHistorial:  (scope: String, scopeId: String) -> Unit = { _, _ -> },
    viewModel: LiderAprobacionesViewModel = hiltViewModel(),
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
        LiderAprobacionesContent(
            uiState        = uiState,
            innerPadding   = innerPadding,
            onNavigateBack = onNavigateBack,
            onVerHistorial = { onVerHistorial("gp", uiState.grupoId) },
            onAprobar      = viewModel::onAprobar,
            onRechazar     = viewModel::onShowReject,
        )
    }

    val rechazando = uiState.rechazando
    if (rechazando != null) {
        RechazarMotivoDialog(
            miembro   = rechazando.miembroNombre,
            reason    = uiState.rejectReason,
            onChange  = viewModel::onRejectReasonChange,
            onConfirm = viewModel::onConfirmarRechazo,
            onDismiss = viewModel::onDismissReject,
        )
    }
}

@Composable
private fun LiderAprobacionesContent(
    uiState:        LiderAprobacionesUiState,
    innerPadding:   PaddingValues,
    onNavigateBack: () -> Unit,
    onVerHistorial: () -> Unit,
    onAprobar:      (AporteMiembro) -> Unit,
    onRechazar:     (AporteMiembro) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .padding(innerPadding),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink)
            }
            Column {
                Text("Aprobaciones", style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("Aportes enviados por tus miembros", style = MaterialTheme.typography.bodyMedium, color = Mid)
            }
        }

        Spacer(Modifier.height(12.dp))

        HistorialCard(onClick = onVerHistorial, modifier = Modifier.padding(horizontal = 20.dp))

        Spacer(Modifier.height(16.dp))

        Text(
            "MONTOS POR APROBAR",
            style = MaterialTheme.typography.labelSmall, color = Muted,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(8.dp))

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            uiState.error != null -> Box(Modifier.fillMaxWidth().padding(24.dp)) {
                Text(uiState.error, color = Blush, style = MaterialTheme.typography.bodyMedium)
            }
            uiState.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sin pendientes", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Spacer(Modifier.height(8.dp))
                    Text("No hay aportes por aprobar", style = MaterialTheme.typography.bodyMedium, color = Muted)
                }
            }
            else -> LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
                val porActividad = uiState.items.groupBy { it.actividadNombre }
                porActividad.forEach { (actividadNombre, registros) ->
                    item(key = "header_$actividadNombre") {
                        Text(
                            text     = actividadNombre,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = Muted,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        )
                    }
                    items(registros, key = { it.key }) { item ->
                        AprobacionMiembroItem(
                            item       = item,
                            procesando = item.key in uiState.procesando,
                            onAprobar  = { onAprobar(item) },
                            onRechazar = { onRechazar(item) },
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

private fun formatValor(item: AporteMiembro): String = when (item.markerType) {
    "monetary" -> "₡${miles(item.total.toLong())}"
    else       -> if (item.unitLabel.isNotBlank()) "${miles(item.total.toLong())} ${item.unitLabel}" else miles(item.total.toLong())
}

@Composable
private fun AprobacionMiembroItem(
    item:       AporteMiembro,
    procesando: Boolean,
    onAprobar:  () -> Unit,
    onRechazar: () -> Unit,
) {
    NeuCard {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.miembroNombre, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Ink)
                    val n = item.entryIds.size
                    Text(
                        text  = if (n == 1) "1 aporte" else "$n aportes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mid,
                    )
                }
                Text(formatValor(item), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Ink)
            }

            Spacer(Modifier.height(12.dp))

            if (procesando) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Accent, strokeWidth = 2.dp)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AccionBtn("✗ Rechazar", Blush, Modifier.weight(1f), onRechazar)
                    Spacer(Modifier.width(8.dp))
                    AccionBtn("✓ Aprobar suma", Sage, Modifier.weight(1f), onAprobar)
                }
            }
        }
    }
}

@Composable
internal fun HistorialCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    NeuCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(Background).neuInsetInner(shadowSize = 10.dp),
            ) {
                Icon(Icons.Default.Leaderboard, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Historial", style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
                Text("Aportes aprobados por actividad y miembro", style = MaterialTheme.typography.bodyMedium, color = Mid)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun AccionBtn(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
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
private fun RechazarMotivoDialog(
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
