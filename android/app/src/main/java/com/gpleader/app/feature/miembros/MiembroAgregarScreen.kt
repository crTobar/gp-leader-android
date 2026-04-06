package com.gpleader.app.feature.miembros

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.Shadow
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun MiembroAgregarScreen(
    onNavigateBack: () -> Unit,
    viewModel: MiembrosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateAgregarBack) {
        if (uiState.navigateAgregarBack) {
            viewModel.consumeNavigateAgregarBack()
            onNavigateBack()
        }
    }

    AgregarContent(
        uiState                  = uiState,
        onNavigateBack           = onNavigateBack,
        onPrimerNombreChange     = viewModel::onAgregarPrimerNombreChange,
        onSegundoNombreChange    = viewModel::onAgregarSegundoNombreChange,
        onPrimerApellidoChange   = viewModel::onAgregarPrimerApellidoChange,
        onSegundoApellidoChange  = viewModel::onAgregarSegundoApellidoChange,
        onTelefonoChange         = viewModel::onAgregarTelefonoChange,
        onCorreoChange           = viewModel::onAgregarCorreoChange,
        onDireccionChange        = viewModel::onAgregarDireccionChange,
        onToggleSegundoNombre    = viewModel::onToggleAgregarSegundoNombre,
        onToggleSegundoApellido  = viewModel::onToggleAgregarSegundoApellido,
        onAgregar                = viewModel::onAgregarMiembro,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun AgregarContent(
    uiState:                 MiembrosUiState,
    onNavigateBack:          () -> Unit,
    onPrimerNombreChange:    (String) -> Unit,
    onSegundoNombreChange:   (String) -> Unit,
    onPrimerApellidoChange:  (String) -> Unit,
    onSegundoApellidoChange: (String) -> Unit,
    onTelefonoChange:        (String) -> Unit,
    onCorreoChange:          (String) -> Unit,
    onDireccionChange:       (String) -> Unit,
    onToggleSegundoNombre:   () -> Unit,
    onToggleSegundoApellido: () -> Unit,
    onAgregar:               () -> Unit,
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            AgregarTopBar(
                onNavigateBack = onNavigateBack,
                modifier       = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                NeuButtonPrimary(
                    text     = stringResource(R.string.agregar_miembro_btn_agregar),
                    onClick  = onAgregar,
                    modifier = Modifier.fillMaxWidth(),
                )
                NeuButtonSecondary(
                    text     = stringResource(R.string.agregar_miembro_btn_cancelar),
                    onClick  = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Avatar placeholder ────────────────────────────────────────────
            item {
                AvatarPreviewSection(iniciales = uiState.agregarInicialesPreview)
            }

            // ── SECCIÓN NOMBRE ────────────────────────────────────────────────
            item {
                AgregarSeccionLabel(stringResource(R.string.agregar_miembro_seccion_nombre))
            }

            // Primer nombre + primer apellido en fila
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        NeuTextField(
                            value         = uiState.agregarPrimerNombre,
                            onValueChange = onPrimerNombreChange,
                            label         = stringResource(R.string.agregar_miembro_label_primer_nombre),
                            placeholder   = "Maria",
                            isError       = uiState.agregarPrimerNombreError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction    = ImeAction.Next,
                            ),
                        )
                        if (uiState.agregarPrimerNombreError) {
                            Text(
                                text     = stringResource(R.string.agregar_miembro_error_primer_nombre),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Blush,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        NeuTextField(
                            value         = uiState.agregarPrimerApellido,
                            onValueChange = onPrimerApellidoChange,
                            label         = stringResource(R.string.agregar_miembro_label_primer_apellido),
                            placeholder   = "López",
                            isError       = uiState.agregarPrimerApellidoError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction    = ImeAction.Next,
                            ),
                        )
                        if (uiState.agregarPrimerApellidoError) {
                            Text(
                                text     = stringResource(R.string.agregar_miembro_error_primer_apellido),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Blush,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                            )
                        }
                    }
                }
            }

            // Expandible segundo nombre
            item {
                AgregarExpandableField(
                    label    = stringResource(R.string.agregar_miembro_expandir_segundo_nombre),
                    expanded = uiState.agregarSegundoNombreExpandido,
                    onToggle = onToggleSegundoNombre,
                ) {
                    NeuTextField(
                        value         = uiState.agregarSegundoNombre,
                        onValueChange = onSegundoNombreChange,
                        label         = stringResource(R.string.agregar_miembro_label_segundo_nombre),
                        placeholder   = "",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction    = ImeAction.Next,
                        ),
                    )
                }
            }

            // Expandible segundo apellido
            item {
                AgregarExpandableField(
                    label    = stringResource(R.string.agregar_miembro_expandir_segundo_apellido),
                    expanded = uiState.agregarSegundoApellidoExpandido,
                    onToggle = onToggleSegundoApellido,
                ) {
                    NeuTextField(
                        value         = uiState.agregarSegundoApellido,
                        onValueChange = onSegundoApellidoChange,
                        label         = stringResource(R.string.agregar_miembro_label_segundo_apellido),
                        placeholder   = "",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction    = ImeAction.Done,
                        ),
                    )
                }
            }

            // Nota: siempre Activo
            item {
                NotaActivoCard()
            }

            // ── SECCIÓN CONTACTO ──────────────────────────────────────────────
            item {
                AgregarSeccionLabel(
                    text     = stringResource(R.string.agregar_miembro_seccion_contacto),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            // Teléfono
            item {
                NeuTextField(
                    value         = uiState.agregarTelefono,
                    onValueChange = onTelefonoChange,
                    label         = stringResource(R.string.agregar_miembro_label_telefono),
                    placeholder   = stringResource(R.string.agregar_miembro_placeholder_telefono),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction    = ImeAction.Next,
                    ),
                    leadingContent = {
                        Text(
                            text  = stringResource(R.string.agregar_miembro_prefijo_telefono),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Muted,
                        )
                    },
                )
            }

            // Correo
            item {
                NeuTextField(
                    value         = uiState.agregarCorreo,
                    onValueChange = onCorreoChange,
                    label         = stringResource(R.string.agregar_miembro_label_correo),
                    placeholder   = stringResource(R.string.agregar_miembro_placeholder_correo),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next,
                    ),
                )
            }

            // Dirección
            item {
                NeuTextField(
                    value         = uiState.agregarDireccion,
                    onValueChange = onDireccionChange,
                    label         = stringResource(R.string.agregar_miembro_label_direccion),
                    placeholder   = stringResource(R.string.agregar_miembro_placeholder_direccion),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction    = ImeAction.Done,
                    ),
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun AgregarTopBar(
    onNavigateBack: () -> Unit,
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
            text  = stringResource(R.string.agregar_miembro_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )
    }
}

