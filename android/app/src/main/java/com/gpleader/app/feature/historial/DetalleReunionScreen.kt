package com.gpleader.app.feature.historial

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.gpleader.app.core.ui.components.AppBottomNavBar
import com.gpleader.app.core.ui.components.NAV_TAB_HISTORIAL
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import java.time.DayOfWeek
import java.time.LocalDate

// ── Helpers de fecha ──────────────────────────────────────────────────────────

private val DIAS_CORTOS_DET = arrayOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
private val DIAS_LARGOS_DET = arrayOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
private val MESES_MAYUS_DET = arrayOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)

private fun LocalDate.diaIdxDet() = if (dayOfWeek == DayOfWeek.SUNDAY) 0 else dayOfWeek.value

/** "Dom 12 De Abril" — para el top bar */
private fun LocalDate.formatoTopBarDet(): String {
    val mes = MESES_MAYUS_DET[monthValue - 1]
    return "${DIAS_CORTOS_DET[diaIdxDet()]} $dayOfMonth De $mes"
}

/** "Domingo 12 De Abril" — para la hero card */
private fun LocalDate.formatoHeroDet(): String {
    val mes = MESES_MAYUS_DET[monthValue - 1]
    return "${DIAS_LARGOS_DET[diaIdxDet()]} $dayOfMonth De $mes"
}

/** Iniciales de nombre completo: "Ana Martínez López" → "AM" */
private fun inicialesDet(nombre: String): String {
    val partes = nombre.trim().split(" ").filter { it.isNotEmpty() }
    return when {
        partes.size >= 2 -> "${partes[0].first()}${partes[1].first()}".uppercase()
        partes.isNotEmpty() -> partes[0].take(2).uppercase()
        else -> "?"
    }
}

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun DetalleReunionScreen(
    onNavigateBack:          () -> Unit,
    onNavigateToHome:        () -> Unit = {},
    onNavigateToHistorial:   () -> Unit = {},
    onNavigateToActividades: () -> Unit = {},
    onEditarClick:           () -> Unit = {},
    viewModel: DetalleReunionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    DetalleReunionContent(
        uiState                 = uiState,
        onNavigateBack          = onNavigateBack,
        onNavigateToHome        = onNavigateToHome,
        onNavigateToHistorial   = onNavigateToHistorial,
        onNavigateToActividades = onNavigateToActividades,
        onEditarClick           = onEditarClick,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun DetalleReunionContent(
    uiState:                 DetalleReunionUiState,
    onNavigateBack:          () -> Unit,
    onNavigateToHome:        () -> Unit = {},
    onNavigateToHistorial:   () -> Unit = {},
    onNavigateToActividades: () -> Unit = {},
    onEditarClick:           () -> Unit = {},
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
        bottomBar = {
            AppBottomNavBar(
                selectedTab        = NAV_TAB_HISTORIAL,
                onInicioClick      = onNavigateToHome,
                onHistorialClick   = onNavigateToHistorial,
                onActividadesClick = onNavigateToActividades,
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Cargando…", style = MaterialTheme.typography.bodyLarge, color = Muted)
                }
            }
            uiState.error != null -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(uiState.error, style = MaterialTheme.typography.bodyLarge, color = Muted)
                }
            }
            else -> LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Hero card ─────────────────────────────────────────────────────
            item { HeroCard(uiState = uiState) }

            // ── Stats row ─────────────────────────────────────────────────────
            item { StatsRow(uiState = uiState) }

            // ── Sección Asistencia ────────────────────────────────────────────
            if (uiState.asistencias.isNotEmpty()) {
                item {
                    Text(
                        text       = "Asistencia",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = Ink,
                        modifier   = Modifier.padding(top = 4.dp),
                    )
                }
                items(uiState.asistencias, key = { it.nombre + it.estado }) { asistencia ->
                    MiembroCard(asistencia = asistencia)
                }
            }

            // ── Sección Actividades ───────────────────────────────────────────
            if (uiState.actividades.isNotEmpty()) {
                val nivelesOrden   = listOf("UNION", "PASTOR", "GP")
                val labelNivel     = mapOf("UNION" to "UNIÓN", "PASTOR" to "PASTOR", "GP" to "MI GP")
                val colorNivel     = mapOf<String, Color>(
                    "UNION"  to Gold,
                    "PASTOR" to Accent,
                    "GP"     to Sage,
                )

                item {
                    Text(
                        text       = "Actividades",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = Ink,
                        modifier   = Modifier.padding(top = 4.dp),
                    )
                }
                nivelesOrden.forEach { nivel ->
                    val acts = uiState.actividades.filter { it.nivel == nivel }
                    if (acts.isNotEmpty()) {
                        item(key = "header_$nivel") {
                            NivelHeader(
                                label = labelNivel[nivel] ?: nivel,
                                color = colorNivel[nivel] ?: Muted,
                            )
                        }
                        items(acts, key = { nivel + it.nombre }) { act ->
                            ActividadFila(actividad = act)
                        }
                    }
                }
            }

            // ── Botón editar ──────────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable(onClick = onEditarClick),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Edit,
                        contentDescription = null,
                        tint               = Accent,
                        modifier           = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = "Editar Reunión",
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = Accent,
                    )
                }
            }
        } // LazyColumn
        } // else
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

        Text(
            text  = fecha.formatoTopBarDet(),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )
    }
}

