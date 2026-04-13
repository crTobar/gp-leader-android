package com.gpleader.app.feature.registro

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import java.time.DayOfWeek
import java.time.LocalDate

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun RegistroPaso3Screen(
    onNavigateBack:           () -> Unit,
    onNavigateToExitoEnviado: () -> Unit,
    onNavigateToExitoOffline: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToExitoEnviado) {
        if (uiState.navigateToExitoEnviado) {
            viewModel.consumeExitoEnviadoNavigation()
            onNavigateToExitoEnviado()
        }
    }

    LaunchedEffect(uiState.navigateToExitoOffline) {
        if (uiState.navigateToExitoOffline) {
            viewModel.consumeExitoOfflineNavigation()
            onNavigateToExitoOffline()
        }
    }

    RegistroPaso3Content(
        uiState        = uiState,
        onNavigateBack = onNavigateBack,
        onEnviar       = viewModel::onEnviarClick,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun RegistroPaso3Content(
    uiState:        RegistroUiState,
    onNavigateBack: () -> Unit,
    onEnviar:       () -> Unit,
) {
    val isEnviando = uiState.isEnviando
    val errorEnvio = uiState.errorEnvio
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 128.dp),
        ) {
            item { Paso3TopBar(onNavigateBack = onNavigateBack) }
            item { StepperRow(pasoActivo = 3) }
            item { Spacer(Modifier.height(20.dp)) }
            item {
                ResumenCard(
                    uiState  = uiState,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item { Spacer(Modifier.height(20.dp)) }
            item {
                ActividadesSeparador(
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                ActividadesResumen(
                    actividades = uiState.actividades,
                    modifier    = Modifier.padding(horizontal = 16.dp),
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        // ── Botones flotantes ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Background)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (errorEnvio != null) {
                Text(
                    text     = errorEnvio,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = Blush,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (isEnviando) {
                Box(
                    modifier         = Modifier.fillMaxWidth().height(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Accent)
                }
            } else {
                NeuButtonPrimary(
                    text     = stringResource(R.string.paso3_btn_enviar),
                    onClick  = onEnviar,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            NeuButtonSecondary(
                text     = stringResource(R.string.paso3_btn_editar),
                onClick  = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun Paso3TopBar(onNavigateBack: () -> Unit) {
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
                contentDescription = null,
                tint               = Ink,
                modifier           = Modifier.size(20.dp),
            )
        }
        Text(
            text      = stringResource(R.string.registro_titulo),
            style     = MaterialTheme.typography.titleLarge,
            color     = Ink,
            textAlign = TextAlign.Center,
            modifier  = Modifier.weight(1f),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Ink)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text(
                text  = stringResource(R.string.registro_badge_paso3),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

// ── Stepper ───────────────────────────────────────────────────────────────────

@Composable
private fun StepperRow(pasoActivo: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        listOf(
            stringResource(R.string.registro_step_asistencia),
            stringResource(R.string.registro_step_actividades),
            stringResource(R.string.registro_step_resumen),
        ).forEachIndexed { idx, label ->
            val activo = idx + 1 == pasoActivo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.weight(1f),
            ) {
                Text(
                    text     = label,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = if (activo) Accent else Muted,
                    modifier = Modifier.padding(vertical = 10.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (activo) Accent else Color.Transparent),
                )
            }
        }
    }
}

// ── Resumen card (dark) ───────────────────────────────────────────────────────

@Composable
private fun ResumenCard(
    uiState:  RegistroUiState,
    modifier: Modifier = Modifier,
) {
    val miembrosPresentes    = uiState.miembros.count { it.estado == EstadoAsistencia.PRESENTE }
    val miembrosAusentes     = uiState.miembros.count { it.estado == EstadoAsistencia.AUSENTE }
    val miembrosJustificados = uiState.miembros.count { it.estado == EstadoAsistencia.JUSTIFICADO }
    val presentes            = miembrosPresentes + uiState.visitasDeHoy.count { it.estado == EstadoAsistencia.PRESENTE }
    val total                = uiState.miembros.size + uiState.visitasDeHoy.size
    val pct                  = if (total > 0) presentes * 100 / total else 0
    val visitasCount         = uiState.visitasDeHoy.size

    val pctColor  = when { pct >= 70 -> Sage; pct >= 40 -> Gold; else -> Blush }
    val fechaStr  = uiState.fecha.formatoResumen()
    val visitasStr = if (visitasCount == 1)
        stringResource(R.string.paso3_visitas_singular, visitasCount)
    else
        stringResource(R.string.paso3_visitas_plural, visitasCount)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Ink)
            .padding(20.dp),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = stringResource(R.string.paso3_label_resumen),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Text(
                text       = stringResource(R.string.paso3_label_grupo_header),
                style      = MaterialTheme.typography.bodyMedium,
                color      = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = Mid.copy(alpha = 0.25f))
        Spacer(Modifier.height(14.dp))

        // ── FECHA ─────────────────────────────────────────────────────────────
        ResumenRow(label = stringResource(R.string.paso3_label_fecha), value = fechaStr)

        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = Mid.copy(alpha = 0.12f))
        Spacer(Modifier.height(10.dp))

        // ── MIEMBROS ──────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text     = stringResource(R.string.paso3_label_miembros),
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.weight(1f).padding(top = 4.dp),
            )
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                MiembroStatRow(
                    count = miembrosPresentes,
                    label = "presente${if (miembrosPresentes != 1) "s" else ""}",
                    color = if (miembrosPresentes > 0) Sage else Muted,
                )
                MiembroStatRow(
                    count = miembrosAusentes,
                    label = "ausente${if (miembrosAusentes != 1) "s" else ""}",
                    color = if (miembrosAusentes > 0) Blush else Muted,
                )
                MiembroStatRow(
                    count = miembrosJustificados,
                    label = "justificado${if (miembrosJustificados != 1) "s" else ""}",
                    color = if (miembrosJustificados > 0) Gold else Muted,
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = Mid.copy(alpha = 0.12f))
        Spacer(Modifier.height(10.dp))

        // ── ASISTENCIA ────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text     = stringResource(R.string.paso3_label_asistencia),
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text  = stringResource(R.string.paso3_asistencia_fraccion, presentes, total),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                )
                Text(
                    text       = stringResource(R.string.paso3_asistencia_pct, pct),
                    style      = MaterialTheme.typography.titleLarge,
                    color      = pctColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = Mid.copy(alpha = 0.12f))
        Spacer(Modifier.height(10.dp))

        // ── VISITAS ───────────────────────────────────────────────────────────
        ResumenRow(label = stringResource(R.string.paso3_label_visitas), value = visitasStr)
    }
}

@Composable
private fun MiembroStatRow(count: Int, label: String, color: Color) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text       = "$count",
            style      = MaterialTheme.typography.bodyMedium,
            color      = color,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text     = " $label",
            style    = MaterialTheme.typography.bodyMedium,
            color    = Color.White.copy(alpha = 0.65f),
        )
    }
}

