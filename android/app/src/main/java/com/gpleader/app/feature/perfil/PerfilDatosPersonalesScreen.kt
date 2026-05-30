package com.gpleader.app.feature.perfil

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Shadow
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInset

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun PerfilDatosPersonalesScreen(
    onNavigateBack: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateDatosPersonalesBack) {
        if (uiState.navigateDatosPersonalesBack) {
            viewModel.consumeDatosPersonalesBackNavigation()
            onNavigateBack()
        }
    }

    DatosPersonalesContent(
        uiState                  = uiState,
        onNavigateBack           = onNavigateBack,
        onPrimerNombreChange     = viewModel::onPrimerNombreChange,
        onSegundoNombreChange    = viewModel::onSegundoNombreChange,
        onPrimerApellidoChange   = viewModel::onPrimerApellidoChange,
        onSegundoApellidoChange  = viewModel::onSegundoApellidoChange,
        onTelefonoChange         = viewModel::onTelefonoChange,
        onDireccionChange        = viewModel::onDireccionChange,
        onToggleSegundoNombre    = viewModel::onToggleSegundoNombre,
        onToggleSegundoApellido  = viewModel::onToggleSegundoApellido,
        onGuardar                = viewModel::onGuardarDatosPersonales,
        onAvatarClick            = viewModel::onEditarAvatarClick,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun DatosPersonalesContent(
    uiState:                 PerfilUiState,
    onNavigateBack:          () -> Unit,
    onPrimerNombreChange:    (String) -> Unit,
    onSegundoNombreChange:   (String) -> Unit,
    onPrimerApellidoChange:  (String) -> Unit,
    onSegundoApellidoChange: (String) -> Unit,
    onTelefonoChange:        (String) -> Unit,
    onDireccionChange:       (String) -> Unit,
    onToggleSegundoNombre:   () -> Unit,
    onToggleSegundoApellido: () -> Unit,
    onGuardar:               () -> Unit,
    onAvatarClick:           () -> Unit,
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            DatosPersonalesTopBar(
                onNavigateBack = onNavigateBack,
                onGuardar      = onGuardar,
                modifier       = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                NeuButtonPrimary(
                    text     = stringResource(R.string.datos_personales_btn_guardar_cambios),
                    onClick  = onGuardar,
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
            // ── Avatar section ────────────────────────────────────────────────
            item {
                AvatarEditSection(
                    iniciales     = uiState.iniciales,
                    onAvatarClick = onAvatarClick,
                )
            }

            // ── Sección NOMBRE ────────────────────────────────────────────────
            item {
                SeccionLabel(stringResource(R.string.datos_personales_seccion_nombre))
            }

            // Primera fila: primer nombre + primer apellido
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        NeuTextField(
                            value         = uiState.primerNombre,
                            onValueChange = onPrimerNombreChange,
                            label         = stringResource(R.string.datos_personales_label_primer_nombre),
                            placeholder   = "Maria",
                            isError       = uiState.primerNombreError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction    = ImeAction.Next,
                            ),
                        )
                        if (uiState.primerNombreError) {
                            Text(
                                text     = stringResource(R.string.datos_personales_error_primer_nombre),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Blush,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        NeuTextField(
                            value         = uiState.primerApellido,
                            onValueChange = onPrimerApellidoChange,
                            label         = stringResource(R.string.datos_personales_label_primer_apellido),
                            placeholder   = "Garcia",
                            isError       = uiState.primerApellidoError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction    = ImeAction.Next,
                            ),
                        )
                        if (uiState.primerApellidoError) {
                            Text(
                                text     = stringResource(R.string.datos_personales_error_primer_apellido),
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
                ExpandableOptionalField(
                    label    = stringResource(R.string.datos_personales_expandir_segundo_nombre),
                    expanded = uiState.segundoNombreExpandido,
                    onToggle = onToggleSegundoNombre,
                ) {
                    NeuTextField(
                        value         = uiState.segundoNombre,
                        onValueChange = onSegundoNombreChange,
                        label         = stringResource(R.string.datos_personales_label_segundo_nombre),
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
                ExpandableOptionalField(
                    label    = stringResource(R.string.datos_personales_expandir_segundo_apellido),
                    expanded = uiState.segundoApellidoExpandido,
                    onToggle = onToggleSegundoApellido,
                ) {
                    NeuTextField(
                        value         = uiState.segundoApellido,
                        onValueChange = onSegundoApellidoChange,
                        label         = stringResource(R.string.datos_personales_label_segundo_apellido),
                        placeholder   = "",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction    = ImeAction.Done,
                        ),
                    )
                }
            }

            // ── Sección CONTACTO ──────────────────────────────────────────────
            item {
                SeccionLabel(
                    text     = stringResource(R.string.datos_personales_seccion_contacto),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            // Teléfono con prefijo fijo
            item {
                NeuTextField(
                    value         = uiState.telefono,
                    onValueChange = onTelefonoChange,
                    label         = stringResource(R.string.datos_personales_label_telefono),
                    placeholder   = "8888-1234",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction    = ImeAction.Next,
                    ),
                    leadingContent = {
                        Text(
                            text  = stringResource(R.string.datos_personales_prefijo_telefono),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Muted,
                        )
                    },
                )
            }

            // Correo readonly
            item {
                Column {
                    NeuTextField(
                        value         = uiState.correo,
                        onValueChange = {},
                        label         = stringResource(R.string.datos_personales_label_correo),
                        readOnly      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    Text(
                        text     = stringResource(R.string.datos_personales_nota_correo),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = Muted,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    )
                }
            }

            // Dirección
            item {
                NeuTextField(
                    value         = uiState.direccion,
                    onValueChange = onDireccionChange,
                    label         = stringResource(R.string.datos_personales_label_direccion),
                    placeholder   = "San José, Costa Rica",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction    = ImeAction.Done,
                    ),
                )
            }

            // ── Sección IGLESIA (readonly) ─────────────────────────────────────
            item {
                SeccionLabel(
                    text     = stringResource(R.string.datos_personales_seccion_iglesia),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            item {
                Column {
                    // NeuCard hundida (neuInset) — readonly
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuInset(cornerRadius = 14.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Background)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Text(
                            text  = uiState.iglesia,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Mid,
                        )
                    }
                    Text(
                        text     = stringResource(R.string.datos_personales_nota_iglesia),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = Muted,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun DatosPersonalesTopBar(
    onNavigateBack: () -> Unit,
    onGuardar:      () -> Unit,
    modifier:       Modifier = Modifier,
) {
    Box(
        modifier          = modifier.fillMaxWidth(),
        contentAlignment  = Alignment.Center,
    ) {
        // Botón atrás (izquierda)
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

        // Título centrado
        Text(
            text  = stringResource(R.string.datos_personales_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )

        // Botón Guardar (derecha)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .neuElevatedSm(cornerRadius = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Background)
                .clickable(onClick = onGuardar)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text  = stringResource(R.string.datos_personales_btn_guardar),
                style = MaterialTheme.typography.labelSmall,
                color = Accent,
            )
        }
    }
}

// ── Avatar section ────────────────────────────────────────────────────────────

@Composable
private fun AvatarEditSection(
    iniciales:     String,
    onAvatarClick: () -> Unit,
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Avatar con badge de cámara
        Box(
            modifier = Modifier.clickable(onClick = onAvatarClick),
        ) {
            // Avatar cuadrado
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Ink),
            ) {
                Text(
                    text  = iniciales,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
            }

            // Badge cámara — esquina inferior-derecha
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(26.dp)
                    .neuElevatedSm(cornerRadius = 13.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Background),
            ) {
                Icon(
                    imageVector        = Icons.Filled.Edit,
                    contentDescription = null,
                    tint               = Muted,
                    modifier           = Modifier.size(12.dp),
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text  = stringResource(R.string.datos_personales_foto_hint),
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
    }
}

// ── Sección label ─────────────────────────────────────────────────────────────

@Composable
private fun SeccionLabel(
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

// ── Expandable optional field ─────────────────────────────────────────────────

@Composable
private fun ExpandableOptionalField(
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
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(dashPx, gapPx), 0f,
                        ),
                    ),
                )
            }
            .animateContentSize(animationSpec = spring()),
    ) {
        Column {
            // Fila encabezado — siempre visible
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

            // Campo — solo visible cuando expandido
            if (expanded) {
                HorizontalDivider(color = Shadow, thickness = 1.dp)
                Box(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun DatosPersonalesPreview() {
    GpLeaderTheme {
        DatosPersonalesContent(
            uiState = PerfilUiState(
                primerNombre            = "Maria",
                primerApellido          = "Garcia",
                telefono                = "8888-1234",
                correo                  = "maria.garcia@gmail.com",
                direccion               = "San José, Costa Rica",
                iglesia                 = "Iglesia Central · San José",
                segundoNombreExpandido  = false,
                segundoApellidoExpandido = false,
            ),
            onNavigateBack           = {},
            onPrimerNombreChange     = {},
            onSegundoNombreChange    = {},
            onPrimerApellidoChange   = {},
            onSegundoApellidoChange  = {},
            onTelefonoChange         = {},
            onDireccionChange        = {},
            onToggleSegundoNombre    = {},
            onToggleSegundoApellido  = {},
            onGuardar                = {},
            onAvatarClick            = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true, name = "Expandido + errores")
@Composable
private fun DatosPersonalesExpandidoPreview() {
    GpLeaderTheme {
        DatosPersonalesContent(
            uiState = PerfilUiState(
                primerNombre             = "",
                primerApellido           = "",
                primerNombreError        = true,
                primerApellidoError      = true,
                telefono                 = "8888-1234",
                correo                   = "maria.garcia@gmail.com",
                direccion                = "San José, Costa Rica",
                iglesia                  = "Iglesia Central · San José",
                segundoNombreExpandido   = true,
                segundoApellidoExpandido = false,
            ),
            onNavigateBack           = {},
            onPrimerNombreChange     = {},
            onSegundoNombreChange    = {},
            onPrimerApellidoChange   = {},
            onSegundoApellidoChange  = {},
            onTelefonoChange         = {},
            onDireccionChange        = {},
            onToggleSegundoNombre    = {},
            onToggleSegundoApellido  = {},
            onGuardar                = {},
            onAvatarClick            = {},
        )
    }
}
