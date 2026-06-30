package com.gpleader.app.feature.miembro

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Repeat
import com.gpleader.app.core.ui.components.FloatingNavScaffold
import com.gpleader.app.core.ui.components.NAV_TAB_PERFIL
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevated

@Composable
fun MiembroPerfilScreen(
    onNavigateToInicio:      () -> Unit,
    onNavigateToActividades: () -> Unit,
    onEntrarComoSuplente:    () -> Unit,
    onCerrarSesion:          () -> Unit,
    viewModel: MiembroPerfilViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            onCerrarSesion()
            viewModel.consumeLoginNavigation()
        }
    }

    FloatingNavScaffold(
        selectedTab        = NAV_TAB_PERFIL,
        onInicioClick      = onNavigateToInicio,
        onActividadesClick = onNavigateToActividades,
        onPerfilClick      = {},
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Cabecera: avatar + nombre ─────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                NeuAvatar(iniciales = uiState.miembroIniciales, size = 64.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text       = uiState.miembroNombre,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = Ink,
                    )
                    val sub = uiState.grupoNombre.ifBlank { uiState.iglesiaNombre }
                    if (sub.isNotBlank()) {
                        Text(text = sub, style = MaterialTheme.typography.bodyMedium, color = Muted)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── IGLESIA ───────────────────────────────────────────────────────
            Text("IGLESIA", style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(Modifier.height(8.dp))
            NeuCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundDeep),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text       = uiState.iglesiaNombre.firstOrNull()?.uppercase() ?: "I",
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color      = Accent,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = uiState.iglesiaNombre,
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color      = Ink,
                        )
                        val ubicacion = listOf(uiState.districtNombre, uiState.campoNombre)
                            .filter { it.isNotBlank() }.joinToString(" · ")
                        if (ubicacion.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(text = ubicacion, style = MaterialTheme.typography.bodyMedium, color = Muted)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── CUENTA ────────────────────────────────────────────────────────
            Text("CUENTA", style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(Modifier.height(8.dp))
            PerfilAccionRow(
                icon    = Lucide.Repeat,
                label   = "Entrar como suplente",
                onClick = onEntrarComoSuplente,
            )
            Spacer(Modifier.height(10.dp))
            PerfilAccionRow(
                icon        = Lucide.LogOut,
                label       = "Cerrar sesión",
                onClick     = viewModel::onCerrarSesion,
                showChevron = false,
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PerfilAccionRow(
    icon:        ImageVector,
    label:       String,
    onClick:     () -> Unit,
    iconTint:    Color = Mid,
    showChevron: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = iconTint,
            modifier           = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color      = Ink,
            modifier   = Modifier.weight(1f),
        )
        if (showChevron) {
            Icon(
                imageVector        = Lucide.ChevronRight,
                contentDescription = null,
                tint               = Muted,
                modifier           = Modifier.size(18.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun MiembroPerfilPreview() {
    // Preview sin VM — solo layout
}
