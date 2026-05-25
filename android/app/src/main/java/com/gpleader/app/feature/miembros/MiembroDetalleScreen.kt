package com.gpleader.app.feature.miembros

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
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

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun MiembroDetalleScreen(
    onNavigateBack:    () -> Unit,
    onNavigateToEditar: () -> Unit,
    viewModel: MiembrosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToEditar) {
        if (uiState.navigateToEditar) {
            viewModel.consumeNavigateToEditar()
            onNavigateToEditar()
        }
    }

    val miembro = uiState.miembroSeleccionado ?: return

    DetalleContent(
        miembro        = miembro,
        onNavigateBack = onNavigateBack,
        onEditarClick  = viewModel::onEditarClick,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun DetalleContent(
    miembro:        MiembroUi,
    onNavigateBack: () -> Unit,
    onEditarClick:  () -> Unit,
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            DetalleTopBar(
                onNavigateBack = onNavigateBack,
                onEditarClick  = onEditarClick,
                modifier       = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Hero card ─────────────────────────────────────────────────────
            item {
                HeroCard(miembro = miembro)
            }

            // ── Card INFO ─────────────────────────────────────────────────────
            item {
                InfoCard(miembro = miembro)
            }

            // ── Historial de asistencia ───────────────────────────────────────
            item {
                HistorialCard(historial = miembro.historial)
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun DetalleTopBar(
    onNavigateBack: () -> Unit,
    onEditarClick:  () -> Unit,
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
            text  = stringResource(R.string.miembros_detalle_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .neuElevatedSm(cornerRadius = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Accent)
                .clickable(onClick = onEditarClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text  = stringResource(R.string.miembros_btn_editar),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

// ── Hero card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(miembro: MiembroUi) {
    val archivado = miembro.estado == EstadoMiembro.ARCHIVADO
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 28.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Background)
            .padding(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.fillMaxWidth(),
        ) {
            // Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .neuElevatedSm(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent),
            ) {
                Text(
                    text  = miembro.iniciales,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(14.dp))

            // Nombre
            val primerNombreCompleto = buildString {
                append(miembro.primerNombre)
                if (miembro.segundoNombre.isNotBlank()) append(" ${miembro.segundoNombre}")
                append(" ${miembro.primerApellido}")
                if (miembro.segundoApellido.isNotBlank()) append(" ${miembro.segundoApellido}")
            }
            Text(
                text  = primerNombreCompleto,
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text  = stringResource(R.string.miembros_desde, miembro.mesIngreso),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )

            Spacer(Modifier.height(12.dp))

            // Badge estado
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (archivado) Muted else Sage)
                    .padding(horizontal = 14.dp, vertical = 5.dp),
            ) {
                Text(
                    text  = if (archivado)
                        stringResource(R.string.miembros_estado_archivado)
                    else
                        stringResource(R.string.miembros_estado_activo),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }
    }
}

// ── Info card ─────────────────────────────────────────────────────────────────

@Composable
private fun InfoCard(miembro: MiembroUi) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 28.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Background)
            .padding(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            val filas = listOf(
                stringResource(R.string.miembros_label_primer_nombre)   to miembro.primerNombre,
                stringResource(R.string.miembros_label_segundo_nombre)  to miembro.segundoNombre.ifBlank { null },
                stringResource(R.string.miembros_label_primer_apellido) to miembro.primerApellido,
                stringResource(R.string.miembros_label_segundo_apellido) to miembro.segundoApellido.ifBlank { null },
                stringResource(R.string.miembros_label_telefono)        to miembro.telefono.ifBlank { null },
                stringResource(R.string.miembros_label_correo)          to miembro.correo.ifBlank { null },
                stringResource(R.string.miembros_label_direccion)       to miembro.direccion.ifBlank { null },
            )
            filas.forEachIndexed { i, (label, valor) ->
                InfoFila(label = label, valor = valor)
                if (i < filas.lastIndex) {
                    HorizontalDivider(color = Shadow, thickness = 0.6.dp)
                }
            }
        }
    }
}

@Composable
private fun InfoFila(
    label: String,
    valor: String?,
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.padding(bottom = 2.dp),
        )
        Text(
            text  = valor ?: "—",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = if (valor != null) Ink else Muted,
        )
    }
}

// ── Historial card ────────────────────────────────────────────────────────────

@Composable
private fun HistorialCard(historial: List<AsistenciaResumen>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 28.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Background)
            .padding(24.dp),
    ) {
        Column {
            Text(
                text     = stringResource(R.string.miembros_historial_titulo),
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.padding(bottom = 14.dp),
            )
            if (historial.isEmpty()) {
                Text(
                    text  = "—",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    historial.forEach { registro ->
                        HistorialFila(registro)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorialFila(registro: AsistenciaResumen) {
    val (badgeBg, badgeText) = when (registro.estado) {
        "P"  -> Sage  to "P"
        "A"  -> Blush to "A"
        else -> Muted to "J"
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
                text  = badgeText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun DetallePreview() {
    GpLeaderTheme {
        val miembro = MiembroUi(
            id           = "1",
            primerNombre = "Carlos",
            primerApellido = "Ramírez",
            telefono     = "8812-3456",
            correo       = "carlos.ramirez@gmail.com",
            direccion    = "San José, Costa Rica",
            mesIngreso   = "Mar 2024",
            historial    = listOf(
                AsistenciaResumen("12 Mar", "P"),
                AsistenciaResumen("05 Mar", "A"),
                AsistenciaResumen("26 Feb", "P"),
            ),
        )
        DetalleContent(
            miembro        = miembro,
            onNavigateBack = {},
            onEditarClick  = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true, name = "Archivado")
@Composable
private fun DetalleArchivadoPreview() {
    GpLeaderTheme {
        val miembro = MiembroUi(
            id           = "9",
            primerNombre = "Miguel",
            primerApellido = "Soto",
            correo       = "miguel.soto@gmail.com",
            estado       = EstadoMiembro.ARCHIVADO,
            mesIngreso   = "Jul 2023",
        )
        DetalleContent(
            miembro        = miembro,
            onNavigateBack = {},
            onEditarClick  = {},
        )
    }
}
