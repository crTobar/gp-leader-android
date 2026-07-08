package com.gpleader.app.feature.iglesia

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.MoneyMovimiento
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.feature.miembro.miles
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MovimientosHistorialScreen(
    onNavigateBack: () -> Unit,
    viewModel: MovimientosHistorialViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val fmt = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale.forLanguageTag("es"))
        .withZone(ZoneId.systemDefault())

    fun valor(v: Double): String =
        if (uiState.markerType == "monetary") "₡${miles(v.toLong())}"
        else "${miles(v.toLong())} ${uiState.unitLabel}".trim()

    Column(modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding()) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink)
            }
            Column {
                Text(uiState.titulo, style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("Cada aprobación queda registrada", style = MaterialTheme.typography.bodyMedium, color = Mid)
            }
        }

        Spacer(Modifier.height(8.dp))

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            uiState.error != null -> Box(Modifier.fillMaxWidth().padding(24.dp)) {
                Text(uiState.error!!, color = Blush, style = MaterialTheme.typography.bodyMedium)
            }
            uiState.movimientos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin movimientos", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            ) {
                items(uiState.movimientos, key = { it.id }) { mov ->
                    MovimientoRow(mov, ::valor, fmt.format(mov.createdAt ?: java.time.Instant.EPOCH))
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun MovimientoRow(mov: MoneyMovimiento, valor: (Double) -> String, fecha: String) {
    val parcial = mov.approved < mov.requested
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Requería ${valor(mov.requested)}", style = MaterialTheme.typography.bodyMedium, color = Mid)
                    Text(
                        "Aprobado ${valor(mov.approved)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (parcial) Blush else Sage,
                    )
                }
                Text(
                    text  = aprobadorLabel(mov.approverLevel),
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
            mov.note?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text("Motivo: $it", style = MaterialTheme.typography.bodyMedium, color = Mid)
            }
            Spacer(Modifier.height(2.dp))
            Text(fecha, style = MaterialTheme.typography.labelSmall, color = Muted)
        }
    }
}

private fun aprobadorLabel(level: String): String = when (level) {
    "church"   -> "Iglesia"
    "district" -> "Pastor"
    "campo"    -> "Asociación"
    "union"    -> "Unión"
    else       -> level
}
