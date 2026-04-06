package com.gpleader.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted

// ── Entry point (Hilt) ────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSuplente: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onNavigateToHome()
            viewModel.consumeLoginSuccess()
        }
    }

    LaunchedEffect(uiState.navigateToSuplente) {
        if (uiState.navigateToSuplente) {
            onNavigateToSuplente()
            viewModel.consumeSuplementeNavigation()
        }
    }

    LoginScreenContent(
        uiState            = uiState,
        onCorreoChange     = viewModel::onCorreoChange,
        onContrasenaChange = viewModel::onContrasenaChange,
        onLoginClick       = viewModel::onLoginClick,
        onSuplementeClick  = viewModel::onSuplementeClick,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSuplementeClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LoginHeader(
            modifier = Modifier
                .fillMaxWidth()
                .weight(35f),
        )
        LoginBody(
            uiState            = uiState,
            onCorreoChange     = onCorreoChange,
            onContrasenaChange = onContrasenaChange,
            onLoginClick       = onLoginClick,
            onSuplementeClick  = onSuplementeClick,
            modifier           = Modifier
                .fillMaxWidth()
                .weight(65f),
        )
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun LoginHeader(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier.background(Ink),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo placeholder
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(
                        width = 1.5.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "GP",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text  = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text  = stringResource(R.string.login_header_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }
    }
}

// ── Body ──────────────────────────────────────────────────────────────────────

@Composable
private fun LoginBody(
    uiState: LoginUiState,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSuplementeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var contrasenaVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            text  = stringResource(R.string.login_title),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text  = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = Mid,
        )

        Spacer(Modifier.height(24.dp))

        NeuTextField(
            value         = uiState.correo,
            onValueChange = onCorreoChange,
            label         = stringResource(R.string.login_label_correo),
            placeholder   = stringResource(R.string.login_placeholder_correo),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier      = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        NeuTextField(
            value                = uiState.contrasena,
            onValueChange        = onContrasenaChange,
            label                = stringResource(R.string.login_label_contrasena),
            placeholder          = stringResource(R.string.login_label_contrasena),
            visualTransformation = if (contrasenaVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier             = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = stringResource(R.string.login_olvide_contrasena),
            style     = MaterialTheme.typography.bodyMedium,
            color     = Accent,
            textAlign = TextAlign.End,
            modifier  = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: flujo recuperar contraseña */ },
        )

        Spacer(Modifier.height(16.dp))

        NoticeCard()

        // Espacio flexible: empuja los botones hacia la parte inferior
        Spacer(Modifier.height(32.dp))

        if (uiState.error != null) {
            Text(
                text     = uiState.error,
                style    = MaterialTheme.typography.bodyMedium,
                color    = Blush,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier         = Modifier.fillMaxWidth().height(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Accent)
            }
        } else {
            NeuButtonPrimary(
                text     = stringResource(R.string.login_btn_entrar),
                onClick  = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(20.dp))

        SeparatorO()

        Spacer(Modifier.height(20.dp))

        NeuButtonSecondary(
            text     = stringResource(R.string.login_btn_suplente),
            onClick  = onSuplementeClick,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ── Notice card con borde punteado ────────────────────────────────────────────

@Composable
private fun NoticeCard() {
    val noticeText = buildAnnotatedString {
        append(stringResource(R.string.login_notice_prefix))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Ink)) {
            append(stringResource(R.string.login_notice_bold))
        }
        append(stringResource(R.string.login_notice_suffix))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Background)
            .dashedBorder(color = Muted, cornerRadius = 14.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text  = noticeText,
            style = MaterialTheme.typography.bodyMedium,
            color = Mid,
        )
    }
}

// ── Separador "o" ─────────────────────────────────────────────────────────────

@Composable
private fun SeparatorO() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = Muted,
            thickness = 1.dp,
        )
        Text(
            text     = stringResource(R.string.login_separator_o),
            style    = MaterialTheme.typography.bodyMedium,
            color    = Muted,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = Muted,
            thickness = 1.dp,
        )
    }
}

// ── Dashed border modifier ────────────────────────────────────────────────────

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp = 14.dp,
    strokeWidth: Dp  = 1.dp,
    dashWidth: Dp    = 6.dp,
    gapWidth: Dp     = 4.dp,
): Modifier = drawBehind {
    val strokePx = strokeWidth.toPx()
    val dashPx   = dashWidth.toPx()
    val gapPx    = gapWidth.toPx()
    val cornerPx = cornerRadius.toPx()

    drawIntoCanvas { canvas ->
        val paint = Paint()
        paint.asFrameworkPaint().apply {
            isAntiAlias = true
            style       = android.graphics.Paint.Style.STROKE
            this.strokeWidth = strokePx
            this.color  = color.toArgb()
            pathEffect  = android.graphics.DashPathEffect(
                floatArrayOf(dashPx, gapPx), 0f,
            )
        }
        canvas.drawRoundRect(
            left         = strokePx / 2f,
            top          = strokePx / 2f,
            right        = size.width  - strokePx / 2f,
            bottom       = size.height - strokePx / 2f,
            radiusX      = cornerPx,
            radiusY      = cornerPx,
            paint        = paint,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true, name = "Login — vacío")
@Composable
private fun LoginScreenEmptyPreview() {
    GpLeaderTheme {
        LoginScreenContent(
            uiState            = LoginUiState(),
            onCorreoChange     = {},
            onContrasenaChange = {},
            onLoginClick       = {},
            onSuplementeClick  = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Login — con datos")
@Composable
private fun LoginScreenFilledPreview() {
    GpLeaderTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                correo     = "juan.perez@iglesia.org",
                contrasena = "••••••••",
            ),
            onCorreoChange     = {},
            onContrasenaChange = {},
            onLoginClick       = {},
            onSuplementeClick  = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Login — error")
@Composable
private fun LoginScreenErrorPreview() {
    GpLeaderTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                correo     = "juan.perez@iglesia.org",
                contrasena = "wrong",
                error      = "Correo o contraseña incorrectos.",
            ),
            onCorreoChange     = {},
            onContrasenaChange = {},
            onLoginClick       = {},
            onSuplementeClick  = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Login — cargando")
@Composable
private fun LoginScreenLoadingPreview() {
    GpLeaderTheme {
        LoginScreenContent(
            uiState            = LoginUiState(isLoading = true),
            onCorreoChange     = {},
            onContrasenaChange = {},
            onLoginClick       = {},
            onSuplementeClick  = {},
        )
    }
}