@Composable
private fun ResumenRow(label: String, value: String) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.weight(1f),
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
    }
}

@Composable
private fun BadgeResumen(text: String, bg: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

// ── Separador ACTIVIDADES ─────────────────────────────────────────────────────

@Composable
private fun ActividadesSeparador(modifier: Modifier = Modifier) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Muted.copy(alpha = 0.4f))
        Text(
            text  = stringResource(R.string.paso3_label_actividades),
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Muted.copy(alpha = 0.4f))
    }
}

// ── Actividades agrupadas ─────────────────────────────────────────────────────

@Composable
private fun ActividadesResumen(
    actividades: List<ActividadRegistro>,
    modifier:    Modifier = Modifier,
) {
    val union  = actividades.filter { it.nivel == NivelActividad.UNION  && it.cantidad != null }
    val pastor = actividades.filter { it.nivel == NivelActividad.PASTOR && it.cantidad != null }
    val gp     = actividades.filter { it.nivel == NivelActividad.GP    && it.cantidad != null }

    if (union.isEmpty() && pastor.isEmpty() && gp.isEmpty()) {
        Text(
            text     = stringResource(R.string.paso3_sin_actividades),
            style    = MaterialTheme.typography.bodyMedium,
            color    = Muted,
            modifier = modifier,
        )
        return
    }

    NeuCard(modifier = modifier.fillMaxWidth()) {
        Column {
            if (union.isNotEmpty()) {
                SeccionResumen(
                    labelNivel      = stringResource(R.string.detalle_actividad_nivel_union),
                    headerBg        = Ink,
                    headerTextColor = Color.White,
                    actividades     = union,
                )
            }
            if (pastor.isNotEmpty()) {
                if (union.isNotEmpty()) HorizontalDivider(color = BackgroundDeep, thickness = 1.dp)
                SeccionResumen(
                    labelNivel      = stringResource(R.string.detalle_actividad_nivel_pastor),
                    headerBg        = Mid,
                    headerTextColor = Color.White,
                    actividades     = pastor,
                )
            }
            if (gp.isNotEmpty()) {
                if (union.isNotEmpty() || pastor.isNotEmpty()) HorizontalDivider(color = BackgroundDeep, thickness = 1.dp)
                SeccionResumen(
                    labelNivel      = stringResource(R.string.registro_nivel_mi_gp),
                    headerBg        = BackgroundDeep,
                    headerTextColor = Ink,
                    actividades     = gp,
                )
            }
        }
    }
}

