package com.gpleader.app.feature.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.MiembroData
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.data.repository.iniciales
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm

@Composable
fun CrearDuoScreen(
    onNavigateBack: () -> Unit,
    viewModel: CrearDuoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedOk) {
        if (uiState.savedOk) onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .neuElevatedSm(cornerRadius = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Background)
                    .clickable(onClick = onNavigateBack)
                    .padding(10.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink, modifier = Modifier.size(20.dp))
            }
            Text(
                text      = "Crear Dúo",
                style     = MaterialTheme.typography.titleLarge,
                color     = Ink,
                textAlign = TextAlign.Center,
                modifier  = Modifier.weight(1f),
            )
            Box(modifier = Modifier.size(40.dp))
        }

        // ── Selección ─────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text  = "Selecciona 2 miembros para el dúo",
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )
            if (uiState.member1Id != null || uiState.member2Id != null) {
                Spacer(Modifier.height(8.dp))
                val sel = listOfNotNull(
                    uiState.miembros.find { it.id == uiState.member1Id }?.nombreCompleto,
                    uiState.miembros.find { it.id == uiState.member2Id }?.nombreCompleto,
                ).joinToString(" + ")
                Text(
                    text       = sel,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            uiState.error?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Blush)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (uiState.isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
            }
        } else {
            LazyColumn(
                modifier            = Modifier.weight(1f),
                contentPadding      = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.miembros) { miembro ->
                    val isSelected = miembro.id == uiState.member1Id || miembro.id == uiState.member2Id
                    MiembroSelectRow(
                        miembro    = miembro,
                        isSelected = isSelected,
                        onClick    = { viewModel.onToggleMiembro(miembro.id) },
                    )
                }
            }
        }

        // ── Botón crear ───────────────────────────────────────────────────────
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            NeuButtonPrimary(
                text     = if (uiState.isGuardando) "Creando…" else "Crear dúo",
                onClick  = viewModel::onCrear,
                modifier = Modifier.fillMaxWidth(),
                enabled  = uiState.member1Id != null && uiState.member2Id != null && !uiState.isGuardando,
            )
        }
    }
}

@Composable
private fun MiembroSelectRow(
    miembro:    MiembroData,
    isSelected: Boolean,
    onClick:    () -> Unit,
) {
    NeuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeuAvatar(iniciales = miembro.iniciales, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                text     = miembro.nombreCompleto,
                style    = MaterialTheme.typography.bodyLarge,
                color    = Ink,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector        = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint               = if (isSelected) Sage else Muted,
                modifier           = Modifier.size(22.dp),
            )
        }
    }
}
