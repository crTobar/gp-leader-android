package com.gpleader.app.feature.perfil

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInset

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun PerfilDatosGrupoScreen(
    onNavigateBack: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateDatosGrupoBack) {
        if (uiState.navigateDatosGrupoBack) {
            viewModel.consumeDatosGrupoBackNavigation()
            onNavigateBack()
        }
    }

    DatosGrupoContent(
        uiState                  = uiState,
        onNavigateBack           = onNavigateBack,
        onNombreGrupoChange      = viewModel::onNombreGrupoChange,
        onDescripcionChange      = viewModel::onDescripcionChange,
        onLugarReunionChange     = viewModel::onLugarReunionChange,
        onCantoFavoritoChange    = viewModel::onCantoFavoritoChange,
        onVersiculoChange        = viewModel::onVersiculoChange,
        onPersonajeBiblicoChange = viewModel::onPersonajeBiblicoChange,
        onDiaSemanaChange        = viewModel::onDiaSemanaChange,
        onHoraInicioChange       = viewModel::onHoraInicioChange,
        onHoraFinChange          = viewModel::onHoraFinChange,
        onToggleDiaDropdown      = viewModel::onToggleDiaDropdown,
        onToggleHoraInicio       = viewModel::onToggleHoraInicioDropdown,
        onToggleHoraFin          = viewModel::onToggleHoraFinDropdown,
        onGuardar                = viewModel::onGuardarDatosGrupo,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun DatosGrupoContent(
    uiState:                  PerfilUiState,
    onNavigateBack:           () -> Unit,
    onNombreGrupoChange:      (String) -> Unit,
    onDescripcionChange:      (String) -> Unit,
    onLugarReunionChange:     (String) -> Unit,
    onCantoFavoritoChange:    (String) -> Unit,
    onVersiculoChange:        (String) -> Unit,
    onPersonajeBiblicoChange: (String) -> Unit,
    onDiaSemanaChange:        (String) -> Unit,
    onHoraInicioChange:       (String) -> Unit,
    onHoraFinChange:          (String) -> Unit,
    onToggleDiaDropdown:      () -> Unit,
    onToggleHoraInicio:       () -> Unit,
    onToggleHoraFin:          () -> Unit,
    onGuardar:                () -> Unit,
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            DatosGrupoTopBar(
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
                    text     = stringResource(R.string.datos_grupo_btn_guardar_cambios),
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
            // ── IDENTIFICACIÓN ────────────────────────────────────────────────
            item {
                GrupoSeccionLabel(stringResource(R.string.datos_grupo_seccion_identificacion))
            }

            // Nombre del grupo
            item {
                Column {
                    NeuTextField(
                        value         = uiState.nombreGrupo,
                        onValueChange = onNombreGrupoChange,
                        label         = stringResource(R.string.datos_grupo_label_nombre),
                        placeholder   = "GP Los Olivos",
                        isError       = uiState.nombreGrupoError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction    = ImeAction.Next,
                        ),
                    )
                    if (uiState.nombreGrupoError) {
                        Text(
                            text     = stringResource(R.string.datos_grupo_error_nombre),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = Blush,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                        )
                    }
                }
            }

            // Descripción
            item {
                NeuTextField(
                    value         = uiState.descripcion,
                    onValueChange = onDescripcionChange,
                    label         = stringResource(R.string.datos_grupo_label_descripcion),
                    placeholder   = stringResource(R.string.datos_grupo_placeholder_descripcion),
                    singleLine    = false,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction    = ImeAction.Next,
                    ),
                )
            }

            // Lugar de reunión
            item {
                NeuTextField(
                    value         = uiState.lugarReunion,
                    onValueChange = onLugarReunionChange,
                    label         = stringResource(R.string.datos_grupo_label_lugar),
                    placeholder   = stringResource(R.string.datos_grupo_placeholder_lugar),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction    = ImeAction.Next,
                    ),
                )
            }

            // Canto favorito
            item {
                NeuTextField(
                    value         = uiState.cantoFavorito,
                    onValueChange = onCantoFavoritoChange,
                    label         = stringResource(R.string.datos_grupo_label_canto),
                    placeholder   = stringResource(R.string.datos_grupo_placeholder_canto),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction    = ImeAction.Next,
                    ),
                )
            }

            // Versículo favorito
            item {
                NeuTextField(
                    value         = uiState.versiculo,
                    onValueChange = onVersiculoChange,
                    label         = stringResource(R.string.datos_grupo_label_versiculo),
                    placeholder   = stringResource(R.string.datos_grupo_placeholder_versiculo),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction    = ImeAction.Next,
                    ),
                )
            }

            // Personaje bíblico
            item {
                NeuTextField(
                    value         = uiState.personajeBiblico,
                    onValueChange = onPersonajeBiblicoChange,
                    label         = stringResource(R.string.datos_grupo_label_personaje),
                    placeholder   = stringResource(R.string.datos_grupo_placeholder_personaje),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction    = ImeAction.Done,
                    ),
                )
            }

            // ── UBICACIÓN ECLESIÁSTICA (readonly) ──────────────────────────────
            item {
                GrupoSeccionLabel(
                    text     = stringResource(R.string.datos_grupo_seccion_ubicacion),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReadonlyField(
                        label = stringResource(R.string.datos_grupo_label_iglesia),
                        value = uiState.iglesia,
                    )
                    ReadonlyField(
                        label = stringResource(R.string.datos_grupo_label_campo),
                        value = uiState.campo,
                    )
                    ReadonlyField(
                        label = stringResource(R.string.datos_grupo_label_distrito),
                        value = uiState.distrito,
                    )
                }
            }

            item {
                Text(
                    text     = stringResource(R.string.datos_grupo_nota_ubicacion),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Muted,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            // ── HORARIO ────────────────────────────────────────────────────────
            item {
                GrupoSeccionLabel(
                    text     = stringResource(R.string.datos_grupo_seccion_horario),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            // Día de la semana — ancho completo
            item {
                HorarioDropdownField(
                    label       = stringResource(R.string.datos_grupo_label_dia),
                    selected    = uiState.diaSemana.display,
                    opciones    = uiState.diasSemanaOpciones,
                    expanded    = uiState.showDiaDropdown,
                    onToggle    = onToggleDiaDropdown,
                    onSelect    = onDiaSemanaChange,
                    modifier    = Modifier.fillMaxWidth(),
                )
            }

            // Hora inicio + Hora fin — en row
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HorarioDropdownField(
                        label    = stringResource(R.string.datos_grupo_label_hora_inicio),
                        selected = uiState.horaInicio,
                        opciones = uiState.horasOpciones,
                        expanded = uiState.showHoraInicioDropdown,
                        onToggle = onToggleHoraInicio,
                        onSelect = onHoraInicioChange,
                        modifier = Modifier.weight(1f),
                    )
                    HorarioDropdownField(
                        label    = stringResource(R.string.datos_grupo_label_hora_fin),
                        selected = uiState.horaFin,
                        opciones = uiState.horasOpciones,
                        expanded = uiState.showHoraFinDropdown,
                        onToggle = onToggleHoraFin,
                        onSelect = onHoraFinChange,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun DatosGrupoTopBar(
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
            text  = stringResource(R.string.datos_grupo_titulo),
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
                text  = stringResource(R.string.datos_grupo_btn_guardar),
                style = MaterialTheme.typography.labelSmall,
                color = Accent,
            )
        }
    }
}

// ── Sección label ─────────────────────────────────────────────────────────────

@Composable
private fun GrupoSeccionLabel(
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

// ── Readonly field (neuInset + BackgroundDeep) ────────────────────────────────

@Composable
private fun ReadonlyField(
    label: String,
    value: String,
) {
    Column {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neuInset(cornerRadius = 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Background)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Mid,
            )
        }
    }
}

// ── Horario dropdown field ────────────────────────────────────────────────────

@Composable
private fun HorarioDropdownField(
    label:    String,
    selected: String,
    opciones: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Box {
            // Campo selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuElevated(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Background)
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text  = selected,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector        = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint               = Muted,
                    modifier           = Modifier.size(18.dp),
                )
            }

            // Dropdown
            DropdownMenu(
                expanded         = expanded,
                onDismissRequest = onToggle,
                modifier         = Modifier.background(Background),
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text   = {
                            Text(
                                text  = opcion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (opcion == selected) Accent else Ink,
                            )
                        },
                        onClick = { onSelect(opcion) },
                    )
                    if (opcion != opciones.last()) {
                        HorizontalDivider(color = Background, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun DatosGrupoPreview() {
    GpLeaderTheme {
        DatosGrupoContent(
            uiState = PerfilUiState(
                nombreGrupo    = "GP Los Olivos",
                descripcion    = "Grupo jóvenes adultos zona norte",
                lugarReunion   = "Casa de Maria Garcia, San José",
                cantoFavorito  = "",
                versiculo      = "",
                personajeBiblico = "",
                iglesia        = "Iglesia Central · San José",
                campo          = "Asociación Central Costarricense",
                distrito       = "Distrito 3 — San José Central",
                diaSemana      = DiaSemana.MIERCOLES,
                horaInicio     = "7:00 PM",
                horaFin        = "9:00 PM",
            ),
            onNavigateBack           = {},
            onNombreGrupoChange      = {},
            onDescripcionChange      = {},
            onLugarReunionChange     = {},
            onCantoFavoritoChange    = {},
            onVersiculoChange        = {},
            onPersonajeBiblicoChange = {},
            onDiaSemanaChange        = {},
            onHoraInicioChange       = {},
            onHoraFinChange          = {},
            onToggleDiaDropdown      = {},
            onToggleHoraInicio       = {},
            onToggleHoraFin          = {},
            onGuardar                = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true, name = "Con error nombre")
@Composable
private fun DatosGrupoErrorPreview() {
    GpLeaderTheme {
        DatosGrupoContent(
            uiState = PerfilUiState(
                nombreGrupo    = "",
                nombreGrupoError = true,
                descripcion    = "",
                lugarReunion   = "",
                iglesia        = "Iglesia Central · San José",
                campo          = "Asociación Central Costarricense",
                distrito       = "Distrito 3 — San José Central",
                diaSemana      = DiaSemana.JUEVES,
                horaInicio     = "6:00 PM",
                horaFin        = "8:00 PM",
            ),
            onNavigateBack           = {},
            onNombreGrupoChange      = {},
            onDescripcionChange      = {},
            onLugarReunionChange     = {},
            onCantoFavoritoChange    = {},
            onVersiculoChange        = {},
            onPersonajeBiblicoChange = {},
            onDiaSemanaChange        = {},
            onHoraInicioChange       = {},
            onHoraFinChange          = {},
            onToggleDiaDropdown      = {},
            onToggleHoraInicio       = {},
            onToggleHoraFin          = {},
            onGuardar                = {},
        )
    }
}
