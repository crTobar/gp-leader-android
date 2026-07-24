package com.gpleader.app.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.data.repository.CampoItem
import com.gpleader.app.core.data.repository.DistritoItem
import com.gpleader.app.core.data.repository.GrupoItem
import com.gpleader.app.core.data.repository.IglesiaItem
import com.gpleader.app.core.ui.components.AppLogo
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInset
import com.gpleader.app.core.ui.theme.neuInsetInner

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    onNavigateToQuienEres: () -> Unit,
    onNavigateToIglesiaHome: () -> Unit = {},
    onNavigateToNivelHome: (nivel: String) -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToQuienEres) {
        if (uiState.navigateToQuienEres) {
            onNavigateToQuienEres()
            viewModel.consumeQuienEresNavigation()
        }
    }

    LaunchedEffect(uiState.navigateToIglesiaHome) {
        if (uiState.navigateToIglesiaHome) {
            onNavigateToIglesiaHome()
            viewModel.consumeIglesiaHomeNavigation()
        }
    }

    LaunchedEffect(uiState.navigateToNivelHome) {
        uiState.navigateToNivelHome?.let {
            onNavigateToNivelHome(it)
            viewModel.consumeNivelHomeNavigation()
        }
    }

    LoginScreenContent(
        uiState                        = uiState,
        onCampoSelected                = viewModel::onCampoSelected,
        onDistritoSelected             = viewModel::onDistritoSelected,
        onIglesiaSelected              = viewModel::onIglesiaSelected,
        onGrupoTap                     = viewModel::onGrupoTap,
        onIngresarComoIglesia          = viewModel::onIngresarComoIglesia,
        onVolverDesdeModoIglesia       = viewModel::onVolverDesdeModoIglesia,
        onIglesiaSearchQueryChange     = viewModel::onIglesiaSearchQueryChange,
        onIglesiaParaLoginSelected     = viewModel::onIglesiaParaLoginSelected,
        onDismissIglesiaPasswordDialog = viewModel::onDismissIglesiaPasswordDialog,
        onConfirmarAccesoIglesia       = viewModel::onConfirmarAccesoIglesia,
        onLoginIglesia                 = viewModel::loginIglesiaDirecto,
    )

    if (uiState.showNivelChooser) {
        NivelChooserDialog(
            onElegir  = viewModel::onNivelElegido,
            onDismiss = viewModel::onDismissNivelChooser,
        )
    }
}

