package com.gpleader.app.feature.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun ExitoEnviadoScreen(
    onNavigateToHome:      () -> Unit,
    onNavigateToHistorial: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ExitoEnviadoContent(
        uiState               = uiState,
        onNavigateToHome      = onNavigateToHome,
        onNavigateToHistorial = onNavigateToHistorial,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun ExitoEnviadoContent(
    uiState:               RegistroUiState,
    onNavigateToHome:      () -> Unit,
    onNavigateToHistorial: () -> Unit,
) {
    val presentes    = uiState.miembros.count { it.estado == EstadoAsistencia.PRESENTE } +
                       uiState.visitasDeHoy.count { it.estado == EstadoAsistencia.PRESENTE }
    val ausentes     = uiState.miembros.count { it.estado == EstadoAsistencia.AUSENTE } +
                       uiState.visitasDeHoy.count { it.estado == EstadoAsistencia.AUSENTE }
    val justificados = uiState.miembros.count { it.estado == EstadoAsistencia.JUSTIFICADO }
    val total        = uiState.miembros.size + uiState.visitasDeHoy.size
    val pct          = if (total > 0) presentes * 100 / total else 0
    val visitasCount = uiState.visitasDeHoy.size

    val fechaLarga   = uiState.fecha.formatoLargoES()
    val fechaCorta   = uiState.fecha.formatoResumen()   // "Lun 16 Mar 2026"
    val hora         = remember { horaActual() }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Hero ~40% ─────────────────────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .background(Ink)
                .statusBarsPadding(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // ✓ en cuadrado con borde blanco
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(72.dp)
                        .border(2.dp, Color.White, RoundedCornerShape(18.dp)),
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Check,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(40.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text      = stringResource(R.string.exito_enviado_titulo),
                    style     = MaterialTheme.typography.displayLarge,
                    color     = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text      = fechaLarga,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = Muted,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // ── Body ~60% ─────────────────────────────────────────────────────────
        LazyColumn(
            modifier            = Modifier
                .fillMaxWidth()
                .weight(0.6f),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Timestamp
            item {
                Text(
                    text      = stringResource(R.string.exito_enviado_timestamp, fechaCorta, hora),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = Muted,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )
            }

            // NeuCard pastor
            item {
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier              = Modifier.padding(16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Avatar placeholder cuadrado redondeado
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(BackgroundDeep),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = stringResource(R.string.exito_pastor_nombre),
                                style = MaterialTheme.typography.titleLarge,
                                color = Ink,
                            )
                            Text(
                                text  = stringResource(R.string.exito_pastor_rol),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                            )
                        }
                        // Círculo ✓ fondo Ink
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Ink),
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Check,
                                contentDescription = null,
                                tint               = Color.White,
                                modifier           = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            // NeuCard stats
            item {
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        StatFila(stringResource(R.string.exito_resumen_presentes),  presentes.toString())
                        StatDivider()
                        StatFila(stringResource(R.string.exito_resumen_ausentes),   ausentes.toString())
                        StatDivider()
                        StatFila(stringResource(R.string.exito_stats_justificados), justificados.toString())
                        StatDivider()
                        StatFila(stringResource(R.string.exito_stats_asistencia),   "$pct%")
                        StatDivider()
                        StatFila(stringResource(R.string.exito_stats_visitas),      visitasCount.toString())
                    }
                }
            }

            // Botones
            item {
                Column(
                    modifier            = Modifier.navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NeuButtonPrimary(
                        text     = stringResource(R.string.exito_enviado_btn),
                        onClick  = onNavigateToHome,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    NeuButtonSecondary(
                        text     = stringResource(R.string.exito_enviado_btn_historial),
                        onClick  = onNavigateToHistorial,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// ── Helpers de stats ─────────────────────────────────────────────────────────

@Composable
private fun StatFila(label: String, value: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = Mid,
            modifier = Modifier.weight(1f),
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color      = Ink,
        )
    }
}

@Composable
private fun StatDivider() {
    HorizontalDivider(color = Muted.copy(alpha = 0.25f))
}

// ── Formateadores de fecha/hora ───────────────────────────────────────────────

private val DIAS_LARGOS_ES  = arrayOf("Domingo","Lunes","Martes","Miércoles","Jueves","Viernes","Sábado")
private val MESES_CORTOS_ES = arrayOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")

private fun LocalDate.formatoLargoES(): String {
    val idx = if (dayOfWeek == DayOfWeek.SUNDAY) 0 else dayOfWeek.value
    return "${DIAS_LARGOS_ES[idx]} $dayOfMonth ${MESES_CORTOS_ES[monthValue - 1]} $year"
}

private fun horaActual(): String {
    val t = LocalTime.now()
    val h = when {
        t.hour == 0  -> 12
        t.hour > 12  -> t.hour - 12
        else         -> t.hour
    }
    val m = t.minute.toString().padStart(2, '0')
    return "$h:$m ${if (t.hour < 12) "AM" else "PM"}"
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun ExitoEnviadoPreview() {
    GpLeaderTheme {
        ExitoEnviadoContent(
            uiState = RegistroUiState(
                fecha  = LocalDate.of(2026, 3, 18),
                miembros = listOf(
                    MiembroAsistencia("m1", "Ana",    "AC", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m2", "Jose",   "JR", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m3", "Lucia",  "LM", EstadoAsistencia.AUSENTE),
                    MiembroAsistencia("m4", "Carlos", "CP", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m5", "Rosa",   "RT", EstadoAsistencia.JUSTIFICADO),
                    MiembroAsistencia("m6", "Miguel", "MS", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m7", "Carmen", "CV", EstadoAsistencia.PRESENTE),
                ),
                visitasDeHoy = listOf(
                    VisitaHoy("v1", "Juan Lopez", esNueva = false, EstadoAsistencia.PRESENTE),
                ),
            ),
            onNavigateToHome      = {},
            onNavigateToHistorial = {},
        )
    }
}
