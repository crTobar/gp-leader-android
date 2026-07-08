package com.gpleader.app.feature.nivel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Logout
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuInsetInner

@Composable
fun NivelHomeScreen(
    onNavigateToAprobaciones: (nivel: String) -> Unit,
    onLogout:                 () -> Unit,
    viewModel: NivelHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                val nameStyle = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 38.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp,
                )
                Text(uiState.nivelLabel, style = nameStyle, color = Ink)
                Text(uiState.nodeNombre.ifBlank { "Mi nivel" }, style = nameStyle, color = Ink)
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(Background).neuInsetInner(shadowSize = 10.dp)
                    .clickable(onClick = onLogout),
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Salir", tint = Mid, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        NivelAccionCard(
            titulo     = "Aprobaciones",
            subtitulo  = "Montos por aprobar de tus ${subtituloHijos(uiState.nivel)}",
            badgeCount = uiState.pendingCount,
            onClick    = { onNavigateToAprobaciones(uiState.nivel) },
        )

        if (uiState.error != null) {
            Spacer(Modifier.height(16.dp))
            Text(uiState.error!!, style = MaterialTheme.typography.bodyMedium, color = Blush)
        }

        Spacer(Modifier.height(24.dp))
    }
}

private fun subtituloHijos(nivel: String): String = when (nivel) {
    "DISTRICT" -> "iglesias"
    "CAMPO"    -> "distritos"
    "UNION"    -> "asociaciones"
    else       -> "grupos"
}

@Composable
private fun NivelAccionCard(
    titulo:     String,
    subtitulo:  String,
    badgeCount: Int,
    onClick:    () -> Unit,
) {
    NeuCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(Background).neuInsetInner(shadowSize = 10.dp),
            ) {
                Icon(Icons.Default.Checklist, contentDescription = null, tint = Blush, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
                Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = Mid)
            }
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Blush)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(badgeCount.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
        }
    }
}
