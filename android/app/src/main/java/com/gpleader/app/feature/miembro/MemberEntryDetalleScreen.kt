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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.MemberEntryEvent
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MemberEntryDetalleScreen(
    onNavigateBack: () -> Unit,
    viewModel: MemberEntryDetalleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) { onNavigateBack(); viewModel.consumeDeleted() }
    }

    var confirmDelete by remember { mutableStateOf(false) }

    val zone = ZoneId.systemDefault()
    val fmt  = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale("es")).withZone(zone)

    Column(
        modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding(),
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
            Text("Aporte", style = MaterialTheme.typography.titleLarge, color = Ink, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Box(Modifier.size(40.dp))
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Accent) }
            return
        }

        val entry = uiState.entry
        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Cabecera ──────────────────────────────────────────────────────
            item {
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("MONTO ACTUAL", style = MaterialTheme.typography.labelSmall, color = Muted)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text       = "₡${miles(entry?.value?.toLong() ?: 0)}",
                            style      = MaterialTheme.typography.headlineMedium,
                            color      = if (entry?.status == "approved") Sage else Accent,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text  = "Agregado: ${entry?.enteredAt?.let { fmt.format(it) } ?: "—"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mid,
                        )
                        Text(
                            text  = "Estado: ${estadoLabel(entry?.status)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mid,
                        )

                        if (uiState.editable) {
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AccionChip("Editar", Icons.Default.Edit, Accent) { viewModel.onShowEdit() }
                                AccionChip("Borrar", Icons.Default.Delete, Blush) { confirmDelete = true }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text     = "HISTORIAL DE CAMBIOS",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Muted,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                )
            }

            if (uiState.eventos.isEmpty()) {
                item { Text("Sin cambios registrados", style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.padding(4.dp)) }
            } else {
                items(uiState.eventos, key = { it.id }) { ev -> EventoRow(ev, fmt) }
            }
        }
    }

    if (uiState.showEditDialog) {
        EditValueDialog(
            value      = uiState.editValue,
            isLoading  = uiState.isGuardando,
            onChange   = viewModel::onEditValueChange,
            onConfirm  = viewModel::onConfirmarEdicion,
            onDismiss  = viewModel::onDismissEdit,
        )
    }

    if (confirmDelete) {
        val montoTxt = "₡${miles(uiState.entry?.value?.toLong() ?: 0)}"
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor   = Background,
            title = { Text("Borrar aporte", style = MaterialTheme.typography.titleLarge, color = Ink) },
            text  = { Text("¿Seguro que quieres borrar el aporte de $montoTxt? Esta acción no se puede deshacer.", style = MaterialTheme.typography.bodyMedium, color = Mid) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; viewModel.onBorrar() }) {
                    Text("Borrar", color = Blush, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar", color = Mid) } },
        )
    }
}

private fun estadoLabel(status: String?): String = when (status) {
    "approved"      -> "Aprobado"
    "pending_board" -> "Pendiente de junta"
    "rejected"      -> "Rechazado"
    else            -> "Pendiente de aprobación"
}

@Composable
private fun AccionChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.12f))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(6.dp))
        Text(text, color = color, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EventoRow(ev: MemberEntryEvent, fmt: DateTimeFormatter) {
    val rol = when (ev.actorRole) {
        "leader" -> "líder"
        "church" -> "iglesia"
        else     -> "miembro"
    }
    // "Nombre (rol)" cuando se conoce el nombre; si no, solo el rol.
    val actor = ev.actorName?.takeIf { it.isNotBlank() }?.let { "$it ($rol)" } ?: rol
    val desc = when (ev.action) {
        "created"        -> "Creado por $actor"
        "edited"         -> "Editado por $actor: ₡${miles(ev.oldValue?.toLong() ?: 0)} → ₡${miles(ev.newValue?.toLong() ?: 0)}"
        "deleted"        -> "Borrado por $actor"
        "approved"       -> "Aprobado por $actor"
        "rejected"       -> "Rechazado por $actor"
        "board_approved" -> "Aprobado por la junta ($actor)"
        else             -> ev.action
    }
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(desc, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.Medium)
            ev.note?.takeIf { it.isNotBlank() }?.let { motivo ->
                Spacer(Modifier.height(4.dp))
                Text("Motivo: $motivo", style = MaterialTheme.typography.bodyMedium, color = Mid)
            }
            Spacer(Modifier.height(2.dp))
            Text(ev.createdAt?.let { fmt.format(it) } ?: "—", style = MaterialTheme.typography.labelSmall, color = Muted)
        }
    }
}

@Composable
private fun EditValueDialog(
    value:     Int,
    isLoading: Boolean,
    onChange:  (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Background,
        title = { Text("Editar aporte", style = MaterialTheme.typography.titleLarge, color = Ink) },
        text = {
            MontoInput(markerType = "monetary", unitLabel = "", cantidad = value, onChange = onChange)
        },
        confirmButton = {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Accent)
            else TextButton(onClick = onConfirm, enabled = value > 0) {
                Text("Guardar", color = if (value > 0) Accent else Muted, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Mid) } },
    )
}
