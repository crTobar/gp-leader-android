package com.gpleader.app.feature.miembro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.FloatingNavScaffold
import com.gpleader.app.core.ui.components.NAV_TAB_ACTIVIDADES
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.OnResumeEffect
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
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInset
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiembroActividadesScreen(
    onNavigateToInicio:    () -> Unit = {},
    onNavigateToPerfil:    () -> Unit = {},
    onNavigateToCampana:   (tipoId: String, nombre: String, desde: String, hasta: String) -> Unit = { _, _, _, _ -> },
    onNavigateToHistorial: (tipoId: String) -> Unit = {},
    viewModel: MiembroActividadesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

    MiembroActividadesContent(
        uiState               = uiState,
        onNavigateToInicio    = onNavigateToInicio,
        onNavigateToPerfil    = onNavigateToPerfil,
        onNavigateToCampana   = onNavigateToCampana,
        onNavigateToHistorial = onNavigateToHistorial,
        onRefresh             = viewModel::onRefresh,
        onToggleDiaria        = viewModel::onToggleDiaria,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MiembroActividadesContent(
    uiState: MiembroActividadesUiState,
    onNavigateToInicio:    () -> Unit = {},
    onNavigateToPerfil:    () -> Unit = {},
    onNavigateToCampana:   (tipoId: String, nombre: String, desde: String, hasta: String) -> Unit = { _, _, _, _ -> },
    onNavigateToHistorial: (tipoId: String) -> Unit = {},
    onRefresh:      () -> Unit = {},
    onToggleDiaria: (String) -> Unit = {},
) {
    FloatingNavScaffold(
        selectedTab        = NAV_TAB_ACTIVIDADES,
        onInicioClick      = onNavigateToInicio,
        onActividadesClick = {},
        onPerfilClick      = onNavigateToPerfil,
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(innerPadding),
    ) {
        // ── TopBar ────────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text       = "Mis Actividades",
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = onRefresh,
            modifier     = Modifier.weight(1f).fillMaxWidth(),
        indicator = {},
        ) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            }

            uiState.error != null -> {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(uiState.error, style = MaterialTheme.typography.bodyLarge, color = Blush)
                }
            }

            uiState.actividades.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text  = "No hay actividades disponibles por el momento.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Muted,
                    )
                }
            }

            else -> {
                val diarias   = uiState.actividades.filterIsInstance<ActividadMiembroUi.Diaria>()
                val semanales = uiState.actividades.filterIsInstance<ActividadMiembroUi.Semanal>()

                val hoy      = LocalDate.now()
                val lunes    = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val domingo  = lunes.plusDays(6)
                val fmtCorto = DateTimeFormatter.ofPattern("d MMM", Locale("es"))
                val fmtDia   = DateTimeFormatter.ofPattern("EEEE d MMM", Locale("es"))
                val rangoSemana = "${lunes.format(fmtCorto)} – ${domingo.format(fmtCorto)}"
                val labelHoy    = hoy.format(fmtDia).replaceFirstChar { it.uppercase() }

                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // ── Resumen semanal ───────────────────────────────────────
                    item {
                        ResumenCard(
                            diarias      = diarias,
                            semanales    = semanales,
                            rangoSemana  = rangoSemana,
                        )
                    }

                    if (diarias.isNotEmpty()) {
                        item {
                            SeccionHeader(titulo = "DIARIAS", subtitulo = labelHoy)
                        }
                        items(diarias, key = { it.tipo.id }) { item ->
                            val esCampana = item.tipo.startDate != null
                            val hastaNav  = (item.tipo.endDate ?: LocalDate.now()).toString()
                            ActividadDiariaCard(
                                item     = item,
                                onToggle = { onToggleDiaria(item.tipo.id) },
                                onTap    = if (esCampana) {
                                    { onNavigateToCampana(item.tipo.id, item.tipo.nombre, item.tipo.startDate!!.toString(), hastaNav) }
                                } else null,
                            )
                        }
                    }

                    if (semanales.isNotEmpty()) {
                        item {
                            if (diarias.isNotEmpty()) Spacer(Modifier.height(4.dp))
                            SeccionHeader(titulo = "ESTA SEMANA", subtitulo = rangoSemana)
                        }
                        items(semanales, key = { it.tipo.id }) { item ->
                            ActividadSemanalCard(
                                item    = item,
                                onClick = { onNavigateToHistorial(item.tipo.id) },
                            )
                        }
                    }
                }
            }
        }
        } // PullToRefreshBox
    }
    } // Scaffold
}

