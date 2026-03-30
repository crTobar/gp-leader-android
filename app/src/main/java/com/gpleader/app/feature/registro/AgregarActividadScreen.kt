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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevatedSm

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun AgregarActividadScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    AgregarActividadContent(
        onNavigateBack = onNavigateBack,
        onAgregar = { nombre, cantidad, unidad ->
            viewModel.onAgregarActividadExtra(nombre, cantidad, unidad)
            onNavigateBack()
        },
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun AgregarActividadContent(
    onNavigateBack: () -> Unit,
    onAgregar:      (nombre: String, cantidad: Int?, unidad: String) -> Unit,
) {
    val unidades = listOf(
        stringResource(R.string.agregar_actividad_unidad_personas),
        stringResource(R.string.agregar_actividad_unidad_visitas),
        stringResource(R.string.agregar_actividad_unidad_sesiones),
        stringResource(R.string.agregar_actividad_unidad_otro),
    )

    var nombre   by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf<Int?>(null) }
    var unidad   by remember { mutableStateOf(unidades[0]) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 112.dp),
        ) {
            item { AgregarActividadTopBar(onNavigateBack = onNavigateBack) }

            item { Spacer(Modifier.height(20.dp)) }

            // ── Nombre ────────────────────────────────────────────────────────
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

            // ── Cantidad ──────────────────────────────────────────────────────
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

            // ── Unidad ────────────────────────────────────────────────────────
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
                            val activo = opcion == unidad
                            UnidadChip(
                                texto   = opcion,
                                activo  = activo,
                                onClick = { unidad = opcion },
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        // ── Botones flotantes ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Background)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NeuButtonPrimary(
                text     = stringResource(R.string.agregar_actividad_btn_agregar),
                onClick  = { if (nombre.isNotBlank()) onAgregar(nombre, cantidad, unidad) },
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
        // Spacer para centrar el título (mismo ancho que el botón ← aprox.)
        Box(modifier = Modifier.size(40.dp))
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
            .then(
                if (!activo) Modifier.background(Background)
                else Modifier
            )
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
            onAgregar      = { _, _, _ -> },
        )
    }
}
