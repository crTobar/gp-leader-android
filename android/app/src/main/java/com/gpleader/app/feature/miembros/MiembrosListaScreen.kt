package com.gpleader.app.feature.miembros

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.components.SwipeAction
import com.gpleader.app.core.ui.components.SwipeableItem
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.Shadow
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInset

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun MiembrosListaScreen(
    onNavigateBack:      () -> Unit,
    onNavigateToDetalle: () -> Unit,
    onNavigateToAgregar: () -> Unit,
    onNavigateToHome:    () -> Unit,
    onNavigateToHistorial: () -> Unit,
    viewModel: MiembrosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToDetalle) {
        if (uiState.navigateToDetalle) {
            viewModel.consumeNavigateToDetalle()
            onNavigateToDetalle()
        }
    }

    LaunchedEffect(uiState.navigateListaBack) {
        if (uiState.navigateListaBack) {
            viewModel.consumeNavigateListaBack()
            onNavigateBack()
        }
    }

    MiembrosListaContent(
        uiState               = uiState,
        onNavigateBack        = onNavigateBack,
        onQueryChange         = viewModel::onQueryChange,
        onMiembroClick        = viewModel::onMiembroClick,
        onToggleEstado        = viewModel::onToggleEstadoDesdeListado,
        onAgregarClick        = onNavigateToAgregar,
        onNavigateToHome      = onNavigateToHome,
        onNavigateToHistorial = onNavigateToHistorial,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun MiembrosListaContent(
    uiState:               MiembrosUiState,
    onNavigateBack:        () -> Unit,
    onQueryChange:         (String) -> Unit,
    onMiembroClick:        (String) -> Unit,
    onToggleEstado:        (String) -> Unit,
    onAgregarClick:        () -> Unit,
    onNavigateToHome:      () -> Unit,
    onNavigateToHistorial: () -> Unit,
) {
    // Clave del ítem actualmente deslizado — solo uno puede estar abierto a la vez
    var openItemId by remember { mutableStateOf<Any?>(null) }

    Scaffold(
        modifier = Modifier.pointerInput(openItemId) {
            if (openItemId != null) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    openItemId = null
                }
            }
        },
        containerColor = Background,
        topBar = {
            MiembrosTopBar(
                onNavigateBack = onNavigateBack,
                onAgregarClick = onAgregarClick,
                modifier       = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        },
        bottomBar = {
            MiembrosBottomNavBar(
                onHomeClick      = onNavigateToHome,
                onHistorialClick = onNavigateToHistorial,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ── Buscador ──────────────────────────────────────────────────────
            item {
                NeuTextField(
                    value         = uiState.query,
                    onValueChange = onQueryChange,
                    placeholder   = stringResource(R.string.miembros_search_placeholder),
                    leadingContent = {
                        Icon(
                            imageVector        = Icons.Filled.Search,
                            contentDescription = null,
                            tint               = Muted,
                            modifier           = Modifier.size(18.dp),
                        )
                    },
                )
            }

            // ── Activos ───────────────────────────────────────────────────────
            if (uiState.activosFiltrados.isNotEmpty() || uiState.query.isBlank()) {
                item {
                    SectionSeparator(
                        label    = stringResource(R.string.miembros_seccion_activos),
                        count    = uiState.activosFiltrados.size,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                items(uiState.activosFiltrados, key = { it.id }) { miembro ->
                    MiembroCard(
                        miembro        = miembro,
                        onClick        = { onMiembroClick(miembro.id) },
                        onToggleEstado = { onToggleEstado(miembro.id) },
                        openItemId     = openItemId,
                        onOpen         = { openItemId = miembro.id },
                    )
                }
            }

            // ── Archivados ────────────────────────────────────────────────────
            if (uiState.archivadosFiltrados.isNotEmpty()) {
                item {
                    SectionSeparator(
                        label    = stringResource(R.string.miembros_seccion_archivados),
                        count    = uiState.archivadosFiltrados.size,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                items(uiState.archivadosFiltrados, key = { it.id }) { miembro ->
                    MiembroCard(
                        miembro        = miembro,
                        onClick        = { onMiembroClick(miembro.id) },
                        onToggleEstado = { onToggleEstado(miembro.id) },
                        openItemId     = openItemId,
                        onOpen         = { openItemId = miembro.id },
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun MiembrosTopBar(
    onNavigateBack: () -> Unit,
    onAgregarClick: () -> Unit,
    modifier:       Modifier = Modifier,
) {
    Box(
        modifier         = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .neuElevatedSm(cornerRadius = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Background)
                .clickable(onClick = onNavigateBack)
                .padding(10.dp),
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint               = Ink,
                modifier           = Modifier.size(20.dp),
            )
        }

        Text(
            text  = stringResource(R.string.miembros_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )

        // Botón "+ Agregar" — fondo Accent
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .neuElevatedSm(cornerRadius = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Accent)
                .clickable(onClick = onAgregarClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text  = stringResource(R.string.miembros_btn_agregar),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

// ── Section separator "──── ACTIVOS  3  ────" ────────────────────────────────

@Composable
private fun SectionSeparator(
    label:    String,
    count:    Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Shadow, thickness = 0.8.dp)
        Row(
            modifier             = Modifier.padding(horizontal = 12.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Accent)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
            ) {
                Text(
                    text  = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.weight(1f), color = Shadow, thickness = 0.8.dp)
    }
}

// ── Miembro card ──────────────────────────────────────────────────────────────

@Composable
private fun MiembroCard(
    miembro:        MiembroUi,
    onClick:        () -> Unit,
    onToggleEstado: () -> Unit,
    openItemId:     Any? = null,
    onOpen:         () -> Unit = {},
) {
    val archivado    = miembro.estado == EstadoMiembro.ARCHIVADO
    val contentAlpha = if (archivado) 0.5f else 1f
    val textColor    = if (archivado) Muted else Ink
    val dotColor     = if (archivado) Muted else Sage

    val swipeLabel = if (archivado)
        stringResource(R.string.miembros_accion_activar)
    else
        stringResource(R.string.miembros_accion_archivar)
    val swipeColor = if (archivado) Sage else Blush

    // Box exterior lleva el shadow neumórfico; sin clip para que las sombras sean visibles
    Box(modifier = Modifier.neuElevated(cornerRadius = 20.dp)) {
        SwipeableItem(
            itemKey          = miembro.id,
            onItemClick      = onClick,
            dimOnSwipe       = true,
            clipCornerRadius = 20.dp,
            openKey          = openItemId,
            onOpen           = onOpen,
            swipeActions     = listOf(
                SwipeAction(
                    label   = swipeLabel,
                    color   = swipeColor,
                    onClick = onToggleEstado,
                )
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Background),
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier              = Modifier
                        .alpha(contentAlpha)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    // Avatar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .neuElevatedSm(cornerRadius = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BackgroundDeep),
                    ) {
                        Text(
                            text  = miembro.iniciales,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Mid,
                        )
                    }

                    // Nombre + teléfono
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text     = miembro.nombreCompleto,
                            style    = MaterialTheme.typography.titleLarge,
                            color    = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (miembro.telefono.isNotBlank()) {
                            Text(
                                text  = miembro.telefono,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                            )
                        }
                    }

                    // Badge archivado
                    if (archivado) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Shadow)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text  = stringResource(R.string.miembros_badge_archivado),
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(dotColor),
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
    }
}

// ── Bottom nav ────────────────────────────────────────────────────────────────

@Composable
private fun MiembrosBottomNavBar(
    onHomeClick:      () -> Unit,
    onHistorialClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(Background)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .neuElevated(cornerRadius = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Background)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NavTab(
                icon    = Icons.Filled.Home,
                label   = stringResource(R.string.home_nav_inicio),
                active  = false,
                onClick = onHomeClick,
            )
            NavTab(
                icon    = Icons.Filled.Search,
                label   = stringResource(R.string.home_nav_historial),
                active  = false,
                onClick = onHistorialClick,
            )
            NavTab(
                icon    = Icons.Filled.Person,
                label   = stringResource(R.string.home_nav_perfil),
                active  = true,
                onClick = {},
            )
        }
    }
}

@Composable
private fun NavTab(
    icon:    androidx.compose.ui.graphics.vector.ImageVector,
    label:   String,
    active:  Boolean,
    onClick: () -> Unit,
) {
    val tint = if (active) Accent else Muted
    Column(
        modifier            = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun MiembrosListaPreview() {
    GpLeaderTheme {
        MiembrosListaContent(
            uiState               = MiembrosUiState(),
            onNavigateBack        = {},
            onQueryChange         = {},
            onMiembroClick        = {},
            onToggleEstado        = {},
            onAgregarClick        = {},
            onNavigateToHome      = {},
            onNavigateToHistorial = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true, name = "Con búsqueda")
@Composable
private fun MiembrosListaSearchPreview() {
    GpLeaderTheme {
        MiembrosListaContent(
            uiState               = MiembrosUiState(query = "Carlos"),
            onNavigateBack        = {},
            onQueryChange         = {},
            onMiembroClick        = {},
            onToggleEstado        = {},
            onAgregarClick        = {},
            onNavigateToHome      = {},
            onNavigateToHistorial = {},
        )
    }
}
