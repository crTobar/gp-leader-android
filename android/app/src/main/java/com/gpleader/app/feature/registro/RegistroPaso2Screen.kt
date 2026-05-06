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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInsetSm
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import java.time.LocalDate
import kotlin.math.roundToInt

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun RegistroPaso2Screen(
    onNavigateBack:      () -> Unit,
    onNavigateToDetalle: (actividadId: String) -> Unit,
    onNavigateToAgregar: () -> Unit,
    onNavigateToPaso3:   () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToPaso3) {
        if (uiState.navigateToPaso3) {
            viewModel.consumePaso3Navigation()
            onNavigateToPaso3()
        }
    }

    RegistroPaso2Content(
        uiState              = uiState,
        onNavigateBack       = onNavigateBack,
        onActividadClick     = onNavigateToDetalle,
        onAgregarExtra       = onNavigateToAgregar,
        onCantidadChange     = viewModel::onCantidadChange,
        onCheckboxToggle     = viewModel::onCheckboxToggle,
        onMontoChange        = viewModel::onMontoChange,
        onToggleDesglose     = viewModel::onToggleDesglose,
        onDesgloseChange     = viewModel::onDesgloseChange,
        onDesgloseMontoChange = viewModel::onDesgloseMontoChange,
        onSiguiente          = viewModel::onSiguienteClick,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun RegistroPaso2Content(
    uiState:          RegistroUiState,
    onNavigateBack:   () -> Unit,
    onActividadClick: (String) -> Unit,
    onAgregarExtra:   () -> Unit,
    onCantidadChange: (String, Int?) -> Unit,
    onCheckboxToggle:     (String) -> Unit,
    onMontoChange:        (String, Double?) -> Unit,
    onToggleDesglose:     (String) -> Unit,
    onDesgloseChange:     (String, String, Int) -> Unit,
    onDesgloseMontoChange: (String, String, Double) -> Unit,
    onSiguiente:          () -> Unit,
) {
    val actividadesUnion  = uiState.actividades.filter { it.nivel == NivelActividad.UNION }
    val actividadesPastor = uiState.actividades.filter { it.nivel == NivelActividad.PASTOR }
    val actividadesGP     = uiState.actividades.filter { it.nivel == NivelActividad.GP }

    val listState        = rememberLazyListState()
    val errorShakeOffset = remember { Animatable(0f) }

    // Al fallar validación: desplazar a sección PASTOR (donde están las obligatorias) y sacudir error
    LaunchedEffect(uiState.errorActividadesObligatoriasTrigger) {
        if (uiState.errorActividadesObligatoriasTrigger > 0) {
            listState.animateScrollToItem(5) // índice de SeccionActividades PASTOR
            repeat(3) {
                errorShakeOffset.animateTo(10f, tween(70))
                errorShakeOffset.animateTo(-10f, tween(70))
            }
            errorShakeOffset.animateTo(0f, tween(70))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            item { Paso2TopBar(pasoActivo = 2, onNavigateBack = onNavigateBack) }
            item { StepperRow(pasoActivo = 2) }
            item { Spacer(Modifier.height(16.dp)) }

            // ── UNIÓN ─────────────────────────────────────────────────────────
            item {
                SeccionActividades(
                    labelNivel       = stringResource(R.string.registro_nivel_union),
                    headerBg         = Ink,
                    headerTextColor  = Color.White,
                    headerIcon       = {
                        Icon(
                            imageVector        = Icons.Filled.Lock,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(14.dp),
                        )
                    },
                    actividades      = actividadesUnion,
                    onActividadClick = { /* bloqueada */ },
                    onCantidadChange = { _, _ -> },
                    onCheckboxToggle      = { },
                    onMontoChange         = { _, _ -> },
                    onToggleDesglose      = { _ -> },
                    onDesgloseChange      = { _, _, _ -> },
                    onDesgloseMontoChange = { _, _, _ -> },
                    onAgregarExtra        = null,
                    modifier              = Modifier.padding(horizontal = 16.dp),
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            // ── PASTOR ────────────────────────────────────────────────────────
            item {
                SeccionActividades(
                    labelNivel            = stringResource(R.string.registro_nivel_pastor),
                    headerBg              = Color(0xFF4A5568),
                    headerTextColor       = Color.White,
                    headerIcon            = {
                        Icon(
                            imageVector        = Icons.Filled.Star,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(14.dp),
                        )
                    },
                    actividades           = actividadesPastor,
                    onActividadClick      = onActividadClick,
                    onCantidadChange      = onCantidadChange,
                    onCheckboxToggle      = onCheckboxToggle,
                    onMontoChange         = onMontoChange,
                    onToggleDesglose      = onToggleDesglose,
                    onDesgloseChange      = onDesgloseChange,
                    onDesgloseMontoChange = onDesgloseMontoChange,
                    onAgregarExtra        = null,
                    modifier              = Modifier.padding(horizontal = 16.dp),
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            // ── MI GP ─────────────────────────────────────────────────────────
            item {
                SeccionActividades(
                    labelNivel            = stringResource(R.string.registro_nivel_mi_gp),
                    headerBg              = BackgroundDeep,
                    headerTextColor       = Ink,
                    headerIcon            = {
                        Icon(
                            imageVector        = Icons.Filled.Star,
                            contentDescription = null,
                            tint               = Accent,
                            modifier           = Modifier.size(14.dp),
                        )
                    },
                    actividades           = actividadesGP,
                    onActividadClick      = onActividadClick,
                    onCantidadChange      = onCantidadChange,
                    onCheckboxToggle      = onCheckboxToggle,
                    onMontoChange         = onMontoChange,
                    onToggleDesglose      = onToggleDesglose,
                    onDesgloseChange      = onDesgloseChange,
                    onDesgloseMontoChange = onDesgloseMontoChange,
                    onAgregarExtra        = onAgregarExtra,
                    modifier         = Modifier.padding(horizontal = 16.dp),
                )
            }

            if (uiState.errorActividadesObligatorias) {
                item {
                    Text(
                        text     = stringResource(R.string.registro_error_obligatorias),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = Blush,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .offset { IntOffset(errorShakeOffset.value.roundToInt(), 0) },
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        // ── Botón flotante ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Background)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            NeuButtonPrimary(
                text     = stringResource(R.string.registro_btn_siguiente),
                onClick  = onSiguiente,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun Paso2TopBar(pasoActivo: Int, onNavigateBack: () -> Unit) {
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
                                drawLine(lineColorLeft,  Offset(0f, lineY),              Offset(centerX - circleR, lineY), strokePx)
                            }
                            if (idx < labels.size - 1) {
                                drawLine(lineColorRight, Offset(centerX + circleR, lineY), Offset(size.width, lineY),      strokePx)
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

// ── Sección de actividades ────────────────────────────────────────────────────

@Composable
private fun SeccionActividades(
    labelNivel:       String,
    headerBg:         Color,
    headerTextColor:  Color,
    headerIcon:       @Composable (() -> Unit)? = null,
    actividades:      List<ActividadRegistro>,
    onActividadClick: (String) -> Unit,
    onCantidadChange: (String, Int?) -> Unit,
    onCheckboxToggle:      (String) -> Unit,
    onMontoChange:         (String, Double?) -> Unit,
    onToggleDesglose:      (String) -> Unit,
    onDesgloseChange:      (String, String, Int) -> Unit,
    onDesgloseMontoChange: (String, String, Double) -> Unit,
    onAgregarExtra:        (() -> Unit)?,
    modifier:              Modifier = Modifier,
) {
    NeuCard(modifier = modifier.fillMaxWidth()) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (headerIcon != null) {
                    headerIcon()
                }
                Text(
                    text       = labelNivel,
                    style      = MaterialTheme.typography.labelSmall,
                    color      = headerTextColor,
                    fontWeight = FontWeight.Bold,
                )
            }

            HorizontalDivider(color = if (headerBg == BackgroundDeep) Background else headerBg.copy(alpha = 0.3f))

            // Actividades
            actividades.forEachIndexed { idx, actividad ->
                ActividadRow(
                    actividad             = actividad,
                    onClick               = { if (!actividad.bloqueada) onActividadClick(actividad.id) },
                    onCantidadChange      = { cant -> onCantidadChange(actividad.id, cant) },
                    onCheckboxToggle      = { onCheckboxToggle(actividad.id) },
                    onMontoChange         = { monto -> onMontoChange(actividad.id, monto) },
                    onToggleDesglose      = { onToggleDesglose(actividad.id) },
                    onDesgloseChange      = { miembroId, cant -> onDesgloseChange(actividad.id, miembroId, cant) },
                    onDesgloseMontoChange = { miembroId, monto -> onDesgloseMontoChange(actividad.id, miembroId, monto) },
                    modifier              = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
                if (idx < actividades.lastIndex || onAgregarExtra != null) {
                    HorizontalDivider(
                        color    = BackgroundDeep,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            // Fila "Agregar actividad extra" (solo GP)
            if (onAgregarExtra != null) {
                AgregarExtraRow(
                    onClick  = onAgregarExtra,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

// ── Fila de actividad ─────────────────────────────────────────────────────────

@Composable
private fun ActividadRow(
    actividad:             ActividadRegistro,
    onClick:               () -> Unit,
    onCantidadChange:      (Int?) -> Unit,
    onCheckboxToggle:      () -> Unit = {},
    onMontoChange:         (Double?) -> Unit = {},
    onToggleDesglose:      () -> Unit = {},
    onDesgloseChange:      (miembroId: String, cantidad: Int) -> Unit = { _, _ -> },
    onDesgloseMontoChange: (miembroId: String, monto: Double) -> Unit = { _, _ -> },
    modifier:              Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!actividad.bloqueada && actividad.tipoMarcador == TipoMarcador.CONTADOR)
                        Modifier.clickable(onClick = onClick)
                    else Modifier
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Ícono de tipo (izquierda)
            when (actividad.tipoMarcador) {
                TipoMarcador.CHECKBOX -> {
                    // Toggle circular interactivo
                    val realizado = actividad.realizado == true
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (realizado) Sage else BackgroundDeep)
                            .then(
                                if (!actividad.bloqueada) Modifier.clickable(onClick = onCheckboxToggle)
                                else Modifier
                            ),
                    ) {
                        if (realizado) {
                            Text(text = "✓", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
                TipoMarcador.MONETARIO -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Gold.copy(alpha = 0.15f)),
                    ) {
                        Text(text = "₡", style = MaterialTheme.typography.labelSmall, color = Gold)
                    }
                }
                TipoMarcador.CONTADOR -> {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .border(1.5.dp, Muted.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Nombre + unidad + badge oficial
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = actividad.nombre,
                        style      = MaterialTheme.typography.bodyLarge,
                        color      = if (actividad.bloqueada) Mid else Ink,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.weight(1f, fill = false),
                    )
                    if (actividad.esOficial) {
                        Spacer(Modifier.width(6.dp))
                        ActividadBadge(texto = stringResource(R.string.registro_badge_oficial), color = Gold)
                    }
                }
                if (actividad.tipoMarcador != TipoMarcador.CHECKBOX) {
                    Text(
                        text  = actividad.unidad,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Control derecho según tipo
            when (actividad.tipoMarcador) {
                TipoMarcador.CONTADOR -> {
                    ContadorInline(
                        valor    = actividad.cantidad,
                        enabled  = !actividad.bloqueada,
                        onChange = onCantidadChange,
                    )
                    Spacer(Modifier.width(6.dp))
                    when {
                        actividad.bloqueada -> Icon(
                            imageVector        = Icons.Default.Lock,
                            contentDescription = null,
                            tint               = Muted,
                            modifier           = Modifier.size(16.dp),
                        )
                        actividad.tieneDesglose -> Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onToggleDesglose)
                                .padding(4.dp),
                        ) {
                            Icon(
                                imageVector        = if (actividad.desgloseExpandido)
                                    Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint               = Accent,
                                modifier           = Modifier.size(20.dp),
                            )
                        }
                        else -> Icon(
                            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint               = Muted,
                            modifier           = Modifier.size(20.dp),
                        )
                    }
                }
                TipoMarcador.CHECKBOX -> {
                    // Nada extra — el toggle está a la izquierda
                }
                TipoMarcador.MONETARIO -> {
                    MontoInline(
                        monto    = actividad.monto,
                        enabled  = !actividad.bloqueada,
                        onChange = onMontoChange,
                    )
                    if (actividad.tieneDesglose) {
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onToggleDesglose)
                                .padding(4.dp),
                        ) {
                            Icon(
                                imageVector        = if (actividad.desgloseExpandido)
                                    Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint               = Accent,
                                modifier           = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }

        // ── Desglose contador (expandible) ────────────────────────────────────
        if (actividad.tipoMarcador == TipoMarcador.CONTADOR &&
            actividad.tieneDesglose && actividad.desgloseExpandido) {
            val totalGeneral = actividad.cantidad ?: 0
            val sumDesglose  = actividad.desgloseMiembros.sumOf { it.cantidad }
            val disponibles  = totalGeneral - sumDesglose

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = BackgroundDeep)
            Spacer(Modifier.height(4.dp))

            // Resumen disponibles
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "Distribuidos por miembro",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
                Text(
                    text  = "$sumDesglose / $totalGeneral",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (disponibles == 0) Accent else Muted,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(4.dp))

            actividad.desgloseMiembros.forEach { miembro ->
                MiembroDesgloseRow(
                    miembro      = miembro,
                    maxAdicional = disponibles,
                    onChange     = { nuevaCant -> onDesgloseChange(miembro.miembroId, nuevaCant) },
                    modifier     = Modifier.padding(vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(4.dp))
        }

        // ── Desglose monetario (expandible) ───────────────────────────────────
        if (actividad.tipoMarcador == TipoMarcador.MONETARIO &&
            actividad.tieneDesglose && actividad.desgloseExpandido) {
            val montoTotal  = actividad.monto ?: 0.0
            val sumDesglose = actividad.desgloseMiembros.sumOf { it.montoDesglose }
            val disponible  = (montoTotal - sumDesglose).coerceAtLeast(0.0)

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = BackgroundDeep)
            Spacer(Modifier.height(4.dp))

            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "Distribuido por miembro",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
                Text(
                    text       = "₡${sumDesglose.toLong()} / ₡${montoTotal.toLong()}",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = if (disponible == 0.0) Accent else Muted,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(4.dp))

            actividad.desgloseMiembros.forEach { miembro ->
                MiembroDesgloseMonetarioRow(
                    miembro    = miembro,
                    maxMonto   = disponible,
                    onChange   = { nuevoMonto -> onDesgloseMontoChange(miembro.miembroId, nuevoMonto) },
                    modifier   = Modifier.padding(vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

// ── Fila de miembro en desglose ───────────────────────────────────────────────

@Composable
internal fun MiembroDesgloseRow(
    miembro:      MiembroDesglose,
    maxAdicional: Int,
    onChange:     (Int) -> Unit,
    modifier:     Modifier = Modifier,
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BackgroundDeep),
        ) {
            Text(
                text  = miembro.iniciales,
                style = MaterialTheme.typography.labelSmall,
                color = Mid,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text     = miembro.nombre,
            style    = MaterialTheme.typography.bodyMedium,
            color    = Ink,
            modifier = Modifier.weight(1f),
        )

        ContadorInline(
            valor            = miembro.cantidad,
            enabled          = miembro.cantidad > 0 || maxAdicional > 0,
            allowDirectInput = false,
            onChange         = { nuevo ->
                val v = nuevo ?: 0
                onChange(v.coerceIn(0, miembro.cantidad + maxAdicional))
            },
        )
    }
}

// ── Fila de miembro en desglose monetario ─────────────────────────────────────

@Composable
internal fun MiembroDesgloseMonetarioRow(
    miembro:  MiembroDesglose,
    maxMonto: Double,
    onChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BackgroundDeep),
        ) {
            Text(
                text       = miembro.iniciales,
                style      = MaterialTheme.typography.labelSmall,
                color      = Mid,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text     = miembro.nombre,
            style    = MaterialTheme.typography.bodyMedium,
            color    = Ink,
            modifier = Modifier.weight(1f),
        )

        MontoInline(
            monto    = miembro.montoDesglose.takeIf { it > 0 },
            enabled  = miembro.montoDesglose > 0 || maxMonto > 0,
            maxMonto = miembro.montoDesglose + maxMonto,
            onChange = { nuevoMonto ->
                val v = (nuevoMonto ?: 0.0).coerceIn(0.0, miembro.montoDesglose + maxMonto)
                onChange(v)
            },
        )
    }
}

// ── Contador inline (- / valor / +) ──────────────────────────────────────────

@Composable
internal fun ContadorInline(
    valor:            Int?,
    enabled:          Boolean = true,
    allowDirectInput: Boolean = true,
    onBlocked:        (() -> Unit)? = null,
    onChange:         (Int?) -> Unit,
) {
    var editando  by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .neuInsetSm(cornerRadius = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundDeep),
    ) {
        // − button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .then(when {
                    enabled && valor != null -> Modifier.clickable {
                        editando = false
                        onChange(if (valor <= 0) null else valor - 1)
                    }
                    onBlocked != null -> Modifier.clickable { onBlocked() }
                    else              -> Modifier
                })
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text  = "−",
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled && valor != null) Ink else Muted.copy(alpha = 0.5f),
            )
        }

        // Valor — tappable para escritura directa
        if (editando && enabled && allowDirectInput) {
            BasicTextField(
                value         = inputText,
                onValueChange = { inputText = it.filter { c -> c.isDigit() } },
                singleLine    = true,
                textStyle     = MaterialTheme.typography.labelSmall.copy(
                    color     = Ink,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction    = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        editando = false
                        onChange(inputText.toIntOrNull())
                    },
                ),
                modifier = Modifier
                    .widthIn(min = 32.dp)
                    .padding(horizontal = 4.dp),
            )
            DisposableEffect(Unit) {
                onDispose {
                    if (editando) onChange(inputText.toIntOrNull())
                }
            }
        } else {
            Text(
                text      = valor?.toString() ?: "—",
                style     = MaterialTheme.typography.labelSmall,
                color     = if (enabled) Ink else Muted.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .widthIn(min = 24.dp)
                    .then(
                        if (enabled && allowDirectInput) Modifier.clickable {
                            inputText = valor?.toString() ?: ""
                            editando = true
                        } else Modifier
                    ),
            )
        }

        // + button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .then(when {
                    enabled           -> Modifier.clickable {
                        editando = false
                        onChange(valor?.plus(1) ?: 0)
                    }
                    onBlocked != null -> Modifier.clickable { onBlocked() }
                    else              -> Modifier
                })
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text  = "+",
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) Ink else Muted.copy(alpha = 0.5f),
            )
        }
    }
}

// ── Campo monto inline (₡) ───────────────────────────────────────────────────

@Composable
internal fun MontoInline(
    monto:    Double?,
    enabled:  Boolean = true,
    maxMonto: Double? = null,
    onChange: (Double?) -> Unit,
) {
    var texto by remember(monto) {
        mutableStateOf(monto?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(
                width = 1.5.dp,
                color = if (enabled) Gold.copy(alpha = 0.6f) else Muted.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
            )
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundDeep)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(
            text  = "₡",
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) Gold else Muted.copy(alpha = 0.5f),
        )
        Spacer(Modifier.width(4.dp))
        BasicTextField(
            value         = texto,
            onValueChange = { new ->
                val filtrado = new.filter { it.isDigit() || it == '.' }
                val parsed   = filtrado.toDoubleOrNull()
                // No exceder el máximo si está definido
                if (maxMonto != null && parsed != null && parsed > maxMonto) return@BasicTextField
                texto = filtrado
                onChange(parsed)
            },
            enabled       = enabled,
            singleLine    = true,
            textStyle     = MaterialTheme.typography.labelSmall.copy(
                color     = if (enabled) Ink else Muted.copy(alpha = 0.5f),
                textAlign = TextAlign.End,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
            ),
            decorationBox = { inner ->
                Box {
                    if (texto.isEmpty()) {
                        Text(
                            text  = "0",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted.copy(alpha = 0.5f),
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.widthIn(min = 52.dp, max = 104.dp),
        )
    }
}

// ── Badge para actividades ────────────────────────────────────────────────────

@Composable
internal fun ActividadBadge(texto: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text  = texto,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

// ── Fila "Agregar actividad extra" ────────────────────────────────────────────

@Composable
private fun AgregarExtraRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxWidth().height(1.dp)
        ) {
            drawLine(
                color       = Muted.copy(alpha = 0.4f),
                start       = androidx.compose.ui.geometry.Offset(0f, 0f),
                end         = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = 2f,
                pathEffect  = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text     = stringResource(R.string.registro_agregar_extra),
            style    = MaterialTheme.typography.bodyMedium,
            color    = Accent,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

private val paso2PreviewState = RegistroUiState(
    actividades = listOf(
        ActividadRegistro("a1", "Recolección ofrendas", NivelActividad.UNION,   "personas", esOficial = true, bloqueada = true),
        ActividadRegistro("a2", "Repartir literatura",  NivelActividad.UNION,   "libros",   esOficial = true, bloqueada = true),
        ActividadRegistro("a3", "Estudios Bíblicos",     NivelActividad.PASTOR, "personas", esOficial = true, esObligatoria = true, cantidad = 3),
        ActividadRegistro("a4", "Peticiones de Oración", NivelActividad.PASTOR, "personas", esOficial = true, esObligatoria = true),
        ActividadRegistro("a5", "Interesados Nuevos",    NivelActividad.PASTOR, "personas", esOficial = true, esObligatoria = true, cantidad = 1),
        ActividadRegistro("a6", "Oración especial",     NivelActividad.GP,     "veces",    esOficial = false, esExtra = true, cantidad = 4),
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "Paso2 — actividades")
@Composable
private fun Paso2Preview() {
    GpLeaderTheme {
        RegistroPaso2Content(
            uiState          = paso2PreviewState,
            onNavigateBack   = {},
            onActividadClick = {},
            onAgregarExtra   = {},
            onCantidadChange = { _, _ -> },
            onCheckboxToggle      = { },
            onMontoChange         = { _, _ -> },
            onToggleDesglose      = { _ -> },
            onDesgloseChange      = { _, _, _ -> },
            onDesgloseMontoChange = { _, _, _ -> },
            onSiguiente           = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "Paso2 — error obligatorias")
@Composable
private fun Paso2ErrorPreview() {
    GpLeaderTheme {
        RegistroPaso2Content(
            uiState          = paso2PreviewState.copy(errorActividadesObligatorias = true),
            onNavigateBack   = {},
            onActividadClick = {},
            onAgregarExtra   = {},
            onCantidadChange = { _, _ -> },
            onCheckboxToggle      = { },
            onMontoChange         = { _, _ -> },
            onToggleDesglose      = { _ -> },
            onDesgloseChange      = { _, _, _ -> },
            onDesgloseMontoChange = { _, _, _ -> },
            onSiguiente           = {},
        )
    }
}
