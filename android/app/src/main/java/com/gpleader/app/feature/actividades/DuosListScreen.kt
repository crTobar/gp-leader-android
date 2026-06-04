package com.gpleader.app.feature.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.DuoMisioneroData
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.data.repository.iniciales
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.OnResumeEffect
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuGlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuosListScreen(
    onNavigateBack:    () -> Unit,
    onNavigateToCrear: () -> Unit,
    onNavigateToDetalle: (duoId: String) -> Unit,
    viewModel: DuosListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink, modifier = Modifier.size(20.dp))
                }
                Text(
                    text      = "Dúos Misioneros",
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Ink,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.weight(1f),
                )
                Box(modifier = Modifier.size(40.dp))
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh    = viewModel::onRefresh,
                modifier     = Modifier.fillMaxSize(),
            indicator = {},
            ) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
                }
                uiState.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error!!, style = MaterialTheme.typography.bodyMedium, color = Blush)
                }
                uiState.duos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Group, null, tint = Muted, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Sin dúos activos", style = MaterialTheme.typography.bodyLarge, color = Muted)
                        Text("Toca + para crear el primer dúo", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    }
                }
                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = androidx.compose.foundation.layout.PaddingValues(
                        start = 20.dp, end = 20.dp, top = 4.dp, bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.duos) { duo ->
                        DuoCard(duo = duo, onClick = { onNavigateToDetalle(duo.id) })
                    }
                }
            }
            } // PullToRefreshBox
        }

        // ── FAB ───────────────────────────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(24.dp)
                .size(56.dp)
                .neuGlow(cornerRadius = 28.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Accent)
                .clickable(onClick = onNavigateToCrear),
        ) {
            Icon(Icons.Default.Add, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun DuoCard(duo: DuoMisioneroData, onClick: () -> Unit) {
    NeuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatares apilados
            Box(modifier = Modifier.size(52.dp)) {
                NeuAvatar(iniciales = duo.member2.iniciales, size = 36.dp, modifier = Modifier.align(Alignment.BottomEnd))
                NeuAvatar(iniciales = duo.member1.iniciales, size = 36.dp, modifier = Modifier.align(Alignment.TopStart))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = duo.member1.nombreCompleto,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text  = "con ${duo.member2.nombreCompleto}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Muted, modifier = Modifier.size(20.dp))
        }
    }
}
