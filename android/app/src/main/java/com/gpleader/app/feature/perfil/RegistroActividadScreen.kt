package com.gpleader.app.feature.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.data.repository.GroupLogEntry
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun RegistroActividadScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegistroActividadViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    RegistroActividadContent(
        uiState        = uiState,
        onNavigateBack = onNavigateBack,
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun RegistroActividadContent(
    uiState:        RegistroActividadUiState,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .neuElevated(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Background)
                    .clickable(onClick = onNavigateBack)
                    .padding(10.dp),
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint               = Ink,
                    modifier           = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text       = stringResource(R.string.perfil_registro_actividad),
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )
        }

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            }

            uiState.error != null -> {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(uiState.error, style = MaterialTheme.typography.bodyLarge, color = Blush)
                }
            }

            uiState.entradas.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text  = "No hay actividad registrada aún.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Muted,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.entradas, key = { it.id }) { entrada ->
                        EntradaRow(entrada = entrada)
                    }
                }
            }
        }
    }
}

// ── Fila ──────────────────────────────────────────────────────────────────────

@Composable
private fun EntradaRow(entrada: GroupLogEntry) {
    val (icono, color) = iconoYColor(entrada.actionType)

    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
            ) {
                Text(text = icono, color = color, style = MaterialTheme.typography.bodyMedium)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = entrada.description,
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = Ink,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text  = formatFecha(entrada),
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun iconoYColor(actionType: String): Pair<String, Color> = when (actionType) {
    "member_unarchived"          -> "↑" to Sage
    "member_archived"            -> "↓" to Gold
    "member_added"               -> "+"  to Sage
    "meeting_submitted"          -> "✓" to Accent
    "saturday_worship_submitted" -> "✓" to Gold
    "meeting_edited"             -> "✎" to Mid
    "activity_updated"           -> "≡" to Accent
    "deputy_submission_created"  -> "→" to Ink
    else                         -> "·" to Muted
}

private val fmtDia   = DateTimeFormatter.ofPattern("EEE", Locale("es"))
private val fmtMes   = DateTimeFormatter.ofPattern("MMMM", Locale("es"))

private fun formatFecha(entrada: GroupLogEntry): String {
    val zdt   = entrada.createdAt.atZone(ZoneId.systemDefault())
    val dia   = zdt.format(fmtDia).replaceFirstChar { it.uppercase() }
    val num   = zdt.dayOfMonth
    val mes   = zdt.format(fmtMes).replaceFirstChar { it.uppercase() }
    return "$dia $num De $mes"
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun RegistroActividadPreview() {
    GpLeaderTheme {
        RegistroActividadContent(
            uiState        = RegistroActividadUiState(isLoading = false, entradas = emptyList()),
            onNavigateBack = {},
        )
    }
}
