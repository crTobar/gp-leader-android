package com.gpleader.app.feature.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.AccentLight
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated

// ── Datos de muestra ──────────────────────────────────────────────────────────

private data class PuntoAsistencia(val semana: String, val pct: Float)
private data class BarActividad(val nombre: String, val cantidad: Int, val color: Color)

private val SAMPLE_TENDENCIA = listOf(
    PuntoAsistencia("S1",  72f),
    PuntoAsistencia("S2",  85f),
    PuntoAsistencia("S3",  60f),
    PuntoAsistencia("S4",  90f),
    PuntoAsistencia("S5",  78f),
    PuntoAsistencia("S6",  95f),
    PuntoAsistencia("S7",  82f),
    PuntoAsistencia("S8",  88f),
)

private val SAMPLE_ACTIVIDADES = listOf(
    BarActividad("Estudios bíblicos",  8,  Accent),
    BarActividad("Oración semanal",   12,  Sage),
    BarActividad("Visitas realizadas", 5,  Gold),
    BarActividad("Libros entregados",  3,  Blush),
)

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun ReportesScreen(
    onNavigateBack: () -> Unit,
) {
    ReportesContent(
        tendencia      = SAMPLE_TENDENCIA,
        actividades    = SAMPLE_ACTIVIDADES,
        promedioPct    = 81,
        ultSemana      = 88,
        semanas        = 8,
        onNavigateBack = onNavigateBack,
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun ReportesContent(
    tendencia:      List<PuntoAsistencia>,
    actividades:    List<BarActividad>,
    promedioPct:    Int,
    ultSemana:      Int,
    semanas:        Int,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .neuElevated(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Background)
                    .clickable(onClick = onNavigateBack)
                    .padding(10.dp),
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint               = Ink,
                    modifier           = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text       = stringResource(R.string.perfil_reportes),
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )
        }

        LazyColumn(
            modifier        = Modifier.fillMaxSize(),
            contentPadding  = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Stats resumidas ───────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        label  = "PROMEDIO",
                        valor  = "$promedioPct%",
                        color  = Accent,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label  = "ÚLT. SEMANA",
                        valor  = "$ultSemana%",
                        color  = Sage,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label  = "SEMANAS",
                        valor  = "$semanas",
                        color  = Gold,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Gráfico de tendencia de asistencia ────────────────────────────
            item {
                Text(
                    text  = "TENDENCIA DE ASISTENCIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
            item {
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LineChart(
                            puntos   = tendencia,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        // Eje X — etiquetas de semanas
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            tendencia.forEach { punto ->
                                Text(
                                    text  = punto.semana,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Muted,
                                )
                            }
                        }
                    }
                }
            }

            // ── Gráfico de actividades del período ────────────────────────────
            item {
                Text(
                    text  = "ACTIVIDADES DEL PERÍODO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
            item {
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        BarChart(
                            barras   = actividades,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        // Leyenda
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            actividades.forEach { bar ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(bar.color),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text     = bar.nombre,
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = Mid,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        text       = "${bar.cantidad}",
                                        style      = MaterialTheme.typography.labelSmall,
                                        color      = Ink,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Stat card ─────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    label:    String,
    valor:    String,
    color:    Color,
    modifier: Modifier = Modifier,
) {
    NeuCard(modifier = modifier) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text       = valor,
                style      = MaterialTheme.typography.titleLarge,
                color      = color,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
    }
}

// ── Line chart ────────────────────────────────────────────────────────────────

@Composable
private fun LineChart(
    puntos:   List<PuntoAsistencia>,
    modifier: Modifier = Modifier,
) {
    if (puntos.isEmpty()) return
    val accentColor = Accent
    val fillStart   = AccentLight.copy(alpha = 0.25f)
    val fillEnd     = Accent.copy(alpha = 0f)
    val gridColor   = BackgroundDeep

    Canvas(modifier = modifier) {
        val w       = size.width
        val h       = size.height
        val padding = 8.dp.toPx()
        val maxPct  = 100f
        val minPct  = 0f
        val xStep   = (w - padding * 2) / (puntos.size - 1).coerceAtLeast(1)

        fun xOf(i: Int) = padding + i * xStep
        fun yOf(pct: Float) = h - padding - (pct - minPct) / (maxPct - minPct) * (h - padding * 2)

        // Grid lines at 25%, 50%, 75%
        listOf(25f, 50f, 75f).forEach { pct ->
            val y = yOf(pct)
            drawLine(
                color       = gridColor,
                start       = Offset(0f, y),
                end         = Offset(w, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        // Fill under the line
        val fillPath = Path().apply {
            moveTo(xOf(0), h)
            lineTo(xOf(0), yOf(puntos.first().pct))
            puntos.forEachIndexed { i, pt ->
                lineTo(xOf(i), yOf(pt.pct))
            }
            lineTo(xOf(puntos.lastIndex), h)
            close()
        }
        drawPath(
            path  = fillPath,
            brush = Brush.verticalGradient(
                colors     = listOf(fillStart, fillEnd),
                startY     = 0f,
                endY       = h,
            ),
        )

        // Line
        val linePath = Path().apply {
            moveTo(xOf(0), yOf(puntos.first().pct))
            puntos.forEachIndexed { i, pt ->
                lineTo(xOf(i), yOf(pt.pct))
            }
        }
        drawPath(
            path  = linePath,
            color = accentColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )

        // Dots
        puntos.forEachIndexed { i, pt ->
            drawCircle(color = accentColor, radius = 4.dp.toPx(), center = Offset(xOf(i), yOf(pt.pct)))
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(xOf(i), yOf(pt.pct)))
        }
    }
}

// ── Bar chart ─────────────────────────────────────────────────────────────────

@Composable
private fun BarChart(
    barras:   List<BarActividad>,
    modifier: Modifier = Modifier,
) {
    if (barras.isEmpty()) return
    val maxVal = barras.maxOf { it.cantidad }.toFloat().coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val w          = size.width
        val h          = size.height
        val padding    = 8.dp.toPx()
        val barCount   = barras.size
        val totalGap   = padding * (barCount + 1)
        val barWidth   = (w - totalGap) / barCount
        val cornerR    = 4.dp.toPx()
        val availableH = h - padding

        barras.forEachIndexed { i, bar ->
            val barH = (bar.cantidad / maxVal) * availableH
            val left = padding + i * (barWidth + padding)
            val top  = h - barH

            drawRoundRect(
                color        = bar.color.copy(alpha = 0.15f),
                topLeft      = Offset(left, padding),
                size         = androidx.compose.ui.geometry.Size(barWidth, h - padding * 2),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerR),
            )
            drawRoundRect(
                color        = bar.color,
                topLeft      = Offset(left, top),
                size         = androidx.compose.ui.geometry.Size(barWidth, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerR),
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun ReportesPreview() {
    GpLeaderTheme {
        ReportesContent(
            tendencia      = SAMPLE_TENDENCIA,
            actividades    = SAMPLE_ACTIVIDADES,
            promedioPct    = 81,
            ultSemana      = 88,
            semanas        = 8,
            onNavigateBack = {},
        )
    }
}
