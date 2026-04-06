package com.gpleader.app.feature.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevated
import java.time.LocalDate

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun ExitoOfflineScreen(
    onNavigateToHome: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ExitoOfflineContent(
        uiState          = uiState,
        onNavigateToHome = onNavigateToHome,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun ExitoOfflineContent(
    uiState:          RegistroUiState,
    onNavigateToHome: () -> Unit,
) {
    val fechaCorta = "${uiState.fecha.dayOfMonth} ${MESES_OFFLINE[uiState.fecha.monthValue - 1]} ${uiState.fecha.year}"

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Hero ~40% ─────────────────────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .background(Mid)
                .statusBarsPadding(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Ícono cuadrado con cuadrado interno (sin conexión)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(72.dp)
                        .border(2.dp, Color.White, RoundedCornerShape(18.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(7.dp)),
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text      = stringResource(R.string.exito_offline_titulo),
                    style     = MaterialTheme.typography.displayLarge,
                    color     = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = stringResource(R.string.exito_offline_subtitulo),
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        // ── Body ~60% ─────────────────────────────────────────────────────────
        LazyColumn(
            modifier            = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(Background),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // NeuCard punteada: estado pendiente
            item {
                DashedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Placeholder ícono cuadrado
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BackgroundDeep),
                        )
                        Text(
                            text  = stringResource(R.string.exito_offline_pendiente),
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink,
                        )
                        Text(
                            text  = stringResource(R.string.exito_offline_esperando, fechaCorta),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )
                    }
                }
            }

            // NeuCard punteada: nota tranquilizadora
            item {
                DashedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text      = stringResource(R.string.exito_offline_nota),
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = Mid,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(20.dp),
                    )
                }
            }

            // Botón
            item {
                NeuButtonPrimary(
                    text     = stringResource(R.string.exito_offline_btn),
                    onClick  = onNavigateToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                )
            }
        }
    }
}

// ── NeuCard con borde punteado ────────────────────────────────────────────────

@Composable
private fun DashedCard(
    modifier:     Modifier = Modifier,
    cornerRadius: Dp       = 28.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .neuElevated(cornerRadius = cornerRadius)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Background)
            .drawWithContent {
                drawContent()
                val strokePx = 1.5.dp.toPx()
                val dashPx   = 12.dp.toPx()
                val gapPx    = 7.dp.toPx()
                val r        = cornerRadius.toPx()
                drawRoundRect(
                    color        = Muted,
                    topLeft      = Offset(strokePx / 2, strokePx / 2),
                    size         = Size(size.width - strokePx, size.height - strokePx),
                    cornerRadius = CornerRadius(r),
                    style        = Stroke(
                        width      = strokePx,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(dashPx, gapPx), 0f,
                        ),
                    ),
                )
            },
    ) {
        content()
    }
}

// ── Constantes ───────────────────────────────────────────────────────────────

private val MESES_OFFLINE = arrayOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun ExitoOfflinePreview() {
    GpLeaderTheme {
        ExitoOfflineContent(
            uiState = RegistroUiState(
                fecha  = LocalDate.of(2026, 3, 18),
                miembros = listOf(
                    MiembroAsistencia("m1", "Ana",    "AC", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m2", "Jose",   "JR", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m3", "Lucia",  "LM", EstadoAsistencia.AUSENTE),
                    MiembroAsistencia("m4", "Carlos", "CP", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m5", "Rosa",   "RT", EstadoAsistencia.JUSTIFICADO),
                ),
                visitasDeHoy = listOf(
                    VisitaHoy("v1", "Juan Lopez", esNueva = false, EstadoAsistencia.PRESENTE),
                ),
            ),
            onNavigateToHome = {},
        )
    }
}
