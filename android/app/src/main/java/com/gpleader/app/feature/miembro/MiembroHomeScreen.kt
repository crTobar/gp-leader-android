package com.gpleader.app.feature.miembro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiembroHomeScreen(
    onCerrarSesion:               () -> Unit,
    onNavigateToActividades:      () -> Unit = {},
    onNavigateToDuoMisionero:     () -> Unit = {},
    onNavigateToEstudiosBiblicos: () -> Unit = {},
    onEntrarComoSuplente:         (solicitudId: String) -> Unit = {},
    viewModel: MiembroHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            onCerrarSesion()
            viewModel.consumeLoginNavigation()
        }
    }

    MiembroHomeContent(
        uiState                      = uiState,
        onCerrarSesion               = viewModel::onCerrarSesion,
        onNavigateToActividades      = onNavigateToActividades,
        onNavigateToDuoMisionero     = onNavigateToDuoMisionero,
        onNavigateToEstudiosBiblicos = onNavigateToEstudiosBiblicos,
        onEntrarComoSuplente         = onEntrarComoSuplente,
        onRefresh                    = viewModel::onRefresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MiembroHomeContent(
    uiState:                      MiembroHomeUiState,
    onCerrarSesion:               () -> Unit = {},
    onNavigateToActividades:      () -> Unit = {},
    onNavigateToDuoMisionero:     () -> Unit = {},
    onNavigateToEstudiosBiblicos: () -> Unit = {},
    onEntrarComoSuplente:         (String) -> Unit = {},
    onRefresh:                    () -> Unit = {},
) {
    if (uiState.isValidandoPerfil) {
        Box(
            modifier         = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Accent)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // ── Cabecera: avatar + nombre ─────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeuAvatar(iniciales = uiState.miembroIniciales, size = 52.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text       = uiState.miembroNombre,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = Ink,
                )
                Text(
                    text  = "Culto sábado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }
        }

        HorizontalDivider(color = Muted.copy(alpha = 0.15f))

        // ── Contenido scrollable ──────────────────────────────────────────────
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = onRefresh,
            modifier     = Modifier.weight(1f).fillMaxWidth(),
        indicator = {},
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
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
                            Text(
                                text  = ubicacion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Botones de acción ─────────────────────────────────────────────
            if (uiState.tieneDuo) {
                NeuButtonSecondary(
                    text     = "Mi Dúo Misionero",
                    onClick  = onNavigateToDuoMisionero,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
            }
            NeuButtonSecondary(
                text     = "Mis Estudios Bíblicos",
                onClick  = onNavigateToEstudiosBiblicos,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            NeuButtonSecondary(
                text     = "Mis Actividades",
                onClick  = onNavigateToActividades,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            // ── HISTORIAL DEL TRIMESTRE (al fondo) ───────────────────────────
            Text("HISTORIAL DEL TRIMESTRE", style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(Modifier.height(8.dp))
            NeuCard(modifier = Modifier.fillMaxWidth()) {
                if (uiState.isLoadingStats) {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Accent, modifier = Modifier.size(24.dp))
                    }
                } else {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        StatItem(
                            value = uiState.cultosAsistidos.toString(),
                            label = "asistidos",
                            color = Accent,
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Muted.copy(alpha = 0.25f)),
                        )
                        StatItem(
                            value = uiState.totalCultosGP.toString(),
                            label = "cultos",
                            color = Ink,
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Muted.copy(alpha = 0.25f)),
                        )
                        StatItem(
                            value = "${uiState.porcentajeAsistencia}%",
                            label = "promedio",
                            color = when {
                                uiState.porcentajeAsistencia >= 75 -> Sage
                                uiState.porcentajeAsistencia >= 50 -> Accent
                                else                               -> Blush
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Entrar como suplente ──────────────────────────────────────────
            NeuButtonPrimary(
                text     = "Entrar como suplente",
                onClick  = { onEntrarComoSuplente("") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            // ── Cerrar sesión ─────────────────────────────────────────────────
            NeuButtonSecondary(
                text     = "Cerrar sesión",
                onClick  = onCerrarSesion,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        } // PullToRefreshBox

    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = color,
            textAlign  = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = label,
            style     = MaterialTheme.typography.bodyMedium,
            color     = Muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun MiembroHomePreview() {
    GpLeaderTheme {
        MiembroHomeContent(
            uiState = MiembroHomeUiState(
                miembroNombre         = "Ana Martínez",
                miembroIniciales      = "AM",
                grupoNombre           = "GP Los Olivos",
                iglesiaNombre         = "Iglesia Central",
                districtNombre        = "Distrito Ibagué",
                campoNombre           = "Campo Central",
                cultosAsistidos      = 7,
                totalCultosGP        = 19,
                porcentajeAsistencia = 36,
                isValidandoPerfil    = false,
                isLoadingStats        = false,
            ),
        )
    }
}
