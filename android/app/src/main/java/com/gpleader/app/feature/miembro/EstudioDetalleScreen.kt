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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.EstudioBiblico
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInset

private val TITULOS_LECCIONES = listOf(
    "La Santa Biblia",
    "Dios",
    "La Oración",
    "Origen del Pecado",
    "Salvación",
    "Perdón de Pecados",
    "Segunda Venida",
    "Señales de la 2ª Venida",
    "Santa Ley de Dios",
    "El Sábado",
    "Cómo Guardar el Sábado",
    "Sostén de la Iglesia",
    "El Bautismo",
    "La Muerte",
    "El Juicio",
    "La Iglesia",
    "Don de Profecía",
    "Normas Cristianas",
    "Vida Cristiana",
    "Dios Nos Llama",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstudioDetalleScreen(
    estudioId:      String,
    onNavigateBack: () -> Unit,
    soloLectura:    Boolean = false,
    viewModel: EstudioDetalleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(estudioId) { viewModel.cargar(estudioId) }

    EstudioDetalleContent(
        uiState        = uiState,
        onNavigateBack = onNavigateBack,
        onRefresh      = { viewModel.onRefresh(estudioId) },
        onToggleLesson = if (soloLectura) null
                         else { lessonNumber -> viewModel.onToggleLesson(estudioId, lessonNumber) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EstudioDetalleContent(
    uiState:        EstudioDetalleUiState,
    onNavigateBack: () -> Unit = {},
    onRefresh:      () -> Unit = {},
    onToggleLesson: ((Int) -> Unit)? = {},
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
                text       = uiState.estudio?.studentName ?: "Estudio",
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )
        }

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

            uiState.estudio != null -> {
                val estudio = uiState.estudio
                val progreso = (estudio.totalCompleted / 20f).coerceIn(0f, 1f)

                LazyColumn(
                    modifier        = Modifier.fillMaxSize(),
                    contentPadding  = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // ── Resumen ───────────────────────────────────────────────
                    item {
                        NeuCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                                Row(
                                    modifier          = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text  = "PROGRESO",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Muted,
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text       = "${estudio.totalCompleted} de 20 lecciones",
                                            style      = MaterialTheme.typography.bodyLarge,
                                            color      = Ink,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    Text(
                                        text       = "${(progreso * 100).toInt()}%",
                                        style      = MaterialTheme.typography.headlineMedium,
                                        color      = Accent,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(BackgroundDeep),
                                ) {
                                    if (progreso > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progreso)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Accent),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── 20 lecciones ──────────────────────────────────────────
                    items(20) { idx ->
                        val leccionNum = idx + 1
                        val completada = leccionNum in estudio.completedLessons
                        val esActual   = leccionNum == estudio.currentLesson
                        val toggling   = uiState.togglingLesson == leccionNum

                        LeccionRow(
                            numero     = leccionNum,
                            titulo     = TITULOS_LECCIONES.getOrElse(idx) { "Lección $leccionNum" },
                            completada = completada,
                            esActual   = esActual && !completada,
                            toggling   = toggling,
                            onClick    = onToggleLesson?.let { fn -> { fn(leccionNum) } },
                        )
                    }
                }
            }
        }
        } // PullToRefreshBox
    }
}

@Composable
private fun LeccionRow(
    numero:     Int,
    titulo:     String,
    completada: Boolean,
    esActual:   Boolean,
    toggling:   Boolean,
    onClick:    (() -> Unit)?,
) {
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Número
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (completada) Sage.copy(alpha = 0.15f) else BackgroundDeep),
            ) {
                Text(
                    text       = numero.toString(),
                    style      = MaterialTheme.typography.labelSmall,
                    color      = if (completada) Sage else Muted,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.width(14.dp))

            // Texto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (completada) Mid else Ink,
                    fontWeight = if (completada) FontWeight.Normal else FontWeight.Medium,
                )
                if (esActual) {
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .background(Accent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text  = "PRÓXIMO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Accent,
                        )
                    }
                }
            }

            // Checkbox neumórfico
            LeccionCheckbox(checked = completada, isLoading = toggling, onClick = onClick)
        }
    }
}

@Composable
private fun LeccionCheckbox(
    checked:   Boolean,
    isLoading: Boolean,
    onClick:   (() -> Unit)?,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .then(
                if (checked) Modifier.neuInset(cornerRadius = 10.dp)
                else Modifier.neuElevatedSm(cornerRadius = 10.dp)
            )
            .background(
                color = if (checked) Sage.copy(alpha = 0.15f) else Background,
                shape = RoundedCornerShape(10.dp),
            )
            .clip(RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable(enabled = !isLoading, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier    = Modifier.size(18.dp),
                color       = Accent,
                strokeWidth = 2.dp,
            )
            checked -> Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = "Completada",
                tint               = Sage,
                modifier           = Modifier.size(22.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun EstudioDetallePreview() {
    GpLeaderTheme {
        EstudioDetalleContent(
            uiState = EstudioDetalleUiState(
                isLoading = false,
                estudio   = EstudioBiblico(
                    id               = "1",
                    memberId         = "m1",
                    studentName      = "Juan García",
                    completedLessons = listOf(1, 2, 3),
                ),
            ),
        )
    }
}
