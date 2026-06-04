package com.gpleader.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.CormorantGaramond
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.neuInsetInner

/**
 * Avatar circular neumórfico con efecto hundido e iniciales.
 * Diseño extraído del prototipo HTML: fondo Background, inset shadow, Cormorant Garamond Bold.
 * fontSize = size * 0.36 (ratio del HTML para todas las escalas).
 */
@Composable
fun NeuAvatar(
    iniciales: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val fontSize = (size.value * 0.36f).sp
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Background)
            .neuInsetInner(shadowSize = size * 0.28f),
    ) {
        Text(
            text  = iniciales.take(2).uppercase(),
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontWeight = FontWeight.Bold,
                fontSize   = fontSize,
            ),
            color = Mid,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun NeuAvatarPreview() {
    GpLeaderTheme {
        NeuAvatar(iniciales = "AM", size = 44.dp)
    }
}