/** Selector DEV del nivel con el que entrar tras confirmar una iglesia. */
@Composable
private fun NivelChooserDialog(
    onElegir:  (String) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Background,
        title = { Text("Entrar como", style = MaterialTheme.typography.titleLarge, color = Ink) },
        text  = { Text("Elige el nivel con el que quieres ingresar.", style = MaterialTheme.typography.bodyMedium, color = Mid) },
        confirmButton = {
            Column {
                listOf(
                    "CHURCH"   to "Iglesia (anciano)",
                    "DISTRICT" to "Pastor (distrito)",
                    "CAMPO"    to "Asociación (campo)",
                    "UNION"    to "Unión",
                ).forEach { (nivel, label) ->
                    androidx.compose.material3.TextButton(onClick = { onElegir(nivel) }) {
                        Text(label, color = Accent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cancelar", color = Mid) } },
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun LoginScreenContent(
    uiState:                         LoginUiState,
    onCampoSelected:                 (CampoItem?) -> Unit,
    onDistritoSelected:              (DistritoItem?) -> Unit,
    onIglesiaSelected:               (IglesiaItem?) -> Unit,
    onGrupoTap:                      (GrupoItem) -> Unit,
    onIngresarComoIglesia:           () -> Unit = {},
    onVolverDesdeModoIglesia:        () -> Unit = {},
    onIglesiaSearchQueryChange:      (String) -> Unit = {},
    onIglesiaParaLoginSelected:      (IglesiaItem) -> Unit = {},
    onDismissIglesiaPasswordDialog:  () -> Unit = {},
    onConfirmarAccesoIglesia:        () -> Unit = {},
    onLoginIglesia:                  (IglesiaItem, String) -> Unit = { _, _ -> },
) {
    var active by remember { mutableStateOf(ActiveDropdown.NONE) }
    var mostrarFiltros by remember { mutableStateOf(false) }
    var cardExpandido  by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var grupoQuery    by remember { mutableStateOf("") }
    var campoQuery    by remember { mutableStateOf("") }
    var distritoQuery by remember { mutableStateOf("") }
    var iglesiaQuery  by remember { mutableStateOf("") }
    // Rediseño: selector de perfil + estado de la pestaña Iglesia (login inline).
    var perfilTab           by remember { mutableStateOf(PerfilTab.GP) }
    var iglesiaTabOpen      by remember { mutableStateOf(false) }
    var selectedIglesiaLogin by remember { mutableStateOf<IglesiaItem?>(null) }
    var iglesiaPass         by remember { mutableStateOf("") }

    // La tarjeta se expande automáticamente al interactuar con cualquier campo o en modo iglesia
    LaunchedEffect(active, mostrarFiltros) {
        if (active != ActiveDropdown.NONE || mostrarFiltros) cardExpandido = true
    }
    LaunchedEffect(uiState.iglesiaMode) {
        if (uiState.iglesiaMode) cardExpandido = true
    }

    fun expand(d: ActiveDropdown) { active = d }
    fun collapse() { active = ActiveDropdown.NONE; focusManager.clearFocus() }

    LaunchedEffect(active) {
        if (active != ActiveDropdown.GRUPO)    grupoQuery = ""
        if (active != ActiveDropdown.CAMPO)    campoQuery = ""
        if (active != ActiveDropdown.DISTRITO) distritoQuery = ""
        if (active != ActiveDropdown.IGLESIA)  iglesiaQuery = ""
    }

    val filteredGrupos = remember(grupoQuery, uiState.filteredGrupos) {
        if (grupoQuery.isBlank()) uiState.filteredGrupos
        else uiState.filteredGrupos.filter {
            it.nombre.contieneBusqueda(grupoQuery) ||
            it.iglesiaNombre.contieneBusqueda(grupoQuery) ||
            it.districtNombre.contieneBusqueda(grupoQuery) ||
            it.campoNombre.contieneBusqueda(grupoQuery)
        }
    }
    val filteredCampos = remember(campoQuery, uiState.allCampos) {
        if (campoQuery.isBlank()) uiState.allCampos
        else uiState.allCampos.filter { it.nombre.contieneBusqueda(campoQuery) }
    }
    val filteredDistritos = remember(distritoQuery, uiState.filteredDistritos) {
        if (distritoQuery.isBlank()) uiState.filteredDistritos
        else uiState.filteredDistritos.filter { it.nombre.contieneBusqueda(distritoQuery) }
    }
    val filteredIglesias = remember(iglesiaQuery, uiState.filteredIglesias) {
        if (iglesiaQuery.isBlank()) uiState.filteredIglesias
        else uiState.filteredIglesias.filter {
            it.nombre.contieneBusqueda(iglesiaQuery) ||
            it.districtNombre.contieneBusqueda(iglesiaQuery) ||
            it.campoNombre.contieneBusqueda(iglesiaQuery)
        }
    }

    SharedTransitionLayout {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenH = with(density) { maxHeight.toPx() }

        // Posición exacta del logo real en el header (se actualiza tras el primer layout)
        var headerLogoCenterY by remember { mutableFloatStateOf(0f) }

        val logoY        = remember { Animatable(0f) }
        val logoScale    = remember { Animatable(1.25f) }  // 80dp / 64dp = 1.25
        val overlayAlpha = remember { Animatable(1f) }

        LaunchedEffect(Unit) {
            delay(1000)
            // targetY calculado con la posición real capturada por onGloballyPositioned
            val targetY = headerLogoCenterY - screenH / 2f
            launch { logoY.animateTo(targetY, tween(700, easing = FastOutSlowInEasing)) }
            launch { logoScale.animateTo(0.8f, tween(700, easing = FastOutSlowInEasing)) }
            delay(700)
            overlayAlpha.animateTo(0f, tween(350))
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Brand (siempre visible). El logo y su animación de intro NO se tocan. ──
        Spacer(Modifier.height(22.dp))
        AppLogo(
            modifier = Modifier.onGloballyPositioned { coords ->
                headerLogoCenterY = coords.positionInRoot().y + coords.size.height / 2f
            },
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text      = stringResource(R.string.login_app_titulo),
            style     = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp),
            color     = Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text      = stringResource(R.string.login_header_subtitle),
            style     = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color     = Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))

        // ── Tarjeta principal ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .neuElevated(cornerRadius = 28.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Background)
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 22.dp),
        ) {
            Text(
                text  = "INICIAR SESIÓN",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 3.sp),
                color = Accent,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = "¿Cómo entrás?",
                style      = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(16.dp))

            PerfilSegmento(
                seleccion   = perfilTab,
                onSeleccion = { perfilTab = it; collapse(); iglesiaTabOpen = false },
            )
            Spacer(Modifier.height(18.dp))

            AnimatedContent(
                targetState    = perfilTab,
                modifier       = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInHorizontally { it / 12 }) togetherWith
                        (fadeOut(tween(120)) + slideOutHorizontally { -it / 12 })
                },
                label = "perfil-tab",
            ) { tab ->
                if (tab == PerfilTab.GP) {
                    // ── Pestaña Grupo pequeño (Accent) ──────────────────────────
                    Column(modifier = Modifier.fillMaxSize()) {
                        val grupoExpandido = active == ActiveDropdown.GRUPO
                        SeccionEyebrow(color = Accent, texto = "TU GRUPO PEQUEÑO")
                        DropdownSearchBox(
                            query        = grupoQuery,
                            selectedName = "",
                            placeholder  = stringResource(R.string.login_buscar_gp_hint),
                            expanded     = grupoExpandido,
                            isActive     = grupoExpandido,
                            leadingIcon  = {
                                Icon(Icons.Default.Search, null, tint = if (grupoExpandido) Accent else Muted, modifier = Modifier.size(18.dp))
                            },
                            onQueryChange = { q -> grupoQuery = q; if (active != ActiveDropdown.GRUPO) expand(ActiveDropdown.GRUPO) },
                            onFocused = { expand(ActiveDropdown.GRUPO) },
                            onToggle  = { if (grupoExpandido) collapse() else expand(ActiveDropdown.GRUPO) },
                        )

                        if (grupoExpandido) {
                            Box(modifier = Modifier.weight(1f)) {
                                GrupoResultList(filteredGrupos) { onGrupoTap(it); collapse() }
                            }
                        } else if (mostrarFiltros) {
                            Spacer(Modifier.height(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                Column(
                                    modifier            = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    FilterBlock(
                                        label = stringResource(R.string.login_label_campo), placeholder = stringResource(R.string.login_placeholder_campo),
                                        query = campoQuery, selectedName = uiState.selectedCampo?.nombre ?: "", isExpanded = active == ActiveDropdown.CAMPO,
                                        onQueryChange = { q -> campoQuery = q; if (active != ActiveDropdown.CAMPO) expand(ActiveDropdown.CAMPO) },
                                        onToggle = { if (active == ActiveDropdown.CAMPO) collapse() else expand(ActiveDropdown.CAMPO) },
                                    ) {
                                        FilterItemList(items = filteredCampos, itemLabel = { it.nombre }, isSelected = { it.nombre == (uiState.selectedCampo?.nombre ?: "") }, onSelected = { onCampoSelected(it); collapse() })
                                    }
                                    FilterBlock(
                                        label = stringResource(R.string.login_label_distrito), placeholder = stringResource(R.string.login_placeholder_distrito),
                                        query = distritoQuery, selectedName = uiState.selectedDistrito?.nombre ?: "", isExpanded = active == ActiveDropdown.DISTRITO,
                                        onQueryChange = { q -> distritoQuery = q; if (active != ActiveDropdown.DISTRITO) expand(ActiveDropdown.DISTRITO) },
                                        onToggle = { if (active == ActiveDropdown.DISTRITO) collapse() else expand(ActiveDropdown.DISTRITO) },
                                    ) {
                                        FilterItemList(items = filteredDistritos, itemLabel = { it.nombre }, isSelected = { it.nombre == (uiState.selectedDistrito?.nombre ?: "") }, onSelected = { onDistritoSelected(it); collapse() })
                                    }
                                    FilterBlock(
                                        label = stringResource(R.string.login_label_iglesia), placeholder = stringResource(R.string.login_placeholder_iglesia),
                                        query = iglesiaQuery, selectedName = uiState.selectedIglesia?.nombre ?: "", isExpanded = active == ActiveDropdown.IGLESIA,
                                        onQueryChange = { q -> iglesiaQuery = q; if (active != ActiveDropdown.IGLESIA) expand(ActiveDropdown.IGLESIA) },
                                        onToggle = { if (active == ActiveDropdown.IGLESIA) collapse() else expand(ActiveDropdown.IGLESIA) },
                                    ) {
                                        IglesiaItemList(items = filteredIglesias, selected = uiState.selectedIglesia, onSelected = { onIglesiaSelected(it); collapse() })
                                    }
                                }
                            }
                            MasOpcionesButton(mostrarFiltros = true, onClick = { mostrarFiltros = false; collapse() })
                        } else {
                            Spacer(Modifier.height(12.dp))
                            MasOpcionesButton(mostrarFiltros = false, onClick = { mostrarFiltros = true })
                            Spacer(Modifier.height(14.dp))
                            InfoNota(fondo = Accent.copy(alpha = 0.09f), texto = "Elegí tu GP y tocá tu nombre para entrar como líder o miembro. Después pedimos tu contraseña.")
                            Spacer(Modifier.weight(1f))
                            if (uiState.error != null) {
                                Text(uiState.error, style = MaterialTheme.typography.bodyMedium, color = Blush, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                            }
                            CtaPrimario(color = Accent, texto = "Buscar mi grupo", onClick = { expand(ActiveDropdown.GRUPO) })
                        }
                    }
                } else {
                    // ── Pestaña Iglesia (Gold) ──────────────────────────────────
                    Column(modifier = Modifier.fillMaxSize()) {
                        SeccionEyebrow(color = Gold, texto = "TU IGLESIA")
                        DropdownSearchBox(
                            query        = iglesiaQuery,
                            selectedName = selectedIglesiaLogin?.nombre ?: "",
                            placeholder  = "Buscar tu iglesia…",
                            expanded     = iglesiaTabOpen,
                            isActive     = iglesiaTabOpen,
                            leadingIcon  = { Icon(Icons.Default.Search, null, tint = if (iglesiaTabOpen) GoldDark else Muted, modifier = Modifier.size(18.dp)) },
                            onQueryChange = { q -> iglesiaQuery = q; iglesiaTabOpen = true },
                            onFocused = { iglesiaTabOpen = true },
                            onToggle  = { iglesiaTabOpen = !iglesiaTabOpen },
                        )
                        if (iglesiaTabOpen) {
                            Box(modifier = Modifier.weight(1f)) {
                                IglesiaResultList(filteredIglesias) { selectedIglesiaLogin = it; iglesiaQuery = ""; iglesiaTabOpen = false; focusManager.clearFocus() }
                            }
                        } else {
                            Spacer(Modifier.height(20.dp))
                            SeccionEyebrow(color = Gold, texto = "CONTRASEÑA DE IGLESIA")
                            NeuTextField(
                                value          = iglesiaPass,
                                onValueChange  = { iglesiaPass = it },
                                placeholder    = "Contraseña",
                                isPassword     = true,
                                leadingContent = { Icon(Icons.Default.Lock, null, tint = Muted, modifier = Modifier.size(18.dp)) },
                            )
                            Spacer(Modifier.height(16.dp))
                            InfoNota(fondo = Gold.copy(alpha = 0.14f), texto = "Ingresás como administrador de iglesia: aprobás aportes y ves todos los GP de tu iglesia.")
                            Spacer(Modifier.weight(1f))
                            CtaPrimario(
                                color   = Gold,
                                texto   = "Ingresar como iglesia",
                                enabled = selectedIglesiaLogin != null,
                                onClick = { selectedIglesiaLogin?.let { onLoginIglesia(it, iglesiaPass) } },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text  = "Ayuda para ingresar",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            color = Color(0xFFB7BFCC),
        )
        Spacer(Modifier.height(6.dp))
    }

        // ── Overlay splash: logo desliza hacia el header ─────────────────────
        if (overlayAlpha.value > 0f) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }
                    .graphicsLayer { alpha = overlayAlpha.value }
                    .background(Background),
                contentAlignment = Alignment.Center,
            ) {
                AppLogo(
                    size         = 80.dp,
                    cornerRadius = 24.dp,
                    iconSize     = 36.dp,
                    modifier     = Modifier.graphicsLayer {
                        translationY = logoY.value
                        scaleX       = logoScale.value
                        scaleY       = logoScale.value
                    },
                )
            }
        }
    } // BoxWithConstraints
    } // SharedTransitionLayout
}

// ── Bloque de filtro: buscador + resultados inline (sin LazyColumn) ───────────
// Usado dentro de un Column(verticalScroll), por eso los resultados son Column.

@Composable
private fun FilterBlock(
    label:          String,
    placeholder:    String,
    query:          String,
    selectedName:   String,
    isExpanded:     Boolean,
    onQueryChange:  (String) -> Unit,
    onToggle:       () -> Unit,
    resultsContent: @Composable () -> Unit,
) {
    Text(
        text     = label,
        style    = MaterialTheme.typography.labelSmall,
        color    = if (isExpanded) Accent else Muted,
        modifier = Modifier.padding(bottom = 4.dp),
    )
    DropdownSearchBox(
        query         = query,
        selectedName  = selectedName,
        placeholder   = placeholder,
        expanded      = isExpanded,
        isActive      = isExpanded,
        leadingIcon   = {
            Icon(
                imageVector        = Icons.Default.Search,
                contentDescription = null,
                tint               = if (isExpanded) Accent else Muted,
                modifier           = Modifier.size(18.dp),
            )
        },
        onQueryChange = onQueryChange,
        onFocused     = { if (!isExpanded) onToggle() },
        onToggle      = onToggle,
    )
    if (isExpanded) {
        Spacer(Modifier.height(6.dp))
        resultsContent()
    }
}

// ── Items de filtro genérico (campo / distrito) ───────────────────────────────

@Composable
private fun <T> FilterItemList(
    items:      List<T>,
    itemLabel:  (T) -> String,
    isSelected: (T) -> Boolean,
    onSelected: (T) -> Unit,
) {
    if (items.isEmpty()) {
        Text(
            text      = stringResource(R.string.login_sin_resultados),
            style     = MaterialTheme.typography.bodyMedium,
            color     = Muted,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    } else {
        Column {
            items.forEachIndexed { idx, item ->
                val sel = isSelected(item)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (sel) Accent.copy(alpha = 0.07f) else Color.Transparent)
                        .clickable { onSelected(item) }
                        .padding(horizontal = 4.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text       = itemLabel(item),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (sel) Accent else Ink,
                        modifier   = Modifier.weight(1f),
                    )
                    if (sel) Icon(Icons.Default.Check, null, tint = Sage, modifier = Modifier.size(16.dp))
                }
                if (idx < items.lastIndex) {
                    HorizontalDivider(color = Muted.copy(alpha = 0.15f))
                }
            }
        }
    }
}

// ── Items de iglesia (2 líneas) ───────────────────────────────────────────────

@Composable
private fun IglesiaItemList(
    items:      List<IglesiaItem>,
    selected:   IglesiaItem?,
    onSelected: (IglesiaItem) -> Unit,
) {
    if (items.isEmpty()) {
        Text(
            text      = stringResource(R.string.login_sin_resultados),
            style     = MaterialTheme.typography.bodyMedium,
            color     = Muted,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    } else {
        Column {
            items.forEachIndexed { idx, iglesia ->
                val sel = iglesia.id == selected?.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (sel) Accent.copy(alpha = 0.07f) else Color.Transparent)
                        .clickable { onSelected(iglesia) }
                        .padding(horizontal = 4.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = iglesia.nombre,
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color      = if (sel) Accent else Ink,
                        )
                        val sub = buildString {
                            if (iglesia.districtNombre.isNotBlank()) append(iglesia.districtNombre)
                            if (iglesia.districtNombre.isNotBlank() && iglesia.campoNombre.isNotBlank()) append(" · ")
                            if (iglesia.campoNombre.isNotBlank()) append(iglesia.campoNombre)
                        }
                        if (sub.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text  = sub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (sel) Accent.copy(alpha = 0.7f) else Muted,
                            )
                        }
                    }
                    if (sel) Icon(Icons.Default.Check, null, tint = Sage, modifier = Modifier.size(18.dp))
                }
                if (idx < items.lastIndex) {
                    HorizontalDivider(color = Muted.copy(alpha = 0.15f))
                }
            }
        }
    }
}

