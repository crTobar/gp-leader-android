package com.gpleader.app.feature.registro

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.AccentLight
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
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
        uiState             = uiState,
        onNavigateBack      = onNavigateBack,
        onEnviar            = viewModel::onEnviarClick,
        onGuardarBorrador   = viewModel::onGuardarBorradorClick,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun RegistroPaso3Content(
    uiState:           RegistroUiState,
    onNavigateBack:    () -> Unit,
    onEnviar:          () -> Unit,
    onGuardarBorrador: () -> Unit,
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
            item { Paso3TopBar(pasoActivo = 3, onNavigateBack = onNavigateBack) }
            item { StepperRow(pasoActivo = 3) }
            item { Spacer(Modifier.height(20.dp)) }
            item {
                ResumenCard(
                    uiState  = uiState,
                    nombreGrupo = uiState.nombreGrupo.ifBlank {
                        stringResource(R.string.paso3_grupo_sin_nombre)
                    },
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
            item { Spacer(Modifier.height(108.dp)) }
        }

        // ── Botones flotantes ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Background)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            uiState.mensajePaso3?.let { info ->
                Text(
                    text     = info,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = Mid,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
                    text         = stringResource(R.string.paso3_btn_enviar),
                    onClick      = onEnviar,
                    modifier     = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                )
            }
            if (!isEnviando) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onGuardarBorrador)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Save,
                        contentDescription = null,
                        tint               = Accent,
                        modifier           = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = stringResource(R.string.paso3_btn_borrador),
                        style      = MaterialTheme.typography.titleLarge,
                        color      = Accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text       = stringResource(R.string.paso3_btn_editar_actividades),
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Muted,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onNavigateBack)
                        .padding(vertical = 10.dp),
                    textAlign  = TextAlign.Center,
                )
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun Paso3TopBar(pasoActivo: Int, onNavigateBack: () -> Unit) {
    val stepLabel = when (pasoActivo) {
        1    -> stringResource(R.string.registro_step_asistencia)
        2    -> stringResource(R.string.registro_step_actividades)
        else -> stringResource(R.string.registro_step_resumen)
    }
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
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
        Column(
            modifier            = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text      = stringResource(R.string.registro_titulo),
                style     = MaterialTheme.typography.titleLarge,
                color     = Ink,
                textAlign = TextAlign.Center,
            )
            Text(
                text      = stringResource(R.string.registro_subtitulo_paso, pasoActivo, stepLabel),
                style     = MaterialTheme.typography.bodySmall,
                color     = Muted,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.size(40.dp))
    }
}

// ── Stepper ───────────────────────────────────────────────────────────────────

