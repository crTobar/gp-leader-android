package com.gpleader.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevated

// ── Entry point (Hilt) ────────────────────────────────────────────────────────

@Composable
fun QuienEresScreen(
    onNavigateToHome: () -> Unit,
    viewModel: QuienEresViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            onNavigateToHome()
            viewModel.consumeHomeNavigation()
        }
    }

    QuienEresContent(
        uiState        = uiState,
        onMiembroClick = viewModel::onMiembroSelected,
    )
}

// ── Content composable ────────────────────────────────────────────────────────

@Composable
fun QuienEresContent(
    uiState: QuienEresUiState,
    onMiembroClick: (MiembroSesion) -> Unit,
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
        Spacer(Modifier.height(32.dp))

        // ── Ícono ─────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(72.dp)
                .neuElevated(cornerRadius = 20.dp)
                .background(Background, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Default.Group,
                contentDescription = null,
                tint               = Accent,
                modifier           = Modifier.size(32.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Título ────────────────────────────────────────────────────────────
        Text(
            text       = "¿Quién entra?",
            style      = MaterialTheme.typography.titleLarge,
            color      = Ink,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(12.dp))

        // ── Subtítulo ─────────────────────────────────────────────────────────
        Text(
            text      = "Elige tu nombre solo para saber quién tiene el dispositivo. " +
                        "Todos los miembros ven la misma información del grupo pequeño: " +
                        "reuniones, asistencia, lista de miembros y reportes.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = Mid,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))

        // ── Sección header ────────────────────────────────────────────────────
        Text(
            text     = "MIEMBROS DEL GRUPO",
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(10.dp))

        // ── Lista de miembros ─────────────────────────────────────────────────
        when {
            uiState.isLoading -> {
                Spacer(Modifier.height(32.dp))
                CircularProgressIndicator(color = Accent)
            }
            uiState.error != null -> {
                Spacer(Modifier.height(16.dp))
                Text(
                    text      = uiState.error,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = Blush,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth(),
                )
            }
            else -> {
                LazyColumn {
                    items(uiState.miembros) { miembro ->
                        MiembroRow(
                            miembro = miembro,
                            onClick = { onMiembroClick(miembro) },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// ── Fila de miembro ───────────────────────────────────────────────────────────

@Composable
private fun MiembroRow(
    miembro: MiembroSesion,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 16.dp)
            .background(Background, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(BackgroundDeep, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = miembro.iniciales,
                style      = MaterialTheme.typography.labelSmall,
                color      = Accent,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text       = miembro.nombre,
            style      = MaterialTheme.typography.bodyLarge,
            color      = Ink,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.weight(1f),
        )

        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = Muted,
            modifier           = Modifier.size(20.dp),
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun QuienEresPreview() {
    GpLeaderTheme {
        QuienEresContent(
            uiState = QuienEresUiState(
                grupoNombre = "GP Los Olivos",
                miembros = listOf(
                    MiembroSesion("m1", "Ana Martínez López", "AM"),
                    MiembroSesion("m2", "Juan Carlos Pérez",  "JP"),
                    MiembroSesion("m3", "María García",       "MG"),
                ),
            ),
            onMiembroClick = {},
        )
    }
}
