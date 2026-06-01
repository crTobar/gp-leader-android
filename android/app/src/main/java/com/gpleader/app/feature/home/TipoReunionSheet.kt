package com.gpleader.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipoReunionSheet(
    onGpMeeting:       () -> Unit,
    onSaturdayWorship: () -> Unit,
    onDismiss:         () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text  = "TIPO DE REGISTRO",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )

            Spacer(Modifier.height(12.dp))

            // ── Reunión GP ────────────────────────────────────────────────────
            NeuCard(
                onClick  = onGpMeeting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NeuIconBadge(
                        icon  = Icons.Default.DateRange,
                        tint  = Accent,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text  = "Reunión GP",
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink,
                        )
                        Text(
                            text  = "Registro semanal del grupo pequeño",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mid,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Culto de sábado ───────────────────────────────────────────────
            NeuCard(
                onClick  = onSaturdayWorship,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NeuIconBadge(
                        icon  = Icons.Default.Star,
                        tint  = Gold,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text  = "Culto de sábado",
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink,
                        )
                        Text(
                            text  = "Asistencia al culto de iglesia del sábado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mid,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            NeuButtonSecondary(
                text     = "Cancelar",
                onClick  = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun NeuIconBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
) {
    androidx.compose.foundation.layout.Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .neuElevated(cornerRadius = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.12f)),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = tint,
            modifier           = Modifier.size(22.dp),
        )
    }
}
