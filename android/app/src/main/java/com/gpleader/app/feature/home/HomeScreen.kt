package com.gpleader.app.feature.home

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.OfflineBanner
import com.gpleader.app.core.ui.components.rememberIsOnline
import com.gpleader.app.core.ui.components.SkeletonBox
import com.gpleader.app.core.ui.components.SkeletonText
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.Violet
import com.gpleader.app.core.ui.components.FloatingNavScaffold
import com.gpleader.app.core.ui.components.NAV_TAB_ACTIVIDADES
import com.gpleader.app.core.ui.components.NAV_TAB_INICIO
import com.gpleader.app.core.ui.components.NAV_TAB_PERFIL
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.data.repository.AsignadoPotencial
import com.gpleader.app.core.data.repository.SabbathMeetingResumen
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInset
import com.gpleader.app.core.ui.theme.neuInsetInner
import com.gpleader.app.core.ui.theme.neuInsetSm
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


// Nombres de mes abreviados en español
private val MESES_ES = arrayOf(
    "ENE","FEB","MAR","ABR","MAY","JUN","JUL","AGO","SEP","OCT","NOV","DIC",
)

// ── Entry point (Hilt) ────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNavigateToRegistro: (kind: String) -> Unit,
    onNavigateToHistorial: () -> Unit,
    onNavigateToDetalle: (String) -> Unit,
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToActividades: () -> Unit = {},
    onNavigateToSabadoCulto: () -> Unit = {},
    onNavigateToActividadesMisioneras: () -> Unit = {},
    onNavigateToAprobaciones: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Recargar GP de hoy + culto de sábado cada vez que el HomeScreen vuelve al frente
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.reloadHome()
        }
    }

    LaunchedEffect(uiState.navigateToHistorial) {
        if (uiState.navigateToHistorial) {
            onNavigateToHistorial()
            viewModel.consumeHistorialNavigation()
        }
    }
    LaunchedEffect(uiState.navigateToDetalle) {
        val id = uiState.navigateToDetalle
        if (id != null) {
            onNavigateToDetalle(id)
            viewModel.consumeDetalleNavigation()
        }
    }
    LaunchedEffect(uiState.navigateToSabadoCulto) {
        if (uiState.navigateToSabadoCulto) {
            onNavigateToSabadoCulto()
            viewModel.consumeSabadoCultoNavigation()
        }
    }
    LaunchedEffect(uiState.navigateToAprobaciones) {
        if (uiState.navigateToAprobaciones) {
            onNavigateToAprobaciones()
            viewModel.consumeAprobacionesNavigation()
        }
    }

    HomeScreenContent(
        uiState                        = uiState,
        onNavigateToRegistro           = onNavigateToRegistro,
        onVerTodasClick                = viewModel::onVerTodasClick,
        onReunionClick                 = viewModel::onReunionClick,
        onHistorialTabClick            = onNavigateToHistorial,
        onPerfilClick                  = onNavigateToPerfil,
        onActividadesTabClick          = onNavigateToActividades,
        onSabadoCultoClick             = viewModel::onSabadoCultoClick,
        onCancelarSolicitud            = viewModel::onCancelarSolicitud,
        onActividadesMisionerasClick   = onNavigateToActividadesMisioneras,
        onAprobacionesClick            = viewModel::onAprobacionesClick,
        onRefresh                      = viewModel::onRefresh,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onNavigateToRegistro: (kind: String) -> Unit = {},
    onVerTodasClick: () -> Unit,
    onReunionClick: (String) -> Unit,
    onHistorialTabClick: () -> Unit,
    onPerfilClick: () -> Unit = {},
    onActividadesTabClick: () -> Unit = {},
    onSabadoCultoClick: () -> Unit = {},
    onCancelarSolicitud: (String) -> Unit = {},
    onActividadesMisionerasClick: () -> Unit = {},
    onAprobacionesClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    var selectedTab        by remember { mutableIntStateOf(NAV_TAB_INICIO) }
    var showRegistrarSheet by remember { mutableStateOf(false) }
    val isOnline = rememberIsOnline()

    FloatingNavScaffold(
        selectedTab        = selectedTab,
        onInicioClick      = { selectedTab = NAV_TAB_INICIO },
        onActividadesClick = {
            selectedTab = NAV_TAB_ACTIVIDADES
            onActividadesTabClick()
        },
        onPerfilClick      = {
            selectedTab = NAV_TAB_PERFIL
            onPerfilClick()
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = onRefresh,
            modifier     = Modifier.fillMaxSize().padding(innerPadding),
        indicator = {},
        ) {
        if (uiState.isLoading) {
            HomeSkeletonContent(modifier = Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(24.dp))

                HomeHeader(
                    grupoNombre   = uiState.grupo?.nombre ?: uiState.nombreLider,
                    totalMiembros = uiState.totalMiembros,
                    diaSemana     = uiState.grupo?.diaSemana ?: "",
                )

                OfflineBanner(modifier = Modifier.padding(top = 16.dp))

                Spacer(Modifier.height(20.dp))

                val gpRegistrado     = uiState.reunionGpHoy != null
                val sabadoRegistrado = uiState.reunionSabadoSemana != null
                val faltaRegistrar   = !gpRegistrado || !sabadoRegistrado

                // Tarjeta "Tomar Asistencia" — siempre arriba mientras falte algo
                if (faltaRegistrar) {
                    RegistrarCard(
                        onClick = { showRegistrarSheet = true },
                        enabled = isOnline,
                    )
                }

                // Confirmaciones debajo
                if (gpRegistrado) {
                    if (faltaRegistrar) Spacer(Modifier.height(10.dp))
                    val gp = uiState.reunionGpHoy
                    YaRegistrasteBadge(
                        titulo    = "Ya registraste hoy tu grupo pequeño",
                        subtitulo = "${gp.presentes} presentes · ${gp.ausentes} ausentes",
                    )
                }
                if (sabadoRegistrado) {
                    if (faltaRegistrar || gpRegistrado) Spacer(Modifier.height(10.dp))
                    YaRegistrasteBadge(
                        titulo    = "Ya registraste el culto de sábado",
                        subtitulo = "${uiState.reunionSabadoSemana.presentes} presentes",
                    )
                }

                Spacer(Modifier.height(12.dp))

                ActividadesMisionerasCard(onClick = onActividadesMisionerasClick)

                Spacer(Modifier.height(12.dp))
                AprobacionesCard(
                    pendientes = uiState.pendingMemberCount,
                    onClick    = onAprobacionesClick,
                )

                Spacer(Modifier.height(12.dp))

                VerHistorialCard(onClick = onHistorialTabClick)

                Spacer(Modifier.height(24.dp))

                // ── Delegaciones activas ─────────────────────────────────────
                if (uiState.delegaciones.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    SectionSeparator(label = "DELEGACIONES")
                    Spacer(Modifier.height(12.dp))
                    uiState.delegaciones.forEach { delegacion ->
                        SolicitudActivaCard(
                            delegacion   = delegacion,
                            onCancelar   = { onCancelarSolicitud(delegacion.codeId) },
                            isCancelando = uiState.cancelandoCodeId == delegacion.codeId,
                            modifier     = Modifier.padding(vertical = 4.dp),
                        )
                    }
                    if (uiState.delegacionError != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = uiState.delegacionError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Blush,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }


                Spacer(Modifier.height(16.dp))
            }
        }
        } // PullToRefreshBox
    }

    // ── Sheet: elegir tipo de registro ────────────────────────────────────────
    if (showRegistrarSheet) {
        TipoRegistroSheet(
            onDismiss            = { showRegistrarSheet = false },
            onGpMeeting          = { showRegistrarSheet = false; onNavigateToRegistro("gp_meeting") },
            onSabado             = { showRegistrarSheet = false; onNavigateToRegistro("saturday_worship") },
            reunionGpHoy         = uiState.reunionGpHoy,
            reunionSabadoSemana  = uiState.reunionSabadoSemana,
        )
    }

}

