package com.gpleader.app.feature.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.AppBottomNavBar
import com.gpleader.app.core.ui.components.NAV_TAB_ACTIVIDADES
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.OnResumeEffect
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInsetInner
import com.gpleader.app.core.ui.theme.neuInsetSm
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun ActividadesListScreen(
    onNavigateBack:          () -> Unit,
    onNavigateToHistorial:   (actividadTipoId: String) -> Unit,
    onNavigateToCrear:       () -> Unit = {},
    onNavigateToHome:        () -> Unit = {},
    onNavigateToPerfil:      () -> Unit = {},
    onNavigateToCampana:     (tipoId: String, nombre: String, desde: String, hasta: String) -> Unit = { _, _, _, _ -> },
    viewModel: ActividadesListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }
    ActividadesListContent(
        uiState               = uiState,
        onNavigateBack        = onNavigateBack,
        onNavigateToHistorial = onNavigateToHistorial,
        onNavigateToCrear     = onNavigateToCrear,
        onNavigateToHome      = onNavigateToHome,
        onNavigateToPerfil    = onNavigateToPerfil,
        onNavigateToCampana   = onNavigateToCampana,
        onFiltroNivel         = viewModel::onFiltroNivel,
        onFiltroEstado        = viewModel::onFiltroEstado,
        onRefresh             = viewModel::onRefresh,
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActividadesListContent(
    uiState:               ActividadesListUiState,
    onNavigateBack:        () -> Unit,
    onNavigateToHistorial: (String) -> Unit,
    onNavigateToCrear:     () -> Unit = {},
    onNavigateToHome:      () -> Unit = {},
    onNavigateToPerfil:    () -> Unit = {},
    onNavigateToCampana:   (tipoId: String, nombre: String, desde: String, hasta: String) -> Unit = { _, _, _, _ -> },
    onFiltroNivel:         (FiltroNivel) -> Unit,
    onFiltroEstado:        (FiltroEstado) -> Unit,
    onRefresh:             () -> Unit = {},
) {
    Scaffold(
        containerColor = Background,
        bottomBar = {
            AppBottomNavBar(
                selectedTab        = NAV_TAB_ACTIVIDADES,
                onInicioClick      = onNavigateToHome,
                onActividadesClick = {},
                onPerfilClick      = onNavigateToPerfil,
            )
        },
    ) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(Background),
    ) {
        var modoGP by remember { mutableStateOf(true) }

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
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
                        contentDescription = null,
                        tint               = Ink,
                        modifier           = Modifier.size(20.dp),
                    )
                }
                Text(
                    text      = "Actividades",
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Ink,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.weight(1f),
                )
                Box(modifier = Modifier.size(40.dp))
            }

            // ── Segmented control GP / Duos ───────────────────────────────────
            ModoSegmentedControl(
                modoGP   = modoGP,
                onSelect = { modoGP = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
                }
                return@Column
            }
            if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error, style = MaterialTheme.typography.bodyMedium, color = Blush)
                }
                return@Column
            }

            if (!modoGP) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = "Actividades de dúos misioneros\npróximamente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                }
                return@Column
            }

            // ── Filtros ───────────────────────────────────────────────────────
            NeuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Nivel
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FiltroNivel.entries.forEach { filtro ->
                            FiltroChip(
                                label    = filtro.label,
                                selected = uiState.filtroNivel == filtro,
                                onClick  = { onFiltroNivel(filtro) },
                            )
                        }
                    }
                    HorizontalDivider(
                        color    = BackgroundDeep,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                    // Estado
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FiltroEstado.entries.forEach { filtro ->
                            FiltroChip(
                                label         = filtro.label,
                                selected      = uiState.filtroEstado == filtro,
                                onClick       = { onFiltroEstado(filtro) },
                                selectedColor = when (filtro) {
                                    FiltroEstado.ACTIVAS   -> Sage
                                    FiltroEstado.INACTIVAS -> Muted
                                    else                   -> Accent
                                },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Lista ─────────────────────────────────────────────────────────
            if (uiState.visibles.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Sin actividades", style = MaterialTheme.typography.bodyMedium, color = Muted)
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh    = onRefresh,
                    modifier     = Modifier.fillMaxSize(),
                indicator = {},
                ) {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = androidx.compose.foundation.layout.PaddingValues(
                        start  = 20.dp,
                        end    = 20.dp,
                        bottom = 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.visibles, key = { it.tipo.id }) { item ->
                        ActividadCard(
                            item    = item,
                            onClick = {
                                if (item.tipo.frecuencia == "diaria") {
                                    val hoy   = java.time.LocalDate.now()
                                    val desde = (item.tipo.startDate ?: hoy.minusDays(30)).toString()
                                    val hasta = (item.tipo.endDate?.let { if (it.isBefore(hoy)) it else hoy } ?: hoy).toString()
                                    onNavigateToCampana(item.tipo.id, item.tipo.nombre, desde, hasta)
                                } else {
                                    onNavigateToHistorial(item.tipo.id)
                                }
                            },
                        )
                    }
                }
                } // PullToRefreshBox
            }
        }

        // ── FAB neumórfico ────────────────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .neuGlow(cornerRadius = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Accent)
                .clickable(onClick = onNavigateToCrear)
                .size(56.dp),
        ) {
            Icon(
                imageVector        = Icons.Default.Add,
                contentDescription = "Nueva actividad",
                tint               = Color.White,
                modifier           = Modifier.size(24.dp),
            )
        }
    }   // Box
    }   // Scaffold
}

