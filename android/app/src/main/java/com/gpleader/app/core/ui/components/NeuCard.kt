package com.gpleader.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.neuElevated

/**
 * Card neumórfica flotante.
 *
 * - Fondo [Background], sombras dobles (oscura + clara).
 * - [onClick] opcional: si es null el card no es clickable.
 * - El `cornerRadius` de [neuElevated] debe coincidir con el [RoundedCornerShape] del clip.
 */
@Composable
fun NeuCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(28.dp)

    val baseModifier = modifier
        .neuElevated(cornerRadius = 28.dp)
        .clip(shape)
        .background(color = Background, shape = shape)
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick)
            else Modifier
        )

    Box(modifier = baseModifier) {
        content()
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun NeuCardPreview() {
    GpLeaderTheme {
        Column(modifier = Modifier.padding(32.dp)) {
            NeuCard(modifier = Modifier.padding(8.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Reunión · 15 mar 2026",
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                    )
                    Text(
                        text = "12 presentes · 2 ausentes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mid,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "NeuCard clickable")
@Composable
private fun NeuCardClickablePreview() {
    GpLeaderTheme {
        Column(modifier = Modifier.padding(32.dp)) {
            NeuCard(
                modifier = Modifier.padding(8.dp),
                onClick  = {},
            ) {
                Text(
                    text     = "Toca para continuar",
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = Ink,
                    modifier = Modifier.padding(24.dp),
                )
            }
        }
    }
}
