package com.gpleader.app.feature.actividades

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.MiembroData
import com.gpleader.app.core.data.repository.iniciales
import com.gpleader.app.core.data.repository.nombreCompleto
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
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInsetSm

@Composable
fun AgregarAporteScreen(
    onNavigateBack: () -> Unit,
    viewModel: AgregarAporteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.guardadoOk) {
        if (uiState.guardadoOk) onNavigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Back button + título ──────────────────────────────────────────
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
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint               = Ink,
                        modifier           = Modifier.size(20.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text      = "Agregar aporte",
                        style     = MaterialTheme.typography.titleLarge,
                        color     = Ink,
                        textAlign = TextAlign.Center,
                    )
                    if (uiState.actividadNombre.isNotBlank()) {
                        Text(
                            text  = uiState.actividadNombre,
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
                Box(modifier = Modifier.size(40.dp))
            }

            // ── Lista de miembros ─────────────────────────────────────────────
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }

                uiState.error != null && uiState.miembros.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(uiState.error!!, color = Blush, textAlign = TextAlign.Center)
                }

                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.miembros, key = { it.id }) { miembro ->
                        val isCheckbox = uiState.markerType == "checkbox" || uiState.markerType == "realizado"
                        if (isCheckbox) {
                            MiembroCheckboxRow(
                                miembro   = miembro,
                                marcado   = miembro.id in uiState.toggleados,
                                onToggle  = { viewModel.onToggle(miembro.id) },
                            )
                        } else {
                            MiembroContadorRow(
                                miembro     = miembro,
                                count       = uiState.contadores[miembro.id] ?: 0,
                                unitLabel   = uiState.unitLabel,
                                onIncrement = { viewModel.onIncrement(miembro.id) },
                                onDecrement = { viewModel.onDecrement(miembro.id) },
                            )
                        }
                    }
                }
            }
        }

        // ── Botón Guardar fijo en bottom ──────────────────────────────────────
        if (!uiState.isLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Background)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                if (uiState.isGuardando) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent)
                    }
                } else {
                    NeuButtonPrimary(
                        text     = "Guardar",
                        onClick  = viewModel::onGuardar,
                        modifier = Modifier.fillMaxWidth(),
                        enabled  = viewModel.hayValores,
                    )
                }
            }
        }
    }
}

@Composable
private fun MiembroContadorRow(
    miembro:     MiembroData,
    count:       Int,
    unitLabel:   String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeuAvatar(iniciales = miembro.iniciales, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                text       = miembro.nombreCompleto,
                style      = MaterialTheme.typography.bodyLarge,
                color      = Ink,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.weight(1f),
            )
            // Botón −
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .neuElevatedSm(cornerRadius = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Background)
                    .clickable(enabled = count > 0, onClick = onDecrement)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text  = "−",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (count > 0) Ink else Muted,
                )
            }
            // Valor
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .neuInsetSm(cornerRadius = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Background)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .width(32.dp),
            ) {
                Text(
                    text      = count.toString(),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = if (count > 0) Accent else Mid,
                    fontWeight = if (count > 0) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
            }
            // Botón +
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .neuGlow(cornerRadius = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Accent)
                    .clickable(onClick = onIncrement)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text  = "+",
                    style = MaterialTheme.typography.titleLarge,
                    color = androidx.compose.ui.graphics.Color.White,
                )
            }
        }
        if (unitLabel.isNotBlank() && count > 0) {
            Text(
                text     = "$count $unitLabel",
                style    = MaterialTheme.typography.labelSmall,
                color    = Accent,
                modifier = Modifier.padding(start = 68.dp, bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun MiembroCheckboxRow(
    miembro:  MiembroData,
    marcado:  Boolean,
    onToggle: () -> Unit,
) {
    NeuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeuAvatar(iniciales = miembro.iniciales, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                text       = miembro.nombreCompleto,
                style      = MaterialTheme.typography.bodyLarge,
                color      = Ink,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.weight(1f),
            )
            Icon(
                imageVector        = if (marcado) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint               = if (marcado) Sage else Muted,
                modifier           = Modifier.size(28.dp),
            )
        }
    }
}
