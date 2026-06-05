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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.gpleader.app.core.data.repository.MemberActivitySubmission
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadHistorialScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAgregarAporte: () -> Unit,
    viewModel: ActividadHistorialViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        // ── Back button + "Detalle" ───────────────────────────────────────────
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
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint               = Ink,
                    modifier           = Modifier.size(20.dp),
                )
            }
            Text(
                text      = "Detalle",
                style     = MaterialTheme.typography.titleLarge,
                color     = Ink,
                textAlign = TextAlign.Center,
                modifier  = Modifier.weight(1f),
            )
            Box(modifier = Modifier.size(40.dp))
        }

        // ── Card info: nombre + total ─────────────────────────────────────────
        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text       = uiState.actividadNombre.ifBlank { "Actividad" },
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f),
                )
                val totalLabel = when (uiState.markerType) {
                    "monetary"              -> "₡${uiState.grupoMonto.toLong()}"
                    "realizado", "checkbox" -> if (uiState.grupoTotal > 0) "${uiState.grupoTotal}×" else "—"
                    else                    -> if (uiState.grupoTotal > 0) "${uiState.grupoTotal} ${uiState.actividadUnidad}" else "—"
                }
                Text(
                    text       = totalLabel,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = if (uiState.grupoTotal > 0 || uiState.grupoMonto > 0) Accent else Muted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Card "Registrar aporte" ───────────────────────────────────────────
        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text  = "Registrar aporte",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "Asigna valores a cada miembro del grupo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
                Spacer(Modifier.height(16.dp))
                NeuButtonPrimary(
                    text     = "Agregar",
                    onClick  = onNavigateToAgregarAporte,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Lista de registros ────────────────────────────────────────────────
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

                uiState.error != null -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(uiState.error!!, style = MaterialTheme.typography.bodyLarge, color = Blush, textAlign = TextAlign.Center)
                }

                uiState.submissions.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = "Sin registros todavía",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.submissions, key = { it.recordId }) { sub ->
                        SubmissionRow(sub = sub, markerType = uiState.markerType, unitLabel = uiState.actividadUnidad)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmissionRow(
    sub:        MemberActivitySubmission,
    markerType: String,
    unitLabel:  String,
) {
    val iniciales = sub.miembroNombre.split(" ")
        .take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")

    val fechaFmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es"))
    val horaFmt  = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

    val statusColor = when (sub.status) {
        "approved", "pending_board" -> Sage
        "rejected"                  -> Blush
        else                        -> Muted
    }
    val statusLabel = when (sub.status) {
        "approved"      -> "Aprobado"
        "pending_board" -> "Pend. junta"
        "rejected"      -> "Rechazado"
        else            -> "Pendiente"
    }

    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeuAvatar(iniciales = iniciales, size = 40.dp)
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = sub.miembroNombre,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = sub.recordDate.format(fechaFmt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    sub.markedAt?.let { ts ->
                        Text(
                            text  = "  ·  ${horaFmt.format(ts)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                when (markerType) {
                    "realizado", "checkbox" -> Icon(
                        imageVector        = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint               = if (sub.isDone) Sage else Muted,
                        modifier           = Modifier.size(24.dp),
                    )
                    "monetary" -> Text(
                        text       = "₡${sub.monto?.toLong() ?: 0}",
                        style      = MaterialTheme.typography.bodyLarge,
                        color      = Accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                    else -> Text(
                        text       = "${sub.count ?: 0} $unitLabel",
                        style      = MaterialTheme.typography.bodyLarge,
                        color      = Accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (markerType == "monetary") {
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text  = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                        )
                    }
                }
            }
        }
    }
}
