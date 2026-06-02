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
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuCard
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

@Composable
fun EstudiosBiblicosListScreen(
    onNavigateBack:      () -> Unit,
    onNavigateToDetalle: (estudioId: String) -> Unit,
    viewModel: EstudiosBiblicosListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

    EstudiosBiblicosListContent(
        uiState              = uiState,
        onNavigateBack       = onNavigateBack,
        onNavigateToDetalle  = onNavigateToDetalle,
        onShowAddDialog      = viewModel::onShowAddDialog,
        onDismissDialog      = viewModel::onDismissDialog,
        onNombreChange       = viewModel::onNombreChange,
        onCrearEstudio       = viewModel::onCrearEstudio,
    )
}

@Composable
private fun EstudiosBiblicosListContent(
    uiState:             EstudiosBiblicosUiState,
    onNavigateBack:      () -> Unit = {},
    onNavigateToDetalle: (String) -> Unit = {},
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
            IconButton(onClick = onShowAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "Agregar alumno", tint = Accent)
            }
        }

        HorizontalDivider(color = Muted.copy(alpha = 0.12f))

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

            uiState.estudios.isEmpty() -> EmptyEstudios(onAgregar = onShowAddDialog)

            else -> LazyColumn(
                modifier        = Modifier.fillMaxSize(),
                contentPadding  = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.estudios, key = { it.id }) { estudio ->
                    EstudioCard(
                        estudio = estudio,
                        onClick = { onNavigateToDetalle(estudio.id) },
                    )
                }
            }
        }
    }

    // ── Diálogo agregar alumno ────────────────────────────────────────────────
    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            containerColor   = Background,
            title = {
                Text(
                    text  = "Agregar alumno",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
            },
            text = {
                Column {
                    Text(
                        text  = "NOMBRE DEL ALUMNO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuInsetSm(cornerRadius = 10.dp)
                            .background(BackgroundDeep, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        if (uiState.nuevoNombre.isEmpty()) {
                            Text("Ej: Juan Pérez", style = MaterialTheme.typography.bodyLarge, color = Muted)
                        }
                        BasicTextField(
                            value         = uiState.nuevoNombre,
                            onValueChange = onNombreChange,
                            textStyle     = MaterialTheme.typography.bodyLarge.copy(color = Ink),
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick  = onCrearEstudio,
                    enabled  = uiState.nuevoNombre.isNotBlank() && !uiState.isCreating,
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Accent, strokeWidth = 2.dp)
                    } else {
                        Text("Crear", color = Accent, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) {
                    Text("Cancelar", color = Mid)
                }
            },
        )
    }
}

@Composable
private fun EmptyEstudios(onAgregar: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text  = "📖",
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "Aún no tienes estudios bíblicos",
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "Agrega a las personas a quienes\nestás dando estudio bíblico.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
        Spacer(Modifier.height(24.dp))
        NeuButtonPrimary(
            text    = "Agregar alumno",
            onClick = onAgregar,
        )
    }
}

@Composable
private fun EstudioCard(estudio: EstudioBiblico, onClick: () -> Unit) {
    NeuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = estudio.studentName,
                        style      = MaterialTheme.typography.bodyLarge,
                        color      = Ink,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text  = "Lección ${estudio.currentLesson} · ${estudio.totalCompleted} de 20 completadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint               = Accent,
                    modifier           = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.height(10.dp))

            // Barra de progreso
            val progreso = (estudio.totalCompleted / 20f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BackgroundDeep),
            ) {
                if (progreso > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progreso)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Accent),
                    )
                }
            }
        }
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
