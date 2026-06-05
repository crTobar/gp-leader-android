package com.gpleader.app.feature.miembro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Violet
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInset
import com.gpleader.app.core.ui.theme.neuInsetSm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuplementeHomeScreen(
    onRegistrarGP:     () -> Unit,
    onRegistrarSabado: () -> Unit,
    onSalir:           () -> Unit,
    viewModel: SuplementeHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // ── Contenido ─────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 16.dp),
        ) {
            // Header — mismo estilo que HomeHeader
            Text(
                text  = "HOY REGISTRÁS COMO SUPLENTE",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = uiState.grupoNombre,
                style      = MaterialTheme.typography.displayLarge.copy(
                    fontSize   = 38.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 40.sp,
                ),
                color = Ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Elegí qué tipo de reunión querés registrar",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = Mid,
            )

            Spacer(Modifier.height(32.dp))

            // ── RegistrarCard ─────────────────────────────────────────────────
            NeuCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 26.dp, vertical = 30.dp)) {
                    Text(
                        text  = "HOY",
                        style = MaterialTheme.typography.labelSmall,
                        color = Accent,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text  = "Aún no has tomado asistencia",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontStyle  = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            fontSize   = 24.sp,
                            lineHeight = 28.sp,
                        ),
                        color = Ink,
                    )
                    Spacer(Modifier.height(22.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuGlow(cornerRadius = 14.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Accent)
                            .clickable { showSheet = true }
                            .padding(vertical = 16.dp),
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint               = Color.White,
                                modifier           = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text       = "Tomar Asistencia",
                                style      = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                                color      = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        // ── Botón Salir ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Background)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            NeuButtonSecondary(
                text     = "Salir del modo suplente",
                onClick  = onSalir,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // ── Sheet tipo de reunión ─────────────────────────────────────────────────
    if (showSheet) {
        TipoRegistroSheet(
            onDismiss   = { showSheet = false },
            onGpMeeting = { showSheet = false; onRegistrarGP() },
            onSabado    = { showSheet = false; onRegistrarSabado() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TipoRegistroSheet(
    onDismiss:   () -> Unit,
    onGpMeeting: () -> Unit,
    onSabado:    () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Background,
        dragHandle       = null,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Handle pill
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .neuInset(cornerRadius = 3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BackgroundDeep),
                )
            }
            Spacer(Modifier.height(4.dp))
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text  = "¿Qué deseas registrar?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "Elige el tipo de registro para esta sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }
            Spacer(Modifier.height(4.dp))
            RegistrarOpcion(
                icon      = Icons.Filled.Groups,
                titulo    = "Reunión de GP",
                subtitulo = "Registro semanal del grupo pequeño",
                color     = Accent,
                onClick   = onGpMeeting,
            )
            RegistrarOpcion(
                icon      = Icons.Filled.AutoAwesome,
                titulo    = "Culto de Sábado",
                subtitulo = "Asistencia al culto del sábado",
                color     = Violet,
                onClick   = onSabado,
            )
        }
    }
}

@Composable
private fun RegistrarOpcion(
    icon:      ImageVector,
    titulo:    String,
    subtitulo: String,
    color:     Color,
    onClick:   () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Background)
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(color),
            )
            Spacer(Modifier.width(16.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .neuInsetSm(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundDeep),
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = color,
                    modifier           = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(
                modifier            = Modifier
                    .weight(1f)
                    .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text       = titulo,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text  = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
            }
            Spacer(Modifier.width(16.dp))
        }
    }
}
