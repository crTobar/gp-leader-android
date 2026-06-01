package com.gpleader.app.feature.auth

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.data.repository.CampoItem
import com.gpleader.app.core.data.repository.DistritoItem
import com.gpleader.app.core.data.repository.GrupoItem
import com.gpleader.app.core.data.repository.IglesiaItem
import com.gpleader.app.core.ui.components.AppLogo
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
import com.gpleader.app.core.ui.theme.neuInset

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    onNavigateToQuienEres: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToQuienEres) {
        if (uiState.navigateToQuienEres) {
            onNavigateToQuienEres()
            viewModel.consumeQuienEresNavigation()
        }
    }

    LoginScreenContent(
        uiState            = uiState,
        onCampoSelected    = viewModel::onCampoSelected,
        onDistritoSelected = viewModel::onDistritoSelected,
        onIglesiaSelected  = viewModel::onIglesiaSelected,
        onGrupoTap         = viewModel::onGrupoTap,
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun LoginScreenContent(
    uiState:            LoginUiState,
    onCampoSelected:    (CampoItem?) -> Unit,
    onDistritoSelected: (DistritoItem?) -> Unit,
    onIglesiaSelected:  (IglesiaItem?) -> Unit,
    onGrupoTap:         (GrupoItem) -> Unit,
) {
    var active by remember { mutableStateOf(ActiveDropdown.NONE) }
    var mostrarFiltros by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var grupoQuery    by remember { mutableStateOf("") }
    var campoQuery    by remember { mutableStateOf("") }
    var distritoQuery by remember { mutableStateOf("") }
    var iglesiaQuery  by remember { mutableStateOf("") }

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
            it.nombre.contains(grupoQuery, ignoreCase = true) ||
            it.iglesiaNombre.contains(grupoQuery, ignoreCase = true) ||
            it.districtNombre.contains(grupoQuery, ignoreCase = true) ||
            it.campoNombre.contains(grupoQuery, ignoreCase = true)
        }
    }
    val filteredCampos = remember(campoQuery, uiState.allCampos) {
        if (campoQuery.isBlank()) uiState.allCampos
        else uiState.allCampos.filter { it.nombre.contains(campoQuery, ignoreCase = true) }
    }
    val filteredDistritos = remember(distritoQuery, uiState.filteredDistritos) {
        if (distritoQuery.isBlank()) uiState.filteredDistritos
        else uiState.filteredDistritos.filter { it.nombre.contains(distritoQuery, ignoreCase = true) }
    }
    val filteredIglesias = remember(iglesiaQuery, uiState.filteredIglesias) {
        if (iglesiaQuery.isBlank()) uiState.filteredIglesias
        else uiState.filteredIglesias.filter {
            it.nombre.contains(iglesiaQuery, ignoreCase = true) ||
            it.districtNombre.contains(iglesiaQuery, ignoreCase = true) ||
            it.campoNombre.contains(iglesiaQuery, ignoreCase = true)
        }
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
        Spacer(Modifier.height(36.dp))

        AppLogo()

        Spacer(Modifier.height(20.dp))

        Text(
            text      = stringResource(R.string.login_app_titulo),
            style     = MaterialTheme.typography.displayLarge,
            color     = Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = stringResource(R.string.login_header_subtitle),
            style     = MaterialTheme.typography.bodyMedium,
            color     = Mid,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(36.dp))

        // ── Tarjeta de inicio de sesión ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .neuElevated(cornerRadius = 28.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Background),
        ) {
            // ── Cabecera ───────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Accent),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text  = stringResource(R.string.login_section_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = stringResource(R.string.login_title),
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = Ink,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
            }

            HorizontalDivider(
                color     = Muted.copy(alpha = 0.2f),
                thickness = 1.dp,
            )

            // ── Formulario: peso 1f, padding propio ───────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {

            // ── Buscador GP fijo (no scrollea) ────────────────────────────────
            val grupoExpandido = active == ActiveDropdown.GRUPO && !mostrarFiltros
            Text(
                text     = stringResource(R.string.login_label_tu_gp),
                style    = MaterialTheme.typography.labelSmall,
                color    = if (grupoExpandido) Accent else Muted,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            DropdownSearchBox(
                query         = grupoQuery,
                selectedName  = "",
                placeholder   = stringResource(R.string.login_buscar_gp_hint),
                expanded      = grupoExpandido,
                isActive      = grupoExpandido,
                leadingIcon   = {
                    Icon(
                        imageVector        = Icons.Default.Search,
                        contentDescription = null,
                        tint               = if (grupoExpandido) Accent else Muted,
                        modifier           = Modifier.size(18.dp),
                    )
                },
                onQueryChange = { q ->
                    grupoQuery = q
                    mostrarFiltros = false
                    if (active != ActiveDropdown.GRUPO) expand(ActiveDropdown.GRUPO)
                },
                onFocused = { mostrarFiltros = false; expand(ActiveDropdown.GRUPO) },
                onToggle  = {
                    if (grupoExpandido) collapse()
                    else { mostrarFiltros = false; expand(ActiveDropdown.GRUPO) }
                },
            )

            Spacer(Modifier.height(12.dp))

            val contentExpandido = grupoExpandido || mostrarFiltros

            if (!contentExpandido) {
                // ── "Más opciones" arriba cuando nada está abierto ───────────
                MasOpcionesButton(
                    mostrarFiltros = mostrarFiltros,
                    onClick        = { mostrarFiltros = !mostrarFiltros; if (mostrarFiltros) collapse() },
                )
                Spacer(Modifier.weight(1f))
            } else {
                // ── Zona scrollable ───────────────────────────────────────────
                Box(modifier = Modifier.weight(1f)) {
                    if (!mostrarFiltros) {
                        // Lista de grupos (LazyColumn, Box tiene altura fija por weight)
                        if (filteredGrupos.isEmpty()) {
                            Text(
                                text      = stringResource(R.string.login_sin_resultados),
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = Muted,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 12.dp),
                            )
                        } else {
                            LazyColumn(
                                modifier       = Modifier.fillMaxSize(),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    top = 4.dp, bottom = 4.dp,
                                ),
                            ) {
                                itemsIndexed(filteredGrupos, key = { _, g -> g.id }) { idx, grupo ->
                                    val subtitulo = listOf(
                                        grupo.iglesiaNombre,
                                        grupo.districtNombre,
                                        grupo.campoNombre,
                                    ).filter { it.isNotBlank() }.joinToString(" · ")
                                    Column {
                                        Row(
                                            modifier          = Modifier
                                                .fillMaxWidth()
                                                .clickable { onGrupoTap(grupo); collapse() }
                                                .padding(horizontal = 4.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text       = grupo.nombre,
                                                    style      = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color      = Ink,
                                                )
                                                if (subtitulo.isNotBlank()) {
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        text  = subtitulo,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Muted,
                                                    )
                                                }
                                            }
                                        }
                                        if (idx < filteredGrupos.lastIndex) {
                                            HorizontalDivider(color = Muted.copy(alpha = 0.15f))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Filtros en orden fijo. Al tocar uno, scroll automático al tope.
                        val filterListState = rememberLazyListState()
                        LaunchedEffect(active) {
                            when (active) {
                                ActiveDropdown.CAMPO    -> filterListState.animateScrollToItem(0)
                                ActiveDropdown.DISTRITO -> filterListState.animateScrollToItem(1)
                                ActiveDropdown.IGLESIA  -> filterListState.animateScrollToItem(2)
                                else                    -> {}
                            }
                        }
                        LazyColumn(
                            state               = filterListState,
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = androidx.compose.foundation.layout.PaddingValues(
                                top = 4.dp, bottom = 12.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item {
                                FilterBlock(
                                    label         = stringResource(R.string.login_label_campo),
                                    placeholder   = stringResource(R.string.login_placeholder_campo),
                                    query         = campoQuery,
                                    selectedName  = uiState.selectedCampo?.nombre ?: "",
                                    isExpanded    = active == ActiveDropdown.CAMPO,
                                    onQueryChange = { q -> campoQuery = q; if (active != ActiveDropdown.CAMPO) expand(ActiveDropdown.CAMPO) },
                                    onToggle      = { if (active == ActiveDropdown.CAMPO) collapse() else expand(ActiveDropdown.CAMPO) },
                                    resultsContent = {
                                        FilterItemList(
                                            items      = filteredCampos,
                                            itemLabel  = { it.nombre },
                                            isSelected = { it.nombre == (uiState.selectedCampo?.nombre ?: "") },
                                            onSelected = { onCampoSelected(it); collapse() },
                                        )
                                    },
                                )
                            }
                            item {
                                FilterBlock(
                                    label         = stringResource(R.string.login_label_distrito),
                                    placeholder   = stringResource(R.string.login_placeholder_distrito),
                                    query         = distritoQuery,
                                    selectedName  = uiState.selectedDistrito?.nombre ?: "",
                                    isExpanded    = active == ActiveDropdown.DISTRITO,
                                    onQueryChange = { q -> distritoQuery = q; if (active != ActiveDropdown.DISTRITO) expand(ActiveDropdown.DISTRITO) },
                                    onToggle      = { if (active == ActiveDropdown.DISTRITO) collapse() else expand(ActiveDropdown.DISTRITO) },
                                    resultsContent = {
                                        FilterItemList(
                                            items      = filteredDistritos,
                                            itemLabel  = { it.nombre },
                                            isSelected = { it.nombre == (uiState.selectedDistrito?.nombre ?: "") },
                                            onSelected = { onDistritoSelected(it); collapse() },
                                        )
                                    },
                                )
                            }
                            item {
                                FilterBlock(
                                    label         = stringResource(R.string.login_label_iglesia),
                                    placeholder   = stringResource(R.string.login_placeholder_iglesia),
                                    query         = iglesiaQuery,
                                    selectedName  = uiState.selectedIglesia?.nombre ?: "",
                                    isExpanded    = active == ActiveDropdown.IGLESIA,
                                    onQueryChange = { q -> iglesiaQuery = q; if (active != ActiveDropdown.IGLESIA) expand(ActiveDropdown.IGLESIA) },
                                    onToggle      = { if (active == ActiveDropdown.IGLESIA) collapse() else expand(ActiveDropdown.IGLESIA) },
                                    resultsContent = {
                                        IglesiaItemList(
                                            items      = filteredIglesias,
                                            selected   = uiState.selectedIglesia,
                                            onSelected = { onIglesiaSelected(it); collapse() },
                                        )
                                    },
                                )
                            }
                        }
                    }
                }

                // ── "Más opciones" baja al fondo cuando hay contenido expandido
                MasOpcionesButton(
                    mostrarFiltros = mostrarFiltros,
                    onClick        = { mostrarFiltros = !mostrarFiltros; if (mostrarFiltros) collapse() },
                )
            }

            // ── Error / Loading ───────────────────────────────────────────────
            if (uiState.error != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = uiState.error,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = Blush,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (uiState.isLoading) {
                Box(
                    modifier         = Modifier.fillMaxWidth().height(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Accent)
                }
            }
            } // formulario
        }

        Spacer(Modifier.height(24.dp))
    }
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
            .neuInset(cornerRadius = 14.dp)
            .background(BackgroundDeep, RoundedCornerShape(14.dp))
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