// ── Hero card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(uiState: DetalleReunionUiState) {
    val (badgeLabel, badgeColor) = when (uiState.estado) {
        EstadoReunionHistorial.ENVIADA        -> "Enviada"   to Sage
        EstadoReunionHistorial.PENDIENTE_SYNC -> "Pendiente" to Gold
    }
    val semana    = (uiState.fecha.dayOfMonth - 1) / 7 + 1
    val subTitulo = "Mes ${uiState.fecha.monthValue}, Semana $semana"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Background)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Badge estado — outline pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, badgeColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text(
                        text  = badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                    )
                }

                // Chip tipo reunión (solo si es culto de sábado)
                if (uiState.tipoReunion == "saturday_worship") {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Accent.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text  = "Culto de Sábado",
                            style = MaterialTheme.typography.labelSmall,
                            color = Accent,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Fecha grande
            Text(
                text       = uiState.fecha.formatoHeroDet(),
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = Ink,
            )

            // Mes / Semana
            Text(
                text  = subTitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )

            // Subtexto: suplente
            if (uiState.creadaPorSuplente && uiState.suplementeNombre != null) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Filled.Person,
                        contentDescription = null,
                        tint               = Muted,
                        modifier           = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "Registro enviado por ${uiState.suplementeNombre} · ${uiState.fecha.formatoTopBarDet()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "Registro enviado por suplente: ${uiState.suplementeNombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
            }
        }
    }
}

