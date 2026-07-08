package com.gpleader.app.feature.actividades

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.MemberEntryAggregate
import com.gpleader.app.core.data.repository.MiembroData
import com.gpleader.app.core.data.repository.iniciales
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.NeuButtonPrimary
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
import com.gpleader.app.feature.miembro.MontoInput
import com.gpleader.app.feature.miembro.miles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadHistorialScreen(
    onNavigateBack:               () -> Unit,
    onNavigateToAprobaciones:     () -> Unit,
    onNavigateToAgregarAporte:    () -> Unit,
    onNavigateToMiembroAportes:   (miembroId: String, nombre: String) -> Unit,
    viewModel: ActividadHistorialViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

    fun fmt(v: Double): String =
        if (uiState.markerType == "monetary") "₡${miles(v.toLong())}"
        else "${miles(v.toLong())} ${uiState.actividadUnidad}".trim()

    Column(
        modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding(),
    ) {
        // ── Back + título ─────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.neuElevatedSm(cornerRadius = 12.dp).clip(RoundedCornerShape(12.dp))
                    .background(Background).clickable(onClick = onNavigateBack).padding(10.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink, modifier = Modifier.size(20.dp))
            }
            Text(
                text = uiState.actividadNombre.ifBlank { "Actividad" },
                style = MaterialTheme.typography.titleLarge, color = Ink,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
            )
            Box(Modifier.size(40.dp))
        }

        // ── Cabecera: dos totales ─────────────────────────────────────────────
        NeuCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text("APROBADO", style = MaterialTheme.typography.labelSmall, color = Muted)
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = fmt(uiState.approvedTotal),
                    style      = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
                    color      = Sage,
                    fontWeight = FontWeight.Bold,
                )
                if (uiState.pendingCount > 0) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(Blush.copy(alpha = 0.10f))
                            .clickable(onClick = onNavigateToAprobaciones)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Por aprobar (${uiState.pendingCount})", style = MaterialTheme.typography.labelSmall, color = Blush)
                            Text(fmt(uiState.pendingTotal), style = MaterialTheme.typography.bodyLarge, color = Blush, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Aprobar", tint = Blush, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Botón Agregar ─────────────────────────────────────────────────────
        NeuCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text("Registrar aporte", style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Elige un miembro y anota su aporte", style = MaterialTheme.typography.bodyMedium, color = Muted)
                Spacer(Modifier.height(16.dp))
                NeuButtonPrimary(text = "Agregar", onClick = onNavigateToAgregarAporte, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Lista de miembros que han aportado ────────────────────────────────
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = viewModel::onRefresh,
            modifier     = Modifier.fillMaxSize(),
            indicator    = {},
        ) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
                uiState.error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(uiState.error!!, style = MaterialTheme.typography.bodyLarge, color = Blush, textAlign = TextAlign.Center)
                }
                uiState.perMember.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Nadie ha aportado todavía", style = MaterialTheme.typography.bodyLarge, color = Muted, textAlign = TextAlign.Center)
                }
                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item(key = "_h") {
                        Text("MIEMBROS QUE HAN APORTADO", style = MaterialTheme.typography.labelSmall, color = Muted, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(uiState.perMember, key = { it.miembroId }) { agg ->
                        MemberAporteRow(
                            agg     = agg,
                            valorTxt = fmt(agg.total),
                            onClick  = { onNavigateToMiembroAportes(agg.miembroId, agg.miembroNombre) },
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun MemberAporteRow(
    agg:      MemberEntryAggregate,
    valorTxt: String,
    onClick:  () -> Unit,
) {
    val iniciales = agg.miembroNombre.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    NeuCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeuAvatar(iniciales = iniciales, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(agg.miembroNombre, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.Medium)
                Text("${agg.count} aporte${if (agg.count == 1) "" else "s"}", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
            Text(valorTxt, style = MaterialTheme.typography.bodyLarge, color = Accent, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
        }
    }
}

