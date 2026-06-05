package com.gpleader.app.feature.registro

import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.OnResumeEffect
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.NeuButtonPrimary

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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
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
        onCheckboxToggle     = viewModel::onCheckboxToggle,
        onCantidadChange     = viewModel::onCantidadChange,
        onMontoInicializar   = { id -> viewModel.onMontoChange(id, 0.0) },
        onSiguiente          = viewModel::onSiguienteClick,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun RegistroPaso2Content(
    uiState:            RegistroUiState,
    onNavigateBack:     () -> Unit,
    onActividadClick:   (String) -> Unit,
    onAgregarExtra:     () -> Unit,
    onCheckboxToggle:   (String) -> Unit,
    onCantidadChange:   (String, Int?) -> Unit,
    onMontoInicializar: (String) -> Unit = {},
    onSiguiente:        () -> Unit,
) {
    val actividadesUnion  = uiState.actividades.filter { it.nivel == NivelActividad.UNION }
    val actividadesPastor = uiState.actividades.filter { it.nivel == NivelActividad.PASTOR }
    val actividadesGP     = uiState.actividades.filter { it.nivel == NivelActividad.GP }

    val totalActividades = uiState.actividades.size
    val registradas = uiState.actividades.count { a ->
        when (a.tipoMarcador) {
            TipoMarcador.CHECKBOX      -> a.realizado != null
            TipoMarcador.MONETARIO     -> a.monto != null
            else                       -> a.cantidad != null
        }
    }

    val listState        = rememberLazyListState()
    val errorShakeOffset = remember { Animatable(0f) }

    // índices en LazyColumn: 0=TopBar, 1=Stepper, 2=Progress, 3=Spacer, 4=UNION, 5=Spacer, 6=PASTOR, 7=Spacer, 8=GP
    fun ActividadRegistro.tieneErrorNuevo() =
        !bloqueada && (
            (tipoMarcador == TipoMarcador.CONTADOR  && cantidad == null) ||
            (tipoMarcador == TipoMarcador.MONETARIO && monto    == null)
        )

    LaunchedEffect(uiState.errorActividadesObligatoriasTrigger) {
        if (uiState.errorActividadesObligatoriasTrigger > 0) {
            val targetIndex = when {
                actividadesUnion .any { it.tieneErrorNuevo() } -> 4
                actividadesPastor.any { it.tieneErrorNuevo() } -> 6
                actividadesGP    .any { it.tieneErrorNuevo() } -> 8
                else -> 6
            }
            listState.animateScrollToItem(targetIndex)
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
            item { ProgressSection(registradas = registradas, total = totalActividades) }
            item { Spacer(Modifier.height(16.dp)) }

            // ── UNIÓN ─────────────────────────────────────────────────────────
            item {
                SeccionActividades(
                    labelNivel          = stringResource(R.string.registro_nivel_union),
                    sectionIcon         = Icons.Filled.AccountBalance,
                    actividades         = actividadesUnion,
                    onActividadClick    = onActividadClick,
                    onCheckboxToggle    = onCheckboxToggle,
                    onCantidadChange    = onCantidadChange,
                    onMontoInicializar  = onMontoInicializar,
                    onAgregarExtra      = null,
                    hayError            = uiState.errorActividadesObligatorias,
                    shakeOffset         = errorShakeOffset.value,
                    modifier            = Modifier.padding(horizontal = 16.dp),
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            // ── PASTOR ────────────────────────────────────────────────────────
            item {
                SeccionActividades(
                    labelNivel          = stringResource(R.string.registro_nivel_pastor),
                    sectionIcon         = Icons.Filled.Star,
                    actividades         = actividadesPastor,
                    onActividadClick    = onActividadClick,
                    onCheckboxToggle    = onCheckboxToggle,
                    onCantidadChange    = onCantidadChange,
                    onMontoInicializar  = onMontoInicializar,
                    onAgregarExtra      = null,
                    hayError            = uiState.errorActividadesObligatorias,
                    shakeOffset         = errorShakeOffset.value,
                    modifier            = Modifier.padding(horizontal = 16.dp),
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            // ── MI GP ─────────────────────────────────────────────────────────
            item {
                SeccionActividades(
                    labelNivel          = stringResource(R.string.registro_nivel_mi_gp),
                    sectionIcon         = Icons.Filled.Group,
                    actividades         = actividadesGP,
                    onActividadClick    = onActividadClick,
                    onCheckboxToggle    = onCheckboxToggle,
                    onCantidadChange    = onCantidadChange,
                    onMontoInicializar  = onMontoInicializar,
                    onAgregarExtra      = if (uiState.registryKind == RegistryKind.SATURDAY_WORSHIP) null else onAgregarExtra,
                    hayError            = uiState.errorActividadesObligatorias,
                    shakeOffset         = errorShakeOffset.value,
                    modifier            = Modifier.padding(horizontal = 16.dp),
                )
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
                            if (completado) {
                                Icon(
                                    imageVector        = Icons.Default.Check,
                                    contentDescription = null,
                                    tint               = Color.White,
                                    modifier           = Modifier.size(16.dp),
                                )
                            } else {
                                Text(
                                    text       = "$numero",
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = if (activo) Color.White else Muted,
                                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
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
    labelNivel:         String,
    sectionIcon:        ImageVector? = null,
    actividades:        List<ActividadRegistro>,
    onActividadClick:   (String) -> Unit,
    onCheckboxToggle:   (String) -> Unit,
    onCantidadChange:   (String, Int?) -> Unit,
    onMontoInicializar: (String) -> Unit = {},
    onAgregarExtra:     (() -> Unit)?,
    hayError:           Boolean = false,
    shakeOffset:        Float   = 0f,
    modifier:           Modifier = Modifier,
) {
    if (actividades.isEmpty() && onAgregarExtra == null) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // ── Encabezado de sección ─────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth().padding(bottom = 2.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (sectionIcon != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .neuInsetSm(cornerRadius = 9.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Background),
                ) {
                    Icon(
                        imageVector        = sectionIcon,
                        contentDescription = null,
                        tint               = Mid,
                        modifier           = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                text       = labelNivel.uppercase(),
                style      = MaterialTheme.typography.labelSmall,
                color      = Mid,
                fontWeight = FontWeight.Bold,
            )
        }

        // Primera actividad editable sin marcar de esta sección (para mostrar el error)
        val primeraVaciaId = if (hayError) {
            actividades.firstOrNull { a ->
                !a.bloqueada && when (a.tipoMarcador) {
                    TipoMarcador.CONTADOR  -> a.cantidad == null
                    TipoMarcador.MONETARIO -> a.monto    == null
                    else                   -> false
                }
            }?.id
        } else null

        // ── Tarjeta individual por actividad ──────────────────────────────────
        actividades.forEach { actividad ->
            val mostrarError = primeraVaciaId != null && actividad.id == primeraVaciaId
            ActividadCard(
                actividad          = actividad,
                onClick            = { onActividadClick(actividad.id) },
                onCheckboxToggle   = { onCheckboxToggle(actividad.id) },
                onDecrement        = { val cur = actividad.cantidad; onCantidadChange(actividad.id, if (cur == null || cur <= 0) null else cur - 1) },
                onIncrement        = { val cur = actividad.cantidad ?: -1; onCantidadChange(actividad.id, cur + 1) },
                onMontoInicializar = { onMontoInicializar(actividad.id) },
                mostrarError       = mostrarError,
                shakeOffset        = if (mostrarError) shakeOffset else 0f,
            )
        }

        // ── Agregar actividad extra (solo GP) ─────────────────────────────────
        if (onAgregarExtra != null) {
            NeuCard(modifier = Modifier.fillMaxWidth()) {
                AgregarExtraRow(
                    onClick  = onAgregarExtra,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }
}

// ── Tarjeta individual de actividad ───────────────────────────────────────────

@Composable
private fun ActividadCard(
    actividad:          ActividadRegistro,
    onClick:            () -> Unit,
    onCheckboxToggle:   () -> Unit,
    onDecrement:        () -> Unit = {},
    onIncrement:        () -> Unit = {},
    onMontoInicializar: () -> Unit = {},
    mostrarError:       Boolean = false,
    shakeOffset:        Float   = 0f,
    modifier:           Modifier = Modifier,
) {
    val tappable = !actividad.bloqueada

    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(shakeOffset.roundToInt(), 0) },
    ) {
    NeuCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (tappable) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── TypeTile ──────────────────────────────────────────────────────
            val tileActive = when (actividad.tipoMarcador) {
                TipoMarcador.CHECKBOX      -> actividad.realizado == true
                TipoMarcador.MONETARIO     -> actividad.monto != null
                else                       -> actividad.cantidad != null
            }
            TypeTile(tipoMarcador = actividad.tipoMarcador, active = tileActive)

            Spacer(Modifier.width(12.dp))

            // ── Nombre + unidad + acumulado ───────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = actividad.nombre,
                        style      = MaterialTheme.typography.bodyLarge,
                        color      = if (actividad.bloqueada) Mid else Ink,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.weight(1f, fill = false),
                    )
                }
                if (actividad.unidad.isNotBlank()) {
                    Text(
                        text  = actividad.unidad,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
                val acumuladoText = when {
                    actividad.tipoMarcador == TipoMarcador.MONETARIO && actividad.montoAcumulado != null ->
                        "Acumulado: ₡${actividad.montoAcumulado.toLong()}"
                    (actividad.tipoMarcador == TipoMarcador.CONTADOR || actividad.tipoMarcador == TipoMarcador.PARTICIPANTES) && actividad.totalAcumulado != null ->
                        "Acumulado: ${actividad.totalAcumulado} ${actividad.unidad}"
                    else -> null
                }
                if (acumuladoText != null) {
                    Text(text = acumuladoText, style = MaterialTheme.typography.labelSmall, color = Muted)
                }
            }

            Spacer(Modifier.width(8.dp))

            // ── Control derecho ───────────────────────────────────────────────
            when (actividad.tipoMarcador) {
                TipoMarcador.CHECKBOX -> {
                    val realizado = actividad.realizado == true
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (realizado) Accent else BackgroundDeep),
                    ) {
                        if (realizado) Icon(
                            imageVector        = Icons.Default.Check,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(16.dp),
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    if (actividad.bloqueada) {
                        Icon(
                            imageVector        = Icons.Default.Lock,
                            contentDescription = null,
                            tint               = Muted,
                            modifier           = Modifier.size(16.dp),
                        )
                    } else {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint               = Accent,
                            modifier           = Modifier.size(20.dp),
                        )
                    }
                }
                else -> {
                    val showInlineCounter = !actividad.bloqueada &&
                        (actividad.tipoMarcador == TipoMarcador.CONTADOR || actividad.tipoMarcador == TipoMarcador.PARTICIPANTES)
                    val showInlineMonetario = !actividad.bloqueada &&
                        actividad.tipoMarcador == TipoMarcador.MONETARIO

                    when {
                        showInlineCounter -> {
                            val tieneValor = actividad.cantidad != null
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .neuInsetSm(cornerRadius = 11.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(Background)
                                        .clickable(onClick = onClick)
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                        .widthIn(min = 44.dp),
                                ) {
                                    Text(
                                        text       = actividad.cantidad?.toString() ?: "—",
                                        style      = MaterialTheme.typography.bodyLarge,
                                        color      = if (tieneValor) Accent else Muted,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign  = TextAlign.Center,
                                    )
                                }
                                if (!tieneValor) {
                                    Spacer(Modifier.width(4.dp))
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Accent)
                                            .clickable(onClick = onIncrement)
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                    ) {
                                        Text("+", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        showInlineMonetario -> {
                            val tieneValor = actividad.monto != null
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Chip monetario: hundido + borde dorado cuando registrado
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .then(
                                            if (tieneValor)
                                                Modifier
                                                    .neuInsetSm(cornerRadius = 11.dp)
                                                    .clip(RoundedCornerShape(11.dp))
                                                    .background(Background)
                                                    .drawWithContent {
                                                        drawContent()
                                                        val s = 1.5.dp.toPx()
                                                        drawRoundRect(
                                                            color        = Gold.copy(alpha = 0.6f),
                                                            topLeft      = androidx.compose.ui.geometry.Offset(s / 2, s / 2),
                                                            size         = Size(size.width - s, size.height - s),
                                                            cornerRadius = CornerRadius(11.dp.toPx()),
                                                            style        = Stroke(width = s),
                                                        )
                                                    }
                                            else
                                                Modifier
                                                    .neuElevatedSm(cornerRadius = 11.dp)
                                                    .clip(RoundedCornerShape(11.dp))
                                                    .background(Background)
                                        )
                                        .clickable(onClick = onClick)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .widthIn(min = 56.dp),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text       = "₡",
                                            style      = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize   = 16.sp,
                                            ),
                                            color      = if (tieneValor) Gold else Muted,
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text       = if (tieneValor) formatMonto(actividad.monto!!) else "0",
                                            style      = MaterialTheme.typography.labelSmall,
                                            color      = if (tieneValor) Ink else Muted,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                }
                                if (!tieneValor) {
                                    Spacer(Modifier.width(4.dp))
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Accent)
                                            .clickable(onClick = onMontoInicializar)
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                    ) {
                                        Text("+", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        else -> {
                            val valorText = actividad.cantidad?.toString() ?: "—"
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BackgroundDeep)
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    text       = valorText,
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = if (valorText == "—") Muted else Accent,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            if (actividad.bloqueada) {
                                Icon(Icons.Default.Lock, null, tint = Muted, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Accent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    if (mostrarError) {
        Text(
            text     = stringResource(R.string.registro_error_obligatorias),
            style    = MaterialTheme.typography.bodySmall,
            color    = Blush,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        )
    }
    } // Column
}


// ── Fila de miembro en desglose ───────────────────────────────────────────────

@Composable
internal fun MiembroDesgloseRow(
    miembro:      MiembroDesglose,
    maxAdicional: Int,
    sinLimite:    Boolean = false,
    onChange:     (Int) -> Unit,
    modifier:     Modifier = Modifier,
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NeuAvatar(iniciales = miembro.iniciales, size = 32.dp)

        Spacer(Modifier.width(10.dp))

        Text(
            text     = miembro.nombre,
            style    = MaterialTheme.typography.bodyMedium,
            color    = Ink,
            modifier = Modifier.weight(1f),
        )

        ContadorInline(
            valor            = miembro.cantidad,
            enabled          = sinLimite || miembro.cantidad > 0 || maxAdicional > 0,
            allowDirectInput = sinLimite,
            onChange         = { nuevo ->
                val v = nuevo ?: 0
                onChange(if (sinLimite) v.coerceAtLeast(0) else v.coerceIn(0, miembro.cantidad + maxAdicional))
            },
        )
    }
}

// ── Fila de miembro en desglose monetario ─────────────────────────────────────

@Composable
internal fun MiembroDesgloseMonetarioRow(
    miembro:   MiembroDesglose,
    maxMonto:  Double,
    sinLimite: Boolean = false,
    onChange:  (Double) -> Unit,
    modifier:  Modifier = Modifier,
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NeuAvatar(iniciales = miembro.iniciales, size = 32.dp)

        Spacer(Modifier.width(10.dp))

        Text(
            text     = miembro.nombre,
            style    = MaterialTheme.typography.bodyMedium,
            color    = Ink,
            modifier = Modifier.weight(1f),
        )

        MontoInline(
            monto    = miembro.montoDesglose.takeIf { it > 0 },
            enabled  = sinLimite || miembro.montoDesglose > 0 || maxMonto > 0,
            maxMonto = if (sinLimite) Double.MAX_VALUE / 2 else miembro.montoDesglose + maxMonto,
            onChange = { nuevoMonto ->
                val v = (nuevoMonto ?: 0.0).coerceAtLeast(0.0)
                onChange(if (sinLimite) v else v.coerceIn(0.0, miembro.montoDesglose + maxMonto))
            },
        )
    }
}

// ── Fila de miembro en desglose participantes ─────────────────────────────────

@Composable
internal fun MiembroParticipacionRow(
    miembro:   MiembroDesglose,
    bloqueado: Boolean,
    onChange:  (Boolean) -> Unit,
    modifier:  Modifier = Modifier,
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NeuAvatar(iniciales = miembro.iniciales, size = 32.dp)

        Spacer(Modifier.width(10.dp))

        Text(
            text     = miembro.nombre,
            style    = MaterialTheme.typography.bodyMedium,
            color    = Ink,
            modifier = Modifier.weight(1f),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .then(
                    when {
                        miembro.participo -> Modifier.background(Sage)
                        bloqueado         -> Modifier.background(BackgroundDeep)
                        else              -> Modifier
                            .background(Background)
                            .border(1.5.dp, Muted.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    }
                )
                .then(
                    if (!bloqueado || miembro.participo)
                        Modifier.clickable { onChange(!miembro.participo) }
                    else Modifier
                ),
        ) {
            if (miembro.participo) {
                Icon(
                    imageVector        = Icons.Filled.Check,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(16.dp),
                )
            }
        }
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

// ── Barra de progreso ─────────────────────────────────────────────────────────

@Composable
private fun ProgressSection(registradas: Int, total: Int) {
    if (total == 0) return
    val progress by animateFloatAsState(
        targetValue    = registradas.toFloat() / total.toFloat(),
        animationSpec  = tween(600),
        label          = "actividadesProgress",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 4.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = "PROGRESO",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Text(
                text       = "$registradas de $total registradas",
                style      = MaterialTheme.typography.bodyMedium,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress      = { progress },
            modifier      = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color         = Accent,
            trackColor    = Muted.copy(alpha = 0.2f),
        )
    }
}

// ── Formato monto ─────────────────────────────────────────────────────────────

private fun formatMonto(monto: Double): String =
    "%,d".format(monto.toLong()).replace(",", " ")

// ── TypeTile — ícono de tipo de actividad ─────────────────────────────────────

@Composable
private fun TypeTile(tipoMarcador: TipoMarcador, active: Boolean, size: Dp = 40.dp) {
    val activeColor = when (tipoMarcador) {
        TipoMarcador.MONETARIO     -> Gold
        TipoMarcador.CHECKBOX      -> Color(0xFF2EA86A)
        else                       -> Accent
    }
    val tileModifier = if (active) {
        Modifier
            .size(size)
            .clip(RoundedCornerShape(11.dp))
            .background(activeColor.copy(alpha = 0.13f))
    } else {
        Modifier
            .size(size)
            .neuInsetSm(cornerRadius = 11.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Background)
    }
    Box(contentAlignment = Alignment.Center, modifier = tileModifier) {
        when (tipoMarcador) {
            TipoMarcador.MONETARIO -> Text(
                text       = "₡",
                style      = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = (size.value * 0.5f).sp,
                ),
                color      = if (active) Gold else Muted,
            )
            TipoMarcador.CHECKBOX -> Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = null,
                tint               = if (active) Color(0xFF2EA86A) else Muted,
                modifier           = Modifier.size(size * 0.46f),
            )
            TipoMarcador.PARTICIPANTES -> Icon(
                imageVector        = Icons.Filled.Group,
                contentDescription = null,
                tint               = if (active) Accent else Muted,
                modifier           = Modifier.size(size * 0.46f),
            )
            TipoMarcador.CONTADOR -> Text(
                text       = "#",
                style      = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = (size.value * 0.36f).sp,
                ),
                color      = if (active) Accent else Muted,
            )
        }
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
            uiState           = paso2PreviewState,
            onNavigateBack    = {},
            onActividadClick  = {},
            onAgregarExtra    = {},
            onCheckboxToggle  = {},
            onCantidadChange  = { _, _ -> },
            onSiguiente       = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "Paso2 — error obligatorias")
@Composable
private fun Paso2ErrorPreview() {
    GpLeaderTheme {
        RegistroPaso2Content(
            uiState           = paso2PreviewState.copy(errorActividadesObligatorias = true),
            onNavigateBack    = {},
            onActividadClick  = {},
            onAgregarExtra    = {},
            onCheckboxToggle  = {},
            onCantidadChange  = { _, _ -> },
            onSiguiente       = {},
        )
    }
}
