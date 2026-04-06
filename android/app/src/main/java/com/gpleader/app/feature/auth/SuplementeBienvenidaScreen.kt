package com.gpleader.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.Shadow
import com.gpleader.app.core.ui.theme.neuElevated

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun SuplementeBienvenidaScreen(
    onNavigateToRegistro: () -> Unit,
    viewModel: SuplementeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToRegistro) {
        if (uiState.navigateToRegistro) {
            onNavigateToRegistro()
            viewModel.consumeRegistroNavigation()
        }
    }

    SuplementeBienvenidaContent(
        uiState                  = uiState,
        onNombreSuplementeChange = viewModel::onNombreSuplementeChange,
        onComenzarRegistro       = viewModel::onComenzarRegistro,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun SuplementeBienvenidaContent(
    uiState:                  SuplementeUiState,
    onNombreSuplementeChange: (String) -> Unit,
    onComenzarRegistro:       () -> Unit,
) {
    val grupoInfo = uiState.grupoInfo ?: GrupoInfoSuplente(
        nombre      = "GP Los Olivos",
        diaSemana   = "Miércoles",
        horaInicio  = "7:00 PM",
        iglesia     = "Iglesia Central",
        liderNombre = "Maria Garcia",
    )

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Hero (Ink, 35%) ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(35f)
                .background(Ink),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .border(
                            width = 1.5.dp,
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = "✓",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Sage,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text  = stringResource(R.string.suplente_bienvenida_titulo),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = stringResource(R.string.suplente_bienvenida_subtitulo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }
        }

        // ── Body ──────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(65f)
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(24.dp))

            // Grupo info card
            GrupoInfoCard(grupoInfo = grupoInfo)

            Spacer(Modifier.height(16.dp))

            // Nota card (dashed Gold)
            NotaCard()

            Spacer(Modifier.height(24.dp))

            // Nombre suplente field
            NeuTextField(
                value         = uiState.nombreSuplente,
                onValueChange = onNombreSuplementeChange,
                label         = stringResource(R.string.suplente_bienvenida_label_nombre),
                placeholder   = stringResource(R.string.suplente_bienvenida_hint_nombre),
                modifier      = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(28.dp))

            NeuButtonPrimary(
                text     = stringResource(R.string.suplente_bienvenida_btn),
                onClick  = onComenzarRegistro,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

// ── Grupo info card ───────────────────────────────────────────────────────────

@Composable
private fun GrupoInfoCard(grupoInfo: GrupoInfoSuplente) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Background)
            .padding(20.dp),
    ) {
        Column {
            Text(
                text     = stringResource(R.string.suplente_bienvenida_grupo_label),
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text  = grupoInfo.nombre,
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "${grupoInfo.diaSemana} · ${grupoInfo.horaInicio} · ${grupoInfo.iglesia}",
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Shadow, thickness = 0.6.dp)
            Spacer(Modifier.height(16.dp))

            // Líder row
            Text(
                text     = stringResource(R.string.suplente_bienvenida_lider_label),
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Ink),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = grupoInfo.liderNombre
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .joinToString("") { it.first().uppercaseChar().toString() },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text  = grupoInfo.liderNombre,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink,
                )
            }
        }
    }
}

// ── Nota card (dashed Gold) ───────────────────────────────────────────────────

@Composable
private fun NotaCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Background)
            .dashedBorder(color = Gold, cornerRadius = 14.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text  = "●",
                style = MaterialTheme.typography.labelSmall,
                color = Gold,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text  = stringResource(R.string.suplente_bienvenida_nota),
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )
        }
    }
}

// ── Dashed border (local copy) ────────────────────────────────────────────────

private fun Modifier.dashedBorder(
    color:        Color,
    cornerRadius: Dp = 14.dp,
    strokeWidth:  Dp = 1.dp,
    dashWidth:    Dp = 6.dp,
    gapWidth:     Dp = 4.dp,
): Modifier = drawBehind {
    val strokePx = strokeWidth.toPx()
    val dashPx   = dashWidth.toPx()
    val gapPx    = gapWidth.toPx()
    val cornerPx = cornerRadius.toPx()
    drawIntoCanvas { canvas ->
        val paint = Paint()
        paint.asFrameworkPaint().apply {
            isAntiAlias  = true
            style        = android.graphics.Paint.Style.STROKE
            this.strokeWidth = strokePx
            this.color   = color.toArgb()
            pathEffect   = android.graphics.DashPathEffect(floatArrayOf(dashPx, gapPx), 0f)
        }
        canvas.drawRoundRect(
            left    = strokePx / 2f,
            top     = strokePx / 2f,
            right   = size.width  - strokePx / 2f,
            bottom  = size.height - strokePx / 2f,
            radiusX = cornerPx,
            radiusY = cornerPx,
            paint   = paint,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true, name = "Bienvenida suplente")
@Composable
private fun SuplementeBienvenidaPreview() {
    GpLeaderTheme {
        SuplementeBienvenidaContent(
            uiState = SuplementeUiState(
                grupoInfo = GrupoInfoSuplente(
                    nombre      = "GP Los Olivos",
                    diaSemana   = "Miércoles",
                    horaInicio  = "7:00 PM",
                    iglesia     = "Iglesia Central",
                    liderNombre = "Maria Garcia",
                ),
                nombreSuplente = "Juan Pérez",
            ),
            onNombreSuplementeChange = {},
            onComenzarRegistro       = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Bienvenida — nombre vacío")
@Composable
private fun SuplementeBienvenidaEmptyPreview() {
    GpLeaderTheme {
        SuplementeBienvenidaContent(
            uiState = SuplementeUiState(
                grupoInfo = GrupoInfoSuplente(
                    nombre      = "GP Los Olivos",
                    diaSemana   = "Miércoles",
                    horaInicio  = "7:00 PM",
                    iglesia     = "Iglesia Central",
                    liderNombre = "Maria Garcia",
                ),
            ),
            onNombreSuplementeChange = {},
            onComenzarRegistro       = {},
        )
    }
}
