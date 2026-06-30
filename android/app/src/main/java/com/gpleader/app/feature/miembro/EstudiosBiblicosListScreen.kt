package com.gpleader.app.feature.miembro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.EstudioBiblico
import com.gpleader.app.core.ui.estudios.AgregarAlumnoEstudioDialog
import com.gpleader.app.core.ui.estudios.EstudiosBiblicosLista
import com.gpleader.app.core.ui.estudios.asItem
import com.gpleader.app.core.ui.components.OnResumeEffect
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuInsetSm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstudiosBiblicosListScreen(
    onNavigateBack:      () -> Unit,
    onNavigateToDetalle: (estudioId: String) -> Unit,
    soloLectura:         Boolean = false,
    viewModel: EstudiosBiblicosListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

    EstudiosBiblicosListContent(
        uiState              = uiState,
        onNavigateBack       = onNavigateBack,
        onNavigateToDetalle  = onNavigateToDetalle,
        soloLectura          = soloLectura,
        onRefresh            = viewModel::onRefresh,
        onShowAddDialog      = viewModel::onShowAddDialog,
        onDismissDialog      = viewModel::onDismissDialog,
        onNombreChange       = viewModel::onNombreChange,
        onCrearEstudio       = viewModel::onCrearEstudio,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EstudiosBiblicosListContent(
    uiState:             EstudiosBiblicosUiState,
    onNavigateBack:      () -> Unit = {},
    onNavigateToDetalle: (String) -> Unit = {},
    soloLectura:         Boolean = false,
    onRefresh:           () -> Unit = {},
    onShowAddDialog:     () -> Unit = {},
    onDismissDialog:     () -> Unit = {},
    onNombreChange:      (String) -> Unit = {},
    onCrearEstudio:      () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // ── TopBar ────────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink)
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text       = "Mis Estudios Bíblicos",
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.weight(1f),
            )
            if (!soloLectura) {
                IconButton(onClick = onShowAddDialog) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar alumno", tint = Accent)
                }
            } else {
                Box(Modifier.size(48.dp))
            }
        }

        HorizontalDivider(color = Muted.copy(alpha = 0.12f))

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = onRefresh,
            modifier     = Modifier.fillMaxSize(),
        indicator = {},
        ) {
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }

            uiState.error != null -> Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(uiState.error, style = MaterialTheme.typography.bodyLarge, color = Blush)
            }

            else -> EstudiosBiblicosLista(
                estudios       = uiState.estudios.map { it.asItem() },
                onEstudioClick = onNavigateToDetalle,
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                emptyTitle     = if (soloLectura) "Este miembro no tiene estudios bíblicos"
                                 else "Aún no tienes estudios bíblicos",
                emptySubtitle  = if (soloLectura) "El miembro aún no ha registrado\nalumnos de estudio bíblico."
                                 else "Agrega a las personas a quienes\nestás dando estudio bíblico.",
                onAgregar      = if (soloLectura) null else onShowAddDialog,
            )
        }
        } // PullToRefreshBox
    }

    // ── Diálogo agregar alumno ────────────────────────────────────────────────
    if (uiState.showAddDialog) {
        AgregarAlumnoEstudioDialog(
            nombre         = uiState.nuevoNombre,
            isCreating     = uiState.isCreating,
            onNombreChange = onNombreChange,
            onDismiss      = onDismissDialog,
            onConfirm      = onCrearEstudio,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun EstudiosBiblicosPreview() {
    GpLeaderTheme {
        EstudiosBiblicosListContent(
            uiState = EstudiosBiblicosUiState(
                isLoading = false,
                estudios  = listOf(
                    EstudioBiblico("1", "m1", "Ana López",   listOf(1, 2, 3, 4, 5)),
                    EstudioBiblico("2", "m1", "Juan García", listOf(1)),
                    EstudioBiblico("3", "m1", "María Ruiz",  emptyList()),
                ),
            ),
        )
    }
}
