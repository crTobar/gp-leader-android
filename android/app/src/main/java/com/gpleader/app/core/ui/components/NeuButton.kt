package com.gpleader.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuGlow

// ── Shared internals ──────────────────────────────────────────────────────────

private val ButtonShape   = RoundedCornerShape(14.dp)
private val ButtonPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)

@Composable
private fun NeuButtonBase(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    background: Color,
    textColor: Color,
    shadowModifier: Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(shadowModifier)
            .clip(ButtonShape)
            .background(color = background, shape = ButtonShape)
            .clickable(onClick = onClick)
            .padding(ButtonPadding),
    ) {
        Text(
            text      = text,
            style     = MaterialTheme.typography.titleLarge,
            color     = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Public buttons ────────────────────────────────────────────────────────────

/**
 * Botón primario — fondo [Accent] azul, texto blanco, halo azul (neuGlow).
 */
@Composable
fun NeuButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NeuButtonBase(
        text           = text,
        onClick        = onClick,
        modifier       = modifier,
        background     = Accent,
        textColor      = Color.White,
        shadowModifier = Modifier.neuGlow(cornerRadius = 14.dp),
    )
}

/**
 * Botón secundario — fondo [Background] gris perla, texto [Accent] azul, sombra elevada.
 */
@Composable
fun NeuButtonSecondary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NeuButtonBase(
        text           = text,
        onClick        = onClick,
        modifier       = modifier,
        background     = Background,
        textColor      = Accent,
        shadowModifier = Modifier.neuElevated(cornerRadius = 14.dp),
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun NeuButtonsPreview() {
    GpLeaderTheme {
        Column(
            modifier            = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NeuButtonPrimary(
                text     = "Enviar al pastor",
                onClick  = {},
                modifier = Modifier
                    .widthIn(min = 240.dp)
                    .padding(8.dp),
            )
            NeuButtonSecondary(
                text     = "Guardar borrador",
                onClick  = {},
                modifier = Modifier
                    .widthIn(min = 240.dp)
                    .padding(8.dp),
            )
        }
    }
}
