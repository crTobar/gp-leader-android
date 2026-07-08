package com.gpleader.app.feature.actividades

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.HistAprobacion
import com.gpleader.app.core.data.repository.MemberEntry
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.OnResumeEffect
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistorialAportesMiembroScreen(
    onNavigateBack:      () -> Unit,
    onNavigateToDetalle: (entryId: String) -> Unit,
    viewModel: HistorialAportesMiembroViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

    val fmt = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale.forLanguageTag("es"))
        .withZone(ZoneId.systemDefault())

    Column(modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding()) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink)
            }
            Column {
                Text(uiState.nombre.ifBlank { "Aprobaciones" }, style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("Aprobaciones y aportes", style = MaterialTheme.typography.bodyMedium, color = Mid)
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
            uiState.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin aprobaciones", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            else -> LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
                items(uiState.items, key = { it.approvedAtKey }) { batch ->
                    AprobacionCard(batch, uiState.marker, fmt, onNavigateToDetalle)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun AprobacionCard(
    batch:  HistAprobacion,
    marker: String,
    fmt:    DateTimeFormatter,
    onAporte: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        formatAporte(marker, "", batch.total),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Sage,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = (batch.fecha?.let { fmt.format(it) } ?: "—") +
                                " · " + (if (batch.count == 1) "1 aporte" else "${batch.count} aportes"),
                        style = MaterialTheme.typography.labelSmall, color = Muted,
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null, tint = Mid,
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    batch.aportes.forEachIndexed { i, ap ->
                        if (i > 0) Spacer(Modifier.height(6.dp))
                        AporteRow(ap, marker, fmt) { onAporte(ap.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AporteRow(ap: MemberEntry, marker: String, fmt: DateTimeFormatter, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)).background(BackgroundDeep)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            ap.enteredAt?.let { fmt.format(it) } ?: "—",
            style = MaterialTheme.typography.bodyMedium, color = Mid, modifier = Modifier.weight(1f),
        )
        Text(
            formatAporte(marker, "", ap.value),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Ink,
        )
    }
}
