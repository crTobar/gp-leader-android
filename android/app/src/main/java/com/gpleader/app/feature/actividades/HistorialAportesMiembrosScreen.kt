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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.HistMiembro
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.OnResumeEffect
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage

@Composable
fun HistorialAportesMiembrosScreen(
    onNavigateBack:     () -> Unit,
    onNavigateToMiembro: (miembroId: String, nombre: String) -> Unit,
    viewModel: HistorialAportesMiembrosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

    Column(modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding()) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink)
            }
            Column {
                Text(uiState.titulo.ifBlank { "Aportes" }, style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("Miembros que aportaron", style = MaterialTheme.typography.bodyMedium, color = Mid)
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
                Text("Sin aportes", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            else -> LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
                items(uiState.items, key = { it.miembroId }) { m ->
                    MiembroRow(m, uiState.marker) { onNavigateToMiembro(m.miembroId, m.nombre) }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun MiembroRow(m: HistMiembro, marker: String, onClick: () -> Unit) {
    NeuCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeuAvatar(iniciales = iniciales(m.nombre), size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(m.nombre.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = Ink)
                Spacer(Modifier.height(2.dp))
                Text(if (m.count == 1) "1 aporte" else "${m.count} aportes", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
            Text(
                formatAporte(marker, "", m.totalAprobado),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = Sage,
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
        }
    }
}

private fun iniciales(nombre: String): String =
    nombre.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