// ── Sabbath meeting card ──────────────────────────────────────────────────────

@Composable
private fun SabadoReunionCard(
    resumen:  SabbathMeetingResumen,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mes = resumen.fecha.month.getDisplayName(TextStyle.SHORT, Locale("es")).uppercase()
    val dia = resumen.fecha.dayOfMonth

    val badgeColor = when (resumen.status.lowercase()) {
        "submitted" -> Sage
        else        -> Gold
    }
    val badgeLabel = when (resumen.status.lowercase()) {
        "submitted" -> "Enviado"
        else        -> "Borrador"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 20.dp)
            .background(Violet.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .background(Violet),
        )
        Spacer(Modifier.width(14.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.width(36.dp),
        ) {
            Text(text = mes, style = MaterialTheme.typography.labelSmall, color = Violet)
            Text(text = dia.toString(), style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f).padding(top = 14.dp, bottom = 14.dp, end = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = stringResource(R.string.sabado_card_titulo),
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(text = badgeLabel, style = MaterialTheme.typography.labelSmall, color = badgeColor)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "${resumen.presentes} presentes",
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )
        }
    }
}

@Composable
private fun EmptyStateSabado() {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = "Sin registros de culto",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
    }
}

// ── Skeleton loading ──────────────────────────────────────────────────────────

