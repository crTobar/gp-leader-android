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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.MemberEntry
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
import com.gpleader.app.feature.miembro.miles
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MiembroAporteHistorialScreen(
    onNavigateBack:      () -> Unit,
    onNavigateToDetalle: (entryId: String) -> Unit,
    viewModel: MiembroAporteHistorialViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

    val zone = ZoneId.systemDefault()
    val fmt  = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale("es")).withZone(zone)

    fun valor(v: Double): String =
        if (uiState.markerType == "monetary") "₡${miles(v.toLong())}"
        else "${miles(v.toLong())} ${uiState.unitLabel}".trim()

    Column(modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding()) {
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
            Text(
                text = uiState.miembroNombre.ifBlank { "Aportes" },
                style = MaterialTheme.typography.titleLarge, color = Ink,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
            )
            Box(Modifier.size(40.dp))
        }

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            uiState.error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = Blush, textAlign = TextAlign.Center)
            }
            uiState.entries.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Sin aportes", color = Muted)
            }
            else -> LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.entries, key = { it.id }) { e ->
                    EntryRowLeader(e, valor(e.value), fmt, onClick = { onNavigateToDetalle(e.id) })
                }
            }
        }
    }
}

@Composable
private fun EntryRowLeader(
    entry:   MemberEntry,
    valorTxt: String,
    fmt:     DateTimeFormatter,
    onClick: () -> Unit,
) {
    val statusColor = when (entry.status) {
        "approved"      -> Sage
        "pending_board" -> Accent
        "rejected"      -> Blush
        else            -> Muted
    }
    val statusLabel = when (entry.status) {
        "approved"      -> "Aprobado"
        "pending_board" -> "Pend. junta"
        "rejected"      -> "Rechazado"
        else            -> "Pendiente"
    }
    NeuCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.enteredAt?.let { fmt.format(it) } ?: "—", style = MaterialTheme.typography.bodyMedium, color = Mid)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(statusColor.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor)
                }
            }
            Text(valorTxt, style = MaterialTheme.typography.titleLarge, color = if (entry.status == "approved") Sage else Accent, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
        }
    }
}
