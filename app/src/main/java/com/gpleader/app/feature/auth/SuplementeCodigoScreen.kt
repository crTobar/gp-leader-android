package com.gpleader.app.feature.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInset
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.PI
import androidx.compose.foundation.clickable

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun SuplementeCodigoScreen(
    onNavigateBack:        () -> Unit,
    onNavigateToBienvenida: () -> Unit,
    viewModel: SuplementeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToBienvenida) {
        if (uiState.navigateToBienvenida) {
            onNavigateToBienvenida()
            viewModel.consumeBienvenidaNavigation()
        }
    }

    SuplementeCodigoContent(
        uiState        = uiState,
        onNavigateBack = onNavigateBack,
        onCodigoChange = viewModel::onCodigoChange,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun SuplementeCodigoContent(
    uiState:        SuplementeUiState,
    onNavigateBack: () -> Unit,
    onCodigoChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Shake animation triggered on each new error
    var errorTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.codigoError) {
        if (uiState.codigoError != null) errorTrigger++
    }
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(errorTrigger) {
        if (errorTrigger > 0) {
            for (i in 0..3) {
                shakeAnim.animateTo(if (i % 2 == 0) 12f else -12f, tween(70))
            }
            shakeAnim.animateTo(0f, tween(70))
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopBar(onNavigateBack = onNavigateBack)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))

            // Hero icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .neuElevatedSm(cornerRadius = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BackgroundDeep),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "🔑",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text  = stringResource(R.string.suplente_codigo_titulo),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.suplente_codigo_instruccion),
                style     = MaterialTheme.typography.bodyMedium,
                color     = Mid,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(40.dp))

            // 6 code boxes (with shake offset)
            CodigoBoxes(
                codigo      = uiState.codigo,
                isError     = uiState.codigoError != null,
                isValidating = uiState.isValidating,
                modifier    = Modifier.offset { IntOffset(shakeAnim.value.roundToInt(), 0) },
            )

            // Invisible BasicTextField — captures numeric keyboard
            BasicTextField(
                value         = uiState.codigo,
                onValueChange = onCodigoChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                textStyle       = MaterialTheme.typography.bodyLarge.copy(color = Color.Transparent),
                cursorBrush     = SolidColor(Color.Transparent),
                modifier        = Modifier
                    .size(1.dp)
                    .alpha(0f)
                    .focusRequester(focusRequester),
            )

            Spacer(Modifier.height(20.dp))

            // Feedback area
            if (uiState.isValidating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        color       = Accent,
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text  = stringResource(R.string.suplente_codigo_validando),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            } else if (uiState.codigoError != null) {
                Text(
                    text      = uiState.codigoError,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = Blush,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
                contentDescription = "Atrás",
                tint               = Ink,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
}

// ── Code digit boxes ──────────────────────────────────────────────────────────

@Composable
private fun CodigoBoxes(
    codigo:      String,
    isError:     Boolean,
    isValidating: Boolean,
    modifier:    Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(6) { index ->
            val char     = codigo.getOrNull(index)
            val isFilled = char != null
            val isCurrent = index == codigo.length && !isValidating

            val accentBorderModifier = when {
                isError -> Modifier.drawWithContent {
                    drawContent()
                    val strokePx = 1.5.dp.toPx()
                    drawRoundRect(
                        color        = Blush,
                        topLeft      = Offset(strokePx / 2, strokePx / 2),
                        size         = Size(size.width - strokePx, size.height - strokePx),
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        style        = Stroke(width = strokePx),
                    )
                }
                isCurrent -> Modifier.drawWithContent {
                    drawContent()
                    val strokePx = 1.5.dp.toPx()
                    drawRoundRect(
                        color        = Accent,
                        topLeft      = Offset(strokePx / 2, strokePx / 2),
                        size         = Size(size.width - strokePx, size.height - strokePx),
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        style        = Stroke(width = strokePx),
                    )
                }
                else -> Modifier
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.75f)
                    .neuInset(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isFilled) BackgroundDeep else Background)
                    .then(accentBorderModifier),
                contentAlignment = Alignment.Center,
            ) {
                if (char != null) {
                    Text(
                        text  = char.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                    )
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true, name = "Código — vacío")
@Composable
private fun SuplementeCodigoEmptyPreview() {
    GpLeaderTheme {
        SuplementeCodigoContent(
            uiState        = SuplementeUiState(),
            onNavigateBack = {},
            onCodigoChange = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Código — parcial")
@Composable
private fun SuplementeCodigoParcialPreview() {
    GpLeaderTheme {
        SuplementeCodigoContent(
            uiState        = SuplementeUiState(codigo = "123"),
            onNavigateBack = {},
            onCodigoChange = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Código — validando")
@Composable
private fun SuplementeCodigoValidandoPreview() {
    GpLeaderTheme {
        SuplementeCodigoContent(
            uiState        = SuplementeUiState(codigo = "123456", isValidating = true),
            onNavigateBack = {},
            onCodigoChange = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Código — error")
@Composable
private fun SuplementeCodigoErrorPreview() {
    GpLeaderTheme {
        SuplementeCodigoContent(
            uiState = SuplementeUiState(
                codigoError = "Código inválido o expirado. Verifica con tu líder.",
            ),
            onNavigateBack = {},
            onCodigoChange = {},
        )
    }
}
