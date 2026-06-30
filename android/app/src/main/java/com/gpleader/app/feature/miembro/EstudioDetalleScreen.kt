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
import com.gpleader.app.core.ui.estudios.EstudioBiblicoDetalleContent
import com.gpleader.app.core.ui.estudios.asItem
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

            uiState.estudio != null -> EstudioBiblicoDetalleContent(
                estudio        = uiState.estudio!!.asItem(),
                togglingLesson = uiState.togglingLesson,
                onToggleLesson = onToggleLesson,
                modifier       = Modifier.fillMaxSize(),
            )
        }
        } // PullToRefreshBox
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
