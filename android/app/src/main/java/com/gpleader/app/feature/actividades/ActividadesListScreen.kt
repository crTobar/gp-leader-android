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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.gpleader.app.core.data.repository.DuoActividadConTotal
import com.gpleader.app.core.data.repository.DuoMisioneroData
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.ui.components.AppBottomNavBar
import com.gpleader.app.core.ui.components.NAV_TAB_ACTIVIDADES
import com.gpleader.app.core.ui.components.NeuAvatar
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
import com.gpleader.app.core.ui.theme.neuElevated
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
    onNavigateBack:               () -> Unit,
    onNavigateToHistorial:        (actividadTipoId: String) -> Unit,
    onNavigateToCrear:            () -> Unit = {},
    onNavigateToHome:             () -> Unit = {},
    onNavigateToPerfil:           () -> Unit = {},
    onNavigateToCampana:          (tipoId: String, nombre: String, desde: String, hasta: String) -> Unit = { _, _, _, _ -> },
    onNavigateToCrearActividadDuo: (duoId: String) -> Unit = {},
    onNavigateToDuoActividad:     (actividadTipoId: String, nombre: String, duoId: String) -> Unit = { _, _, _ -> },
    viewModel: ActividadesListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect {
        viewModel.cargar()
        viewModel.cargarDuoActividades()
    }
    ActividadesListContent(
        uiState                    = uiState,
        onNavigateBack             = onNavigateBack,
        onNavigateToHistorial      = onNavigateToHistorial,
        onNavigateToCrear          = onNavigateToCrear,
        onNavigateToHome           = onNavigateToHome,
        onNavigateToPerfil         = onNavigateToPerfil,
        onNavigateToCampana        = onNavigateToCampana,
        onNavigateToCrearActividadDuo = onNavigateToCrearActividadDuo,
        onNavigateToDuoActividad   = onNavigateToDuoActividad,
        onFiltroNivel              = viewModel::onFiltroNivel,
        onFiltroEstado             = viewModel::onFiltroEstado,
        onRefresh                  = viewModel::onRefresh,
        onModoChange               = viewModel::onModoChange,
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActividadesListContent(
    uiState:                       ActividadesListUiState,
    onNavigateBack:                () -> Unit,
    onNavigateToHistorial:         (String) -> Unit,
    onNavigateToCrear:             () -> Unit = {},
    onNavigateToHome:              () -> Unit = {},
    onNavigateToPerfil:            () -> Unit = {},
    onNavigateToCampana:           (tipoId: String, nombre: String, desde: String, hasta: String) -> Unit = { _, _, _, _ -> },
    onNavigateToCrearActividadDuo: (duoId: String) -> Unit = {},
    onNavigateToDuoActividad:      (actividadTipoId: String, nombre: String, duoId: String) -> Unit = { _, _, _ -> },
    onFiltroNivel:                 (FiltroNivel) -> Unit,
    onFiltroEstado:                (FiltroEstado) -> Unit,
    onRefresh:                     () -> Unit = {},
    onModoChange:                  (Boolean) -> Unit = {},
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
        val modoGP = uiState.modoGP

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
                onSelect = { onModoChange(it) },
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
                DuoActividadesContent(
                    actividades                = uiState.duoActividades,
                    duosActivos               = uiState.duosActivos,
                    isLoading                 = uiState.isDuoLoading,
                    onCardClick               = { item -> onNavigateToDuoActividad(item.tipo.id, item.tipo.nombre, item.duo.id) },
                    onFabClick                = {},
                    onNavigateToCrearActividad = { onNavigateToCrearActividadDuo("todos") },
                    modifier                  = Modifier.weight(1f),
                )
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
                val ordenNiveles = listOf("union", "pastor", "my_group")
                val porNivel     = uiState.visibles.groupBy { it.tipo.level }
                val nivelesOrdenados = (ordenNiveles + porNivel.keys).distinct()
                    .filter { !porNivel[it].isNullOrEmpty() }

                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = androidx.compose.foundation.layout.PaddingValues(
                        start  = 20.dp,
                        end    = 20.dp,
                        bottom = 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    nivelesOrdenados.forEachIndexed { idx, nivel ->
                        val actividadesNivel = porNivel[nivel].orEmpty()

                        // ── Marcador de nivel (una vez por grupo) ─────────────
                        item(key = "nivel-$nivel") {
                            NivelBadge(
                                level    = nivel,
                                modifier = Modifier.padding(
                                    top    = if (idx == 0) 0.dp else 8.dp,
                                    start  = 4.dp,
                                    bottom = 2.dp,
                                ),
                            )
                        }

                        items(actividadesNivel, key = { it.tipo.id }) { item ->
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
                }
                } // PullToRefreshBox
            }
        }

        // ── FAB neumórfico — solo en modo GP ─────────────────────────────────
        if (modoGP) {
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
        }
    }   // Box
    }   // Scaffold
}

