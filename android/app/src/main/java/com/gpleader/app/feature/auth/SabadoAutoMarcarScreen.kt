package com.gpleader.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.data.repository.IglesiaItem
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuInset

@Composable
fun SabadoAutoMarcarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToConfirmacion: (iglesiaName: String) -> Unit,
    viewModel: SabadoAutoMarcarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToConfirmacion) {
        if (uiState.navigateToConfirmacion) {
            onNavigateToConfirmacion(uiState.iglesiaConfirmadaNombre)
            viewModel.consumeConfirmacionNavigation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint               = Ink,
                )
            }
            Text(
                text       = stringResource(R.string.sabado_automarcar_titulo),
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.weight(1f).padding(start = 4.dp),
            )
        }

        if (!uiState.esSabado) {
            // ── No es sábado ─────────────────────────────────────────────────
            Box(
                modifier         = Modifier.weight(1f).fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector        = Icons.Default.Church,
                        contentDescription = null,
                        tint               = Muted,
                        modifier           = Modifier.size(56.dp),
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text      = "Solo puedes marcar tu asistencia al culto los sábados",
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = Mid,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            Text(
                text     = "Selecciona la iglesia a la que asististe hoy",
                style    = MaterialTheme.typography.bodyMedium,
                color    = Mid,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // ── Iglesia seleccionada ──────────────────────────────────────────
            if (uiState.selectedIglesiaId.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neuElevated(cornerRadius = 16.dp)
                        .background(Background, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Sage.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Check,
                            contentDescription = null,
                            tint               = Sage,
                            modifier           = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = "Iglesia seleccionada",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                        Text(
                            text       = uiState.selectedIglesiaNombre,
                            style      = MaterialTheme.typography.bodyLarge,
                            color      = Ink,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // ── Buscador de iglesias ──────────────────────────────────────────
            Text(
                text     = "Buscar otra iglesia",
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuInset(cornerRadius = 14.dp)
                    .background(BackgroundDeep, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                NeuTextField(
                    value         = uiState.busqueda,
                    onValueChange = viewModel::onBusquedaChange,
                    label         = null,
                    placeholder   = "Nombre de iglesia…",
                    modifier      = Modifier.fillMaxWidth(),
                    leadingContent = {
                        Icon(
                            imageVector        = Icons.Default.Search,
                            contentDescription = null,
                            tint               = Muted,
                            modifier           = Modifier.size(22.dp),
                        )
                    },
                )

                if (uiState.busqueda.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    val filtradas = viewModel.iglesiasFiltradas()
                    if (filtradas.isEmpty()) {
                        Text(
                            text     = "Sin resultados",
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = Muted,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    } else {
                        filtradas.forEachIndexed { idx, iglesia ->
                            IglesiaFila(
                                iglesia    = iglesia,
                                seleccionada = iglesia.id == uiState.selectedIglesiaId,
                                onClick    = { viewModel.onIglesiaSelected(iglesia) },
                            )
                            if (idx != filtradas.lastIndex) {
                                HorizontalDivider(color = Background, thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            // ── Error ─────────────────────────────────────────────────────────
            uiState.error?.let { err ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text  = err,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Blush,
                )
            }
        }

        // ── Botón marcar ─────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            if (uiState.isSending) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            } else {
                NeuButtonPrimary(
                    text     = stringResource(R.string.sabado_automarcar_btn),
                    onClick  = viewModel::onMarcarAsistencia,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                )
            }
        }

        } // end else (esSabado)
    }
}

@Composable
private fun IglesiaFila(
    iglesia: IglesiaItem,
    seleccionada: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = Icons.Default.Church,
            contentDescription = null,
            tint               = if (seleccionada) Accent else Muted,
            modifier           = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = iglesia.nombre,
                style      = MaterialTheme.typography.bodyLarge,
                color      = Ink,
                fontWeight = if (seleccionada) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (iglesia.districtNombre.isNotBlank()) {
                Text(
                    text  = iglesia.districtNombre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }
        }
        if (seleccionada) {
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = null,
                tint               = Accent,
                modifier           = Modifier.size(18.dp),
            )
        }
    }
}
