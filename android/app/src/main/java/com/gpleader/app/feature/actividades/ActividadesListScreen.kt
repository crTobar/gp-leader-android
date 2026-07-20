package com.gpleader.app.feature.actividades

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.DuoActividadConTotal
import com.gpleader.app.core.data.repository.DuoMisioneroData
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.ui.components.AppBottomNavBar
import com.gpleader.app.core.ui.components.NAV_TAB_ACTIVIDADES
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.OfflineBanner
import com.gpleader.app.core.ui.components.rememberIsOnline
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
import com.gpleader.app.core.ui.theme.neuInset
import com.gpleader.app.core.ui.theme.neuInsetInner
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.gpleader.app.core.ui.theme.Shadow

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    var collapsedNiveles by remember { mutableStateOf(setOf<String>()) }
    val isOnline = rememberIsOnline()

    Scaffold(
        containerColor = Background,
    ) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
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

            OfflineBanner(
                // Los dúos no tienen caché en Room: offline no hay datos guardados que mostrar.
                mensaje           = if (modoGP) "Sin conexión · mostrando datos guardados"
                                    else        "Sin conexión · los dúos no están disponibles",
                mostrarUltimaSync = modoGP,
                modifier          = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
            )

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

            // ── Filtros (desplegables Nivel / Estado) ─────────────────────────
            FiltrosDropdownRow(
                filtroNivel    = uiState.filtroNivel,
                filtroEstado   = uiState.filtroEstado,
                onFiltroNivel  = onFiltroNivel,
                onFiltroEstado = onFiltroEstado,
                modifier       = Modifier.padding(top = 16.dp),
            )

            // ── Resumen de conteo ─────────────────────────────────────────────
            val totalVisibles = uiState.visibles.size
            val conRegistros  = uiState.visibles.count { it.totalCantidad > 0 || it.montoTotal > 0 }
            val contadorStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 1.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp)
                    .padding(top = 16.dp, bottom = 6.dp),
            ) {
                Text(text = "$totalVisibles actividades", style = contadorStyle, color = Muted)
                Text(text = "  ·  ", style = contadorStyle, color = Muted)
                // Verde vivo #2EA86A (mismo del check "ya registraste"), ≠ Sage.
                Text(text = "$conRegistros con registros", style = contadorStyle, color = Color(0xFF2EA86A))
            }

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
                        bottom = 110.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    nivelesOrdenados.forEach { nivel ->
                        val actividadesNivel = porNivel[nivel].orEmpty()
                        val isCollapsed      = nivel in collapsedNiveles

                        stickyHeader(key = "nivel-$nivel") {
                            NivelSectionHeader(
                                level       = nivel,
                                count       = actividadesNivel.size,
                                isCollapsed = isCollapsed,
                                onToggle    = {
                                    collapsedNiveles = if (isCollapsed) collapsedNiveles - nivel
                                                       else collapsedNiveles + nivel
                                },
                            )
                        }

                        if (!isCollapsed) {
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
                }
                } // PullToRefreshBox
            }
        }

        // ── Overlay inferior: FAB (solo GP) + menú flotante sobre el contenido ─
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            // Crear actividad escribe en Supabase → no disponible sin conexión (modo solo lectura).
            if (modoGP && isOnline) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 20.dp, bottom = 8.dp)
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
            AppBottomNavBar(
                selectedTab        = NAV_TAB_ACTIVIDADES,
                onInicioClick      = onNavigateToHome,
                onActividadesClick = {},
                onPerfilClick      = onNavigateToPerfil,
            )
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
    val isOnline = rememberIsOnline()

    // Sin conexión los dúos no se cachean: no hay nada que mostrar ni se puede crear.
    val fabEnabled  = isOnline && !isLoading && duosActivos.isNotEmpty()
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
                // Offline la lista siempre viene vacía (sin caché), así que no se puede afirmar
                // que no haya dúos ni invitar a crear uno.
                Text(
                    text      = if (isOnline) "Sin actividades de dúos.\nToca + para crear una."
                                else          "Los dúos no están disponibles sin conexión.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = Muted,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(actividades, key = { it.tipo.id }) { item ->
                    DuoActividadCard(item = item, onClick = { onCardClick(item) })
                }
            }
        }

        // FAB — sobre el menú flotante (navigationBarsPadding + altura del menú)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 110.dp)
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
                // weight + maxLines: el nombre toma el espacio sobrante y se trunca; así el chip de
                // tipo conserva su ancho natural en vez de comprimirse y hacer wrap vertical.
                Text(
                    text     = "${item.duo.member1.primerNombre} & ${item.duo.member2.primerNombre}",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Accent.copy(alpha = 0.12f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(tipoLabel, style = MaterialTheme.typography.labelSmall, color = Accent, fontWeight = FontWeight.Bold, maxLines = 1)
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

// ── Filtros desplegables (Nivel / Estado) ─────────────────────────────────────

private enum class MenuFiltro { NIVEL, ESTADO }

/** Color del punto de un nivel; gris (Shadow) para "Todo". Reusa levelColor(). */
private fun nivelDotColor(filtro: FiltroNivel): Color =
    if (filtro == FiltroNivel.TODOS) Shadow else levelColor(filtro.valor ?: "")

/** Color del punto de estado: gris por defecto · Sage=Activas · Blush (rojo/alerta)=Inactivas. */
private fun estadoDotColor(filtro: FiltroEstado): Color = when (filtro) {
    FiltroEstado.TODAS     -> Shadow
    FiltroEstado.ACTIVAS   -> Sage
    FiltroEstado.INACTIVAS -> Blush
}

/**
 * Reemplaza las dos filas de chips por dos botones desplegables lado a lado (Nivel · Estado).
 * Abrir uno cierra el otro; cada menú se cierra al seleccionar o al tocar fuera.
 */
@Composable
private fun FiltrosDropdownRow(
    filtroNivel:    FiltroNivel,
    filtroEstado:   FiltroEstado,
    onFiltroNivel:  (FiltroNivel) -> Unit,
    onFiltroEstado: (FiltroEstado) -> Unit,
    modifier:       Modifier = Modifier,
) {
    var abierto by remember { mutableStateOf<MenuFiltro?>(null) }

    Row(
        modifier              = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // NIVEL
        FiltroDropdown(
            label     = filtroNivel.label,
            dotColor  = nivelDotColor(filtroNivel),
            activo    = filtroNivel != FiltroNivel.TODOS,
            abierto   = abierto == MenuFiltro.NIVEL,
            onToggle  = { abierto = if (abierto == MenuFiltro.NIVEL) null else MenuFiltro.NIVEL },
            onDismiss = { abierto = null },
            eyebrow   = "NIVEL DE ACTIVIDAD",
        ) {
            FiltroNivel.entries.forEach { opt ->
                FiltroMenuItem(
                    label        = opt.label,
                    dotColor     = nivelDotColor(opt),
                    seleccionado = opt == filtroNivel,
                    onClick      = { onFiltroNivel(opt); abierto = null },
                )
            }
        }
        // ESTADO
        FiltroDropdown(
            label     = filtroEstado.label,
            dotColor  = estadoDotColor(filtroEstado),
            activo    = filtroEstado != FiltroEstado.TODAS,
            abierto   = abierto == MenuFiltro.ESTADO,
            onToggle  = { abierto = if (abierto == MenuFiltro.ESTADO) null else MenuFiltro.ESTADO },
            onDismiss = { abierto = null },
            eyebrow   = "ESTADO",
        ) {
            FiltroEstado.entries.forEach { opt ->
                FiltroMenuItem(
                    label        = opt.label,
                    dotColor     = null,   // el menú de Estado no lleva punto en las filas
                    seleccionado = opt == filtroEstado,
                    onClick      = { onFiltroEstado(opt); abierto = null },
                )
            }
        }
    }
}

/** Botón desplegable neumórfico (44dp, elevado) + su menú anclado debajo. */
@Composable
private fun FiltroDropdown(
    label:     String,
    dotColor:  Color,
    activo:    Boolean,
    abierto:   Boolean,
    onToggle:  () -> Unit,
    onDismiss: () -> Unit,
    eyebrow:   String,
    opciones:  @Composable ColumnScope.() -> Unit,
) {
    val rotacion by animateFloatAsState(if (abierto) 180f else 0f, label = "chevron")
    val density = LocalDensity.current

    Box {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier
                .height(44.dp)
                .neuElevatedSm(cornerRadius = 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Background)
                .clickable(onClick = onToggle)
                .padding(horizontal = 15.dp),
        ) {
            Box(Modifier.size(7.dp).clip(RoundedCornerShape(2.dp)).background(dotColor))
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                color = if (activo) Ink else Mid,
            )
            Icon(
                imageVector        = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint               = if (activo) Ink else Mid,
                modifier           = Modifier.size(18.dp).rotate(rotacion),
            )
        }

        if (abierto) {
            Popup(
                alignment        = Alignment.TopStart,
                offset           = with(density) { IntOffset(0, 52.dp.roundToPx()) },
                onDismissRequest = onDismiss,
                properties       = PopupProperties(focusable = true),
            ) {
                Column(
                    modifier = Modifier
                        .width(196.dp)
                        .neuElevated(cornerRadius = 18.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Background)
                        .padding(8.dp),
                ) {
                    Text(
                        text     = eyebrow,
                        style    = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp),
                        color    = Muted,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 6.dp),
                    )
                    opciones()
                }
            }
        }
    }
}

