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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevated

@Composable
fun ConfirmarIdentidadScreen(
    onConfirmado: () -> Unit,
    onNoSoyYo: () -> Unit,
    viewModel: ConfirmarIdentidadViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToMiembroHome) {
        if (uiState.navigateToMiembroHome) {
            onConfirmado()
            viewModel.consumeNavigation()
        }
    }

    ConfirmarIdentidadContent(
        miembroNombre = uiState.miembroNombre,
        grupoNombre   = uiState.grupoNombre,
        onConfirmar   = viewModel::onConfirmarIdentidad,
        onNoSoyYo     = onNoSoyYo,
    )
}

@Composable
private fun ConfirmarIdentidadContent(
    miembroNombre: String,
    grupoNombre: String,
    onConfirmar: () -> Unit = {},
    onNoSoyYo: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        val iniciales = miembroNombre.trim().split(" ").let { partes ->
            when {
                partes.size >= 2 -> "${partes[0].firstOrNull() ?: ""}${partes[1].firstOrNull() ?: ""}".uppercase()
                partes.size == 1 -> partes[0].take(2).uppercase()
                else -> "??"
            }
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .neuElevated(cornerRadius = 40.dp)
                .background(Background, RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = iniciales,
                style      = MaterialTheme.typography.titleLarge,
                color      = Accent,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text  = "¿Eres tú?",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text  = "Confirma tu identidad para guardar tu perfil en este dispositivo.",
            style = MaterialTheme.typography.bodyMedium,
            color = Mid,
        )

        Spacer(Modifier.height(32.dp))

        NeuCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text  = "NOMBRE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = miembroNombre,
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text  = "GRUPO PEQUEÑO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = grupoNombre,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Mid,
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        NeuButtonPrimary(
            text     = "Sí, soy yo",
            onClick  = onConfirmar,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        NeuButtonSecondary(
            text     = "No soy yo",
            onClick  = onNoSoyYo,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun ConfirmarIdentidadPreview() {
    GpLeaderTheme {
        ConfirmarIdentidadContent(
            miembroNombre = "Juan Carlos Pérez",
            grupoNombre   = "GP Los Olivos",
        )
    }
}