// ── Avatar preview ────────────────────────────────────────────────────────────

@Composable
private fun AvatarPreviewSection(iniciales: String) {
    val hasIniciales = iniciales.isNotEmpty()
    val bgColor by animateColorAsState(
        targetValue   = if (hasIniciales) Ink else Color.Transparent,
        animationSpec = spring(),
        label         = "avatarBg",
    )
    val dashedAlpha by animateFloatAsState(
        targetValue   = if (hasIniciales) 0f else 1f,
        animationSpec = spring(),
        label         = "dashedAlpha",
    )

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .drawBehind {
                    val strokePx = 1.5.dp.toPx()
                    val dashPx   = 8.dp.toPx()
                    val gapPx    = 5.dp.toPx()
                    drawCircle(
                        color  = Muted.copy(alpha = dashedAlpha),
                        radius = size.minDimension / 2f - strokePx / 2f,
                        style  = Stroke(
                            width      = strokePx,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f),
                        ),
                    )
                }
                .clip(CircleShape)
                .background(bgColor),
        ) {
            AnimatedContent(
                targetState   = hasIniciales,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label         = "avatarContent",
            ) { showIniciales ->
                if (showIniciales) {
                    Text(
                        text      = iniciales,
                        style     = MaterialTheme.typography.headlineMedium,
                        color     = Color.White,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Filled.Add,
                        contentDescription = null,
                        tint               = Muted,
                        modifier           = Modifier.size(28.dp),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !hasIniciales,
            enter   = fadeIn(),
            exit    = fadeOut(),
        ) {
            Text(
                text      = stringResource(R.string.agregar_miembro_avatar_hint),
                style     = MaterialTheme.typography.labelSmall,
                color     = Muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Sección label ─────────────────────────────────────────────────────────────

@Composable
private fun AgregarSeccionLabel(
    text:     String,
    modifier: Modifier = Modifier,
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelSmall,
        color    = Muted,
        modifier = modifier.padding(bottom = 4.dp),
    )
}

// ── Expandable field (dashed border + animateContentSize) ─────────────────────

@Composable
private fun AgregarExpandableField(
    label:    String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content:  @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Background)
            .drawWithContent {
                drawContent()
                val strokePx = 1.2.dp.toPx()
                val dashPx   = 10.dp.toPx()
                val gapPx    = 6.dp.toPx()
                drawRoundRect(
                    color        = Shadow,
                    topLeft      = Offset(strokePx / 2, strokePx / 2),
                    size         = Size(size.width - strokePx, size.height - strokePx),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style        = Stroke(
                        width      = strokePx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f),
                    ),
                )
            }
            .animateContentSize(animationSpec = spring()),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector        = if (expanded)
                        Icons.Filled.KeyboardArrowDown
                    else
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint               = Muted,
                    modifier           = Modifier.size(18.dp),
                )
                Text(
                    text  = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }

            if (expanded) {
                HorizontalDivider(color = Shadow, thickness = 1.dp)
                Box(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

// ── Nota: miembro siempre Activo ──────────────────────────────────────────────

@Composable
private fun NotaActivoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Background)
            .drawWithContent {
                drawContent()
                val strokePx = 1.2.dp.toPx()
                val dashPx   = 8.dp.toPx()
                val gapPx    = 5.dp.toPx()
                drawRoundRect(
                    color        = Sage.copy(alpha = 0.4f),
                    topLeft      = Offset(strokePx / 2, strokePx / 2),
                    size         = Size(size.width - strokePx, size.height - strokePx),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style        = Stroke(
                        width      = strokePx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f),
                    ),
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Sage),
            )
            Text(
                text  = stringResource(R.string.agregar_miembro_nota_activo),
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun AgregarVacioPreview() {
    GpLeaderTheme {
        AgregarContent(
            uiState                  = MiembrosUiState(),
            onNavigateBack           = {},
            onPrimerNombreChange     = {},
            onSegundoNombreChange    = {},
            onPrimerApellidoChange   = {},
            onSegundoApellidoChange  = {},
            onTelefonoChange         = {},
            onCorreoChange           = {},
            onDireccionChange        = {},
            onToggleSegundoNombre    = {},
            onToggleSegundoApellido  = {},
            onAgregar                = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true, name = "Con datos — Maria Lopez")
@Composable
private fun AgregarConDatosPreview() {
    GpLeaderTheme {
        AgregarContent(
            uiState = MiembrosUiState(
                agregarPrimerNombre   = "Maria",
                agregarPrimerApellido = "Lopez",
                agregarTelefono       = "8888-1234",
                agregarCorreo         = "maria.lopez@gmail.com",
                agregarDireccion      = "San José, Costa Rica",
            ),
            onNavigateBack           = {},
            onPrimerNombreChange     = {},
            onSegundoNombreChange    = {},
            onPrimerApellidoChange   = {},
            onSegundoApellidoChange  = {},
            onTelefonoChange         = {},
            onCorreoChange           = {},
            onDireccionChange        = {},
            onToggleSegundoNombre    = {},
            onToggleSegundoApellido  = {},
            onAgregar                = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true, name = "Con errores")
@Composable
private fun AgregarErroresPreview() {
    GpLeaderTheme {
        AgregarContent(
            uiState = MiembrosUiState(
                agregarPrimerNombreError   = true,
                agregarPrimerApellidoError = true,
            ),
            onNavigateBack           = {},
            onPrimerNombreChange     = {},
            onSegundoNombreChange    = {},
            onPrimerApellidoChange   = {},
            onSegundoApellidoChange  = {},
            onTelefonoChange         = {},
            onCorreoChange           = {},
            onDireccionChange        = {},
            onToggleSegundoNombre    = {},
            onToggleSegundoApellido  = {},
            onAgregar                = {},
        )
    }
}
