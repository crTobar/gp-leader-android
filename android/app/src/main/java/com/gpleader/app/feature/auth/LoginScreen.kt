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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
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

    fun expand(d: ActiveDropdown) { active = d }
    fun collapse() { active = ActiveDropdown.NONE; focusManager.clearFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
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

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text  = stringResource(R.string.login_section_label),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
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

        Spacer(Modifier.height(24.dp))

        // ── Grupo (primario, siempre visible) ─────────────────────────────────
        GrupoDropdown(
            label          = stringResource(R.string.login_label_tu_gp),
            placeholder    = stringResource(R.string.login_buscar_gp_hint),
            selectedGrupo  = null,
            items          = uiState.filteredGrupos,
            onItemSelected = { item -> onGrupoTap(item); collapse() },
            expanded       = active == ActiveDropdown.GRUPO,
            onExpand       = { expand(ActiveDropdown.GRUPO) },
            onCollapse     = ::collapse,
            modifier       = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // ── Filtros avanzados (colapsados por defecto) ────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    mostrarFiltros = !mostrarFiltros
                    if (!mostrarFiltros) {
                        collapse()
                    }
                }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text      = stringResource(R.string.login_mas_opciones),
                style     = MaterialTheme.typography.bodyMedium,
                color     = Mid,
                modifier  = Modifier.weight(1f),
            )
            Icon(
                imageVector        = if (mostrarFiltros) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint               = Muted,
                modifier           = Modifier.size(18.dp),
            )
        }

        AnimatedVisibility(visible = mostrarFiltros) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(4.dp))

                SimpleCardDropdown(
                    label        = stringResource(R.string.login_label_campo),
                    placeholder  = stringResource(R.string.login_placeholder_campo),
                    selectedName = uiState.selectedCampo?.nombre ?: "",
                    items        = uiState.allCampos,
                    itemLabel    = { it.nombre },
                    onItemSelected = { item -> onCampoSelected(item); collapse() },
                    expanded   = active == ActiveDropdown.CAMPO,
                    onExpand   = { expand(ActiveDropdown.CAMPO) },
                    onCollapse = ::collapse,
                    modifier   = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                SimpleCardDropdown(
                    label        = stringResource(R.string.login_label_distrito),
                    placeholder  = stringResource(R.string.login_placeholder_distrito),
                    selectedName = uiState.selectedDistrito?.nombre ?: "",
                    items        = uiState.filteredDistritos,
                    itemLabel    = { it.nombre },
                    onItemSelected = { item -> onDistritoSelected(item); collapse() },
                    expanded   = active == ActiveDropdown.DISTRITO,
                    onExpand   = { expand(ActiveDropdown.DISTRITO) },
                    onCollapse = ::collapse,
                    modifier   = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                IglesiaDropdown(
                    label        = stringResource(R.string.login_label_iglesia),
                    placeholder  = stringResource(R.string.login_placeholder_iglesia),
                    selectedIglesia = uiState.selectedIglesia,
                    items        = uiState.filteredIglesias,
                    onItemSelected = { iglesia -> onIglesiaSelected(iglesia); collapse() },
                    expanded   = active == ActiveDropdown.IGLESIA,
                    onExpand   = { expand(ActiveDropdown.IGLESIA) },
                    onCollapse = ::collapse,
                    modifier   = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        if (uiState.error != null) {
            Text(
                text     = uiState.error,
                style    = MaterialTheme.typography.bodyMedium,
                color    = Blush,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
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

        Spacer(Modifier.height(40.dp))
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
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .neuInset(cornerRadius = 14.dp)
            .background(BackgroundDeep, RoundedCornerShape(14.dp))
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
                        tint               = Muted,
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