// ── ResumenCard ───────────────────────────────────────────────────────────────

@Composable
private fun ResumenCard(
    diarias:     List<ActividadMiembroUi.Diaria>,
    semanales:   List<ActividadMiembroUi.Semanal>,
    rangoSemana: String,
) {
    val diariasMarcadas      = diarias.count { it.marcadaHoy }
    val semanalesConRegistro = semanales.count { it.totalHistorico > 0 }
    val total                = diarias.size + semanales.size
    val completadas          = diariasMarcadas + semanalesConRegistro
    val hayAvance            = completadas > 0

    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "SEMANA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = rangoSemana,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                    fontWeight = FontWeight.Medium,
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = if (hayAvance) Accent.copy(alpha = 0.10f) else BackgroundDeep,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    text  = "$completadas de $total",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hayAvance) Accent else Muted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── SeccionHeader ─────────────────────────────────────────────────────────────

@Composable
private fun SeccionHeader(titulo: String, subtitulo: String? = null) {
    Column(modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)) {
        Text(
            text  = titulo,
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
        if (subtitulo != null) {
            Text(
                text  = subtitulo,
                style = MaterialTheme.typography.labelSmall,
                color = Muted.copy(alpha = 0.6f),
            )
        }
    }
}

// ── NivelChip ─────────────────────────────────────────────────────────────────

