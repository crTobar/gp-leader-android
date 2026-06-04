package com.gpleader.app.feature.actividades

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.DiaStat
import com.gpleader.app.core.data.repository.MiembroMarcado
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampanaDetalleScreen(
    onNavigateBack: () -> Unit,
    viewModel: CampanaDetalleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // ── TopBar ────────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink)
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text       = uiState.nombreCampana,
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = viewModel::onRefresh,
            modifier     = Modifier.fillMaxSize(),
        indicator = {},
        ) {
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }

            uiState.error != null -> Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(uiState.error!!, style = MaterialTheme.typography.bodyLarge, color = Blush)
            }

            else -> {
                val totalDias       = uiState.dias.size
                val diasConMitad    = uiState.dias.count { it.completados >= it.total / 2f && it.total > 0 }
                val promedioGeneral = if (totalDias > 0 && uiState.dias.firstOrNull()?.total ?: 0 > 0) {
                    (uiState.dias.sumOf { it.completados }.toFloat() /
                            (uiState.dias.sumOf { it.total }.toFloat())) * 100
                } else 0f

                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // ── Resumen ───────────────────────────────────────────────
                    item {
                        NeuCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text  = "ESTADÍSTICAS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Muted,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text       = "$totalDias días · ${uiState.dias.firstOrNull()?.total ?: 0} miembros",
                                        style      = MaterialTheme.typography.bodyLarge,
                                        color      = Ink,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                Text(
                                    text       = "${promedioGeneral.toInt()}%",
                                    style      = MaterialTheme.typography.headlineMedium,
                                    color      = if (promedioGeneral >= 50) Sage else Blush,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    // ── Días ─────────────────────────────────────────────────
                    items(uiState.dias, key = { it.fecha.toString() }) { dia ->
                        DiaStatRow(
                            dia          = dia,
                            expanded     = uiState.expandedFecha == dia.fecha,
                            togglingKey  = uiState.togglingKey,
                            onClick      = { viewModel.onToggleDia(dia.fecha) },
                            onToggleMiembro = { miembroId, marcado ->
                                viewModel.onToggleMiembro(miembroId, dia.fecha, marcado)
                            },
                        )
                    }
                }
            }
        }
        } // PullToRefreshBox
    }
}

@Composable
private fun DiaStatRow(
    dia:             DiaStat,
    expanded:        Boolean,
    togglingKey:     String?,
    onClick:         () -> Unit,
    onToggleMiembro: (miembroId: String, marcadoActualmente: Boolean) -> Unit,
) {
    val mes       = dia.fecha.month.getDisplayName(TextStyle.SHORT, Locale("es")).uppercase()
    val diaN      = dia.fecha.dayOfMonth
    val diaNombre = dia.fecha.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es"))
        .replaceFirstChar { it.uppercase() }
    val esHoy     = dia.fecha == LocalDate.now()
    val esFuturo  = dia.fecha.isAfter(LocalDate.now())

    val chipBg    = when {
        esFuturo       -> BackgroundDeep
        dia.completados >= (dia.total / 2f).coerceAtLeast(1f) -> Sage.copy(alpha = 0.15f)
        else           -> Blush.copy(alpha = 0.15f)
    }
    val chipColor = when {
        esFuturo       -> Muted
        dia.completados >= (dia.total / 2f).coerceAtLeast(1f) -> Sage
        else           -> Blush
    }

    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Fecha badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.width(36.dp),
                ) {
                    Text(text = mes, style = MaterialTheme.typography.labelSmall, color = if (esHoy) Accent else Mid)
                    Text(
                        text       = diaN.toString(),
                        style      = MaterialTheme.typography.titleLarge,
                        color      = if (esHoy) Accent else Ink,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.width(14.dp))

                // Nombre del día
                Text(
                    text       = diaNombre,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.weight(1f),
                )

                // Chip X/Y
                if (!esFuturo) {
                    Box(
                        modifier = Modifier
                            .background(chipBg, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text       = "${dia.completados}/${dia.total}",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = chipColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Lista expandible de miembros
            AnimatedVisibility(visible = expanded && !esFuturo) {
                Column {
                    HorizontalDivider(
                        color     = BackgroundDeep,
                        modifier  = Modifier.padding(horizontal = 16.dp),
                    )
                    dia.miembros.forEach { miembro ->
                        MiembroRow(
                            miembro     = miembro,
                            isToggling  = togglingKey == "${miembro.id}_${dia.fecha}",
                            onToggle    = { onToggleMiembro(miembro.id, miembro.marcado) },
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun MiembroRow(
    miembro:    MiembroMarcado,
    isToggling: Boolean,
    onToggle:   () -> Unit,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isToggling, onClick = onToggle)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (miembro.marcado) Sage else BackgroundDeep),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = miembro.nombre,
                style = MaterialTheme.typography.bodyMedium,
                color = if (miembro.marcado) Ink else Muted,
            )
            if (miembro.marcado && miembro.marcadaEn != null) {
                val hora = remember(miembro.marcadaEn) {
                    DateTimeFormatter.ofPattern("HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .format(miembro.marcadaEn)
                }
                Text(
                    text  = hora,
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
        if (isToggling) {
            CircularProgressIndicator(
                modifier    = Modifier.size(18.dp),
                color       = Accent,
                strokeWidth = 2.dp,
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (miembro.marcado) Sage else BackgroundDeep),
            ) {
                if (miembro.marcado) {
                    Icon(
                        imageVector        = Icons.Default.Check,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
