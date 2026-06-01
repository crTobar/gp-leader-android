package com.gpleader.app.feature.sabado

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.IglesiaItem
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInsetSm
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SabadoCultoScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SabadoCultoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToExito) {
        if (uiState.navigateToExito) {
            onNavigateToHome()
            viewModel.consumeExitoNavigation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        contentDescription = "Volver",
                        tint               = Ink,
                        modifier           = Modifier.size(20.dp),
                    )
                }

                Column(
                    modifier            = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text  = "Culto de Sábado",
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                    )
                    if (!uiState.isLoading) {
                        val mes = uiState.fecha.month.getDisplayName(TextStyle.FULL, Locale("es"))
                        Text(
                            text  = "${uiState.fecha.dayOfMonth} de $mes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }

                Spacer(Modifier.size(40.dp))
            }

            // ── Contenido ─────────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(
                        modifier         = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Accent)
                    }
                }

                uiState.meetingId.isBlank() -> {
                    Box(
                        modifier         = Modifier.weight(1f).fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = "No hay culto de sábado programado para esta semana.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Mid,
                        )
                    }
                }

                else -> {
                    val presentes = uiState.miembros.count { it.presente }

                    LazyColumn(
                        modifier       = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    ) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            MiembrosHeaderSabado(
                                presentes  = presentes,
                                total      = uiState.miembros.size,
                                onSelTodos = viewModel::onSelTodos,
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        items(uiState.miembros) { miembro ->
                            MiembroSabadoRow(
                                miembro        = miembro,
                                onToggle       = { viewModel.onTogglePresencia(miembro.id) },
                                onIglesiaClick = { viewModel.onShowIglesiaSheet(miembro.id) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    if (uiState.error != null) {
                        Text(
                            text     = uiState.error!!,
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = Blush,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    // ── Botón enviar ───────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Background)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        if (uiState.isSending) {
                            Box(
                                modifier         = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = Accent)
                            }
                        } else {
                            NeuButtonPrimary(
                                text         = "Enviar al pastor",
                                onClick      = viewModel::onEnviarAlPastor,
                                modifier     = Modifier.fillMaxWidth(),
                                cornerRadius = 14.dp,
                            )
                        }
                    }
                }
            }
        }
    }

    // ── BottomSheet: seleccionar iglesia ──────────────────────────────────────
    val miembroIdSheet = uiState.showIglesiaSheetForMiembro
    if (miembroIdSheet != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissIglesiaSheet,
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor   = Background,
        ) {
            IglesiaSeleccionarSheet(
                iglesias     = uiState.iglesias,
                seleccionada = uiState.miembros.find { it.id == miembroIdSheet }?.iglesiaVisitadaId,
                onSelect     = { iglesia -> viewModel.onIglesiaSeleccionada(miembroIdSheet, iglesia) },
            )
        }
    }
}

// ── Header con contador y Sel. todos ─────────────────────────────────────────

@Composable
private fun MiembrosHeaderSabado(
    presentes:  Int,
    total:      Int,
    onSelTodos: (Boolean) -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text  = "ASISTENCIA",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Spacer(Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Ink)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text  = "$presentes/$total",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }

        Box {
            var expanded by remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .neuElevatedSm(cornerRadius = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Background)
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(
                    text  = "Sel. todos",
                    style = MaterialTheme.typography.labelSmall,
                    color = Mid,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector        = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint               = Muted,
                    modifier           = Modifier.size(14.dp),
                )
            }
            DropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text    = { Text("Todos presentes", style = MaterialTheme.typography.bodyMedium, color = Ink) },
                    onClick = { onSelTodos(true); expanded = false },
                )
                DropdownMenuItem(
                    text    = { Text("Todos ausentes", style = MaterialTheme.typography.bodyMedium, color = Ink) },
                    onClick = { onSelTodos(false); expanded = false },
                )
            }
        }
    }
}

// ── Fila de miembro ───────────────────────────────────────────────────────────