// ── Stats row ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(uiState: DetalleReunionUiState) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCell(valor = uiState.presentes.toString(),          label = "PRESENTES",     color = Sage,   modifier = Modifier.weight(1f))
        StatCell(valor = uiState.justificados.toString(),       label = "JUSTIFICA\nDOS",color = Gold,   modifier = Modifier.weight(1f))
        StatCell(valor = uiState.ausentes.toString(),           label = "AUSENTES",      color = Blush,  modifier = Modifier.weight(1f))
        StatCell(valor = "${uiState.porcentajeAsistencia}%",    label = "ASIST.",        color = Accent, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(
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
        Spacer(Modifier.height(2.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            maxLines = 2,
        )
    }
}

// ── Tarjeta de miembro ────────────────────────────────────────────────────────

@Composable
private fun MiembroCard(asistencia: AsistenciaDetalle) {
    val (badgeBg, badgeLabel) = when (asistencia.estado) {
        "P"  -> Sage  to "P"
        "A"  -> Blush to "A"
        "J"  -> Gold  to "J"
        else -> Muted to asistencia.estado
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevatedSm(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar con iniciales
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(BackgroundDeep),
            ) {
                Text(
                    text       = inicialesDet(asistencia.nombre),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = Mid,
                )
            }

            Spacer(Modifier.width(12.dp))

            // Nombre
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = asistencia.nombre,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = Ink,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                if (asistencia.esVisita) {
                    Text(
                        text  = "Visita",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
                if (!asistencia.iglesiaVisitada.isNullOrBlank()) {
                    Text(
                        text  = asistencia.iglesiaVisitada,
                        style = MaterialTheme.typography.labelSmall,
                        color = Accent,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Badge P / A / J
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeBg),
            ) {
                Text(
                    text       = badgeLabel,
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                )
            }
        }
    }
}

// ── Actividades ───────────────────────────────────────────────────────────────

@Composable
private fun NivelHeader(label: String, color: Color) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .neuElevatedSm(cornerRadius = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Background)
            .padding(vertical = 10.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Spacer(Modifier.width(10.dp))
        Text(text = "☆", style = MaterialTheme.typography.labelSmall, color = color)
        Spacer(Modifier.width(6.dp))
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color      = Ink,
        )
    }
}

@Composable
private fun ActividadFila(actividad: ActividadDetalle) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .neuElevatedSm(cornerRadius = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "☆", style = MaterialTheme.typography.bodyMedium, color = Muted)
        Spacer(Modifier.width(10.dp))
        Text(
            text     = actividad.nombre,
            style    = MaterialTheme.typography.bodyLarge,
            color    = Ink,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        if (actividad.cantidad != null) {
            Text(
                text       = "${actividad.cantidad} ${actividad.unidad}",
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = Accent,
            )
        } else {
            Text(text = "—", style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

// ── Bottom nav bar ────────────────────────────────────────────────────────────

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun DetallePresentsPreview() {
    GpLeaderTheme {
        DetalleReunionContent(
            uiState = DetalleReunionUiState(
                reunionId            = "r1",
                fecha                = LocalDate.of(2026, 4, 12),
                estado               = EstadoReunionHistorial.ENVIADA,
                presentes            = 4,
                ausentes             = 0,
                justificados         = 0,
                porcentajeAsistencia = 100,
                asistencias = listOf(
                    AsistenciaDetalle("Ana Martínez López", "P"),
                    AsistenciaDetalle("John Casildo",       "P"),
                    AsistenciaDetalle("Juan Carlos Pérez",  "P"),
                    AsistenciaDetalle("María García",       "P"),
                ),
                actividades = listOf(
                    ActividadDetalle("Visitas pastorales", "PASTOR", 1, "visitas"),
                    ActividadDetalle("Estudio bíblico",   "GP",     2, "sesiones"),
                ),
            ),
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true, name = "Ausentes 0%")
@Composable
private fun DetalleAusentesPreview() {
    GpLeaderTheme {
        DetalleReunionContent(
            uiState = DetalleReunionUiState(
                reunionId            = "r2",
                fecha                = LocalDate.of(2026, 4, 13),
                estado               = EstadoReunionHistorial.ENVIADA,
                presentes            = 0,
                ausentes             = 4,
                justificados         = 0,
                porcentajeAsistencia = 0,
                asistencias = listOf(
                    AsistenciaDetalle("Ana Martínez López", "A"),
                    AsistenciaDetalle("John Casildo",       "A"),
                    AsistenciaDetalle("Juan Carlos Pérez",  "A"),
                    AsistenciaDetalle("María García",       "A"),
                ),
                actividades = listOf(
                    ActividadDetalle("Visitas pastorales", "PASTOR", 1, "visitas"),
                ),
            ),
            onNavigateBack = {},
        )
    }
}