// ── Botón "Más opciones" con diseño card ─────────────────────────────────────

@Composable
private fun MasOpcionesButton(
    mostrarFiltros: Boolean,
    onClick:        () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (mostrarFiltros) Modifier.neuInset(cornerRadius = 14.dp)
                else Modifier.neuElevated(cornerRadius = 14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(if (mostrarFiltros) BackgroundDeep else Background)
            .then(if (mostrarFiltros) Modifier.drawWithContent {
                drawContent()
                val s = 1.5.dp.toPx()
                drawRoundRect(
                    color        = Accent,
                    topLeft      = Offset(s / 2, s / 2),
                    size         = Size(size.width - s, size.height - s),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style        = Stroke(width = s),
                )
            } else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text     = stringResource(R.string.login_mas_opciones),
                style    = MaterialTheme.typography.bodyMedium,
                color    = if (mostrarFiltros) Accent else Mid,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector        = if (mostrarFiltros) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint               = if (mostrarFiltros) Accent else Muted,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
}

// ── Dropdown genérico con tarjetas simples ────────────────────────────────────

private enum class ActiveDropdown { NONE, CAMPO, DISTRITO, IGLESIA, GRUPO }

@Composable
private fun <T> SimpleCardDropdown(
    label:         String,
    placeholder:   String,
    selectedName:  String,
    items:         List<T>,
    itemLabel:     (T) -> String,
    onItemSelected: (T) -> Unit,
    expanded:      Boolean,
    onExpand:      () -> Unit,
    onCollapse:    () -> Unit,
    modifier:      Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    LaunchedEffect(expanded) { if (!expanded) query = "" }

    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items.filter { itemLabel(it).contains(query, ignoreCase = true) }
    }

    Column(modifier = modifier) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        // Campo de búsqueda
        DropdownSearchBox(
            query        = query,
            selectedName = selectedName,
            placeholder  = placeholder,
            expanded     = expanded,
            onQueryChange = { query = it; if (!expanded) onExpand() },
            onFocused    = { if (!expanded) onExpand() },
            onToggle     = { if (expanded) onCollapse() else onExpand() },
        )

        // Lista de resultados como tarjetas
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (filtered.isEmpty()) {
                    Text(
                        text      = stringResource(R.string.login_sin_resultados),
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = Muted,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    )
                } else {
                    filtered.forEach { item ->
                        val isSelected = itemLabel(item) == selectedName
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isSelected) Modifier.neuInset(cornerRadius = 12.dp) else Modifier.neuElevated(cornerRadius = 12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BackgroundDeep else Background)
                                .then(if (isSelected) Modifier.drawWithContent {
                                    drawContent()
                                    val s = 1.5.dp.toPx()
                                    drawRoundRect(color = Accent, topLeft = Offset(s/2, s/2),
                                        size = Size(size.width - s, size.height - s),
                                        cornerRadius = CornerRadius(12.dp.toPx()), style = Stroke(s))
                                } else Modifier)
                                .clickable { onItemSelected(item) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text       = itemLabel(item),
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color      = if (isSelected) Accent else Ink,
                                    modifier   = Modifier.weight(1f),
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector        = Icons.Default.Check,
                                        contentDescription = null,
                                        tint               = Sage,
                                        modifier           = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Dropdown de iglesias con tarjetas de 2 líneas ─────────────────────────────

@Composable
private fun IglesiaDropdown(
    label:           String,
    placeholder:     String,
    selectedIglesia: IglesiaItem?,
    items:           List<IglesiaItem>,
    onItemSelected:  (IglesiaItem) -> Unit,
    expanded:        Boolean,
    onExpand:        () -> Unit,
    onCollapse:      () -> Unit,
    modifier:        Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    LaunchedEffect(expanded) { if (!expanded) query = "" }

    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items.filter {
            it.nombre.contains(query, ignoreCase = true) ||
            it.districtNombre.contains(query, ignoreCase = true) ||
            it.campoNombre.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = modifier) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        DropdownSearchBox(
            query        = query,
            selectedName = selectedIglesia?.nombre ?: "",
            placeholder  = placeholder,
            expanded     = expanded,
            onQueryChange = { query = it; if (!expanded) onExpand() },
            onFocused    = { if (!expanded) onExpand() },
            onToggle     = { if (expanded) onCollapse() else onExpand() },
        )

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (filtered.isEmpty()) {
                    Text(
                        text      = stringResource(R.string.login_sin_resultados),
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = Muted,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    )
                } else {
                    filtered.forEach { iglesia ->
                        val isSelected = iglesia.id == selectedIglesia?.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isSelected) Modifier.neuInset(cornerRadius = 14.dp) else Modifier.neuElevated(cornerRadius = 14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) BackgroundDeep else Background)
                                .then(if (isSelected) Modifier.drawWithContent {
                                    drawContent()
                                    val s = 1.5.dp.toPx()
                                    drawRoundRect(color = Accent, topLeft = Offset(s/2, s/2),
                                        size = Size(size.width - s, size.height - s),
                                        cornerRadius = CornerRadius(14.dp.toPx()), style = Stroke(s))
                                } else Modifier)
                                .clickable { onItemSelected(iglesia) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text       = iglesia.nombre,
                                        style      = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = if (isSelected) Accent else Ink,
                                    )
                                    if (iglesia.districtNombre.isNotBlank() || iglesia.campoNombre.isNotBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text  = buildString {
                                                if (iglesia.districtNombre.isNotBlank()) append(iglesia.districtNombre)
                                                if (iglesia.districtNombre.isNotBlank() && iglesia.campoNombre.isNotBlank()) append(" · ")
                                                if (iglesia.campoNombre.isNotBlank()) append(iglesia.campoNombre)
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) Accent.copy(alpha = 0.7f) else Muted,
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Spacer(Modifier.size(8.dp))
                                    Icon(
                                        imageVector        = Icons.Default.Check,
                                        contentDescription = null,
                                        tint               = Sage,
                                        modifier           = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Caja de búsqueda reutilizable ─────────────────────────────────────────────

@Composable
private fun DropdownSearchBox(
    query:        String,
    selectedName: String,
    placeholder:  String,
    expanded:     Boolean,
    onQueryChange: (String) -> Unit,
    onFocused:    () -> Unit,
    onToggle:     () -> Unit,
    isActive:     Boolean = false,
    leadingIcon:  (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Background)
            .neuInsetInner(cornerRadius = 14.dp)
            .then(if (isActive) Modifier.drawWithContent {
                drawContent()
                val s = 1.5.dp.toPx()
                drawRoundRect(
                    color        = Accent,
                    topLeft      = Offset(s / 2, s / 2),
                    size         = Size(size.width - s, size.height - s),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style        = Stroke(width = s),
                )
            } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        BasicTextField(
            value         = query,
            onValueChange = onQueryChange,
            readOnly      = !expanded,
            singleLine    = true,
            textStyle     = MaterialTheme.typography.bodyMedium.copy(color = Ink),
            modifier      = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused && !expanded) onFocused() },
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(Modifier.width(10.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        val hint = when {
                            query.isNotEmpty()        -> null
                            selectedName.isNotEmpty() -> selectedName
                            else                      -> placeholder
                        }
                        if (hint != null) {
                            Text(
                                text  = hint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedName.isNotEmpty() && !expanded) Ink else Muted,
                            )
                        }
                        innerTextField()
                    }
                    Icon(
                        imageVector        = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint               = if (isActive) Accent else Muted,
                        modifier           = Modifier.size(20.dp).clickable(onClick = onToggle),
                    )
                }
            },
        )
    }
}

// ── Dropdown de grupos con tarjetas de 2 líneas ───────────────────────────────

@Composable
private fun GrupoDropdown(
    label:         String,
    placeholder:   String,
    selectedGrupo: GrupoItem?,
    items:         List<GrupoItem>,
    onItemSelected: (GrupoItem) -> Unit,
    expanded:      Boolean,
    onExpand:      () -> Unit,
    onCollapse:    () -> Unit,
    modifier:      Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    LaunchedEffect(expanded) { if (!expanded) query = "" }

    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items.filter {
            it.nombre.contains(query, ignoreCase = true) ||
            it.iglesiaNombre.contains(query, ignoreCase = true) ||
            it.districtNombre.contains(query, ignoreCase = true) ||
            it.campoNombre.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = modifier) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        DropdownSearchBox(
            query        = query,
            selectedName = selectedGrupo?.nombre ?: "",
            placeholder  = placeholder,
            expanded     = expanded,
            onQueryChange = { query = it; if (!expanded) onExpand() },
            onFocused    = { if (!expanded) onExpand() },
            onToggle     = { if (expanded) onCollapse() else onExpand() },
        )

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (filtered.isEmpty()) {
                    Text(
                        text      = stringResource(R.string.login_sin_resultados),
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = Muted,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    )
                } else {
                    filtered.forEach { grupo ->
                        val isSelected = grupo.id == selectedGrupo?.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isSelected) Modifier.neuInset(cornerRadius = 14.dp) else Modifier.neuElevated(cornerRadius = 14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) BackgroundDeep else Background)
                                .then(if (isSelected) Modifier.drawWithContent {
                                    drawContent()
                                    val s = 1.5.dp.toPx()
                                    drawRoundRect(color = Accent, topLeft = Offset(s/2, s/2),
                                        size = Size(size.width - s, size.height - s),
                                        cornerRadius = CornerRadius(14.dp.toPx()), style = Stroke(s))
                                } else Modifier)
                                .clickable { onItemSelected(grupo) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text       = grupo.nombre,
                                        style      = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = if (isSelected) Accent else Ink,
                                    )
                                    val subtitulo = listOf(grupo.iglesiaNombre, grupo.districtNombre, grupo.campoNombre)
                                        .filter { it.isNotBlank() }.joinToString(" · ")
                                    if (subtitulo.isNotBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text  = subtitulo,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) Accent.copy(alpha = 0.7f) else Muted,
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Spacer(Modifier.size(8.dp))
                                    Icon(
                                        imageVector        = Icons.Default.Check,
                                        contentDescription = null,
                                        tint               = Sage,
                                        modifier           = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Notice card ───────────────────────────────────────────────────────────────

@Composable
private fun NoticeCard() {
    val text = buildAnnotatedString {
        append(stringResource(R.string.login_notice_prefix))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Ink)) {
            append(stringResource(R.string.login_notice_bold))
        }
        append(stringResource(R.string.login_notice_suffix))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Background)
            .dashedBorder(color = Muted, cornerRadius = 14.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Mid)
    }
}

// ── Dashed border ─────────────────────────────────────────────────────────────

private fun Modifier.dashedBorder(
    color: Color, cornerRadius: Dp = 14.dp, strokeWidth: Dp = 1.dp,
    dashWidth: Dp = 6.dp, gapWidth: Dp = 4.dp,
): Modifier = drawBehind {
    val s = strokeWidth.toPx()
    drawIntoCanvas { canvas ->
        val paint = Paint()
        paint.asFrameworkPaint().apply {
            isAntiAlias = true; style = android.graphics.Paint.Style.STROKE
            this.strokeWidth = s; this.color = color.toArgb()
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(dashWidth.toPx(), gapWidth.toPx()), 0f)
        }
        canvas.drawRoundRect(s/2f, s/2f, size.width - s/2f, size.height - s/2f,
            cornerRadius.toPx(), cornerRadius.toPx(), paint)
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

// ── Sección búsqueda de iglesia (DEV mode) ────────────────────────────────────

@Composable
private fun IglesiaLoginSection(
    query:             String,
    allIglesias:       List<IglesiaItem>,
    onQueryChange:     (String) -> Unit,
    onIglesiaSelected: (IglesiaItem) -> Unit,
    onVolver:          () -> Unit,
) {
    val filtradas = remember(query, allIglesias) {
        if (query.isBlank()) allIglesias
        else allIglesias.filter {
            it.nombre.contains(query, ignoreCase = true) ||
            it.districtNombre.contains(query, ignoreCase = true) ||
            it.campoNombre.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clickable(onClick = onVolver)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = Icons.Default.ExpandLess,
                contentDescription = "Volver",
                tint               = Accent,
                modifier           = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text  = "Volver",
                style = MaterialTheme.typography.bodyMedium,
                color = Accent,
            )
        }

        Text(
            text     = "IGLESIA",
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        DropdownSearchBox(
            query         = query,
            selectedName  = "",
            placeholder   = "Buscar iglesia…",
            expanded      = true,
            isActive      = true,
            leadingIcon   = {
                Icon(Icons.Default.Search, null, tint = Muted, modifier = Modifier.size(18.dp))
            },
            onQueryChange = onQueryChange,
            onFocused     = {},
            onToggle      = {},
        )

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            IglesiaItemList(
                items      = filtradas,
                selected   = null,
                onSelected = onIglesiaSelected,
            )
        }
    }
}

@Preview(showSystemUi = true, name = "Login — vacío")
@Composable
private fun LoginPreviewEmpty() {
    GpLeaderTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onCampoSelected = {}, onDistritoSelected = {}, onIglesiaSelected = {},
            onGrupoTap = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Login — con datos")
@Composable
private fun LoginPreviewData() {
    GpLeaderTheme {
        val iglesias = listOf(
            IglesiaItem("i1", "d1", "Iglesia Los Olivos", "Distrito Central", "ACS"),
            IglesiaItem("i2", "d1", "Iglesia Central",    "Distrito Central", "ACS"),
            IglesiaItem("i3", "d2", "Iglesia La Luz",     "Distrito Norte",   "ACS"),
        )
        LoginScreenContent(
            uiState = LoginUiState(
                allCampos    = listOf(CampoItem("c1", "Asociación Central Sur")),
                allDistritos = listOf(DistritoItem("d1", "c1", "Distrito Central")),
                allIglesias  = iglesias,
                allGrupos    = listOf(GrupoItem("g1", "i1", "GP Los Olivos")),
                selectedIglesia = iglesias.first(),
            ),
            onCampoSelected = {}, onDistritoSelected = {}, onIglesiaSelected = {},
            onGrupoTap = {},
        )
    }
}

// ── Búsqueda sin acentos ──────────────────────────────────────────────────────

/** Minúsculas + sin tildes/diacríticos, para buscar "Restauracion" y matchear "Restauración". */
private fun String.sinAcentos(): String =
    java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase()

/** contains() insensible a mayúsculas Y a acentos. */
private fun String.contieneBusqueda(query: String): Boolean =
    this.sinAcentos().contains(query.sinAcentos())

// ── Rediseño login: selector de perfil + piezas ──────────────────────────────

private enum class PerfilTab { GP, IGLESIA }

private val GoldDark = Color(0xFF8A7328)

@Composable
private fun PerfilSegmento(seleccion: PerfilTab, onSeleccion: (PerfilTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuInset(cornerRadius = 18.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Background)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SegmentoOpcion("Grupo pequeño", seleccion == PerfilTab.GP, Ink, Modifier.weight(1f)) { onSeleccion(PerfilTab.GP) }
        SegmentoOpcion("Iglesia", seleccion == PerfilTab.IGLESIA, GoldDark, Modifier.weight(1f)) { onSeleccion(PerfilTab.IGLESIA) }
    }
}

@Composable
private fun SegmentoOpcion(texto: String, activo: Boolean, colorActivo: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(if (activo) Modifier.neuElevated(cornerRadius = 13.dp) else Modifier)
            .clip(RoundedCornerShape(13.dp))
            .background(if (activo) Background else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
    ) {
        Text(
            text       = texto,
            style      = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color      = if (activo) colorActivo else Muted,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SeccionEyebrow(color: Color, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.padding(top = 20.dp, bottom = 9.dp),
    ) {
        Box(modifier = Modifier.size(7.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp), color = Muted)
    }
}

@Composable
private fun InfoNota(fondo: Color, texto: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(fondo)
            .padding(horizontal = 15.dp, vertical = 13.dp),
    ) {
        Text(texto, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, lineHeight = 19.sp), color = Mid)
    }
}

@Composable
private fun CtaPrimario(color: Color, texto: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .then(if (enabled) Modifier.neuGlow(cornerRadius = 16.dp, glowColor = color.copy(alpha = 0.38f)) else Modifier)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) color else color.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Text(
            text       = texto,
            style      = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color      = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** Lista de resultados de GP (nombre + ubicación), scrollable. */
@Composable
private fun GrupoResultList(grupos: List<GrupoItem>, onTap: (GrupoItem) -> Unit) {
    if (grupos.isEmpty()) {
        Text(
            text      = stringResource(R.string.login_sin_resultados),
            style     = MaterialTheme.typography.bodyMedium,
            color     = Muted,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
        return
    }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(4.dp))
        grupos.forEachIndexed { idx, grupo ->
            val subtitulo = listOf(grupo.iglesiaNombre, grupo.districtNombre, grupo.campoNombre)
                .filter { it.isNotBlank() }.joinToString(" · ")
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onTap(grupo) }.padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(grupo.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Ink)
                    if (subtitulo.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = Muted)
                    }
                }
            }
            if (idx < grupos.lastIndex) HorizontalDivider(color = Muted.copy(alpha = 0.15f))
        }
        Spacer(Modifier.height(4.dp))
    }
}

/** Lista de resultados de iglesias (para el login de iglesia). */
@Composable
private fun IglesiaResultList(iglesias: List<IglesiaItem>, onTap: (IglesiaItem) -> Unit) {
    if (iglesias.isEmpty()) {
        Text(
            text      = stringResource(R.string.login_sin_resultados),
            style     = MaterialTheme.typography.bodyMedium,
            color     = Muted,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
        return
    }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(4.dp))
        iglesias.forEachIndexed { idx, iglesia ->
            val subtitulo = listOf(iglesia.districtNombre, iglesia.campoNombre).filter { it.isNotBlank() }.joinToString(" · ")
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onTap(iglesia) }.padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(iglesia.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Ink)
                    if (subtitulo.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = Muted)
                    }
                }
            }
            if (idx < iglesias.lastIndex) HorizontalDivider(color = Muted.copy(alpha = 0.15f))
        }
        Spacer(Modifier.height(4.dp))
    }
}
