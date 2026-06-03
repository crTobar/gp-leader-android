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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.RegistroDiario
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInset
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ActividadCampanaScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActividadCampanaViewModel = hiltViewModel(),
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
                val marcados = uiState.dias.count { it.marcada }
                val total    = uiState.dias.size
                val progreso = if (total > 0) marcados.toFloat() / total else 0f

                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // ── Resumen ───────────────────────────────────────────────
                    item {
                        NeuCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                                Row(
                                    modifier          = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text  = "PROGRESO",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Muted,
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text       = "$marcados de $total días completados",
                                            style      = MaterialTheme.typography.bodyLarge,
                                            color      = Ink,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    Text(
                                        text       = "${(progreso * 100).toInt()}%",
                                        style      = MaterialTheme.typography.headlineMedium,
                                        color      = Accent,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(BackgroundDeep),
                                ) {
                                    if (progreso > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progreso)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Sage),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Días ─────────────────────────────────────────────────
                    items(uiState.dias, key = { it.fecha.toString() }) { dia ->
                        DiaRow(
                            dia      = dia,
                            toggling = uiState.togglingFecha == dia.fecha,
                            onClick  = { viewModel.onToggleDia(dia.fecha) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaRow(
    dia:      RegistroDiario,
    toggling: Boolean,
    onClick:  () -> Unit,
) {
    val hoy         = LocalDate.now()
    val esFuturo    = dia.fecha.isAfter(hoy)
    val esHoy       = dia.fecha == hoy
    val fmtDia      = DateTimeFormatter.ofPattern("EEEE d MMM", Locale("es"))
    val fmtHora     = DateTimeFormatter.ofPattern("HH:mm", Locale("es"))
    val nombreDia   = dia.fecha.format(fmtDia).replaceFirstChar { it.uppercase() }
    val horaMarcada = dia.marcadaEn?.atZone(ZoneId.systemDefault())?.format(fmtHora)

    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Badge número del día
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            dia.marcada -> Sage.copy(alpha = 0.15f)
                            esHoy       -> Accent.copy(alpha = 0.12f)
                            else        -> BackgroundDeep
                        }
                    ),
            ) {
                Text(
                    text       = dia.fecha.dayOfMonth.toString(),
                    style      = MaterialTheme.typography.labelSmall,
                    color      = when {
                        dia.marcada -> Sage
                        esHoy       -> Accent
                        else        -> Muted
                    },
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = nombreDia,
                        style      = MaterialTheme.typography.bodyLarge,
                        color      = if (dia.marcada) Mid else Ink,
                        fontWeight = if (dia.marcada) FontWeight.Normal else FontWeight.Medium,
                    )
                    if (esHoy && !dia.marcada) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Accent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text  = "HOY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Accent,
                            )
                        }
                    }
                }
                if (dia.marcada && horaMarcada != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = "Marcada a las $horaMarcada",
                        style = MaterialTheme.typography.labelSmall,
                        color = Sage,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Checkbox neumórfico
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .then(
                        if (dia.marcada) Modifier.neuInset(cornerRadius = 10.dp)
                        else Modifier.neuElevatedSm(cornerRadius = 10.dp)
                    )
                    .background(
                        color = if (dia.marcada) Sage.copy(alpha = 0.15f) else Background,
                        shape = RoundedCornerShape(10.dp),
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = !toggling && !esFuturo, onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    toggling    -> CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        color       = Accent,
                        strokeWidth = 2.dp,
                    )
                    dia.marcada -> Icon(
                        imageVector        = Icons.Default.Check,
                        contentDescription = "Completado",
                        tint               = Sage,
                        modifier           = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}
