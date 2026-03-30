package com.gpleader.app.feature.miembros

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.font.FontWeight
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
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.Shadow
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInset
import com.gpleader.app.core.ui.theme.neuInsetSm

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun MiembroEditarScreen(
    onNavigateBack: () -> Unit,
    viewModel: MiembrosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateEditarBack) {
        if (uiState.navigateEditarBack) {
            viewModel.consumeNavigateEditarBack()
            onNavigateBack()
        }
    }

    val miembro = uiState.miembroSeleccionado ?: return

    EditarContent(
        uiState                  = uiState,
        historial                = miembro.historial,
        onNavigateBack           = onNavigateBack,
        onPrimerNombreChange     = viewModel::onEditPrimerNombreChange,
        onSegundoNombreChange    = viewModel::onEditSegundoNombreChange,
        onPrimerApellidoChange   = viewModel::onEditPrimerApellidoChange,
        onSegundoApellidoChange  = viewModel::onEditSegundoApellidoChange,
        onTelefonoChange         = viewModel::onEditTelefonoChange,
        onCorreoChange           = viewModel::onEditCorreoChange,
        onDireccionChange        = viewModel::onEditDireccionChange,
        onToggleSegundoNombre    = viewModel::onToggleEditSegundoNombre,
        onToggleSegundoApellido  = viewModel::onToggleEditSegundoApellido,
        onToggleEstado           = viewModel::onToggleEstado,
        onGuardar                = viewModel::onGuardarEdicion,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun EditarContent(
    uiState:                 MiembrosUiState,
    historial:               List<AsistenciaResumen>,
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
    onToggleEstado:          () -> Unit,
    onGuardar:               () -> Unit,
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            EditarTopBar(
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
                    text     = stringResource(R.string.miembros_btn_guardar_cambios),
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
            // ── NOMBRE ────────────────────────────────────────────────────────
            item {
                EditarSeccionLabel(stringResource(R.string.miembros_seccion_nombre))
            }

            // Primer nombre + primer apellido en row
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        NeuTextField(
                            value         = uiState.editPrimerNombre,
                            onValueChange = onPrimerNombreChange,
                            label         = stringResource(R.string.miembros_label_primer_nombre),
                            placeholder   = "Maria",
                            isError       = uiState.editPrimerNombreError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction    = ImeAction.Next,
                            ),
                        )
                        if (uiState.editPrimerNombreError) {
                            Text(
                                text     = stringResource(R.string.miembros_error_primer_nombre),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Blush,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        NeuTextField(
                            value         = uiState.editPrimerApellido,
                            onValueChange = onPrimerApellidoChange,
                            label         = stringResource(R.string.miembros_label_primer_apellido),
                            placeholder   = "Garcia",
                            isError       = uiState.editPrimerApellidoError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction    = ImeAction.Next,
                            ),
                        )
                        if (uiState.editPrimerApellidoError) {
                            Text(
                                text     = stringResource(R.string.miembros_error_primer_apellido),
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
                ExpandableField(
                    label    = stringResource(R.string.miembros_expandir_segundo_nombre),
                    expanded = uiState.editSegundoNombreExpandido,
                    onToggle = onToggleSegundoNombre,
                ) {
                    NeuTextField(
                        value         = uiState.editSegundoNombre,
                        onValueChange = onSegundoNombreChange,
                        label         = stringResource(R.string.miembros_label_segundo_nombre_expanded),
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
                ExpandableField(
                    label    = stringResource(R.string.miembros_expandir_segundo_apellido),
                    expanded = uiState.editSegundoApellidoExpandido,
                    onToggle = onToggleSegundoApellido,
                ) {
                    NeuTextField(
                        value         = uiState.editSegundoApellido,
                        onValueChange = onSegundoApellidoChange,
                        label         = stringResource(R.string.miembros_label_segundo_apellido_expanded),
                        placeholder   = "",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction    = ImeAction.Done,
                        ),
                    )
                }
            }

            // ── CONTACTO ──────────────────────────────────────────────────────
            item {
                EditarSeccionLabel(
                    text     = stringResource(R.string.miembros_seccion_contacto),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            // Teléfono con prefijo
            item {
                NeuTextField(
                    value         = uiState.editTelefono,
                    onValueChange = onTelefonoChange,
                    label         = stringResource(R.string.miembros_label_telefono),
                    placeholder   = "8888-1234",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction    = ImeAction.Next,
                    ),
                    leadingContent = {
                        Text(
                            text  = stringResource(R.string.miembros_prefijo_telefono),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Muted,
                        )
                    },
                )
            }

            // Correo
            item {
                NeuTextField(
                    value         = uiState.editCorreo,
                    onValueChange = onCorreoChange,
                    label         = stringResource(R.string.miembros_label_correo),
                    placeholder   = "correo@ejemplo.com",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next,
                    ),
                )
            }

            // Dirección
            item {
                NeuTextField(
                    value         = uiState.editDireccion,
                    onValueChange = onDireccionChange,
                    label         = stringResource(R.string.miembros_label_direccion),
                    placeholder   = "San José, Costa Rica",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction    = ImeAction.Done,
                    ),
                )
            }

            // ── ESTADO ────────────────────────────────────────────────────────
            item {
                EditarSeccionLabel(
                    text     = stringResource(R.string.miembros_seccion_estado),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            item {
                EstadoToggle(
                    estado    = uiState.editEstado,
                    onToggle  = onToggleEstado,
                )
            }

            // ── HISTORIAL (readonly) ───────────────────────────────────────────
            if (historial.isNotEmpty()) {
                item {
                    EditarSeccionLabel(
                        text     = stringResource(R.string.miembros_historial_titulo),
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                item {
                    HistorialReadonlyCard(historial = historial)
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun EditarTopBar(
    onNavigateBack: () -> Unit,
    onGuardar:      () -> Unit,
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
            text  = stringResource(R.string.miembros_editar_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )

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
                text  = stringResource(R.string.miembros_btn_guardar),
                style = MaterialTheme.typography.labelSmall,
                color = Accent,
            )
        }
    }
}

// ── Sección label ─────────────────────────────────────────────────────────────

@Composable
private fun EditarSeccionLabel(
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
private fun ExpandableField(
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

// ── Estado toggle ─────────────────────────────────────────────────────────────

@Composable
private fun EstadoToggle(
    estado:   EstadoMiembro,
    onToggle: () -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ACTIVO
        val activoActive = estado == EstadoMiembro.ACTIVO
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (activoActive) Modifier.neuInset(cornerRadius = 14.dp)
                    else Modifier.neuElevated(cornerRadius = 14.dp)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(Background)
                .clickable(onClick = { if (!activoActive) onToggle() })
                .padding(vertical = 14.dp),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (activoActive) Sage else Muted),
                )
                Text(
                    text  = stringResource(R.string.miembros_estado_activo),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (activoActive) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = if (activoActive) Ink else Muted,
                )
            }
        }

        // ARCHIVADO
        val archivadoActive = estado == EstadoMiembro.ARCHIVADO
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (archivadoActive) Modifier.neuInset(cornerRadius = 14.dp)
                    else Modifier.neuElevated(cornerRadius = 14.dp)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(Background)
                .clickable(onClick = { if (!archivadoActive) onToggle() })
                .padding(vertical = 14.dp),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (archivadoActive) Muted else Muted),
                )
                Text(
                    text  = stringResource(R.string.miembros_estado_archivado),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (archivadoActive) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = if (archivadoActive) Ink else Muted,
                )
            }
        }
    }
}

