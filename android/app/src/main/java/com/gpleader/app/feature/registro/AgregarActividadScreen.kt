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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun AgregarActividadScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    AgregarActividadContent(
        onNavigateBack = onNavigateBack,
        onAgregar = { nombre, tipoMarcador, cantidad, unidad, monto, tieneDesglose ->
            viewModel.onAgregarActividadExtra(nombre, tipoMarcador, cantidad, unidad, monto, tieneDesglose)
            onNavigateBack()
        },
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun AgregarActividadContent(
    onNavigateBack: () -> Unit,
    onAgregar: (nombre: String, tipoMarcador: TipoMarcador, cantidad: Int?, unidad: String, monto: Double?, tieneDesglose: Boolean) -> Unit,
) {
    val unidades = listOf(
        stringResource(R.string.agregar_actividad_unidad_personas),
        stringResource(R.string.agregar_actividad_unidad_visitas),
        stringResource(R.string.agregar_actividad_unidad_sesiones),
        stringResource(R.string.agregar_actividad_unidad_otro),
    )

    var nombre        by remember { mutableStateOf("") }
    var tipoMarcador  by remember { mutableStateOf(TipoMarcador.CONTADOR) }
    var cantidad      by remember { mutableStateOf<Int?>(null) }
    var unidad        by remember { mutableStateOf(unidades[0]) }
    var montoTexto    by remember { mutableStateOf("") }
    var tieneDesglose by remember { mutableStateOf(false) }

    // El layout usa Column en lugar de Box+LazyColumn para que el TopBar
    // quede siempre fijo arriba (no dentro del scroll)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        // ── Top bar fijo ──────────────────────────────────────────────────────
        AgregarActividadTopBar(onNavigateBack = onNavigateBack)

        // ── Contenido scrolleable ─────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                // ── Nombre ────────────────────────────────────────────────────
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text     = stringResource(R.string.agregar_actividad_label_nombre),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = Muted,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        OutlinedTextField(
                            value         = nombre,
                            onValueChange = { nombre = it },
                            placeholder   = {
                                Text(
                                    text  = stringResource(R.string.agregar_actividad_hint_nombre),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Muted,
                                )
                            },
                            singleLine = true,
                            shape      = RoundedCornerShape(14.dp),
                            colors     = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Background,
                                focusedContainerColor   = Background,
                                unfocusedBorderColor    = Muted.copy(alpha = 0.4f),
                                focusedBorderColor      = Accent,
                                cursorColor             = Accent,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                // ── Tipo de marcador ──────────────────────────────────────────
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text     = stringResource(R.string.agregar_actividad_label_tipo),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = Muted,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TipoMarcadorChip(
                                texto   = stringResource(R.string.tipo_marcador_contador),
                                activo  = tipoMarcador == TipoMarcador.CONTADOR,
                                onClick = { tipoMarcador = TipoMarcador.CONTADOR },
                            )
                            TipoMarcadorChip(
                                texto   = stringResource(R.string.tipo_marcador_checkbox),
                                activo  = tipoMarcador == TipoMarcador.CHECKBOX,
                                onClick = { tipoMarcador = TipoMarcador.CHECKBOX },
                            )
                            TipoMarcadorChip(
                                texto   = stringResource(R.string.tipo_marcador_monetario),
                                activo  = tipoMarcador == TipoMarcador.MONETARIO,
                                onClick = { tipoMarcador = TipoMarcador.MONETARIO },
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        val desc = when (tipoMarcador) {
                            TipoMarcador.CONTADOR  -> stringResource(R.string.tipo_marcador_contador_desc)
                            TipoMarcador.CHECKBOX  -> stringResource(R.string.tipo_marcador_checkbox_desc)
                            TipoMarcador.MONETARIO -> stringResource(R.string.tipo_marcador_monetario_desc)
                        }
                        Text(
                            text  = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                // ── Campo según tipo ──────────────────────────────────────────
                when (tipoMarcador) {
                    TipoMarcador.CONTADOR -> {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text     = stringResource(R.string.agregar_actividad_label_cantidad),
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = Muted,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                                ContadorGrande(
                                    valor    = cantidad,
                                    onChange = { cantidad = it },
                                )
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text     = stringResource(R.string.agregar_actividad_label_unidad),
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = Muted,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    unidades.forEach { opcion ->
                                        UnidadChip(
                                            texto   = opcion,
                                            activo  = opcion == unidad,
                                            onClick = { unidad = opcion },
                                        )
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                        item {
                            DesgloseToggle(
                                activo   = tieneDesglose,
                                onToggle = { tieneDesglose = !tieneDesglose },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    TipoMarcador.CHECKBOX -> {
                        // Vista previa de cómo se verá en el registro
                        item {
                            CheckboxPreview(
                                nombreActividad = nombre.ifBlank {
                                    stringResource(R.string.agregar_actividad_hint_nombre)
                                },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    TipoMarcador.MONETARIO -> {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text     = stringResource(R.string.agregar_actividad_label_monto),
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = Muted,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                                OutlinedTextField(
                                    value         = montoTexto,
                                    onValueChange = { montoTexto = it.filter { c -> c.isDigit() || c == '.' } },
                                    placeholder   = {
                                        Text(
                                            text  = "0.00",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Muted,
                                        )
                                    },
                                    prefix          = { Text("₡ ", color = Ink) },
                                    singleLine      = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape           = RoundedCornerShape(14.dp),
                                    colors          = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = Background,
                                        focusedContainerColor   = Background,
                                        unfocusedBorderColor    = Muted.copy(alpha = 0.4f),
                                        focusedBorderColor      = Accent,
                                        cursorColor             = Accent,
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                        item {
                            DesgloseToggle(
                                activo   = tieneDesglose,
                                onToggle = { tieneDesglose = !tieneDesglose },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }
        }

        // ── Botones fijos abajo ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Background)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NeuButtonPrimary(
                text    = stringResource(R.string.agregar_actividad_btn_agregar),
                onClick = {
                    if (nombre.isNotBlank()) {
                        val monto = montoTexto.toDoubleOrNull()
                        onAgregar(nombre, tipoMarcador, cantidad, unidad, monto, tieneDesglose)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            NeuButtonSecondary(
                text     = stringResource(R.string.registro_confirm_cancelar),
                onClick  = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Vista previa del checkbox ─────────────────────────────────────────────────

@Composable
private fun CheckboxPreview(
    nombreActividad: String,
    modifier: Modifier = Modifier,
) {
    var marcado by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text     = stringResource(R.string.tipo_marcador_checkbox_preview_label),
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        NeuCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar placeholder (igual que en asistencia)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BackgroundDeep),
                ) {
                    Text(
                        text  = "☆",
                        style = MaterialTheme.typography.labelSmall,
                        color = Mid,
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text     = nombreActividad,
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = if (nombreActividad == stringResource(R.string.agregar_actividad_hint_nombre)) Muted else Ink,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )

                // Checkbox igual al de asistencia (imagen #12)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (marcado)
                                Modifier.background(Sage)
                            else
                                Modifier
                                    .background(Background)
                                    .border(1.5.dp, Muted.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        )
                        .clickable { marcado = !marcado },
                ) {
                    if (marcado) {
                        Icon(
                            imageVector        = Icons.Default.Check,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text  = stringResource(R.string.tipo_marcador_checkbox_preview_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun AgregarActividadTopBar(onNavigateBack: () -> Unit) {
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
            text      = stringResource(R.string.agregar_actividad_titulo),
            style     = MaterialTheme.typography.titleLarge,
            color     = Ink,
            textAlign = TextAlign.Center,
            modifier  = Modifier.weight(1f),
        )
        Box(modifier = Modifier.size(40.dp))
    }
}

// ── Toggle desglose por miembro ───────────────────────────────────────────────

@Composable
private fun DesgloseToggle(
    activo:   Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundDeep)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Checkbox cuadrado
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .then(
                    if (activo) Modifier.background(Accent)
                    else Modifier
                        .background(Background)
                        .border(1.5.dp, Muted.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ),
        ) {
            if (activo) {
                Icon(
                    imageVector        = Icons.Default.Check,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(14.dp),
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text       = stringResource(R.string.agregar_actividad_desglose_titulo),
                style      = MaterialTheme.typography.bodyLarge,
                color      = Ink,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text  = stringResource(R.string.agregar_actividad_desglose_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }
    }
}

// ── Chip de tipo de marcador ──────────────────────────────────────────────────

@Composable
private fun TipoMarcadorChip(texto: String, activo: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .then(
                if (activo) Modifier.background(Accent, RoundedCornerShape(8.dp))
                else Modifier.neuElevatedSm(cornerRadius = 8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .then(if (!activo) Modifier.background(Background) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(
            text       = texto,
            style      = MaterialTheme.typography.labelSmall,
            color      = if (activo) Color.White else Ink,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

// ── Chip de unidad ────────────────────────────────────────────────────────────

@Composable
private fun UnidadChip(
    texto:   String,
    activo:  Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .then(
                if (activo) Modifier.background(Ink, RoundedCornerShape(8.dp))
                else Modifier.neuElevatedSm(cornerRadius = 8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .then(if (!activo) Modifier.background(Background) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text       = texto,
            style      = MaterialTheme.typography.labelSmall,
            color      = if (activo) Color.White else Ink,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "AgregarActividad")
@Composable
private fun AgregarActividadPreview() {
    GpLeaderTheme {
        AgregarActividadContent(
            onNavigateBack = {},
            onAgregar      = { _, _, _, _, _, _ -> },
        )
    }
}
