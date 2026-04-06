package com.gpleader.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
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
import com.gpleader.app.core.ui.components.NeuButtonSecondary
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
import com.gpleader.app.feature.auth.SuplementeUiState
import com.gpleader.app.feature.auth.SuplementeViewModel

// ── Entry point ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetGenerarCodigoSuplente(
    onDismiss: () -> Unit,
    viewModel: SuplementeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showRevocarDialog) {
        RevocarDialog(
            onConfirm = { viewModel.onRevocarCodigoConfirm(); onDismiss() },
            onDismiss = viewModel::onDismissRevocarDialog,
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = Background,
        dragHandle       = { SheetHandle() },
    ) {
        SheetContent(
            uiState          = uiState,
            onCompartir      = { /* TODO: share intent */ },
            onGenerarNuevo   = viewModel::onGenerarCodigo,
            onRevocar        = viewModel::onShowRevocarDialog,
        )
    }
}

// ── Handle ────────────────────────────────────────────────────────────────────

@Composable
private fun SheetHandle() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Shadow),
        )
    }
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun SheetContent(
    uiState:        SuplementeUiState,
    onCompartir:    () -> Unit,
    onGenerarNuevo: () -> Unit,
    onRevocar:      () -> Unit,
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))

        // Header
        Text(
            text  = stringResource(R.string.sheet_codigo_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = stringResource(R.string.sheet_codigo_subtitulo),
            style     = MaterialTheme.typography.bodyMedium,
            color     = Mid,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))

        // 6 code digit boxes
        if (uiState.isGenerating || uiState.codigoGenerado.isEmpty()) {
            CodigoBoxesLoading()
        } else {
            CodigoBoxes(codigo = uiState.codigoGenerado)
        }

        Spacer(Modifier.height(20.dp))

        // Vigency progress bar
        VigenciaBar(expiraEn = uiState.codigoExpiraEn)

        Spacer(Modifier.height(20.dp))

        // Nota card (dashed Muted border)
        NotaCard()

        Spacer(Modifier.height(28.dp))

        // Compartir
        NeuButtonPrimary(
            text     = stringResource(R.string.sheet_codigo_btn_compartir),
            onClick  = onCompartir,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // Generar nuevo
        NeuButtonSecondary(
            text     = stringResource(R.string.sheet_codigo_btn_generar_nuevo),
            onClick  = onGenerarNuevo,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        HorizontalDivider(color = Shadow, thickness = 0.6.dp)

        Spacer(Modifier.height(8.dp))

        // Revocar — Blush text
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .neuElevated(cornerRadius = 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Background)
                .clickable(onClick = onRevocar)
                .padding(vertical = 14.dp),
        ) {
            Text(
                text  = stringResource(R.string.sheet_codigo_btn_revocar),
                style = MaterialTheme.typography.titleLarge,
                color = Blush,
            )
        }
    }
}

// ── 6 digit boxes ─────────────────────────────────────────────────────────────

@Composable
private fun CodigoBoxes(codigo: String) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        codigo.forEach { digit ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.75f)
                    .neuElevatedSm(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Ink),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = digit.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun CodigoBoxesLoading() {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        repeat(6) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.75f)
                    .neuElevatedSm(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundDeep),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier  = Modifier.size(20.dp),
                    color     = Muted,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

// ── Vigency bar ───────────────────────────────────────────────────────────────

@Composable
private fun VigenciaBar(expiraEn: Int) {
    val total    = 1440f
    val progress = (expiraEn / total).coerceIn(0f, 1f)
    val barColor = when {
        progress > 0.5f  -> Sage
        progress > 0.15f -> Gold
        else             -> Blush
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = stringResource(R.string.sheet_codigo_vigencia),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Text(
                text  = vigenciaText(expiraEn),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = barColor,
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress   = { progress },
            modifier   = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color      = barColor,
            trackColor = BackgroundDeep,
            strokeCap  = StrokeCap.Round,
        )
    }
}

private fun vigenciaText(minutos: Int): String = when {
    minutos >= 60 -> "${minutos / 60}h ${minutos % 60}min"
    minutos > 0   -> "${minutos}min restantes"
    else          -> "Expirado"
}

// ── Nota card (dashed) ────────────────────────────────────────────────────────

@Composable
private fun NotaCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Background)
            .dashedBorder(color = Muted, cornerRadius = 14.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text  = "●",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text  = stringResource(R.string.sheet_codigo_nota),
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )
        }
    }
}

// ── Revocar dialog ────────────────────────────────────────────────────────────

@Composable
private fun RevocarDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Background,
        title = {
            Text(
                text  = stringResource(R.string.sheet_codigo_revocar_titulo),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
        },
        text = {
            Text(
                text  = stringResource(R.string.sheet_codigo_revocar_cuerpo),
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text  = stringResource(R.string.sheet_codigo_revocar_confirmar),
                    color = Blush,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text  = stringResource(R.string.sheet_codigo_revocar_cancelar),
                    color = Mid,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
    )
}

// ── Dashed border (local copy) ────────────────────────────────────────────────

private fun Modifier.dashedBorder(
    color:        Color,
    cornerRadius: Dp  = 14.dp,
    strokeWidth:  Dp  = 1.dp,
    dashWidth:    Dp  = 6.dp,
    gapWidth:     Dp  = 4.dp,
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

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun SheetCodigoPreview() {
    GpLeaderTheme {
        SheetContent(
            uiState = SuplementeUiState(
                codigoGenerado = "847392",
                codigoExpiraEn = 900,
            ),
            onCompartir    = {},
            onGenerarNuevo = {},
            onRevocar      = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "Cargando")
@Composable
private fun SheetCodigoLoadingPreview() {
    GpLeaderTheme {
        SheetContent(
            uiState = SuplementeUiState(isGenerating = true),
            onCompartir    = {},
            onGenerarNuevo = {},
            onRevocar      = {},
        )
    }
}
