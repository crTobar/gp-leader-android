package com.gpleader.app.feature.sabado

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.data.repository.IglesiaItem
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
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
import java.time.format.DateTimeFormatter
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint               = Ink,
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    text       = stringResource(R.string.sabado_culto_titulo),
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Ink,
                    fontWeight = FontWeight.Bold,
                )
                if (!uiState.isLoading) {
                    val mes = uiState.fecha.month.getDisplayName(TextStyle.FULL, Locale("es"))
                    Text(
                        text  = "${uiState.fecha.dayOfMonth} de $mes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mid,
                    )
                }
            }
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                ) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text     = "ASISTENCIA",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = Muted,
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                    }
                    items(uiState.miembros) { miembro ->
                        MiembroSabadoRow(
                            miembro            = miembro,
                            onToggle           = { viewModel.onTogglePresencia(miembro.id) },
                            onIglesiaClick     = { viewModel.onShowIglesiaSheet(miembro.id) },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                if (uiState.error != null) {
                    Text(
                        text     = uiState.error!!,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = Blush,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    )
                }

                // ── Botones ────────────────────────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    if (uiState.isSending) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Accent)
                        }
                    } else {
                        NeuButtonPrimary(
                            text         = "Enviar al pastor",
                            onClick      = viewModel::onEnviarAlPastor,
                            modifier     = Modifier.fillMaxWidth(),
                            cornerRadius = 24.dp,
                        )
                    }
                }
            }
        }
    }

    // ── BottomSheet iglesia ───────────────────────────────────────────────────
    val miembroIdSheet = uiState.showIglesiaSheetForMiembro
    if (miembroIdSheet != null) {
        ModalBottomSheet(
            onDismissRequest    = viewModel::onDismissIglesiaSheet,
            sheetState          = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor      = Background,
        ) {
            IglesiaSeleccionarSheet(
                iglesias    = uiState.iglesias,
                seleccionada = uiState.miembros.find { it.id == miembroIdSheet }?.iglesiaVisitadaId,
                onSelect    = { iglesia -> viewModel.onIglesiaSeleccionada(miembroIdSheet, iglesia) },
            )
        }
    }
}

@Composable
private fun MiembroSabadoRow(
    miembro: MiembroSabado,
    onToggle: () -> Unit,
    onIglesiaClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 16.dp)
            .background(Background, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(BackgroundDeep, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = miembro.iniciales,
                style      = MaterialTheme.typography.labelSmall,
                color      = Accent,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = miembro.nombre,
                style      = MaterialTheme.typography.bodyLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )
            if (miembro.presente && miembro.iglesiaVisitadaNombre != null) {
                Row(
                    modifier   = Modifier
                        .clickable(onClick = onIglesiaClick)
                        .padding(top = 2.dp),
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
                    modifier = Modifier.clickable(onClick = onIglesiaClick),
                )
            }
        }

        // Toggle P/A
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color  = if (miembro.presente) Sage else BackgroundDeep,
                    shape  = CircleShape,
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            if (miembro.presente) {
                Icon(
                    imageVector        = Icons.Default.Check,
                    contentDescription = null,
                    tint               = Background,
                    modifier           = Modifier.size(18.dp),
                )
            } else {
                Text(
                    text  = "A",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
    }
}

@Composable
private fun IglesiaSeleccionarSheet(
    iglesias: List<IglesiaItem>,
    seleccionada: String?,
    onSelect: (IglesiaItem) -> Unit,
) {
    var busqueda by remember { mutableStateOf("") }
    val filtradas = if (busqueda.isBlank()) iglesias
                   else iglesias.filter { it.nombre.contains(busqueda, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text       = "¿A qué iglesia fue?",
            style      = MaterialTheme.typography.titleLarge,
            color      = Ink,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
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
