package com.gpleader.app.feature.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.gpleader.app.core.data.repository.DuoActividadRecord
import com.gpleader.app.core.data.repository.DuoActividadTipo
import com.gpleader.app.core.data.repository.DuoBibleStudy
import com.gpleader.app.core.data.repository.iniciales
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.NeuTextField
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuoDetalleScreen(
    onNavigateBack:          () -> Unit,
    onNavigateToCrearActividad: (duoId: String) -> Unit,
    viewModel: DuoDetalleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink, modifier = Modifier.size(20.dp))
                }
                Text(
                    text      = "Dúo Misionero",
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Ink,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.weight(1f),
                )
                Box(modifier = Modifier.size(40.dp))
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh    = viewModel::onRefresh,
                modifier     = Modifier.weight(1f).fillMaxWidth(),
            indicator = {},
            ) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
                }
                uiState.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error!!, style = MaterialTheme.typography.bodyMedium, color = Blush)
                }
                uiState.duo != null -> {
                    val duo = uiState.duo!!

                    // ── Header pareja ─────────────────────────────────────────
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        NeuAvatar(iniciales = duo.member1.iniciales, size = 44.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("·", style = MaterialTheme.typography.headlineMedium, color = Muted)
                        Spacer(Modifier.width(8.dp))
                        NeuAvatar(iniciales = duo.member2.iniciales, size = 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(duo.member1.primerNombre, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
                            Text(duo.member2.primerNombre, style = MaterialTheme.typography.bodyMedium, color = Mid)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Segmented control ─────────────────────────────────────
                    DuoTabControl(
                        tab      = uiState.tabActivo,
                        onSelect = viewModel::onTabChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                    )

                    Spacer(Modifier.height(12.dp))

                    when (uiState.tabActivo) {
                        DuoDetalleTab.ACTIVIDADES -> TabActividades(
                            actividades  = uiState.actividades,
                            registrosHoy = uiState.registrosHoy,
                            onToggle     = viewModel::onToggleActividad,
                            onIncrementar = viewModel::onIncrementar,
                            onDecrementar = viewModel::onDecrementar,
                            modifier     = Modifier.fillMaxSize(),
                        )
                        DuoDetalleTab.ESTUDIOS -> TabEstudios(
                            estudios      = uiState.estudios,
                            onToggleLeccion = viewModel::onToggleLeccion,
                            onCrearEstudio  = viewModel::onCrearEstudio,
                            modifier        = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
            } // PullToRefreshBox
        }

        // ── FAB crear actividad (solo en tab actividades) ─────────────────────
        if (uiState.tabActivo == DuoDetalleTab.ACTIVIDADES && uiState.duo != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(24.dp)
                    .size(56.dp)
                    .neuGlow(cornerRadius = 28.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Accent)
                    .clickable { onNavigateToCrearActividad(uiState.duo!!.id) },
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun DuoTabControl(
    tab:      DuoDetalleTab,
    onSelect: (DuoDetalleTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Background)
            .neuInsetInner(shadowSize = 8.dp)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DuoDetalleTab.entries.forEach { t ->
            val label = if (t == DuoDetalleTab.ACTIVIDADES) "Actividades" else "Estudios Bíblicos"
            val selected = tab == t
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (selected) Modifier.neuElevatedSm(cornerRadius = 50.dp).clip(RoundedCornerShape(50.dp)).background(Background)
                        else Modifier.clip(RoundedCornerShape(50.dp))
                    )
                    .clickable { onSelect(t) }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
            ) {
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color      = if (selected) Ink else Muted,
                    textAlign  = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TabActividades(
    actividades:   List<DuoActividadTipo>,
    registrosHoy:  Map<String, DuoActividadRecord>,
    onToggle:      (tipoId: String, markerType: String) -> Unit,
    onIncrementar: (tipoId: String) -> Unit,
    onDecrementar: (tipoId: String) -> Unit,
    modifier:      Modifier,
) {
    if (actividades.isEmpty()) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Sin actividades. Toca + para crear una.", style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = TextAlign.Center)
        }
        return
    }
    LazyColumn(
        modifier            = modifier,
        contentPadding      = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 4.dp, bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(actividades) { tipo ->
            val record = registrosHoy[tipo.id]
            ActividadDuoCard(
                tipo      = tipo,
                record    = record,
                onToggle  = { onToggle(tipo.id, tipo.markerType) },
                onIncrement = { onIncrementar(tipo.id) },
                onDecrement = { onDecrementar(tipo.id) },
            )
        }
    }
}

@Composable
private fun ActividadDuoCard(
    tipo:       DuoActividadTipo,
    record:     DuoActividadRecord?,
    onToggle:   () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(tipo.nombre, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            when (tipo.markerType) {
                "checkbox" -> Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggle),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val done = record?.isDone ?: false
                    Icon(
                        imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (done) Sage else Muted,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(if (done) "Completado hoy" else "Marcar como hecho", style = MaterialTheme.typography.bodyMedium, color = if (done) Sage else Mid)
                }
                else -> Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .neuElevatedSm(cornerRadius = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Background)
                            .clickable(onClick = onDecrement)
                    ) { Text("−", style = MaterialTheme.typography.titleLarge, color = Ink) }
                    Spacer(Modifier.width(20.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = "${record?.count ?: 0}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = androidx.compose.ui.unit.TextUnit(32f, androidx.compose.ui.unit.TextUnitType.Sp)),
                            color = Accent,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(tipo.unitLabel, style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                    Spacer(Modifier.width(20.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .neuElevatedSm(cornerRadius = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Background)
                            .clickable(onClick = onIncrement)
                    ) { Text("+", style = MaterialTheme.typography.titleLarge, color = Ink) }
                }
            }
        }
    }
}

@Composable
private fun TabEstudios(
    estudios:       List<DuoBibleStudy>,
    onToggleLeccion: (estudioId: String, leccion: Int, completado: Boolean) -> Unit,
    onCrearEstudio:  (studentName: String) -> Unit,
    modifier:        Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; nuevoNombre = "" },
            title   = { Text("Nuevo estudio bíblico") },
            text    = {
                NeuTextField(
                    value         = nuevoNombre,
                    onValueChange = { nuevoNombre = it },
                    label         = "Nombre del estudiante",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nuevoNombre.isNotBlank()) { onCrearEstudio(nuevoNombre.trim()); nuevoNombre = ""; showDialog = false }
                }) { Text("Crear") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false; nuevoNombre = "" }) { Text("Cancelar") } },
        )
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text("Estudios Bíblicos del Dúo", style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .neuElevatedSm(cornerRadius = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Background)
                    .clickable { showDialog = true }
                    .padding(8.dp),
            ) {
                Icon(Icons.Default.Add, null, tint = Accent, modifier = Modifier.size(20.dp))
            }
        }

        if (estudios.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin estudios. Toca + para agregar.", style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                contentPadding      = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(estudios) { estudio ->
                    EstudioDuoCard(
                        estudio         = estudio,
                        onToggleLeccion = { l, c -> onToggleLeccion(estudio.id, l, c) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EstudioDuoCard(
    estudio: DuoBibleStudy,
    onToggleLeccion: (leccion: Int, completado: Boolean) -> Unit,
) {
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(estudio.studentName, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
            Text("${estudio.totalCompleted}/20 lecciones", style = MaterialTheme.typography.bodyMedium, color = Mid)
            Spacer(Modifier.height(10.dp))
            // Grid de lecciones 1-20
            val chunks = (1..20).chunked(5)
            chunks.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { n ->
                        val done = estudio.completedLessons.contains(n)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .then(
                                    if (done) Modifier.clip(RoundedCornerShape(8.dp)).background(Sage)
                                    else Modifier.neuElevatedSm(cornerRadius = 8.dp).clip(RoundedCornerShape(8.dp)).background(Background)
                                )
                                .clickable { onToggleLeccion(n, !done) },
                        ) {
                            Text("$n", style = MaterialTheme.typography.bodyMedium, color = if (done) Color.White else Mid, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}