@Composable
private fun SeccionResumen(
    labelNivel:      String,
    headerBg:        Color,
    headerTextColor: Color,
    actividades:     List<ActividadRegistro>,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text  = labelNivel,
                style = MaterialTheme.typography.labelSmall,
                color = headerTextColor,
            )
        }
        actividades.forEach { act ->
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text     = act.nombre,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = Ink,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text  = "${act.cantidad} ${act.unidad}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
            }
        }
    }
}

// ── Date formatter ────────────────────────────────────────────────────────────

internal val MESES = arrayOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
internal val DIAS  = arrayOf("Dom","Lun","Mar","Mié","Jue","Vie","Sáb")

// DayOfWeek: MONDAY=1..SUNDAY=7  →  index 0=Dom,1=Lun,...,6=Sáb
internal fun LocalDate.formatoResumen(): String {
    val diaIdx = if (dayOfWeek == DayOfWeek.SUNDAY) 0 else dayOfWeek.value
    return "${DIAS[diaIdx]} $dayOfMonth ${MESES[monthValue - 1]} $year"
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun RegistroPaso3Preview() {
    GpLeaderTheme {
        RegistroPaso3Content(
            uiState = RegistroUiState(
                fecha  = LocalDate.of(2026, 3, 16),
                miembros = listOf(
                    MiembroAsistencia("m1", "Ana Castillo",   "AC", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m2", "Jose Rodriguez", "JR", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m3", "Lucia Martinez", "LM", EstadoAsistencia.AUSENTE),
                    MiembroAsistencia("m4", "Carlos Perez",   "CP", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m5", "Rosa Torres",    "RT", EstadoAsistencia.JUSTIFICADO),
                    MiembroAsistencia("m6", "Miguel Santos",  "MS", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m7", "Carmen Vega",    "CV", EstadoAsistencia.PRESENTE),
                ),
                visitasDeHoy = listOf(
                    VisitaHoy("v1", "Juan Lopez", esNueva = false, EstadoAsistencia.PRESENTE),
                ),
                actividades = listOf(
                    ActividadRegistro("a1", "Recolección ofrendas", NivelActividad.UNION,  "personas", esOficial = true, bloqueada = true, cantidad = 8),
                    ActividadRegistro("a2", "Repartir literatura",  NivelActividad.UNION,  "libros",   esOficial = true, bloqueada = true, cantidad = 5),
                    ActividadRegistro("a3", "Estudios Bíblicos",    NivelActividad.PASTOR, "personas", esOficial = true, cantidad = 3),
                    ActividadRegistro("a4", "Peticiones de Oración",NivelActividad.PASTOR, "personas", esOficial = true, cantidad = 5),
                    ActividadRegistro("a5", "Interesados Nuevos",   NivelActividad.PASTOR, "personas", esOficial = true, cantidad = 1),
                    ActividadRegistro("a6", "Oración especial",     NivelActividad.GP,     "veces",    esOficial = false, esExtra = true, cantidad = 4),
                ),
            ),
            onNavigateBack = {},
            onEnviar       = {},
        )
    }
}
