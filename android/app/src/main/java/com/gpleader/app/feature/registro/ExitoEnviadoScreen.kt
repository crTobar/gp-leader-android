package com.gpleader.app.feature.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Ícono checkmark ───────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .size(72.dp)
                            .neuElevated(cornerRadius = 36.dp)
                            .clip(CircleShape)
                            .background(Background),
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Check,
                            contentDescription = null,
                            tint               = Sage,
                            modifier           = Modifier.size(32.dp),
                        )
                    }
                }
            }

            // ── Título y fecha ────────────────────────────────────────────────
            item {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text      = stringResource(R.string.exito_enviado_titulo),
                        style     = MaterialTheme.typography.headlineMedium,
                        color     = Ink,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = fechaLarga.uppercase(),
                        style     = MaterialTheme.typography.labelSmall,
                        color     = Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // ── Card de stats ─────────────────────────────────────────────────
            item {
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        StatFilaColoreada(
                            icono = "✓",
                            label = stringResource(R.string.exito_resumen_presentes),
                            valor = presentes.toString(),
                            color = Sage,
                        )
                        StatDivider()
                        StatFilaColoreada(
                            icono = "✕",
                            label = stringResource(R.string.exito_resumen_ausentes),
                            valor = ausentes.toString(),
                            color = Blush,
                        )
                        StatDivider()
                        StatFilaColoreada(
                            icono = "◷",
                            label = stringResource(R.string.exito_stats_justificados),
                            valor = justificados.toString(),
                            color = Gold,
                        )
                        StatDivider()
                        StatFilaColoreada(
                            icono = "≡",
                            label = stringResource(R.string.exito_stats_asistencia),
                            valor = "$pct%",
                            color = Accent,
                        )
                    }
                }
            }

            // ── Sección actividades ───────────────────────────────────────────
            if (uiState.actividades.any { it.cantidad != null }) {
                item {
                    Text(
                        text  = stringResource(R.string.paso3_label_actividades),
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                item {
                    NeuCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            uiState.actividades
                                .filter { it.cantidad != null }
                                .forEachIndexed { idx, act ->
                                    if (idx > 0) StatDivider()
                                    StatFilaActividad(
                                        nombre   = act.nombre,
                                        cantidad = act.cantidad ?: 0,
                                        unidad   = act.unidad,
                                    )
                                }
                        }
                    }
                }
            }

            // ── Botones ───────────────────────────────────────────────────────
            item {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NeuButtonPrimary(
                        text     = stringResource(R.string.exito_enviado_btn),
                        onClick  = onNavigateToHome,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text      = stringResource(R.string.exito_enviado_btn_historial),
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = Accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier  = Modifier.clickable(onClick = onNavigateToHistorial),
                    )
                }
            }
        }
    }
}

// ── Helpers de stats ─────────────────────────────────────────────────────────

@Composable
private fun StatFilaColoreada(icono: String, label: String, valor: String, color: Color) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
        ) {
            Text(text = icono, color = color, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = Mid,
            modifier = Modifier.weight(1f),
        )
        Text(
            text       = valor,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color      = color,
        )
    }
}

@Composable
private fun StatFilaActividad(nombre: String, cantidad: Int, unidad: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = nombre,
            style    = MaterialTheme.typography.bodyMedium,
            color    = Ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            text       = "$cantidad $unidad",
            style      = MaterialTheme.typography.bodyMedium,
            color      = Muted,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text       = "Completada",
            style      = MaterialTheme.typography.labelSmall,
            color      = Sage,
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