// ── Dúo: lista de actividades ─────────────────────────────────────────────────

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DuoActividadesContent(
    actividades:               List<DuoActividadConTotal>,
    duosActivos:               List<DuoMisioneroData>,
    isLoading:                 Boolean,
    onCardClick:               (DuoActividadConTotal) -> Unit,
    onFabClick:                () -> Unit,
    onNavigateToCrearActividad: (duoId: String) -> Unit = {},
    modifier:                  Modifier = Modifier,
) {
    var showDuoPicker by remember { mutableStateOf(false) }

    val fabEnabled  = !isLoading && duosActivos.isNotEmpty()
    val fabAlpha    = if (fabEnabled) 1f else 0.5f

    val handleFabClick: () -> Unit = {
        if (fabEnabled) {
            when {
                duosActivos.size == 1 -> onNavigateToCrearActividad(duosActivos[0].id)
                duosActivos.size > 1  -> showDuoPicker = true
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
            }
        } else if (actividades.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin actividades de dúos.\nToca + para crear una.", style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(actividades, key = { it.tipo.id }) { item ->
                    DuoActividadCard(item = item, onClick = { onCardClick(item) })
                }
            }
        }

        // FAB
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .alpha(fabAlpha)
                .neuGlow(cornerRadius = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Accent)
                .clickable(enabled = fabEnabled, onClick = handleFabClick)
                .size(56.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva actividad dúo", tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }

    // Sheet selector de dúo cuando hay más de uno
    if (showDuoPicker) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showDuoPicker = false },
            containerColor   = Background,
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "¿Para qué dúo?",
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Ink,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                duosActivos.forEach { duo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuElevated(cornerRadius = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Background)
                            .clickable {
                                showDuoPicker = false
                                onNavigateToCrearActividad(duo.id)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(44.dp)) {
                            NeuAvatar(iniciales = inicialesActividadesListDuo(duo.member2.nombreCompleto), size = 32.dp, modifier = Modifier.align(Alignment.BottomEnd))
                            NeuAvatar(iniciales = inicialesActividadesListDuo(duo.member1.nombreCompleto), size = 32.dp, modifier = Modifier.align(Alignment.TopStart))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "${duo.member1.primerNombre} & ${duo.member2.primerNombre}",
                            style      = MaterialTheme.typography.bodyLarge,
                            color      = Ink,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DuoActividadCard(
    item:    DuoActividadConTotal,
    onClick: () -> Unit,
) {
    val inic1 = inicialesActividadesListDuo(item.duo.member1.nombreCompleto)
    val inic2 = inicialesActividadesListDuo(item.duo.member2.nombreCompleto)
    val valorStr = when (item.tipo.markerType) {
        "monetary"      -> "₡${item.montoTotal.toLong()}"
        "daily_checker" -> "${item.diasMarcados} días"
        "checkbox"      -> if (item.diasMarcados > 0) "✓" else "—"
        else            -> "${item.totalCantidad} ${item.tipo.unitLabel}"
    }
    val tipoLabel = when (item.tipo.markerType) {
        "monetary"      -> "Monetario"
        "daily_checker" -> "Diario"
        "checkbox"      -> "Verificación"
        else            -> "Contador"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatares dúo
        Box(modifier = Modifier.size(44.dp)) {
            NeuAvatar(iniciales = inic2, size = 32.dp, modifier = Modifier.align(Alignment.BottomEnd))
            NeuAvatar(iniciales = inic1, size = 32.dp, modifier = Modifier.align(Alignment.TopStart))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.tipo.nombre, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${item.duo.member1.primerNombre} & ${item.duo.member2.primerNombre}", style = MaterialTheme.typography.labelSmall, color = Muted)
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Accent.copy(alpha = 0.12f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(tipoLabel, style = MaterialTheme.typography.labelSmall, color = Accent, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(valorStr, style = MaterialTheme.typography.bodyLarge, color = Accent, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Muted, modifier = Modifier.size(18.dp))
    }
}

private fun inicialesActividadesListDuo(nombre: String): String {
    val partes = nombre.trim().split(" ").filter { it.isNotBlank() }
    return when {
        partes.size >= 2 -> "${partes[0].first().uppercaseChar()}${partes[1].first().uppercaseChar()}"
        partes.size == 1 -> partes[0].take(2).uppercase()
        else             -> "?"
    }
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
    val estadoBadge = when {
        item.esProxima -> EstadoActividad.PROXIMA
        item.esActiva  -> EstadoActividad.ACTIVA
        else           -> EstadoActividad.VENCIDA
    }

    NeuCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (item.esProxima) Modifier.alpha(0.6f) else Modifier),
        onClick  = if (item.esProxima) null else onClick,
    ) {
        Column {
            // ── Nombre + estado + flecha ──────────────────────────────────────
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
                EstadoBadge(estado = estadoBadge)
                Spacer(Modifier.width(8.dp))
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
                    // Tipo realizado: ícono en pozo neumórfico hundido
                    val hecho = item.totalCantidad > 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .neuInsetSm(cornerRadius = 18.dp)
                                .clip(CircleShape)
                                .background(Background),
                        ) {
                            Icon(
                                imageVector = if (hecho) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint     = if (hecho) Sage else Muted,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
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
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .neuInsetSm(cornerRadius = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Background)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text       = formatTotalValor(item),
                            style      = MaterialTheme.typography.titleLarge,
                            color      = levelColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
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
    }   // NeuCard
}

// ── Badges ────────────────────────────────────────────────────────────────────

@Composable
private fun NivelBadge(level: String, modifier: Modifier = Modifier) {
    val label = when (level) { "union" -> "NIVEL UNIÓN"; "pastor" -> "NIVEL PASTOR"; else -> "NIVEL MI GP" }
    val icon: ImageVector = when (level) {
        "union"  -> Icons.Filled.AccountBalance
        "pastor" -> Icons.Filled.Star
        else     -> Icons.Filled.Group
    }
    Row(
        modifier              = modifier,
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .neuInsetSm(cornerRadius = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Background),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = Mid,
                modifier           = Modifier.size(15.dp),
            )
        }
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = Mid,
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
