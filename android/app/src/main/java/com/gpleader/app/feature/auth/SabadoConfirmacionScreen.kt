package com.gpleader.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage

@Composable
fun SabadoConfirmacionScreen(
    iglesiaNombre: String,
    onCerrar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        // ── Check circle ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Sage.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Sage, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Default.Check,
                    contentDescription = null,
                    tint               = Background,
                    modifier           = Modifier.size(36.dp),
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text       = stringResource(R.string.sabado_confirmacion_titulo),
            style      = MaterialTheme.typography.headlineMedium,
            color      = Ink,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text      = "Tu asistencia al culto de sábado fue registrada exitosamente.",
            style     = MaterialTheme.typography.bodyLarge,
            color     = Mid,
            textAlign = TextAlign.Center,
        )

        if (iglesiaNombre.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDeep, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text  = "Iglesia",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = iglesiaNombre,
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                    textAlign  = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        NeuButtonSecondary(
            text         = stringResource(R.string.sabado_confirmacion_btn),
            onClick      = onCerrar,
            modifier     = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun SabadoConfirmacionPreview() {
    GpLeaderTheme {
        SabadoConfirmacionScreen(
            iglesiaNombre = "Iglesia Central",
            onCerrar = {},
        )
    }
}