// ── Chip de filtro ────────────────────────────────────────────────────────────

@Composable
private fun FiltroChip(
    label:         String,
    selected:      Boolean,
    onClick:       () -> Unit,
    selectedColor: Color = Accent,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .then(
                if (selected)
                    Modifier.neuGlow(cornerRadius = 20.dp)
                else
                    Modifier.neuElevatedSm(cornerRadius = 20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) selectedColor else Background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = if (selected) Color.White else Mid,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

// ── Card de actividad ─────────────────────────────────────────────────────────

@Composable
private fun ActividadCard(
    item:    ActividadConResumen,
    onClick: () -> Unit,
) {
    val fmt        = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))
    val levelColor = levelColor(item.tipo.level)

    NeuCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (item.esProxima) Modifier.alpha(0.6f) else Modifier),
        onClick  = if (item.esProxima) null else onClick,
    ) {
        Column {
            // ── Franja de nivel ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(levelColor.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    NivelBadge(level = item.tipo.level)
                    val estadoBadge = when {
                        item.esProxima  -> EstadoActividad.PROXIMA
                        item.esActiva   -> EstadoActividad.ACTIVA
                        else            -> EstadoActividad.VENCIDA
                    }
                    EstadoBadge(estado = estadoBadge)
                }
            }

            // ── Nombre + flecha ───────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text       = item.tipo.nombre,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f),
                )
                if (item.esProxima) {
                    Icon(
                        imageVector        = Icons.Default.Schedule,
                        contentDescription = null,
                        tint               = Muted,
                        modifier           = Modifier.size(18.dp),
                    )
                } else {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint               = Muted,
                        modifier           = Modifier.size(20.dp),
                    )
                }
            }

            HorizontalDivider(
                color    = BackgroundDeep,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            // ── Total acumulado ───────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (item.tipo.markerType == "realizado" || item.tipo.markerType == "checkbox") {
                    // Tipo realizado: ícono de check o circle vacío
                    val hecho = item.totalCantidad > 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hecho) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint     = if (hecho) Sage else Muted,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text  = if (hecho) "Realizado" else "Pendiente",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (hecho) Sage else Muted,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                Column {
                    Text(
                        text  = "ACUMULADO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = formatTotalValor(item),
                        style      = MaterialTheme.typography.titleLarge,
                        color      = levelColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
                }

                // Rango de fechas (compacto)
                if (item.tipo.startDate != null || item.tipo.endDate != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text  = "PERÍODO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                        Spacer(Modifier.height(4.dp))
                        val rango = when {
                            item.tipo.startDate != null && item.tipo.endDate != null ->
                                "${item.tipo.startDate.format(fmt)} –\n${item.tipo.endDate.format(fmt)}"
                            item.tipo.startDate != null ->
                                "Desde\n${item.tipo.startDate.format(fmt)}"
                            else ->
                                "Hasta\n${item.tipo.endDate!!.format(fmt)}"
                        }
                        Text(
                            text      = rango,
                            style     = MaterialTheme.typography.labelSmall,
                            color     = Mid,
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
        }
    }
}

// ── Badges ────────────────────────────────────────────────────────────────────

@Composable
private fun NivelBadge(level: String) {
    val bg    = levelColor(level)
    val label = when (level) { "union" -> "UNIÓN"; "pastor" -> "PASTOR"; else -> "MI GP" }
    Box(
        modifier = Modifier
            .neuElevatedSm(cornerRadius = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

private enum class EstadoActividad { ACTIVA, VENCIDA, PROXIMA }

@Composable
private fun EstadoBadge(estado: EstadoActividad) {
    val (color, label) = when (estado) {
        EstadoActividad.ACTIVA  -> Sage  to "Activa"
        EstadoActividad.VENCIDA -> Blush to "Vencida"
        EstadoActividad.PROXIMA -> Accent to "Próxima"
    }
    Box(
        modifier = Modifier
            .neuElevatedSm(cornerRadius = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun levelColor(level: String): Color = when (level) {
    "union"  -> Gold
    "pastor" -> Ink
    else     -> Accent
}

private fun formatTotalValor(item: ActividadConResumen): String = when (item.tipo.markerType) {
    "monetary"                  -> "₡${item.montoTotal.toLong()}"
    "realizado", "checkbox"     -> ""
    else                        -> "${item.totalCantidad} ${item.tipo.unitLabel}"
}

// ── Segmented control GP / Duos ───────────────────────────────────────────────

@Composable
private fun ModoSegmentedControl(
    modoGP:   Boolean,
    onSelect: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Contenedor hundido (seg-tabs del HTML: neuInSm + border-radius pill)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Background)
            .neuInsetInner(shadowSize = 8.dp)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SegTab(
            label    = "Grupo pequeño",
            selected = modoGP,
            onClick  = { onSelect(true) },
            modifier = Modifier.weight(1f),
        )
        SegTab(
            label    = "Dúo misionero",
            selected = !modoGP,
            onClick  = { onSelect(false) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SegTab(
    label:    String,
    selected: Boolean,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(
                if (selected) Modifier
                    .neuElevatedSm(cornerRadius = 50.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Background)
                else Modifier.clip(RoundedCornerShape(50.dp))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyMedium.copy(
                fontWeight    = FontWeight.SemiBold,
                fontSize      = 13.sp,
                letterSpacing = 0.sp,
            ),
            color      = if (selected) Ink else Muted,
            textAlign  = TextAlign.Center,
        )
    }
}
