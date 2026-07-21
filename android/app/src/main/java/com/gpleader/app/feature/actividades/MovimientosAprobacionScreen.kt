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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.MovimientoAprobacion
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.OfflineBanner
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInsetInner
import com.gpleader.app.core.ui.theme.neuInsetSm
import com.gpleader.app.feature.miembro.miles
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MovimientosAprobacionScreen(
    onNavigateBack: () -> Unit,
    viewModel: MovimientosAprobacionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

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
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Movimientos", style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("Aprobaciones, rechazos y ediciones", style = MaterialTheme.typography.bodyMedium, color = Mid)
            }
        }

        OfflineBanner(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

        if (!uiState.isLoading && uiState.error == null && uiState.movimientos.isNotEmpty()) {
            MovFiltroBoton(
                filtro   = uiState.filtro,
                onChange = viewModel::onFiltroChange,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 8.dp),
            )
        }

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            uiState.error != null -> Box(Modifier.fillMaxWidth().padding(24.dp)) {
                Text(uiState.error!!, color = Blush, style = MaterialTheme.typography.bodyMedium)
            }
            uiState.movimientos.isEmpty() -> EmptyState(
                titulo   = "Sin movimientos",
                subtitulo = "Aún no hay actividad de aprobaciones",
            )
            uiState.visibles.isEmpty() -> EmptyState(
                titulo    = "Sin movimientos de este tipo",
                subtitulo = "Prueba con otro filtro",
            )
            else -> LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp)) {
                items(uiState.visibles, key = { it.id }) { mov ->
                    MovimientoRow(mov)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(titulo: String, subtitulo: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titulo, style = MaterialTheme.typography.titleLarge, color = Ink)
            Spacer(Modifier.height(8.dp))
            Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

/** Botón-pill compacto que despliega un dropdown para elegir el tipo de acción. */
@Composable
private fun MovFiltroBoton(
    filtro:   MovFiltro,
    onChange: (MovFiltro) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .neuInsetSm(cornerRadius = 12.dp).clip(RoundedCornerShape(12.dp))
                .background(Background).clickable { expanded = true }
                .padding(start = 16.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(filtro.label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Ink)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Filtrar", tint = Mid, modifier = Modifier.size(20.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MovFiltro.entries.forEach { f ->
                DropdownMenuItem(
                    text    = { Text(f.label, color = if (f == filtro) Accent else Ink) },
                    onClick = { expanded = false; onChange(f) },
                )
            }
        }
    }
}

private val fmtMov: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale("es")).withZone(ZoneId.systemDefault())

private fun formatValor(mov: MovimientoAprobacion, valor: Double): String {
    val n = miles(valor.toLong())
    return when {
        mov.markerType == "monetary" -> "₡$n"
        mov.unitLabel.isNotBlank()   -> "$n ${mov.unitLabel}"
        else                         -> n
    }
}

private data class AccionEstilo(val icono: ImageVector, val color: Color, val titulo: String)

private fun estiloAccion(action: String): AccionEstilo = when (action) {
    "approved"       -> AccionEstilo(Icons.Default.CheckCircle, Sage, "Aporte aprobado")
    "board_approved" -> AccionEstilo(Icons.Default.CheckCircle, Sage, "Aprobado por la junta")
    "rejected"       -> AccionEstilo(Icons.Default.Cancel, Blush, "Aporte rechazado")
    "edited"         -> AccionEstilo(Icons.Default.Edit, Accent, "Total editado")
    "created"        -> AccionEstilo(Icons.Default.Add, Gold, "Aporte creado")
    "deleted"        -> AccionEstilo(Icons.Default.Delete, Muted, "Aporte borrado")
    else             -> AccionEstilo(Icons.Default.History, Mid, action)
}

@Composable
private fun MovimientoRow(mov: MovimientoAprobacion) {
    val estilo = estiloAccion(mov.action)
    val rol = when (mov.actorRole) {
        "leader" -> "líder"
        "church" -> "iglesia"
        else     -> "miembro"
    }
    val actor = mov.actorName?.takeIf { it.isNotBlank() }?.let { "$it ($rol)" } ?: rol

    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(Background).neuInsetInner(shadowSize = 10.dp),
            ) {
                Icon(estilo.icono, contentDescription = null, tint = estilo.color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(estilo.titulo, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "${mov.miembroNombre} · ${mov.actividadNombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
                if (mov.action == "edited") {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text       = "${formatValor(mov, mov.oldValue ?: 0.0)} → ${formatValor(mov, mov.newValue ?: 0.0)}",
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = Accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                } else if (mov.action in setOf("approved", "board_approved", "rejected")) {
                    // Monto aprobado/rechazado, en el color de la acción (Sage/Blush).
                    mov.entryValue?.let { valor ->
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text       = formatValor(mov, valor),
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = estilo.color,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text("por $actor", style = MaterialTheme.typography.bodyMedium, color = Mid)
                mov.note?.takeIf { it.isNotBlank() }?.let { motivo ->
                    Spacer(Modifier.height(2.dp))
                    Text("Motivo: $motivo", style = MaterialTheme.typography.bodyMedium, color = Mid)
                }
                Spacer(Modifier.height(4.dp))
                Text(mov.createdAt?.let { fmtMov.format(it) } ?: "—", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
        }
    }
}