// ── Badge: ya registrado ──────────────────────────────────────────────────────

@Composable
private fun YaRegistrasteBadge(titulo: String, subtitulo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Background)
            .neuInsetInner(cornerRadius = 24.dp)
            .padding(horizontal = 24.dp, vertical = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF2EA86A)),
        ) {
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text       = titulo,
                style      = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color      = Ink,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = subtitulo,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = Mid,
            )
        }
    }
}

// ── Carta de registro ─────────────────────────────────────────────────────────

@Composable
private fun RegistrarCard(onClick: () -> Unit, enabled: Boolean = true) {
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 26.dp, vertical = 30.dp)) {
            Text(
                text  = "HOY",
                style = MaterialTheme.typography.labelSmall,
                color = Accent,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = "Aún no has tomado asistencia",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontStyle  = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 24.sp,
                    lineHeight = 28.sp,
                ),
                color = Ink,
            )
            Spacer(Modifier.height(22.dp))
            // Registrar una reunión escribe en Supabase → deshabilitado sin conexión.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (enabled) Modifier.neuGlow(cornerRadius = 14.dp) else Modifier)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) Accent else Muted.copy(alpha = 0.35f))
                    .clickable(enabled = enabled, onClick = onClick)
                    .padding(vertical = 16.dp),
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector        = if (enabled) Icons.Default.AssignmentTurnedIn else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint               = if (enabled) Color.White else Mid,
                        modifier           = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text       = if (enabled) "Tomar Asistencia" else "Necesitas conexión",
                        style      = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                        color      = if (enabled) Color.White else Mid,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActividadesMisionerasCard(onClick: () -> Unit) {
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
                    imageVector        = Icons.Default.Public,
                    contentDescription = null,
                    tint               = Sage,
                    modifier           = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Actividades misioneras",
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text  = "Administrador de dúos misioneros y estudios individuales",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
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

@Composable
private fun VerHistorialCard(onClick: () -> Unit) {
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
                    imageVector        = Icons.Default.Leaderboard,
                    contentDescription = null,
                    tint               = Gold,
                    modifier           = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Ver historial",
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text  = "Ver reuniones pasadas y asistencia a cultos de sábado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
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

@Composable
private fun AprobacionesCard(pendientes: Int, onClick: () -> Unit) {
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
                    imageVector        = Icons.Default.Check,
                    contentDescription = null,
                    tint               = Blush,
                    modifier           = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Aprobaciones",
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text  = if (pendientes > 0) "Aportes de miembros pendientes de aprobar"
                            else "No hay aportes pendientes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
            }
            if (pendientes > 0) {
                Box(
                    modifier         = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Blush)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = pendientes.toString(),
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

@Composable
private fun HomeSkeletonContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        // Header skeleton
        Column {
            SkeletonText(width = 160.dp, height = 10.dp)
            Spacer(Modifier.height(8.dp))
            SkeletonText(width = 60.dp, height = 22.dp)
            Spacer(Modifier.height(2.dp))
            SkeletonText(width = 200.dp, height = 22.dp)
            Spacer(Modifier.height(6.dp))
            SkeletonText(width = 140.dp, height = 12.dp)
        }

        Spacer(Modifier.height(20.dp))

        // RegistrarCard skeleton
        NeuCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                SkeletonText(width = 32.dp, height = 10.dp)
                Spacer(Modifier.height(10.dp))
                SkeletonText(width = 220.dp, height = 18.dp)
                Spacer(Modifier.height(16.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(48.dp), cornerRadius = 12.dp)
            }
        }

        Spacer(Modifier.height(24.dp))

        // StatsRow skeleton
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Dark cell
            SkeletonBox(
                modifier     = Modifier.weight(1f).padding(6.dp).height(88.dp),
                cornerRadius = 20.dp,
            )
            // Light cell 1
            SkeletonBox(
                modifier     = Modifier.weight(1f).padding(6.dp).height(88.dp),
                cornerRadius = 20.dp,
            )
            // Light cell 2
            SkeletonBox(
                modifier     = Modifier.weight(1f).padding(6.dp).height(88.dp),
                cornerRadius = 20.dp,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Section label skeleton
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBox(modifier = Modifier.weight(1f).height(1.dp), cornerRadius = 1.dp)
            Spacer(Modifier.width(12.dp))
            SkeletonText(width = 80.dp, height = 10.dp)
            Spacer(Modifier.width(12.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(1.dp), cornerRadius = 1.dp)
        }

        Spacer(Modifier.height(12.dp))

        // ReunionCard skeletons x2
        repeat(2) {
            NeuCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Date block
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.width(44.dp),
                    ) {
                        SkeletonText(width = 30.dp, height = 22.dp)
                        Spacer(Modifier.height(4.dp))
                        SkeletonText(width = 24.dp, height = 10.dp)
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .width(1.dp)
                            .height(56.dp)
                            .background(Muted.copy(alpha = 0.2f)),
                    )

                    // Content
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonText(width = 140.dp, height = 16.dp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SkeletonBox(modifier = Modifier.width(52.dp).height(20.dp), cornerRadius = 4.dp)
                            SkeletonBox(modifier = Modifier.width(44.dp).height(20.dp), cornerRadius = 4.dp)
                        }
                        Spacer(Modifier.height(8.dp))
                        SkeletonBox(
                            modifier     = Modifier.fillMaxWidth().height(5.dp),
                            cornerRadius = 3.dp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    grupoNombre:   String,
    totalMiembros: Int,
    diaSemana:     String,
) {
    val today      = LocalDate.now()
    val diaSemanaLabel = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es")).uppercase()
    val diaN       = today.dayOfMonth
    val mes        = today.month.getDisplayName(TextStyle.FULL, Locale("es")).uppercase()
    val fechaLabel = "$diaSemanaLabel, $diaN DE $mes"

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text  = fechaLabel,
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
        Spacer(Modifier.height(6.dp))
        val groupNameStyle = MaterialTheme.typography.displayLarge.copy(
            fontSize   = 38.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp,
        )
        Text(text = "Grupo",    style = groupNameStyle, color = Ink)
        Text(text = grupoNombre, style = groupNameStyle, color = Ink)
        if (totalMiembros > 0 || diaSemana.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            val partes = buildList {
                if (totalMiembros > 0) add("$totalMiembros miembros")
                if (diaSemana.isNotBlank()) add("reunión semanal")
            }
            Text(
                text  = partes.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = Mid,
            )
        }
    }
}


// ── Botones pequeños (variante compacta del design system) ────────────────────

@Composable
private fun SmallButtonPrimary(text: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .neuGlow(cornerRadius = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Accent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color      = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SmallButtonSecondary(text: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .neuElevatedSm(cornerRadius = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Background)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color      = Accent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}


// ── Fila de estadísticas ──────────────────────────────────────────────────────

@Composable
private fun StatsRow(
    porcentaje:    Int,
    presentes:     Int,
    ausentes:      Int,
    justificados:  Int,
    totalMiembros: Int,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Fila superior: dark card (alto) + PRESENTES y AUSENTES apilados
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCellFeatured(
                presentes     = presentes,
                totalMiembros = totalMiembros,
                porcentaje    = porcentaje,
                modifier      = Modifier.weight(1f).padding(6.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                StatCellLight(
                    value    = presentes.toString(),
                    label    = stringResource(R.string.home_label_presentes),
                    color    = Sage,
                    modifier = Modifier.fillMaxWidth().padding(start = 0.dp, end = 6.dp, top = 6.dp, bottom = 3.dp),
                )
                StatCellLight(
                    value    = ausentes.toString(),
                    label    = stringResource(R.string.home_label_ausentes),
                    color    = Blush,
                    modifier = Modifier.fillMaxWidth().padding(start = 0.dp, end = 6.dp, top = 3.dp, bottom = 6.dp),
                )
            }
        }
        // Fila inferior: JUSTIFICAR y PROMEDIO a ancho completo
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCellLight(
                value    = justificados.toString(),
                label    = stringResource(R.string.home_label_justificados),
                color    = Muted,
                modifier = Modifier.weight(1f).padding(start = 6.dp, end = 3.dp, top = 0.dp, bottom = 6.dp),
            )
            StatCellLight(
                value    = "$porcentaje%",
                label    = stringResource(R.string.home_label_promedio),
                color    = Accent,
                modifier = Modifier.weight(1f).padding(start = 3.dp, end = 6.dp, top = 0.dp, bottom = 6.dp),
                sublabel = "trimestre",
            )
        }
    }
}

@Composable
private fun StatCellFeatured(
    presentes:     Int,
    totalMiembros: Int,
    porcentaje:    Int,
    modifier:      Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .neuGlow(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Accent),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(vertical = 20.dp, horizontal = 8.dp),
        ) {
            Text(
                text  = "$presentes de $totalMiembros",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = "$porcentaje%",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = stringResource(R.string.home_label_ult_reunion),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun StatCellLight(
    value:    String,
    label:    String,
    color:    Color,
    modifier: Modifier = Modifier,
    sublabel: String?  = null,
) {
    NeuCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
        ) {
            Text(
                text  = value,
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text          = label,
                style         = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color         = Muted,
                textAlign     = androidx.compose.ui.text.style.TextAlign.Center,
            )
            if (sublabel != null) {
                Text(
                    text      = sublabel,
                    style     = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.sp,
                        fontSize      = 10.sp,
                    ),
                    color     = Muted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

// ── Separador de sección ──────────────────────────────────────────────────────

@Composable
private fun SectionSeparator(label: String) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Muted.copy(alpha = 0.4f))
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Muted.copy(alpha = 0.4f))
    }
}

// ── Card de reunión ───────────────────────────────────────────────────────────

@Composable
private fun ReunionCard(
    reunion: ReunionResumen,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dia = reunion.fecha.dayOfMonth.toString()
    val mes = MESES_ES[reunion.fecha.monthValue - 1]
    val total = reunion.presentes + reunion.ausentes
    val progreso = if (total > 0) reunion.presentes / total.toFloat() else 0f

    val (badgeColor, badgeBgColor, badgeTexto) = when (reunion.estado) {
        EstadoReunion.ENVIADA        -> Triple(Accent, Accent.copy(alpha = 0.12f), stringResource(R.string.home_badge_enviada))
        EstadoReunion.APROBADA       -> Triple(Sage,   Sage.copy(alpha = 0.15f),   stringResource(R.string.home_badge_aprobada))
        EstadoReunion.PENDIENTE_SYNC -> Triple(Gold,   Gold.copy(alpha = 0.15f),   stringResource(R.string.home_badge_pendiente))
        EstadoReunion.BORRADOR       -> Triple(Muted,  Muted.copy(alpha = 0.15f),  stringResource(R.string.home_badge_borrador))
    }

    NeuCard(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp), onClick = onClick) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Fecha — izquierda
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.width(44.dp),
            ) {
                Text(text = dia, style = MaterialTheme.typography.headlineMedium, color = Ink)
                Text(text = mes, style = MaterialTheme.typography.labelSmall,     color = Muted)
            }

            Spacer(Modifier.width(12.dp))

            // Contenido central
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = stringResource(R.string.home_reunion_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    color = Accent,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Badge estado
                    Badge(text = badgeTexto, color = badgeColor, bgColor = badgeBgColor)
                    // Badge presentes
                    Badge(
                        text    = stringResource(R.string.home_badge_presentes, reunion.presentes),
                        color   = Mid,
                        bgColor = BackgroundDeep,
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress   = { progreso },
                    modifier   = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color      = Accent,
                    trackColor = BackgroundDeep,
                    strokeCap  = StrokeCap.Round,
                )
            }

            // Flecha
            Text(
                text     = "›",
                style    = MaterialTheme.typography.headlineMedium,
                color    = Muted,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun Badge(text: String, color: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

// ── Estado vacío ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyStateReuniones() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(BackgroundDeep),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "📅", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text  = stringResource(R.string.home_empty_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = stringResource(R.string.home_empty_subtitulo),
            style = MaterialTheme.typography.bodyMedium,
            color = Mid,
        )
    }
}



