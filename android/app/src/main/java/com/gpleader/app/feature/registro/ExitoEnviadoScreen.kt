package com.gpleader.app.feature.registro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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
    val esSabado     = uiState.registryKind == RegistryKind.SATURDAY_WORSHIP

    // Confeti — corre los primeros 3s; se reinicia al tocar el círculo
    var confettiKey by remember { mutableStateOf(0) }
    val particles = remember(confettiKey) { buildConfettiParticles() }
    var confettiTime by remember(confettiKey) { mutableStateOf(0L) }
    val showConfetti = confettiTime < 3000L

    LaunchedEffect(confettiKey) {
        val start = withInfiniteAnimationFrameMillis { it }
        while (true) {
            val now = withInfiniteAnimationFrameMillis { it }
            confettiTime = now - start
            if (confettiTime >= 3000L) break
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // ── Confeti overlay ───────────────────────────────────────────────────
        if (showConfetti) {
            val progress = (confettiTime / 3000f).coerceIn(0f, 1f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { p ->
                    val y = p.startY + p.vy * progress * size.height * 1.4f
                    val x = p.startX * size.width + sin(p.vx * progress * 6f) * 40f
                    val alpha = if (progress > 0.7f) 1f - ((progress - 0.7f) / 0.3f) else 1f
                    rotate(degrees = p.rotation * progress * 360f, pivot = androidx.compose.ui.geometry.Offset(x, y)) {
                        if (p.isCircle) {
                            drawCircle(color = p.color.copy(alpha = alpha), radius = p.size, center = androidx.compose.ui.geometry.Offset(x, y))
                        } else {
                            drawRect(color = p.color.copy(alpha = alpha), topLeft = androidx.compose.ui.geometry.Offset(x - p.size, y - p.size * 0.5f), size = androidx.compose.ui.geometry.Size(p.size * 2f, p.size))
                        }
                    }
                }
            }
        }

        // ── Contenido centrado ────────────────────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── Círculo con checkmark (toca para volver a lanzar confeti) ────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .neuElevated(cornerRadius = 60.dp)
                    .clip(CircleShape)
                    .background(Background)
                    .clickable { confettiKey++ },
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2EA86A)),
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Check,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(40.dp),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Título ────────────────────────────────────────────────────────
            Text(
                text      = "¡Listo!",
                style     = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                color     = Ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text      = if (esSabado) "Culto de sábado registrado" else "Reunión enviada al pastor",
                style     = MaterialTheme.typography.bodyLarge,
                color     = Mid,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(36.dp))

            // ── Tarjetas de stats ─────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    count    = presentes,
                    label    = "PRESENTES",
                    color    = Color(0xFF2EA86A),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    count    = ausentes,
                    label    = "AUSENTES",
                    color    = Blush,
                    modifier = Modifier.weight(1f),
                )
                if (!esSabado) {
                    StatCard(
                        count    = justificados,
                        label    = "JUSTIF.",
                        color    = Gold,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Botón Volver al inicio ────────────────────────────────────────
            NeuCard(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .fillMaxWidth(),
                onClick  = onNavigateToHome,
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Home,
                        contentDescription = null,
                        tint               = Accent,
                        modifier           = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text       = "Volver al inicio",
                        style      = MaterialTheme.typography.titleLarge,
                        color      = Ink,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ── StatCard ─────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(count: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    NeuCard(modifier = modifier) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text       = count.toString(),
                style      = MaterialTheme.typography.displayLarge.copy(fontSize = 38.sp),
                color      = color,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelSmall,
                color     = Muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Confeti ───────────────────────────────────────────────────────────────────

private data class ConfettiParticle(
    val startX:   Float,
    val startY:   Float,
    val vx:       Float,
    val vy:       Float,
    val color:    Color,
    val size:     Float,
    val rotation: Float,
    val isCircle: Boolean,
)

private fun buildConfettiParticles(): List<ConfettiParticle> {
    val colors = listOf(
        Color(0xFF2EA86A), Color(0xFF3B7DD8), Color(0xFFC9A84C),
        Color(0xFFD4836A), Color(0xFF6497E0), Color(0xFF6AAB8E),
    )
    return List(80) {
        ConfettiParticle(
            startX   = Random.nextFloat(),
            startY   = -Random.nextFloat() * 200f,
            vx       = Random.nextFloat() * 4f - 2f,
            vy       = 0.4f + Random.nextFloat() * 0.6f,
            color    = colors[Random.nextInt(colors.size)],
            size     = 6f + Random.nextFloat() * 8f,
            rotation = Random.nextFloat() * 3f - 1.5f,
            isCircle = Random.nextBoolean(),
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun ExitoEnviadoPreview() {
    GpLeaderTheme {
        ExitoEnviadoContent(
            uiState = RegistroUiState(
                fecha  = LocalDate.of(2026, 6, 5),
                miembros = listOf(
                    MiembroAsistencia("m1", "Ana",    "AC", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m2", "Jose",   "JR", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m3", "Lucia",  "LM", EstadoAsistencia.AUSENTE),
                    MiembroAsistencia("m4", "Carlos", "CP", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m5", "Rosa",   "RT", EstadoAsistencia.JUSTIFICADO),
                ),
            ),
            onNavigateToHome      = {},
            onNavigateToHistorial = {},
        )
    }
}