@Composable
private fun NivelChip(level: String) {
    val (label, color) = when (level) {
        "union"  -> "Unión"  to Gold
        "pastor" -> "Pastor" to Ink
        else     -> "Mi GP"  to Accent
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

// ── ActividadDiariaCard ───────────────────────────────────────────────────────

private val DIAS_SEMANA_LABELS = listOf("D", "L", "M", "M", "J", "V", "S")
// DayOfWeek mapping: SUNDAY=0, MONDAY=1, ..., SATURDAY=6
private fun DayOfWeek.indexSemana(): Int = when (this) {
    DayOfWeek.SUNDAY    -> 0
    DayOfWeek.MONDAY    -> 1
    DayOfWeek.TUESDAY   -> 2
    DayOfWeek.WEDNESDAY -> 3
    DayOfWeek.THURSDAY  -> 4
    DayOfWeek.FRIDAY    -> 5
    DayOfWeek.SATURDAY  -> 6
}

@Composable
private fun ActividadDiariaCard(
    item:     ActividadMiembroUi.Diaria,
    onToggle: () -> Unit,
    onTap:    (() -> Unit)? = null,
) {
    val hoy       = LocalDate.now()
    val domingo   = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val diasSemana = (0..6).map { domingo.plusDays(it.toLong()) }

    NeuCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onTap != null) Modifier.clickable(onClick = onTap) else Modifier),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            // Nombre de la actividad
            Text(
                text       = item.tipo.nombre,
                style      = MaterialTheme.typography.bodyLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(12.dp))

            // Cuadros D L M M J V S
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                diasSemana.forEachIndexed { idx, dia ->
                    val esHoy       = dia == hoy
                    val marcado     = item.diasMarcadosSemana.contains(dia)
                    val esPasado    = dia.isBefore(hoy)
                    val dentroRango = item.tipo.startDate?.let { !dia.isBefore(it) } ?: true

                    val bgColor = when {
                        esHoy && marcado                           -> Sage.copy(alpha = 0.20f)
                        esHoy && !marcado                          -> Accent.copy(alpha = 0.15f)
                        esPasado && marcado && dentroRango         -> Sage.copy(alpha = 0.15f)
                        esPasado && !marcado && dentroRango        -> Blush.copy(alpha = 0.15f)
                        else                                       -> BackgroundDeep
                    }
                    val txtColor = when {
                        esHoy && marcado                           -> Sage
                        esHoy && !marcado                          -> Accent
                        esPasado && marcado && dentroRango         -> Sage
                        esPasado && !marcado && dentroRango        -> Blush
                        else                                       -> Muted
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor),
                    ) {
                        Text(
                            text       = DIAS_SEMANA_LABELS[idx],
                            style      = MaterialTheme.typography.labelSmall,
                            color      = txtColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Botón "Realizado hoy"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (item.marcadaHoy) Sage.copy(alpha = 0.15f)
                        else Accent.copy(alpha = 0.12f)
                    )
                    .clickable(enabled = !item.isToggling, onClick = onToggle)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (item.isToggling) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        color       = if (item.marcadaHoy) Sage else Accent,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (item.marcadaHoy) {
                            Icon(
                                imageVector        = Icons.Default.Check,
                                contentDescription = null,
                                tint               = Sage,
                                modifier           = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text       = "Realizado hoy",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = Sage,
                                fontWeight = FontWeight.SemiBold,
                            )
                        } else {
                            Text(
                                text       = "Realizado hoy",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = Accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── ActividadSemanalCard ──────────────────────────────────────────────────────

@Composable
private fun ActividadSemanalCard(
    item:    ActividadMiembroUi.Semanal,
    onClick: () -> Unit,
) {
    NeuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = item.tipo.nombre,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                NivelChip(item.tipo.level)
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                when (item.tipo.markerType) {
                    "realizado", "checkbox" -> Icon(
                        imageVector        = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint               = if (item.totalHistorico > 0) Sage else Muted,
                        modifier           = Modifier.size(24.dp),
                    )
                    "monetary" -> Text(
                        text       = "₡${item.totalHistorico}",
                        style      = MaterialTheme.typography.titleLarge,
                        color      = if (item.totalHistorico > 0) Accent else Muted,
                        fontWeight = FontWeight.Bold,
                    )
                    else -> {
                        Text(
                            text       = item.totalHistorico.toString(),
                            style      = MaterialTheme.typography.titleLarge,
                            color      = if (item.totalHistorico > 0) Accent else Muted,
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
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = Muted,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
}

// ── CheckboxNeu ───────────────────────────────────────────────────────────────

@Composable
private fun CheckboxNeu(
    checked: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .then(
                if (checked) Modifier.neuInset(cornerRadius = 10.dp)
                else Modifier.neuElevatedSm(cornerRadius = 10.dp)
            )
            .background(
                color = if (checked) Sage.copy(alpha = 0.15f) else Background,
                shape = RoundedCornerShape(10.dp),
            )
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Accent, strokeWidth = 2.dp)
        } else if (checked) {
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = "Marcado",
                tint               = Sage,
                modifier           = Modifier.size(24.dp),
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun MiembroActividadesPreview() {
    GpLeaderTheme {
        MiembroActividadesContent(
            uiState = MiembroActividadesUiState(
                isLoading = false,
                actividades = listOf(
                    ActividadMiembroUi.Diaria(
                        tipo = com.gpleader.app.core.data.repository.ActividadTipoData(
                            id = "1", nombre = "Semana de Oración", level = "pastor",
                            markerType = "checkbox", unitLabel = "", sortOrder = 1,
                            scope = "global", churchId = null, startDate = null, endDate = null,
                            frecuencia = "diaria", isMemberAccessible = true,
                            districtId = null, campoId = null, grupoId = null,
                        ),
                        marcadaHoy = true,
                    ),
                    ActividadMiembroUi.Semanal(
                        tipo = com.gpleader.app.core.data.repository.ActividadTipoData(
                            id = "2", nombre = "Llamadas misioneras", level = "my_group",
                            markerType = "counter", unitLabel = "llamadas", sortOrder = 2,
                            scope = "church", churchId = null, startDate = null, endDate = null,
                            frecuencia = "semanal", isMemberAccessible = true,
                            districtId = null, campoId = null, grupoId = null,
                        ),
                        totalHistorico = 3,
                    ),
                    ActividadMiembroUi.Semanal(
                        tipo = com.gpleader.app.core.data.repository.ActividadTipoData(
                            id = "3", nombre = "Ofrenda semanal", level = "my_group",
                            markerType = "monetary", unitLabel = "", sortOrder = 3,
                            scope = "church", churchId = null, startDate = null, endDate = null,
                            frecuencia = "semanal", isMemberAccessible = true,
                            districtId = null, campoId = null, grupoId = null,
                        ),
                        totalHistorico = 5200,
                    ),
                ),
            ),
        )
    }
}