// ── Delegación: tarjeta activa ────────────────────────────────────────────────

@Composable
private fun SolicitudActivaCard(
    delegacion:   DelegacionActiva,
    onCancelar:   () -> Unit,
    isCancelando: Boolean = false,
    modifier:     Modifier = Modifier,
) {
    val tiempoRestante = remember(delegacion.expiresAt) {
        delegacion.expiresAt?.let { iso ->
            runCatching {
                val diff = java.time.Duration.between(java.time.Instant.now(), java.time.Instant.parse(iso))
                when {
                    diff.isNegative      -> "Vencida"
                    diff.toHours() >= 1  -> "Vence en ${diff.toHours()}h ${diff.toMinutes() % 60}m"
                    diff.toMinutes() > 0 -> "Vence en ${diff.toMinutes()}m"
                    else                 -> "Vence pronto"
                }
            }.getOrNull()
        }
    }

    NeuCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Delegación activa",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = Muted,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text       = delegacion.nombreAsignado.ifBlank { "Suplente asignado" },
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                )
                if (tiempoRestante != null) {
                    Text(
                        text  = tiempoRestante,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (tiempoRestante == "Vencida") Blush else Mid,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            if (isCancelando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Blush)
            } else {
                TextButton(onClick = onCancelar) {
                    Text("Cancelar", color = Blush, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── (obsoleto) ActivarSolicitudDialog ────────────────────────────────────────
// Mantenido para evitar romper referencias de compilación
@Suppress("unused")
@Composable
private fun ActivarSolicitudDialog_unused(
    onActivar: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Background,
        title = {
            Text(
                text  = "Solicitud de delegación",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
        },
        text = {
            Column {
                Text(
                    text  = "Tenés una reunión pendiente por registrar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onActivar) {
                Text("Activar y registrar", color = Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Más tarde", color = Mid)
            }
        },
    )
}

// ── Solicitud: sheet crear delegación ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DelegarReunionSheet(
    asignados: List<AsignadoPotencial>,
    isLoading: Boolean,
    isCreando: Boolean,
    error:     String?,
    onCrear:   (assignedToId: String, nota: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedId by remember { mutableStateOf<String?>(null) }
    var nota       by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text  = "Delegar reunión",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Seleccioná a quién se delega el registro.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )
            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = Accent) }
                }
                asignados.isEmpty() -> {
                    Text(
                        text  = "No hay delegados registrados en este grupo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
                else -> {
                    LazyColumn(
                        modifier             = Modifier.fillMaxWidth(),
                        verticalArrangement  = Arrangement.spacedBy(6.dp),
                    ) {
                        items(asignados) { asignado ->
                            val isSelected = asignado.profileId == selectedId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (isSelected) Modifier.neuElevatedSm(14.dp)
                                        else Modifier.neuElevated(14.dp)
                                    )
                                    .background(
                                        if (isSelected) Accent.copy(alpha = 0.08f) else Background,
                                        RoundedCornerShape(14.dp),
                                    )
                                    .clickable { selectedId = asignado.profileId }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text  = asignado.nombre,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected) Accent else Ink,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    val rolDisplay = when (asignado.rol) {
                                        "leader"             -> "Líder"
                                        "co_leader"          -> "Co-líder"
                                        "anciano"            -> "Anciano"
                                        "pastor_practicante" -> "Pastor practicante"
                                        "member"             -> "Miembro"
                                        else                 -> asignado.rol
                                    }
                                    Text(
                                        text  = rolDisplay,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Mid,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Campo nota opcional
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuElevated(cornerRadius = 14.dp)
                    .background(Background, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                if (nota.isEmpty()) {
                    Text(
                        text  = "Nota opcional…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
                BasicTextField(
                    value         = nota,
                    onValueChange = { nota = it },
                    textStyle     = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                    modifier      = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(8.dp))

            if (error != null) {
                Text(
                    text  = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Blush,
                )
                Spacer(Modifier.height(8.dp))
            }

            val canCreate = selectedId != null && !isCreando
            Box(modifier = Modifier.fillMaxWidth()) {
                if (isCreando) {
                    CircularProgressIndicator(
                        color    = Accent,
                        modifier = Modifier.align(Alignment.Center).size(28.dp),
                    )
                } else {
                    com.gpleader.app.core.ui.components.NeuButtonPrimary(
                        text     = "Crear delegación",
                        onClick  = {
                            val id = selectedId ?: return@NeuButtonPrimary
                            onCrear(id, nota.trim().ifBlank { null })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (!canCreate) Modifier.alpha(0.5f) else Modifier),
                    )
                }
            }
        }
    }
}

// ── Sheet: elegir tipo de registro ───────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TipoRegistroSheet(
    onDismiss:           () -> Unit,
    onGpMeeting:         () -> Unit,
    onSabado:            () -> Unit,
    reunionGpHoy:        ReunionResumen?         = null,
    reunionSabadoSemana: SabbathMeetingResumen?  = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Background,
        dragHandle       = null,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── Handle pill ───────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .neuInset(cornerRadius = 3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BackgroundDeep),
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Título ────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text  = "¿Qué deseas registrar?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "Elige el tipo de registro para esta sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Opciones ──────────────────────────────────────────────────────
            if (reunionGpHoy != null) {
                YaRegistrasteBadge(
                    titulo    = "Ya registraste hoy tu grupo pequeño",
                    subtitulo = "${reunionGpHoy.presentes} presentes · ${reunionGpHoy.ausentes} ausentes",
                )
            } else {
                RegistrarOpcion(
                    icon      = Icons.Filled.Groups,
                    titulo    = "Reunión de GP",
                    subtitulo = "Registro semanal del grupo pequeño",
                    color     = Accent,
                    onClick   = onGpMeeting,
                )
            }

            if (reunionSabadoSemana != null) {
                YaRegistrasteBadge(
                    titulo    = "Ya registraste el culto de sábado",
                    subtitulo = "${reunionSabadoSemana.presentes} presentes",
                )
            } else {
                RegistrarOpcion(
                    icon      = Icons.Filled.AutoAwesome,
                    titulo    = "Culto de Sábado",
                    subtitulo = "Asistencia al culto del sábado",
                    color     = Violet,
                    onClick   = onSabado,
                )
            }
        }
    }
}

@Composable
private fun RegistrarOpcion(
    icon:      ImageVector,
    titulo:    String,
    subtitulo: String,
    color:     Color,
    onClick:   () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Background)
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth(),
        ) {
            // ── Franja de color lateral ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(color),
            )

            Spacer(Modifier.width(16.dp))

            // ── Ícono neumórfico (hundido) ────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .neuInsetSm(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundDeep),
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = color,
                    modifier           = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            // ── Textos ────────────────────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .weight(1f)
                    .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
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

            // ── Flecha ────────────────────────────────────────────────────────
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = Muted,
                modifier           = Modifier
                    .size(20.dp)
                    .padding(end = 4.dp),
            )

            Spacer(Modifier.width(16.dp))
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewUiState = HomeUiState(
    nombreLider          = "Maria Garcia",
    grupo                = GrupoInfo("GP Los Olivos", "Miércoles", "7:00 PM", "Iglesia Central"),
    porcentajeAsistencia = 67,
    totalPresentes       = 2,
    totalAusentes        = 1,
    totalJustificados    = 1,
    totalMiembros        = 4,
    reunionesRecientes   = listOf(
        ReunionResumen("r1", LocalDate.of(2026, 2, 26), EstadoReunion.ENVIADA, 12, 2),
        ReunionResumen("r2", LocalDate.of(2026, 2, 19), EstadoReunion.ENVIADA, 10, 3),
    ),
)

@Preview(showSystemUi = true, name = "Home — con reuniones")
@Composable
private fun HomeScreenPreview() {
    GpLeaderTheme {
        HomeScreenContent(
            uiState             = previewUiState,
            onVerTodasClick     = {},
            onReunionClick      = {},
            onHistorialTabClick = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Home — sin reuniones")
@Composable
private fun HomeScreenEmptyPreview() {
    GpLeaderTheme {
        HomeScreenContent(
            uiState             = previewUiState.copy(reunionesRecientes = emptyList()),
            onVerTodasClick     = {},
            onReunionClick      = {},
            onHistorialTabClick = {},
        )
    }
}
