package com.gpleader.app.feature.iglesia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.PendingBoardItem
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.components.NeuCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AprobacionesScreen(
    onNavigateBack: () -> Unit,
    viewModel: AprobacionesViewModel = hiltViewModel(),
) {
    val uiState      by viewModel.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMsg) {
        uiState.toastMsg?.let {
            snackbarHost.showSnackbar(it)
            viewModel.consumeToast()
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost   = { SnackbarHost(snackbarHost) },
    ) { innerPadding ->
        AprobacionesContent(
            uiState        = uiState,
            innerPadding   = innerPadding,
            onNavigateBack = onNavigateBack,
            onAprobar      = viewModel::onAprobar,
            onRechazar     = viewModel::onRechazar,
        )
    }
}

@Composable
private fun AprobacionesContent(
    uiState:        AprobacionesUiState,
    innerPadding:   PaddingValues,
    onNavigateBack: () -> Unit,
    onAprobar:      (String) -> Unit,
    onRechazar:     (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .padding(innerPadding),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink)
            }
            Column {
                Text(
                    text  = "Aprobaciones",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                Text(
                    text  = "Actividades monetarias pendientes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            uiState.error != null -> Box(Modifier.fillMaxWidth().padding(24.dp)) {
                Text(uiState.error, color = Blush, style = MaterialTheme.typography.bodyMedium)
            }
            uiState.items.isEmpty() -> Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sin pendientes", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Spacer(Modifier.height(8.dp))
                    Text("Todas las actividades están al día", style = MaterialTheme.typography.bodyMedium, color = Muted)
                }
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            ) {
                // Agrupa por grupo
                val porGrupo = uiState.items.groupBy { it.grupoNombre }
                porGrupo.forEach { (grupoNombre, registros) ->
                    item(key = "header_$grupoNombre") {
                        Text(
                            text     = grupoNombre,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = Muted,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        )
                    }
                    items(registros, key = { it.recordId }) { item ->
                        AprobacionItem(
                            item       = item,
                            procesando = item.recordId in uiState.procesando,
                            onAprobar  = { onAprobar(item.recordId) },
                            onRechazar = { onRechazar(item.recordId) },
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AprobacionItem(
    item:       PendingBoardItem,
    procesando: Boolean,
    onAprobar:  () -> Unit,
    onRechazar: () -> Unit,
) {
    val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es"))
    NeuCard {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = item.miembroNombre,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Ink,
                    )
                    Text(
                        text  = item.actividadNombre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mid,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text  = "₡${item.monto.toLong()}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                    Text(
                        text  = item.recordDate.format(fmt),
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                if (procesando) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Accent, strokeWidth = 2.dp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Sage)
                            .clickable(onClick = onAprobar)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✓ Aprobar", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Blush)
                            .clickable(onClick = onRechazar)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✗ Rechazar", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun AprobacionesPreview() {
    GpLeaderTheme {
        AprobacionesContent(
            uiState = AprobacionesUiState(
                items = listOf(
                    PendingBoardItem("1", "Juan Pérez", "GP Los Olivos", "Diezmo", 25000.0, LocalDate.now()),
                    PendingBoardItem("2", "María López", "GP Las Palmas", "Ofrenda especial", 15000.0, LocalDate.now()),
                ),
            ),
            innerPadding   = PaddingValues(),
            onNavigateBack = {},
            onAprobar      = {},
            onRechazar     = {},
        )
    }
}