// ── Historial readonly card ───────────────────────────────────────────────────

@Composable
private fun HistorialReadonlyCard(historial: List<AsistenciaResumen>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Background)
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            historial.forEach { registro ->
                val (badgeBg) = when (registro.estado) {
                    "P"  -> listOf(Sage)
                    "A"  -> listOf(Blush)
                    else -> listOf(Muted)
                }
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text     = registro.fecha,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = Mid,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg),
                    ) {
                        Text(
                            text  = registro.estado,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun EditarPreview() {
    GpLeaderTheme {
        EditarContent(
            uiState = MiembrosUiState(
                editPrimerNombre   = "Carlos",
                editPrimerApellido = "Ramírez",
                editTelefono       = "8812-3456",
                editCorreo         = "carlos.ramirez@gmail.com",
                editDireccion      = "San José, Costa Rica",
                editEstado         = EstadoMiembro.ACTIVO,
            ),
            historial = listOf(
                AsistenciaResumen("12 Mar", "P"),
                AsistenciaResumen("05 Mar", "A"),
                AsistenciaResumen("26 Feb", "P"),
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
            onToggleEstado           = {},
            onGuardar                = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true, name = "Archivado + errores")
@Composable
private fun EditarArchivadoPreview() {
    GpLeaderTheme {
        EditarContent(
            uiState = MiembrosUiState(
                editPrimerNombre        = "",
                editPrimerApellido      = "",
                editPrimerNombreError   = true,
                editPrimerApellidoError = true,
                editEstado              = EstadoMiembro.ARCHIVADO,
                editSegundoNombreExpandido = true,
            ),
            historial                = emptyList(),
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
            onToggleEstado           = {},
            onGuardar                = {},
        )
    }
}
