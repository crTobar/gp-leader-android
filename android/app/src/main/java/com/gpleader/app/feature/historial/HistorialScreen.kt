package com.gpleader.app.feature.historial

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.SwipeAction
import com.gpleader.app.core.ui.components.SwipeableItem
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
import com.gpleader.app.core.ui.theme.Shadow
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInsetSm
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun HistorialScreen(
    onNavigateToHome:    () -> Unit,
    onNavigateToDetalle: (String) -> Unit,
    onNavigateToPerfil:  () -> Unit = {},
    viewModel: HistorialViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToDetalle) {
        val id = uiState.navigateToDetalle
        if (id != null) {
            viewModel.consumeDetalleNavigation()
            onNavigateToDetalle(id)
        }
    }

    HistorialContent(
        uiState                = uiState,
        onNavigateToHome       = onNavigateToHome,
        onNavigateToPerfil     = onNavigateToPerfil,
        onTrimestralChange     = viewModel::onTrimestralChange,
        onVerTodoClick         = viewModel::onVerTodoClick,
        onBuscarClick          = viewModel::onBuscarClick,
        onReunionClick         = viewModel::onReunionClick,
        onEditarReunionClick   = viewModel::onEditarReunionClick,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun HistorialContent(
    uiState:              HistorialUiState,
    onNavigateToHome:     () -> Unit,
    onNavigateToPerfil:   () -> Unit,
    onTrimestralChange:   (String) -> Unit,
    onVerTodoClick:       () -> Unit,
    onBuscarClick:        () -> Unit,
    onReunionClick:       (String) -> Unit,
    onEditarReunionClick: (String) -> Unit = {},
) {
    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavBar(
                onInicioClick    = onNavigateToHome,
                onHistorialClick = { },
                onPerfilClick    = onNavigateToPerfil,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier              = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement   = Arrangement.spacedBy(12.dp),
            contentPadding        = PaddingValues(bottom = 20.dp),
        ) {
            // ── Top bar ───────────────────────────────────────────────────────
            item {
                HistorialTopBar(
                    onBuscarClick = onBuscarClick,
                    modifier      = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                )
            }

            // ── Chips de trimestres ───────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Background)
                        .padding(bottom = 4.dp),
                ) {
                    TrimestresRow(
                        trimestres            = uiState.trimestres,
                        trimestreSeleccionado = uiState.trimestreSeleccionado,
                        onTrimestralChange    = onTrimestralChange,
                        onVerTodoClick        = onVerTodoClick,
                    )
                }
            }

            // ── Stats row ─────────────────────────────────────────────────────
            item {
                StatsRow(
                    stats    = uiState.stats,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // ── Grupos de mes ─────────────────────────────────────────────────
            uiState.grupos.forEach { grupo ->
                item(key = "header_${grupo.anio}_${grupo.mes}") {
                    MesHeader(
                        mes      = grupo.mes,
                        anio     = grupo.anio,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }
                items(grupo.reuniones, key = { it.id }) { reunion ->
                    ReunionCard(
                        reunion              = reunion,
                        onClick              = { onReunionClick(reunion.id) },
                        onEditarClick        = { onEditarReunionClick(reunion.id) },
                        modifier             = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun HistorialTopBar(
    onBuscarClick: () -> Unit,
    modifier:      Modifier = Modifier,
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = stringResource(R.string.historial_titulo),
            style    = MaterialTheme.typography.displayLarge,
            color    = Ink,
            modifier = Modifier.weight(1f),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .neuElevatedSm(cornerRadius = 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Background)
                .clickable(onClick = onBuscarClick)
                .padding(12.dp),
        ) {
            Icon(
                imageVector        = Icons.Filled.Search,
                contentDescription = stringResource(R.string.historial_btn_buscar),
                tint               = Ink,
                modifier           = Modifier.size(22.dp),
            )
        }
    }
}

// ── Chips de trimestres ───────────────────────────────────────────────────────

@Composable
private fun TrimestresRow(
    trimestres:            List<Trimestre>,
    trimestreSeleccionado: String,
    onTrimestralChange:    (String) -> Unit,
    onVerTodoClick:        () -> Unit,
) {
    Row(
        modifier              = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        trimestres.forEach { trimestre ->
            val activo = trimestre.id == trimestreSeleccionado
            val parts      = trimestre.nombre.split(" ", limit = 2)
            val numeracion = parts.getOrElse(0) { trimestre.nombre }
            val rango      = parts.getOrElse(1) { "" }
            TrimestralChip(
                numeracion = numeracion,
                rango      = rango,
                activo     = activo,
                onClick    = { onTrimestralChange(trimestre.id) },
            )
        }
        VerTodoChip(
            activo  = trimestreSeleccionado == "todo",
            onClick = onVerTodoClick,
        )
    }
}

@Composable
private fun TrimestralChip(
    numeracion: String,
    rango:      String,
    activo:     Boolean,
    onClick:    () -> Unit,
) {
    val animSpec  = spring<Color>(stiffness = Spring.StiffnessMedium)
    val bg        by animateColorAsState(
        targetValue   = if (activo) Ink else Background,
        animationSpec = animSpec,
        label         = "chipBg",
    )
    val line1Color by animateColorAsState(
        targetValue   = if (activo) Color.White else Ink,
        animationSpec = animSpec,
        label         = "chipLine1",
    )
    val line2Color by animateColorAsState(
        targetValue   = if (activo) Color.White.copy(alpha = 0.65f) else Muted,
        animationSpec = animSpec,
        label         = "chipLine2",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .widthIn(min = 80.dp)
            .then(
                if (activo) Modifier.neuGlow(cornerRadius = 20.dp)
                else        Modifier.neuElevated(cornerRadius = 20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Línea 1: "1er Trim." bold + ✓ Sage si activo
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text       = "$numeracion Trim.",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = line1Color,
                )
                if (activo) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector        = Icons.Filled.Check,
                        contentDescription = null,
                        tint               = Sage,
                        modifier           = Modifier.size(10.dp),
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            // Línea 2: "Ene-Mar"
            Text(
                text  = rango,
                style = MaterialTheme.typography.labelSmall,
                color = line2Color,
            )
        }
    }
}

@Composable
private fun VerTodoChip(
    activo:  Boolean,
    onClick: () -> Unit,
) {
    val animSpec  = spring<Color>(stiffness = Spring.StiffnessMedium)
    val bg        by animateColorAsState(
        targetValue   = if (activo) Ink else Background,
        animationSpec = animSpec,
        label         = "verTodoBg",
    )
    val textColor by animateColorAsState(
        targetValue   = if (activo) Color.White else Accent,
        animationSpec = animSpec,
        label         = "verTodoText",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .widthIn(min = 80.dp)
            .then(
                if (activo) Modifier.neuGlow(cornerRadius = 20.dp)
                else        Modifier.neuElevated(cornerRadius = 20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .drawWithContent {
                drawContent()
                if (!activo) {
                    val strokePx = 1.2.dp.toPx()
                    val dashPx   = 8.dp.toPx()
                    val gapPx    = 5.dp.toPx()
                    drawRoundRect(
                        color        = Accent,
                        topLeft      = Offset(strokePx / 2, strokePx / 2),
                        size         = Size(size.width - strokePx, size.height - strokePx),
                        cornerRadius = CornerRadius(20.dp.toPx()),
                        style        = Stroke(
                            width      = strokePx,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(dashPx, gapPx), 0f,
                            ),
                        ),
                    )
                }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text  = stringResource(R.string.historial_tab_ver_todo),
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
            )
            if (activo) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector        = Icons.Filled.Check,
                    contentDescription = null,
                    tint               = Sage,
                    modifier           = Modifier.size(10.dp),
                )
            }
        }
    }
}

// ── Stats row ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(
    stats:    HistorialStats,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCelda(
            valor    = "${stats.promedioAsistencia}%",
            label    = stringResource(R.string.historial_stat_promedio),
            fondoInk = true,
            modifier = Modifier.weight(1f),
        )
        StatCelda(
            valor    = stats.totalReuniones.toString(),
            label    = stringResource(R.string.historial_stat_reuniones),
            modifier = Modifier.weight(1f),
        )
        StatCelda(
            valor    = stats.enviadas.toString(),
            label    = stringResource(R.string.historial_stat_enviadas),
            modifier = Modifier.weight(1f),
        )
        StatCelda(
            valor    = stats.pendientes.toString(),
            label    = stringResource(R.string.historial_stat_pendiente),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCelda(
    valor:    String,
    label:    String,
    fondoInk: Boolean  = false,
    modifier: Modifier = Modifier,
) {
    val cardBg   = if (fondoInk) Ink else Background
    val numColor = if (fondoInk) Color.White else Ink
    val lblColor = if (fondoInk) Sage else Muted

    Box(
        modifier = modifier
            .height(90.dp)
            .neuElevated(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text      = valor,
                style     = if (fondoInk)
                    MaterialTheme.typography.displayLarge
                else
                    MaterialTheme.typography.headlineMedium,
                color     = numColor,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text          = label,
                style         = MaterialTheme.typography.labelSmall,
                color         = lblColor,
                textAlign     = TextAlign.Center,
                maxLines      = 1,
                fontSize      = 9.sp,
                letterSpacing = 1.sp,
            )
        }
    }
}

// ── Header de mes ─────────────────────────────────────────────────────────────

private val MESES_HISTORIAL = mapOf(
    Month.JANUARY   to "ENERO",
    Month.FEBRUARY  to "FEBRERO",
    Month.MARCH     to "MARZO",
    Month.APRIL     to "ABRIL",
    Month.MAY       to "MAYO",
    Month.JUNE      to "JUNIO",
    Month.JULY      to "JULIO",
    Month.AUGUST    to "AGOSTO",
    Month.SEPTEMBER to "SEPTIEMBRE",
    Month.OCTOBER   to "OCTUBRE",
    Month.NOVEMBER  to "NOVIEMBRE",
    Month.DECEMBER  to "DICIEMBRE",
)

@Composable
private fun MesHeader(
    mes:      Month,
    anio:     Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = Shadow,
            thickness = 1.dp,
        )
        Text(
            text  = "${MESES_HISTORIAL[mes]} $anio",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = Shadow,
            thickness = 1.dp,
        )
    }
}

// ── Tarjeta de reunión ────────────────────────────────────────────────────────

private val DIAS_CORTOS  = arrayOf("Dom","Lun","Mar","Mié","Jue","Vie","Sáb")
private val MESES_CORTOS = arrayOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")

private fun LocalDate.diaSemanaCorto(): String {
    val idx = if (dayOfWeek == DayOfWeek.SUNDAY) 0 else dayOfWeek.value
    return DIAS_CORTOS[idx]
}

@Composable
private fun ReunionCard(
    reunion:       ReunionResumen,
    onClick:       () -> Unit,
    onEditarClick: () -> Unit = {},
    modifier:      Modifier = Modifier,
) {
    val esPendiente = reunion.estado == EstadoReunionHistorial.PENDIENTE_SYNC
    val pct         = reunion.porcentajeAsistencia
    val diaSemana   = reunion.fecha.diaSemanaCorto()
    val mes         = MESES_CORTOS[reunion.fecha.monthValue - 1]

    // Animación de escala al presionar
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed         by interactionSource.collectIsPressedAsState()
    val scale             by animateFloatAsState(
        targetValue   = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "cardScale",
    )

    // Animación de progreso
    val animatedProgress by animateFloatAsState(
        targetValue   = pct / 100f,
        animationSpec = spring(
            stiffness    = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy,
        ),
        label = "progress",
    )

    SwipeableItem(
        itemKey      = reunion.id,
        onItemClick  = onClick,
        swipeActions = if (reunion.permitirEdicion) listOf(
            SwipeAction(
                label   = stringResource(R.string.historial_accion_editar),
                color   = Accent,
                onClick = onEditarClick,
            )
        ) else emptyList(),
        modifier = modifier,
    ) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Background),
            // Sin .clickable — el tap lo maneja SwipeableItem via onItemClick
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── Columna fecha ──────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (esPendiente) Muted else BackgroundDeep)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                val fechaTextColor = if (esPendiente) Mid else Muted
                val numColor       = if (esPendiente) Mid else Ink
                Text(
                    text  = diaSemana.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = fechaTextColor,
                )
                Text(
                    text       = reunion.fecha.dayOfMonth.toString(),
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = numColor,
                )
                Text(
                    text  = mes.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = fechaTextColor,
                )
            }

            Spacer(Modifier.width(12.dp))

            // ── Contenido ─────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = stringResource(R.string.historial_reunion_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )

                Spacer(Modifier.height(5.dp))

                // Badges estado + editar
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BadgeEstado(esPendiente = esPendiente)
                    if (reunion.permitirEdicion) {
                        BadgeEditar()
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Dots + porcentaje
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DotsAsistencia(
                        presentes    = reunion.presentes,
                        ausentes     = reunion.ausentes,
                        justificados = reunion.justificados,
                        modifier     = Modifier.weight(1f),
                    )
                    Text(
                        text       = stringResource(R.string.historial_pct, pct),
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color      = Ink,
                    )
                }

                Spacer(Modifier.height(7.dp))

                // Barra de progreso neumórfica
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .neuInsetSm(cornerRadius = 3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BackgroundDeep),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Sage),
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // ── Flecha ────────────────────────────────────────────────────────
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = Muted,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
    } // SwipeableItem
}

