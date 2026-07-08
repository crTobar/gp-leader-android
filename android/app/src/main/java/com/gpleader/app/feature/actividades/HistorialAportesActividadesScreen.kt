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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.gpleader.app.core.data.repository.HistActividad
import com.gpleader.app.core.data.repository.HistFiltroTrimestre
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.OnResumeEffect
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuInsetSm
import com.gpleader.app.feature.miembro.miles

/** Formato de valor de aporte: ₡ si es monetario, si no número (+ unidad si viene). */
internal fun formatAporte(marker: String, unit: String, v: Double): String = when (marker) {
    "monetary" -> "₡${miles(v.toLong())}"
    else       -> if (unit.isNotBlank()) "${miles(v.toLong())} $unit" else miles(v.toLong())
}

internal fun filtroLabel(f: HistFiltroTrimestre): String = when (f) {
    HistFiltroTrimestre.ACTUAL     -> "Trimestre actual"
    HistFiltroTrimestre.ANTERIORES -> "Trimestres anteriores"
    HistFiltroTrimestre.TODOS      -> "Todos"
}

@Composable
fun HistorialAportesActividadesScreen(
    onNavigateBack:      () -> Unit,
    onNavigateToMiembros: (activityId: String, marker: String, titulo: String, filtro: HistFiltroTrimestre) -> Unit,
    viewModel: HistorialAportesActividadesViewModel = hiltViewModel(),
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
                Text("Historial", style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("Aportes aprobados", style = MaterialTheme.typography.bodyMedium, color = Mid)
            }
        }

        Spacer(Modifier.height(12.dp))

        FiltroDropdown(
            filtro   = uiState.filtro,
            onChange = viewModel::onFiltroChange,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(12.dp))

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            uiState.error != null -> Box(Modifier.fillMaxWidth().padding(24.dp)) {
                Text(uiState.error!!, color = Blush, style = MaterialTheme.typography.bodyMedium)
            }
            uiState.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin aportes aprobados", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            else -> LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
                items(uiState.items, key = { it.activityId }) { act ->
                    ActividadRow(act) {
                        onNavigateToMiembros(act.activityId, act.markerType, act.nombre, uiState.filtro)
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
internal fun FiltroDropdown(
    filtro:   HistFiltroTrimestre,
    onChange: (HistFiltroTrimestre) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .neuInsetSm(cornerRadius = 12.dp).clip(RoundedCornerShape(12.dp))
                .background(Background).clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(filtroLabel(filtro), style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Mid)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            HistFiltroTrimestre.entries.forEach { f ->
                DropdownMenuItem(
                    text    = { Text(filtroLabel(f), color = if (f == filtro) Accent else Ink) },
                    onClick = { expanded = false; onChange(f) },
                )
            }
        }
    }
}

@Composable
private fun ActividadRow(act: HistActividad, onClick: () -> Unit) {
    NeuCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(act.nombre, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Ink)
                Spacer(Modifier.height(2.dp))
                Text("Total aprobado", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
            Text(
                formatAporte(act.markerType, act.unitLabel, act.totalAprobado),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Sage,
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
        }
    }
}