@Composable
private fun StepperRow(pasoActivo: Int) {
    val labels = listOf(
        stringResource(R.string.registro_step_asistencia),
        stringResource(R.string.registro_step_actividades),
        stringResource(R.string.registro_step_resumen),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEachIndexed { idx, label ->
                val numero         = idx + 1
                val activo         = numero == pasoActivo
                val completado     = numero < pasoActivo
                val lineColorLeft  = if (pasoActivo > idx)     Accent else Muted.copy(alpha = 0.4f)
                val lineColorRight = if (pasoActivo > idx + 1) Accent else Muted.copy(alpha = 0.4f)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .drawBehind {
                            val circleR  = 22.dp.toPx()
                            val lineY    = circleR
                            val centerX  = size.width / 2f
                            val strokePx = 1.5.dp.toPx()
                            if (idx > 0) {
                                drawLine(lineColorLeft,  Offset(0f, lineY),               Offset(centerX - circleR, lineY), strokePx)
                            }
                            if (idx < labels.size - 1) {
                                drawLine(lineColorRight, Offset(centerX + circleR, lineY), Offset(size.width, lineY),       strokePx)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier.size(44.dp),
                    ) {
                        if (activo) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Accent.copy(alpha = 0.15f)),
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (activo || completado) Accent else Color.Transparent)
                                .then(
                                    if (!activo && !completado)
                                        Modifier.border(1.5.dp, Muted, CircleShape)
                                    else Modifier
                                ),
                        ) {
                            Text(
                                text       = "$numero",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = if (activo || completado) Color.White else Muted,
                                fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = label,
                        style     = MaterialTheme.typography.labelSmall,
                        color     = if (activo) Accent else Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

// ── Resumen card (claro, estilo mock) ─────────────────────────────────────────

@Composable
private fun ResumenCard(
    uiState:     RegistroUiState,
    nombreGrupo: String,
    modifier:    Modifier = Modifier,
) {
    val miembrosPresentes    = uiState.miembros.count { it.estado == EstadoAsistencia.PRESENTE }
    val miembrosAusentes     = uiState.miembros.count { it.estado == EstadoAsistencia.AUSENTE }
    val miembrosJustificados = uiState.miembros.count { it.estado == EstadoAsistencia.JUSTIFICADO }
    val presentes            = miembrosPresentes + uiState.visitasDeHoy.count { it.estado == EstadoAsistencia.PRESENTE }
    val total                = uiState.miembros.size + uiState.visitasDeHoy.size
    val pct                  = if (total > 0) presentes * 100 / total else 0
    val frac                 = if (total > 0) presentes.toFloat() / total else 0f
    val visitasCount         = uiState.visitasDeHoy.size

    val fechaStr = uiState.fecha.formatoResumen()
    val visitasStr = if (visitasCount == 1)
        stringResource(R.string.paso3_visitas_singular, visitasCount)
    else
        stringResource(R.string.paso3_visitas_plural, visitasCount)

    val chipPresente = pluralStringResource(R.plurals.paso3_chip_presente, miembrosPresentes, miembrosPresentes)
    val chipAusente  = pluralStringResource(R.plurals.paso3_chip_ausente, miembrosAusentes, miembrosAusentes)
    val chipJust     = pluralStringResource(R.plurals.paso3_chip_justificado, miembrosJustificados, miembrosJustificados)

    NeuCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text       = nombreGrupo,
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Ink,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.weight(1f),
                )
                Icon(
                    imageVector        = Icons.Default.Groups,
                    contentDescription = null,
                    tint               = Accent,
                    modifier           = Modifier.size(26.dp),
                )
            }

            Spacer(Modifier.height(18.dp))

            ResumenRowLight(
                label = stringResource(R.string.paso3_label_fecha),
                value = fechaStr,
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = BackgroundDeep, thickness = 1.dp)
            Spacer(Modifier.height(14.dp))

            Text(
                text     = stringResource(R.string.paso3_label_miembros),
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier          = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AsistenciaChip(
                    text       = chipPresente,
                    textColor  = if (miembrosPresentes > 0) Sage else Muted,
                )
                AsistenciaChip(
                    text       = chipAusente,
                    textColor  = if (miembrosAusentes > 0) Blush else Muted,
                )
                AsistenciaChip(
                    text       = chipJust,
                    textColor  = if (miembrosJustificados > 0) Gold else Muted,
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = BackgroundDeep, thickness = 1.dp)
            Spacer(Modifier.height(14.dp))

            Text(
                text     = stringResource(R.string.paso3_label_asistencia),
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = stringResource(R.string.paso3_asistencia_fraccion, presentes, total),
                style      = MaterialTheme.typography.headlineSmall,
                color      = Ink,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text       = "$presentes/$total",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = Mid,
                )
                LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color      = Accent,
                    trackColor = BackgroundDeep,
                )
                Text(
                    text       = stringResource(R.string.paso3_asistencia_pct, pct),
                    style      = MaterialTheme.typography.bodySmall,
                    color      = Mid,
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = BackgroundDeep, thickness = 1.dp)
            Spacer(Modifier.height(14.dp))

            ResumenRowLight(
                label = stringResource(R.string.paso3_label_visitas),
                value = visitasStr,
            )
        }
    }
}

@Composable
private fun ResumenRowLight(label: String, value: String) {
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
            text       = value,
            style      = MaterialTheme.typography.bodyLarge,
            color      = Ink,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun AsistenciaChip(text: String, textColor: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(textColor.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.labelSmall,
            color      = textColor,
            fontWeight = FontWeight.SemiBold,
        )
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

// ── Actividades agrupadas (tarjetas claras + acento vertical) ─────────────────

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

    Column(
        modifier              = modifier.fillMaxWidth(),
        verticalArrangement   = Arrangement.spacedBy(12.dp),
    ) {
        if (union.isNotEmpty()) {
            SeccionActividadesCard(
                labelNivel  = stringResource(R.string.detalle_actividad_nivel_union),
                accentColor = Gold,
                actividades = union,
            )
        }
        if (pastor.isNotEmpty()) {
            SeccionActividadesCard(
                labelNivel  = stringResource(R.string.detalle_actividad_nivel_pastor),
                accentColor = Accent,
                actividades = pastor,
            )
        }
        if (gp.isNotEmpty()) {
            SeccionActividadesCard(
                labelNivel  = stringResource(R.string.registro_nivel_mi_gp),
                accentColor = AccentLight,
                actividades = gp,
            )
        }
    }
}

@Composable
private fun SeccionActividadesCard(
    labelNivel:   String,
    accentColor:  Color,
    actividades:  List<ActividadRegistro>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Background),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .heightIn(min = 48.dp)
                    .background(accentColor),
            )
            Text(
                text       = labelNivel,
                style      = MaterialTheme.typography.labelSmall,
                color      = Ink,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            )
        }
        HorizontalDivider(color = BackgroundDeep, thickness = 1.dp)
        actividades.forEach { act ->
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text     = act.nombre,
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = Ink,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text       = "${act.cantidad} ${act.unidad}",
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Accent,
                    fontWeight = FontWeight.SemiBold,
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
                nombreGrupo = "GP Los Olivos",
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
            onNavigateBack      = {},
            onEnviar            = {},
            onGuardarBorrador   = {},
        )
    }
}