// ── Badges ────────────────────────────────────────────────────────────────────

@Composable
private fun BadgeEstado(esPendiente: Boolean) {
    val bg    = if (esPendiente) Gold else Sage
    val label = if (esPendiente)
        stringResource(R.string.historial_badge_pendiente)
    else
        stringResource(R.string.historial_badge_enviada)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}

@Composable
private fun BadgeEditar() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .drawWithContent {
                drawContent()
                val strokePx = 1.dp.toPx()
                val dashPx   = 6.dp.toPx()
                val gapPx    = 4.dp.toPx()
                drawRoundRect(
                    color        = Accent,
                    topLeft      = Offset(strokePx / 2, strokePx / 2),
                    size         = Size(size.width - strokePx, size.height - strokePx),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    style        = Stroke(
                        width      = strokePx,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(dashPx, gapPx), 0f,
                        ),
                    ),
                )
            }
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text  = stringResource(R.string.historial_badge_editar),
            style = MaterialTheme.typography.labelSmall,
            color = Accent,
        )
    }
}

// ── Dots de asistencia ────────────────────────────────────────────────────────

@Composable
private fun DotsAsistencia(
    presentes:    Int,
    ausentes:     Int,
    justificados: Int,
    modifier:     Modifier = Modifier,
) {
    Row(
        modifier              = modifier,
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DotLabel(presentes,    Sage,  R.string.historial_dots_presentes)
        DotLabel(ausentes,     Blush, R.string.historial_dots_ausentes)
        if (justificados > 0) {
            DotLabel(justificados, Muted, R.string.historial_dots_justificados)
        }
    }
}