@Composable
private fun MiembroSabadoRow(
    miembro:        MiembroSabado,
    onToggle:       () -> Unit,
    onIglesiaClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Background),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            // ── Fila principal ─────────────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar circular neuElevatedSm
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .neuElevatedSm(cornerRadius = 20.dp)
                        .clip(CircleShape)
                        .background(BackgroundDeep),
                ) {
                    Text(
                        text  = miembro.iniciales,
                        style = MaterialTheme.typography.labelSmall,
                        color = Mid,
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Nombre + iglesia seleccionada / hint
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = miembro.nombre,
                        style      = MaterialTheme.typography.bodyLarge,
                        color      = Ink,
                        fontWeight = FontWeight.Medium,
                    )
                    if (miembro.presente && miembro.iglesiaVisitadaNombre != null) {
                        Row(
                            modifier          = Modifier
                                .clickable(onClick = onIglesiaClick)
                                .padding(top = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Church,
                                contentDescription = null,
                                tint               = Accent,
                                modifier           = Modifier.size(12.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text  = miembro.iglesiaVisitadaNombre,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Accent,
                            )
                        }
                    } else if (miembro.presente) {
                        Text(
                            text     = "Toca para indicar iglesia",
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = Muted,
                            modifier = Modifier
                                .clickable(onClick = onIglesiaClick)
                                .padding(top = 2.dp),
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Checkbox P/A (estilo AsistenciaCheckbox)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .then(
                            if (miembro.presente) Modifier.neuInsetSm(cornerRadius = 10.dp)
                            else Modifier.neuElevatedSm(cornerRadius = 10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (miembro.presente) Sage.copy(alpha = 0.15f) else Background
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (miembro.presente) Sage else Muted.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                        )
                        .clickable(onClick = onToggle),
                ) {
                    if (miembro.presente) {
                        Icon(
                            imageVector        = Icons.Default.Check,
                            contentDescription = null,
                            tint               = Sage,
                            modifier           = Modifier.size(20.dp),
                        )
                    } else {
                        Text(
                            text       = "A",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = Muted,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

        }
    }
}

// ── Sheet: seleccionar iglesia ────────────────────────────────────────────────

@Composable
private fun IglesiaSeleccionarSheet(
    iglesias:     List<IglesiaItem>,
    seleccionada: String?,
    onSelect:     (IglesiaItem) -> Unit,
) {
    var busqueda by remember { mutableStateOf("") }
    val filtradas = if (busqueda.isBlank()) iglesias
                   else iglesias.filter { it.nombre.contains(busqueda, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Text(
            text       = "¿A qué iglesia fue?",
            style      = MaterialTheme.typography.headlineMedium,
            color      = Ink,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(16.dp))
        NeuTextField(
            value         = busqueda,
            onValueChange = { busqueda = it },
            label         = null,
            placeholder   = "Buscar iglesia…",
            modifier      = Modifier.fillMaxWidth(),
            leadingContent = {
                Icon(
                    imageVector        = Icons.Default.Search,
                    contentDescription = null,
                    tint               = Muted,
                    modifier           = Modifier.size(20.dp),
                )
            },
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            if (filtradas.isEmpty()) {
                item {
                    Text(
                        text     = "Sin resultados",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = Muted,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            } else {
                items(filtradas) { iglesia ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(iglesia) }
                            .padding(vertical = 14.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = iglesia.nombre,
                                style      = MaterialTheme.typography.bodyLarge,
                                color      = Ink,
                                fontWeight = if (iglesia.id == seleccionada) FontWeight.SemiBold else FontWeight.Normal,
                            )
                            if (iglesia.districtNombre.isNotBlank()) {
                                Text(
                                    text  = iglesia.districtNombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Muted,
                                )
                            }
                        }
                        if (iglesia.id == seleccionada) {
                            Icon(
                                imageVector        = Icons.Default.Check,
                                contentDescription = null,
                                tint               = Accent,
                                modifier           = Modifier.size(20.dp),
                            )
                        }
                    }
                    HorizontalDivider(color = BackgroundDeep, thickness = 1.dp)
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
