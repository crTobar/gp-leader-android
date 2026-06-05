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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.gpleader.app.core.ui.estudios.AgregarAlumnoEstudioDialog
import com.gpleader.app.core.ui.estudios.EstudiosBiblicosLista
import com.gpleader.app.core.ui.estudios.asItem
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.OnResumeEffect
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
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInsetInner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuoDetalleScreen(
    onNavigateBack:             () -> Unit,
    onNavigateToCrearActividad: (duoId: String) -> Unit,
    onNavigateToDetalle:        (duoId: String, tipoId: String) -> Unit = { _, _ -> },
    onNavigateToEstudioDetalle: (estudioId: String) -> Unit = {},
    viewModel: DuoDetalleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

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

                    Column(modifier = Modifier.fillMaxSize()) {
                        // ── Header pareja ─────────────────────────────────────
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment     = Alignment.CenterVertically,
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

                        // ── Segmented control: Actividades / Estudios Bíblicos del dúo ──
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
                                duoId        = duo.id,
                                onCardClick  = { tipoId -> onNavigateToDetalle(duo.id, tipoId) },
                                onToggle     = viewModel::onToggleActividad,
                                modifier     = Modifier.weight(1f).fillMaxWidth(),
                            )
                            DuoDetalleTab.ESTUDIOS -> TabEstudios(
                                estudios               = uiState.estudios,
                                onEstudioClick         = onNavigateToEstudioDetalle,
                                onCrearEstudio         = viewModel::onCrearEstudio,
                                modifier               = Modifier.weight(1f).fillMaxWidth(),
                            )
                        }
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
    actividades:  List<DuoActividadTipo>,
    registrosHoy: Map<String, DuoActividadRecord>,
    duoId:        String,
    onCardClick:  (tipoId: String) -> Unit,
    onToggle:     (tipoId: String, markerType: String) -> Unit,
    modifier:     Modifier,
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
            if (tipo.markerType == "daily_checker") {
                DailyCheckerCard(tipo = tipo, record = record, onToggle = { onToggle(tipo.id, tipo.markerType) })
            } else {
                ActividadDuoCardSimple(tipo = tipo, record = record, onClick = { onCardClick(tipo.id) })
            }
        }
    }
}

@Composable
private fun ActividadDuoCardSimple(
    tipo:    DuoActividadTipo,
    record:  DuoActividadRecord?,
    onClick: () -> Unit,
) {
    val tipoLabel = when (tipo.markerType) {
        "monetary" -> "Monetario"
        "checkbox" -> "Verificación"
        else       -> "Contador"
    }
    val valorStr = when (tipo.markerType) {
        "monetary" -> if (record?.count != null) "₡${record.count}" else "—"
        "checkbox" -> if (record?.isDone == true) "✓" else "—"
        else       -> if (record?.count != null) "${record.count} ${tipo.unitLabel}" else "—"
    }
    val valorColor = if (record != null && (record.count != null || record.isDone)) Sage else Muted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevatedSm(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(tipo.nombre, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("$tipoLabel · ${tipo.unitLabel.ifBlank { tipoLabel }}", style = MaterialTheme.typography.labelSmall, color = Muted)
        }
        Spacer(Modifier.width(8.dp))
        Text(valorStr, style = MaterialTheme.typography.bodyLarge, color = valorColor, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = Muted,
            modifier           = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun DailyCheckerCard(
    tipo:     DuoActividadTipo,
    record:   DuoActividadRecord?,
    onToggle: () -> Unit,
) {
    val done = record?.isDone == true
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tipo.nombre, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Accent.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("DIARIO", style = MaterialTheme.typography.labelSmall, color = Accent, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (done) Sage else Muted,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(if (done) "Marcado hoy" else "Marcar hoy", style = MaterialTheme.typography.bodyMedium, color = if (done) Sage else Mid)
            }
        }
    }
}

@Composable
private fun TabEstudios(
    estudios:       List<DuoBibleStudy>,
    onEstudioClick: (estudioId: String) -> Unit,
    onCrearEstudio: (studentName: String) -> Unit,
    modifier:       Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }

    if (showDialog) {
        AgregarAlumnoEstudioDialog(
            nombre         = nuevoNombre,
            isCreating     = false,
            onNombreChange = { nuevoNombre = it },
            onDismiss      = { showDialog = false; nuevoNombre = "" },
            onConfirm      = {
                if (nuevoNombre.isNotBlank()) {
                    onCrearEstudio(nuevoNombre.trim())
                    nuevoNombre = ""
                    showDialog = false
                }
            },
        )
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Estudios Bíblicos del Dúo",
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
                fontWeight = FontWeight.SemiBold,
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.neuElevatedSm(cornerRadius = 10.dp).clip(RoundedCornerShape(10.dp))
                    .background(Background).clickable { showDialog = true }.padding(8.dp),
            ) {
                Icon(Icons.Default.Add, null, tint = Accent, modifier = Modifier.size(20.dp))
            }
        }

        EstudiosBiblicosLista(
            estudios       = estudios.map { it.asItem() },
            onEstudioClick = onEstudioClick,
            modifier       = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp,
            ),
            emptyTitle    = "Aún no hay estudios bíblicos del dúo",
            emptySubtitle = "Agrega a las personas a quienes\nel dúo está dando estudio bíblico.",
            onAgregar     = { showDialog = true },
        )
    }
}