/** Fila de opción del menú. Seleccionada = hundida (neuInset) + texto Ink SemiBold + check Accent. */
@Composable
private fun FiltroMenuItem(
    label:        String,
    dotColor:     Color?,
    seleccionado: Boolean,
    onClick:      () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (seleccionado) Modifier.neuInset(cornerRadius = 12.dp) else Modifier)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        if (dotColor != null) {
            Box(Modifier.size(7.dp).clip(RoundedCornerShape(2.dp)).background(dotColor))
            Spacer(Modifier.width(9.dp))
        }
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (seleccionado) Ink else Mid,
            modifier   = Modifier.weight(1f),
        )
        if (seleccionado) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Card de actividad (compacta) ──────────────────────────────────────────────

@Composable
private fun ActividadCard(
    item:    ActividadConResumen,
    onClick: () -> Unit,
) {
    val levelColor = levelColor(item.tipo.level)
    val esCheckbox = item.tipo.markerType == "realizado" || item.tipo.markerType == "checkbox"
    val tieneValor = item.totalCantidad > 0 || item.montoTotal > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (item.esProxima) Modifier.alpha(0.6f) else Modifier)
            .neuElevated(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Background)
            .then(if (item.esProxima) Modifier else Modifier.clickable(onClick = onClick))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Ícono de tipo (tile hundido) ──────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Background)
                .neuInsetInner(shadowSize = 10.dp),
        ) {
            ActividadTipoIcon(markerType = item.tipo.markerType, tint = levelColor)
        }

        Spacer(Modifier.width(14.dp))

        // ── Nombre + badge de nivel ───────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = item.tipo.nombre,
                style      = MaterialTheme.typography.bodyLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 2,
            )
            Spacer(Modifier.height(4.dp))
            NivelPill(level = item.tipo.level)
        }

        Spacer(Modifier.width(10.dp))

        // ── Valor a la derecha ────────────────────────────────────────────────
        if (item.esProxima) {
            Icon(Icons.Default.Schedule, null, tint = Muted, modifier = Modifier.size(18.dp))
        } else {
            when {
                esCheckbox -> {
                    Icon(
                        imageVector        = if (tieneValor) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint               = if (tieneValor) Sage else Muted,
                        modifier           = Modifier.size(24.dp),
                    )
                }
                item.tipo.markerType == "monetary" -> {
                    Text(
                        text       = "₡${item.montoTotal.toLong()}",
                        style      = MaterialTheme.typography.headlineSmall,
                        color      = levelColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
                else -> {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text       = item.totalCantidad.toString(),
                            style      = MaterialTheme.typography.headlineSmall,
                            color      = if (tieneValor) levelColor else Muted,
                            fontWeight = FontWeight.Bold,
                        )
                        if (item.tipo.unitLabel.isNotBlank()) {
                            Text(
                                text  = item.tipo.unitLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = Muted,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
}

// ── Ícono de tipo de actividad ────────────────────────────────────────────────

@Composable
private fun ActividadTipoIcon(markerType: String, tint: Color) {
    when (markerType) {
        "monetary" -> Text(
            text       = "₡",
            style      = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color      = tint,
        )
        "realizado", "checkbox" -> Icon(
            imageVector        = Icons.Default.CheckCircle,
            contentDescription = null,
            tint               = tint,
            modifier           = Modifier.size(22.dp),
        )
        "participants" -> Icon(
            imageVector        = Icons.Filled.Group,
            contentDescription = null,
            tint               = tint,
            modifier           = Modifier.size(22.dp),
        )
        else -> Text(
            text       = "#",
            style      = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color      = tint,
        )
    }
}

// ── Pill de nivel (badge bajo el nombre) ──────────────────────────────────────

@Composable
private fun NivelPill(level: String) {
    val (label, color) = when (level) {
        "union"  -> "Unión" to Gold
        "pastor" -> "Pastor" to Ink
        else     -> "Mi GP" to Accent
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Header de sección por nivel (colapsable) ──────────────────────────────────

@Composable
private fun NivelSectionHeader(
    level:       String,
    count:       Int,
    isCollapsed: Boolean,
    onToggle:    () -> Unit,
) {
    val color = levelColor(level)
    val label = when (level) { "union" -> "UNIÓN"; "pastor" -> "PASTOR"; else -> "MI GP" }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = Ink,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text  = "· $count",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector        = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = if (isCollapsed) "Expandir" else "Colapsar",
            tint               = Muted,
            modifier           = Modifier.size(20.dp),
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun levelColor(level: String): Color = when (level) {
    "union"  -> Gold
    "pastor" -> Ink
    else     -> Accent
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
