package com.gpleader.app.feature.miembro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInset
import kotlin.math.roundToInt

@Composable
fun SuplementeCodigoEntryScreen(
    onNavigateBack:        () -> Unit,
    onCodigoValidado:      (deputyCodeId: String) -> Unit,
    viewModel: SuplementeCodigoEntryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(uiState.deputyCodeId) {
        if (uiState.deputyCodeId.isNotBlank()) {
            onCodigoValidado(uiState.deputyCodeId)
        }
    }

    LaunchedEffect(uiState.errorTrigger) {
        if (uiState.errorTrigger > 0) {
            repeat(3) {
                shakeOffset.animateTo(12f, tween(60))
                shakeOffset.animateTo(-12f, tween(60))
            }
            shakeOffset.animateTo(0f, tween(60))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Back button ───────────────────────────────────────────────────────
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
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text       = "Código de suplente",
            style      = MaterialTheme.typography.headlineMedium,
            color      = Ink,
            fontWeight = FontWeight.SemiBold,
            textAlign  = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = "Ingresá el código de 6 dígitos que te compartió el líder.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = Mid,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp),
        )

        Spacer(Modifier.height(48.dp))

        // ── 6 boxes ───────────────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        ) {
            uiState.digitos.forEachIndexed { index, digit ->
                val esCursor = index == uiState.digitos.count { it.isNotEmpty() }.coerceAtMost(5)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .then(
                            if (digit.isNotEmpty() || esCursor)
                                Modifier.neuInset(cornerRadius = 12.dp)
                            else
                                Modifier.neuElevatedSm(cornerRadius = 12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(Background),
                ) {
                    Text(
                        text       = digit.ifEmpty { "" },
                        style      = MaterialTheme.typography.titleLarge,
                        color      = if (digit.isNotEmpty()) Accent else Muted,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (uiState.error != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text  = uiState.error!!,
                style = MaterialTheme.typography.bodyMedium,
                color = Blush,
            )
        }

        Spacer(Modifier.height(48.dp))

        // ── Teclado numérico ──────────────────────────────────────────────────
        val teclas = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
        teclas.chunked(3).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                fila.forEach { tecla ->
                    if (tecla.isEmpty()) {
                        Spacer(Modifier.size(80.dp))
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(80.dp)
                                .neuElevatedSm(cornerRadius = 20.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Background)
                                .clickable {
                                    if (tecla == "⌫") viewModel.onBorrar()
                                    else viewModel.onDigito(tecla)
                                },
                        ) {
                            Text(
                                text       = tecla,
                                style      = MaterialTheme.typography.titleLarge,
                                color      = Ink,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.weight(1f))

        // ── Botón confirmar ───────────────────────────────────────────────────
        if (uiState.isValidando) {
            CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
        } else {
            NeuButtonPrimary(
                text     = "Confirmar",
                enabled  = uiState.digitos.all { it.isNotEmpty() },
                onClick  = viewModel::onConfirmar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}
