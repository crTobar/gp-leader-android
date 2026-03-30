package com.gpleader.app.feature.historial

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.Shadow
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun LocalDate.formatoLargo(): String {
    val mes  = month.getDisplayName(TextStyle.FULL, Locale("es"))
        .replaceFirstChar { it.uppercase() }
    return "$dayOfMonth de $mes, $year"
}

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun DetalleReunionScreen(
    onNavigateBack: () -> Unit,
    viewModel: DetalleReunionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    DetalleReunionContent(uiState = uiState, onNavigateBack = onNavigateBack)
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun DetalleReunionContent(
    uiState:        DetalleReunionUiState,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            DetalleTopBar(
                fecha          = uiState.fecha,
                onNavigateBack = onNavigateBack,
                modifier       = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Banner suplente
            if (uiState.creadaPorSuplente) {
                item {
                    BannerSuplente(nombre = uiState.suplementeNombre ?: "")
                }
            }

            // Stats
            item { StatsCard(uiState = uiState) }

            // Asistencia
            if (uiState.asistencias.isNotEmpty()) {
                item { AsistenciaCard(asistencias = uiState.asistencias) }
            }

            // Actividades por nivel
            val niveles = listOf("UNION", "PASTOR", "GP")
            val labelNivel = mapOf("UNION" to "UNIÓN", "PASTOR" to "PASTOR", "GP" to "MI GP")
            niveles.forEach { nivel ->
                val acts = uiState.actividades.filter { it.nivel == nivel }
                if (acts.isNotEmpty()) {
                    item {
                        ActividadesCard(
                            nivelLabel  = labelNivel[nivel] ?: nivel,
                            actividades = acts,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun DetalleTopBar(
    fecha:          LocalDate,
    onNavigateBack: () -> Unit,
    modifier:       Modifier = Modifier,
) {
    Box(
        modifier         = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .neuElevatedSm(cornerRadius = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Background)
                .clickable(onClick = onNavigateBack)
                .padding(10.dp),
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint               = Ink,
                modifier           = Modifier.size(20.dp),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = "Reunión Semanal",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Text(
                text  = fecha.formatoLargo(),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }
    }
}

// ── Banner suplente ───────────────────────────────────────────────────────────

@Composable
private fun BannerSuplente(nombre: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Ink)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text  = if (nombre.isNotBlank()) "Registrado por $nombre · Modo suplente"
                    else "Registrado en modo suplente",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}

// ── Stats card ────────────────────────────────────────────────────────────────

@Composable
private fun StatsCard(uiState: DetalleReunionUiState) {
    val badgeEstado = when (uiState.estado) {
        EstadoReunionHistorial.ENVIADA        -> "Enviada"   to Sage
        EstadoReunionHistorial.PENDIENTE_SYNC -> "Pendiente" to Gold
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 28.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Background)
            .padding(20.dp),
    ) {
        Column {
            // Badge estado
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeEstado.second.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text  = badgeEstado.first,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeEstado.second,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Cuatro celdas de stats en fila
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatChip(
                    valor  = uiState.presentes.toString(),
                    label  = "PRESENTES",
                    color  = Sage,
                    modifier = Modifier.weight(1f),
                )
                StatChip(
                    valor  = uiState.ausentes.toString(),
                    label  = "AUSENTES",
                    color  = Blush,
                    modifier = Modifier.weight(1f),
                )
                StatChip(
                    valor  = uiState.justificados.toString(),
                    label  = "JUSTIF.",
                    color  = Muted,
                    modifier = Modifier.weight(1f),
                )
                StatChip(
                    valor  = "${uiState.porcentajeAsistencia}%",
                    label  = "ASIST.",
                    color  = Ink,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    valor:    String,
    label:    String,
    color:    Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier
            .neuElevatedSm(cornerRadius = 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Background)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = valor,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
    }
}

// ── Asistencia card ───────────────────────────────────────────────────────────

@Composable
private fun AsistenciaCard(asistencias: List<AsistenciaDetalle>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 28.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Background)
            .padding(20.dp),
    ) {
        Column {
            Text(
                text     = "ASISTENCIA",
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            asistencias.forEachIndexed { i, asistencia ->
                AsistenciaFila(asistencia = asistencia)
                if (i < asistencias.lastIndex) {
                    HorizontalDivider(color = Shadow, thickness = 0.6.dp)
                }
            }
        }
    }
}

@Composable
private fun AsistenciaFila(asistencia: AsistenciaDetalle) {
    val (badgeBg, badgeColor) = when (asistencia.estado) {
        "P"  -> Sage  to Color.White
        "A"  -> Blush to Color.White
        else -> Muted to Color.White
    }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = asistencia.nombre,
                style    = MaterialTheme.typography.bodyLarge,
                color    = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (asistencia.esVisita) {
                Text(
                    text  = "Visita",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeBg),
        ) {
            Text(
                text  = asistencia.estado,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = badgeColor,
            )
        }
    }
}

// ── Actividades card ──────────────────────────────────────────────────────────

@Composable
private fun ActividadesCard(
    nivelLabel:  String,
    actividades: List<ActividadDetalle>,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 28.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Background)
            .padding(20.dp),
    ) {
        Column {
            Text(
                text     = nivelLabel,
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            actividades.forEachIndexed { i, actividad ->
                ActividadFila(actividad = actividad)
                if (i < actividades.lastIndex) {
                    HorizontalDivider(color = Shadow, thickness = 0.6.dp)
                }
            }
        }
    }
}

@Composable
private fun ActividadFila(actividad: ActividadDetalle) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = actividad.nombre,
            style    = MaterialTheme.typography.bodyLarge,
            color    = Ink,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(12.dp))
        if (actividad.cantidad != null) {
            Text(
                text  = "${actividad.cantidad} ${actividad.unidad}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Ink,
            )
        } else {
            Text(
                text  = "—",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun DetalleReunionPreview() {
    GpLeaderTheme {
        DetalleReunionContent(
            uiState = DetalleReunionUiState(
                reunionId            = "r1",
                fecha                = LocalDate.of(2026, 3, 4),
                estado               = EstadoReunionHistorial.ENVIADA,
                presentes            = 6,
                ausentes             = 1,
                justificados         = 1,
                porcentajeAsistencia = 75,
                asistencias = listOf(
                    AsistenciaDetalle("Carlos Ramírez", "P"),
                    AsistenciaDetalle("Ana López",      "P"),
                    AsistenciaDetalle("Luis Hernández", "A"),
                    AsistenciaDetalle("Sofía Vargas",   "J"),
                    AsistenciaDetalle("Juan García",    "P", esVisita = true),
                ),
                actividades = listOf(
                    ActividadDetalle("Estudio bíblico",    "UNION",  1,    "sesión"),
                    ActividadDetalle("Visitas realizadas", "GP",     2,    "personas"),
                    ActividadDetalle("Misioneros del mes", "PASTOR", null, "personas"),
                ),
            ),
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true, name = "Suplente + Pendiente")
@Composable
private fun DetalleSuplementePreview() {
    GpLeaderTheme {
        DetalleReunionContent(
            uiState = DetalleReunionUiState(
                reunionId            = "r3",
                fecha                = LocalDate.of(2026, 2, 19),
                estado               = EstadoReunionHistorial.PENDIENTE_SYNC,
                presentes            = 5,
                ausentes             = 3,
                justificados         = 0,
                porcentajeAsistencia = 62,
                creadaPorSuplente    = true,
                suplementeNombre     = "José Ramírez",
                asistencias = listOf(
                    AsistenciaDetalle("Carlos Ramírez", "P"),
                    AsistenciaDetalle("Ana López",      "A"),
                    AsistenciaDetalle("Luis Hernández", "A"),
                ),
                actividades = listOf(
                    ActividadDetalle("Estudio bíblico", "UNION", 1, "sesión"),
                ),
            ),
            onNavigateBack = {},
        )
    }
}
