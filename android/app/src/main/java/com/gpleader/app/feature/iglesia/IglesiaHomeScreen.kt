package com.gpleader.app.feature.iglesia

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.FloatingNavScaffold
import com.gpleader.app.core.ui.components.NAV_TAB_ACTIVIDADES
import com.gpleader.app.core.ui.components.NAV_TAB_INICIO
import com.gpleader.app.core.ui.components.NAV_TAB_PERFIL
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
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInsetInner

@Composable
fun IglesiaHomeScreen(
    onNavigateToRegistro:              (kind: String) -> Unit,
    onNavigateToActividadesMisioneras: () -> Unit,
    onNavigateToGrupos:                () -> Unit,
    onNavigateToAprobaciones:          () -> Unit,
    onNavigateToHistorial:             () -> Unit,
    onNavigateToActividades:           () -> Unit,
    onNavigateToPerfil:                () -> Unit,
    viewModel: IglesiaHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    IglesiaHomeContent(
        uiState                          = uiState,
        onNavigateToRegistro             = onNavigateToRegistro,
        onNavigateToActividadesMisioneras = onNavigateToActividadesMisioneras,
        onNavigateToGrupos               = onNavigateToGrupos,
        onNavigateToAprobaciones         = onNavigateToAprobaciones,
        onNavigateToHistorial            = onNavigateToHistorial,
        onNavigateToActividades          = onNavigateToActividades,
        onNavigateToPerfil               = onNavigateToPerfil,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IglesiaHomeContent(
    uiState:                           IglesiaHomeUiState,
    onNavigateToRegistro:              (kind: String) -> Unit,
    onNavigateToActividadesMisioneras: () -> Unit,
    onNavigateToGrupos:                () -> Unit,
    onNavigateToAprobaciones:          () -> Unit,
    onNavigateToHistorial:             () -> Unit,
    onNavigateToActividades:           () -> Unit,
    onNavigateToPerfil:                () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(NAV_TAB_INICIO) }

    FloatingNavScaffold(
        selectedTab        = selectedTab,
        onInicioClick      = { selectedTab = NAV_TAB_INICIO },
        onActividadesClick = {
            selectedTab = NAV_TAB_ACTIVIDADES
            onNavigateToActividades()
        },
        onPerfilClick      = {
            selectedTab = NAV_TAB_PERFIL
            onNavigateToPerfil()
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(24.dp))

            IglesiaHeader(uiState = uiState)

            Spacer(Modifier.height(20.dp))

            RegistrarCard(onClick = { onNavigateToRegistro("saturday_worship") })

            Spacer(Modifier.height(12.dp))

            IglesiaAccionCard(
                icon      = Icons.Default.Groups,
                iconTint  = Accent,
                titulo    = "Grupos pequeños",
                subtitulo = "${uiState.totalGrupos} grupos · ${uiState.totalMiembros} miembros",
                onClick   = onNavigateToGrupos,
            )

            Spacer(Modifier.height(12.dp))

            IglesiaAccionCard(
                icon       = Icons.Default.Checklist,
                iconTint   = Blush,
                titulo     = "Aprobaciones",
                subtitulo  = "Actividades monetarias pendientes de aprobar",
                badgeCount = uiState.pendingBoardCount,
                onClick    = onNavigateToAprobaciones,
            )

            Spacer(Modifier.height(12.dp))

            IglesiaAccionCard(
                icon      = Icons.Default.Public,
                iconTint  = Sage,
                titulo    = "Actividades misioneras",
                subtitulo = "Administrador de dúos misioneros y estudios individuales",
                onClick   = onNavigateToActividadesMisioneras,
            )

            Spacer(Modifier.height(12.dp))

            IglesiaAccionCard(
                icon      = Icons.Default.Leaderboard,
                iconTint  = Gold,
                titulo    = "Ver historial",
                subtitulo = "Ver reuniones pasadas y asistencia a cultos de sábado",
                onClick   = onNavigateToHistorial,
            )

            if (uiState.error != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text  = uiState.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Blush,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun IglesiaHeader(uiState: IglesiaHomeUiState) {
    val nameStyle = MaterialTheme.typography.displayLarge.copy(
        fontSize   = 38.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp,
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Iglesia", style = nameStyle, color = Ink)
        Text(text = uiState.iglesiaNombre.ifBlank { "Mi Iglesia" }, style = nameStyle, color = Ink)
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "${uiState.totalGrupos} grupos · ${uiState.totalMiembros} miembros",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = Mid,
        )
    }
}

// ── Tarjeta "Tomar asistencia" (idéntica a la del líder) ──────────────────────

@Composable
private fun RegistrarCard(onClick: () -> Unit) {
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 26.dp, vertical = 30.dp)) {
            Text(
                text  = "HOY",
                style = MaterialTheme.typography.labelSmall,
                color = Accent,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = "Registra la asistencia al culto de sábado",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontStyle  = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 24.sp,
                    lineHeight = 28.sp,
                ),
                color = Ink,
            )
            Spacer(Modifier.height(22.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .neuGlow(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent)
                    .clickable(onClick = onClick)
                    .padding(vertical = 16.dp),
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Default.AssignmentTurnedIn,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text       = "Tomar Asistencia",
                        style      = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ── Tarjeta de acción genérica (Grupos / Aprobaciones / Misioneras / Historial)

@Composable
private fun IglesiaAccionCard(
    icon:       ImageVector,
    iconTint:   Color,
    titulo:     String,
    subtitulo:  String,
    badgeCount: Int = 0,
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Background)
                    .neuInsetInner(shadowSize = 10.dp),
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = iconTint,
                    modifier           = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = titulo,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text  = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
            }
            if (badgeCount > 0) {
                Box(
                    modifier         = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Blush)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = badgeCount.toString(),
                        style      = MaterialTheme.typography.labelSmall,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = Muted,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun IglesiaHomePreview() {
    GpLeaderTheme {
        IglesiaHomeContent(
            uiState = IglesiaHomeUiState(
                iglesiaNombre     = "Central Barrio Belén",
                totalGrupos       = 8,
                totalMiembros     = 64,
                pendingBoardCount = 3,
            ),
            onNavigateToRegistro              = {},
            onNavigateToActividadesMisioneras = {},
            onNavigateToGrupos                = {},
            onNavigateToAprobaciones          = {},
            onNavigateToHistorial             = {},
            onNavigateToActividades           = {},
            onNavigateToPerfil                = {},
        )
    }
}
