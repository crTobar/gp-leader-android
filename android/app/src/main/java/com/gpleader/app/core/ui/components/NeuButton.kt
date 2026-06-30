package com.gpleader.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuGlow

// ── Shared internals ──────────────────────────────────────────────────────────

private val ButtonPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)

@Composable
private fun NeuButtonBase(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    background: Color,
    textColor: Color,
    shadowModifier: Modifier,
    cornerRadius: Dp,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val contentColor = if (enabled) textColor else textColor.copy(alpha = 0.5f)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .background(color = if (enabled) background else background.copy(alpha = 0.5f), shape = shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(ButtonPadding),
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector        = leadingIcon,
                    contentDescription = null,
                    tint               = contentColor,
                    modifier           = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text      = text,
                style     = MaterialTheme.typography.titleLarge,
                color     = contentColor,
                textAlign = TextAlign.Center,
            )
        }
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
    cornerRadius: Dp = 14.dp,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    NeuButtonBase(
        text           = text,
        onClick        = onClick,
        modifier       = modifier,
        background     = Accent,
        textColor      = Color.White,
        shadowModifier = Modifier.neuGlow(cornerRadius = cornerRadius),
        cornerRadius   = cornerRadius,
        enabled        = enabled,
        leadingIcon    = leadingIcon,
    )
}

/**
 * Botón secundario — fondo [Background] gris perla, sombra elevada.
 * El color del texto es [Accent] azul por defecto; se puede sobreescribir con [textColor].
 */
@Composable
fun NeuButtonSecondary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    textColor: Color = Accent,
    leadingIcon: ImageVector? = null,
) {
    NeuButtonBase(
        text           = text,
        onClick        = onClick,
        modifier       = modifier,
        background     = Background,
        textColor      = textColor,
        shadowModifier = Modifier.neuElevated(cornerRadius = cornerRadius),
        cornerRadius   = cornerRadius,
        leadingIcon    = leadingIcon,
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
