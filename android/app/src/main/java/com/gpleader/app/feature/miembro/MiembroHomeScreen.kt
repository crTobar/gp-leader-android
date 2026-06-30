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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.NeuCard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Users
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.History
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.gpleader.app.core.ui.components.FloatingNavScaffold
import com.gpleader.app.core.ui.components.NAV_TAB_INICIO
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuInsetInner
import com.gpleader.app.core.ui.components.OnResumeEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiembroHomeScreen(
    onCerrarSesion:               () -> Unit,
    onNavigateToActividades:      () -> Unit = {},
    onNavigateToPerfil:           () -> Unit = {},
    onNavigateToDuoMisionero:     () -> Unit = {},
    onNavigateToEstudiosBiblicos: () -> Unit = {},
    onNavigateToHistorial:        () -> Unit = {},
    viewModel: MiembroHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            onCerrarSesion()
            viewModel.consumeLoginNavigation()
        }
    }

    OnResumeEffect { viewModel.onRefresh() }

    MiembroHomeContent(
        uiState                      = uiState,
        onNavigateToActividades      = onNavigateToActividades,
        onNavigateToPerfil           = onNavigateToPerfil,
        onNavigateToDuoMisionero     = onNavigateToDuoMisionero,
        onNavigateToEstudiosBiblicos = onNavigateToEstudiosBiblicos,
        onNavigateToHistorial        = onNavigateToHistorial,
        onRefresh                    = viewModel::onRefresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MiembroHomeContent(
    uiState:                      MiembroHomeUiState,
    onNavigateToActividades:      () -> Unit = {},
    onNavigateToPerfil:           () -> Unit = {},
    onNavigateToDuoMisionero:     () -> Unit = {},
    onNavigateToEstudiosBiblicos: () -> Unit = {},
    onNavigateToHistorial:        () -> Unit = {},
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

    FloatingNavScaffold(
        selectedTab        = NAV_TAB_INICIO,
        onInicioClick      = {},
        onActividadesClick = onNavigateToActividades,
        onPerfilClick      = onNavigateToPerfil,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding),
        ) {
            // ── Cabecera: fecha + avatar con nombre al lado ───────────────────
            MiembroHeader(
                iniciales   = uiState.miembroIniciales,
                nombre      = uiState.miembroNombre,
                grupoNombre = uiState.grupoNombre,
            )

            // ── Contenido scrollable ──────────────────────────────────────────
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh    = onRefresh,
                modifier     = Modifier.weight(1f).fillMaxWidth(),
                indicator    = {},
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 20.dp, bottom = 24.dp),
            ) {
                // ── Métricas del trimestre (asistencia) ───────────────────────
                Text("ESTE TRIMESTRE", style = MaterialTheme.typography.labelSmall, color = Muted)
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

                Spacer(Modifier.height(16.dp))

                // ── Tarjetas de acceso ────────────────────────────────────────
                if (uiState.tieneDuo) {
                    MiembroOpcionCard(
                        icon      = Lucide.Users,
                        iconTint  = Sage,
                        titulo    = "Ver Dúo Misionero",
                        subtitulo = "Tu pareja misionera y registros compartidos",
                        onClick   = onNavigateToDuoMisionero,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                MiembroOpcionCard(
                    icon      = Lucide.BookOpen,
                    iconTint  = Accent,
                    titulo    = "Ver Mis Estudios Bíblicos",
                    subtitulo = "Estudios bíblicos que estás dando",
                    onClick   = onNavigateToEstudiosBiblicos,
                )
                Spacer(Modifier.height(12.dp))
                MiembroOpcionCard(
                    icon      = Lucide.History,
                    iconTint  = Gold,
                    titulo    = "Ver Historial",
                    subtitulo = "Tu asistencia a reuniones y cultos de sábado",
                    onClick   = onNavigateToHistorial,
                )
            }
            } // PullToRefreshBox
        }
    }
}

@Composable
private fun MiembroHeader(
    iniciales:   String,
    nombre:      String,
    grupoNombre: String,
) {
    val today          = LocalDate.now()
    val diaSemanaLabel = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale("es")).uppercase()
    val diaN           = today.dayOfMonth
    val mes            = today.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("es")).uppercase()
    val fechaLabel     = "$diaSemanaLabel, $diaN DE $mes"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 4.dp),
    ) {
        Text(
            text  = fechaLabel,
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            NeuAvatar(iniciales = iniciales, size = 56.dp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = nombre,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = Ink,
                )
                Text(
                    text  = "Miembro",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        val nameStyle = MaterialTheme.typography.displayLarge.copy(
            fontSize   = 38.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp,
        )
        Text(text = "Grupo",     style = nameStyle, color = Ink)
        Text(text = grupoNombre, style = nameStyle, color = Ink)
    }
}

@Composable
private fun MiembroOpcionCard(
    icon:      ImageVector,
    iconTint:  Color,
    titulo:    String,
    subtitulo: String,
    onClick:   () -> Unit,
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
            Icon(
                imageVector        = Lucide.ChevronRight,
                contentDescription = null,
                tint               = Muted,
                modifier           = Modifier.size(20.dp),
            )
        }
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