@Composable
private fun DotLabel(count: Int, color: Color, res: Int) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
        Text(
            text  = stringResource(res, count),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

// ── Bottom nav bar ────────────────────────────────────────────────────────────

@Composable
private fun BottomNavBar(
    onInicioClick:    () -> Unit,
    onHistorialClick: () -> Unit,
    onPerfilClick:    () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        NeuCard(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                NavTabItem(Icons.Default.Home,      stringResource(R.string.home_nav_inicio),    false, onInicioClick)
                NavTabItem(Icons.Default.DateRange, stringResource(R.string.home_nav_historial), true,  onHistorialClick)
                NavTabItem(Icons.Default.Person,    stringResource(R.string.home_nav_perfil),    false, onPerfilClick)
            }
        }
    }
}

@Composable
private fun NavTabItem(
    icon:     ImageVector,
    label:    String,
    isActive: Boolean,
    onClick:  () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = if (isActive) Accent else Muted,
            modifier           = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Accent else Muted,
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun HistorialPreview() {
    GpLeaderTheme {
        val reuniones = listOf(
            ReunionResumen("r1", LocalDate.of(2026, 3, 4),  EstadoReunionHistorial.ENVIADA,        6, 1, 1, 75, true),
            ReunionResumen("r2", LocalDate.of(2026, 2, 26), EstadoReunionHistorial.ENVIADA,        7, 1, 0, 86),
            ReunionResumen("r3", LocalDate.of(2026, 2, 19), EstadoReunionHistorial.ENVIADA,        6, 2, 0, 71),
            ReunionResumen("r4", LocalDate.of(2026, 2, 12), EstadoReunionHistorial.PENDIENTE_SYNC, 5, 3, 0, 62),
        )
        val grupos = listOf(
            GrupoMes(Month.MARCH,    2026, reuniones.filter { it.fecha.month == Month.MARCH }),
            GrupoMes(Month.FEBRUARY, 2026, reuniones.filter { it.fecha.month == Month.FEBRUARY }),
        )
        HistorialContent(
            uiState = HistorialUiState(
                trimestres = listOf(
                    Trimestre("t1", "1er Ene-Mar", LocalDate.of(2026, 1, 1),  LocalDate.of(2026, 3, 31)),
                    Trimestre("t2", "2do Abr-Jun", LocalDate.of(2026, 4, 1),  LocalDate.of(2026, 6, 30)),
                    Trimestre("t3", "3er Jul-Sep", LocalDate.of(2026, 7, 1),  LocalDate.of(2026, 9, 30)),
                    Trimestre("t4", "4to Oct-Dic", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 31)),
                ),
                trimestreSeleccionado = "t1",
                grupos  = grupos,
                stats   = HistorialStats(78, 8, 7, 1),
            ),
            onNavigateToHome   = {},
            onNavigateToPerfil = {},
            onTrimestralChange = {},
            onVerTodoClick     = {},
            onBuscarClick      = {},
            onReunionClick     = {},
        )
    }
}
