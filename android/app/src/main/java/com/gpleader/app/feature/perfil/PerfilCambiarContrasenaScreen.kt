package com.gpleader.app.feature.perfil

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInset

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun PerfilCambiarContrasenaScreen(
    onNavigateBack: () -> Unit,
    esPrimerLogin: Boolean = false,
    viewModel: PerfilViewModel = hiltViewModel(),
) {
    val uiState          by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarColor    by remember { mutableStateOf(Sage) }

    val successMsg = stringResource(R.string.cambiar_contrasena_success)

    LaunchedEffect(uiState.passwordUpdateSuccess) {
        if (uiState.passwordUpdateSuccess) {
            snackbarColor = Sage
            viewModel.consumePasswordUpdateSuccess()
            snackbarHostState.showSnackbar(successMsg)
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.passwordUpdateError) {
        val error = uiState.passwordUpdateError
        if (error != null) {
            snackbarColor = Blush
            snackbarHostState.showSnackbar(error)
            viewModel.consumePasswordUpdateError()
        }
    }

    CambiarContrasenaContent(
        uiState               = uiState,
        esPrimerLogin         = esPrimerLogin,
        snackbarHostState     = snackbarHostState,
        snackbarColor         = snackbarColor,
        onNavigateBack        = onNavigateBack,
        onContrasenaActualChange  = viewModel::onContrasenaActualChange,
        onNuevaContrasenaChange   = viewModel::onNuevaContrasenaChange,
        onConfirmarContrasenaChange = viewModel::onConfirmarContrasenaChange,
        onActualizarClick     = { viewModel.onActualizarContrasenaClick(esPrimerLogin) },
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun CambiarContrasenaContent(
    uiState:                    PerfilUiState,
    esPrimerLogin:              Boolean = false,
    snackbarHostState:          SnackbarHostState,
    snackbarColor:              Color,
    onNavigateBack:             () -> Unit,
    onContrasenaActualChange:   (String) -> Unit,
    onNuevaContrasenaChange:    (String) -> Unit,
    onConfirmarContrasenaChange: (String) -> Unit,
    onActualizarClick:          () -> Unit,
) {
    val allMet = uiState.requisitos.todosCompletos &&
        (esPrimerLogin || uiState.contrasenaActual.isNotBlank())

    Scaffold(
        containerColor = Background,
        snackbarHost   = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = snackbarColor,
                    contentColor   = Color.White,
                    snackbarData   = data,
                    modifier       = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        },
        topBar = {
            CambiarContrasenaTopBar(
                onNavigateBack = onNavigateBack,
                modifier       = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                NeuButtonPrimary(
                    text     = stringResource(R.string.cambiar_contrasena_btn_actualizar),
                    onClick  = onActualizarClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (allMet) 1f else 0.5f),
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Hero ─────────────────────────────────────────────────────────
            item {
                HeroSection()
            }

            // ── Contraseña actual (solo si no es primer login) ────────────────
            if (!esPrimerLogin) {
                item {
                    NeuTextField(
                        value         = uiState.contrasenaActual,
                        onValueChange = onContrasenaActualChange,
                        label         = stringResource(R.string.cambiar_contrasena_label_actual),
                        placeholder   = "",
                        isPassword    = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Next,
                        ),
                    )
                }
            }

            // ── Nueva contraseña ──────────────────────────────────────────────
            item {
                val req = uiState.requisitos
                val nuevaConBorde = uiState.nuevaContrasena.isNotEmpty()
                NeuTextField(
                    value         = uiState.nuevaContrasena,
                    onValueChange = onNuevaContrasenaChange,
                    label         = stringResource(R.string.cambiar_contrasena_label_nueva),
                    placeholder   = stringResource(R.string.cambiar_contrasena_placeholder_nueva),
                    isPassword    = true,
                    isSuccess     = nuevaConBorde && req.tieneOchoCaracteres && req.tieneMayuscula && req.tieneNumero,
                    isError       = nuevaConBorde && !(req.tieneOchoCaracteres && req.tieneMayuscula && req.tieneNumero),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Next,
                    ),
                )
            }

            // ── Confirmar contraseña ──────────────────────────────────────────
            item {
                val confirmarConBorde = uiState.confirmarContrasena.isNotEmpty()
                NeuTextField(
                    value         = uiState.confirmarContrasena,
                    onValueChange = onConfirmarContrasenaChange,
                    label         = stringResource(R.string.cambiar_contrasena_label_confirmar),
                    placeholder   = stringResource(R.string.cambiar_contrasena_placeholder_confirmar),
                    isPassword    = true,
                    isSuccess     = confirmarConBorde && uiState.requisitos.contrasenasCoinciden,
                    isError       = confirmarConBorde && !uiState.requisitos.contrasenasCoinciden,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done,
                    ),
                )
            }

            // ── Checklist de requisitos ───────────────────────────────────────
            item {
                ChecklistCard(requisitos = uiState.requisitos)
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun CambiarContrasenaTopBar(
    onNavigateBack: () -> Unit,
    modifier:       Modifier = Modifier,
) {
    Box(
        modifier         = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
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

        Text(
            text  = stringResource(R.string.cambiar_contrasena_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )
    }
}

// ── Hero section ──────────────────────────────────────────────────────────────

@Composable
private fun HeroSection() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Ícono candado en NeuCard cuadrado
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .neuElevated(cornerRadius = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Background),
        ) {
            Icon(
                imageVector        = Icons.Filled.Lock,
                contentDescription = null,
                tint               = Accent,
                modifier           = Modifier.size(32.dp),
            )
        }

        Text(
            text  = stringResource(R.string.cambiar_contrasena_hero_titulo),
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
        )
        Text(
            text      = stringResource(R.string.cambiar_contrasena_hero_subtitulo),
            style     = MaterialTheme.typography.bodyMedium,
            color     = Mid,
            textAlign = TextAlign.Center,
            maxLines  = 2,
            modifier  = Modifier.padding(horizontal = 16.dp),
        )
    }
}

// ── Checklist card ────────────────────────────────────────────────────────────

@Composable
private fun ChecklistCard(requisitos: PasswordRequisitos) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Background),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text     = stringResource(R.string.cambiar_contrasena_checklist_header),
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.padding(bottom = 14.dp),
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RequisitoCelda(
                    cumplido = requisitos.tieneOchoCaracteres,
                    label    = stringResource(R.string.cambiar_contrasena_req_8_chars),
                )
                RequisitoCelda(
                    cumplido = requisitos.tieneMayuscula,
                    label    = stringResource(R.string.cambiar_contrasena_req_mayuscula),
                )
                RequisitoCelda(
                    cumplido = requisitos.tieneNumero,
                    label    = stringResource(R.string.cambiar_contrasena_req_numero),
                )
                RequisitoCelda(
                    cumplido = requisitos.contrasenasCoinciden,
                    label    = stringResource(R.string.cambiar_contrasena_req_coinciden),
                )
            }
        }
    }
}

@Composable
private fun RequisitoCelda(
    cumplido: Boolean,
    label:    String,
) {
    val animSpec      = spring<Color>()
    val checkboxBg    by animateColorAsState(
        targetValue   = if (cumplido) Sage else Background,
        animationSpec = animSpec,
        label         = "checkboxBg",
    )
    val textColor     by animateColorAsState(
        targetValue   = if (cumplido) Sage else Mid,
        animationSpec = animSpec,
        label         = "checkText",
    )

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Checkbox animado
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .then(
                    if (cumplido) Modifier.neuInset(cornerRadius = 6.dp)
                    else          Modifier.neuElevatedSm(cornerRadius = 6.dp)
                )
                .clip(RoundedCornerShape(6.dp))
                .background(checkboxBg),
        ) {
            if (cumplido) {
                Icon(
                    imageVector        = Icons.Filled.Check,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(13.dp),
                )
            }
        }

        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun CambiarContrasenaPreview() {
    GpLeaderTheme {
        CambiarContrasenaContent(
            uiState = PerfilUiState(
                contrasenaActual    = "MiContraActual",
                nuevaContrasena     = "Abc12345",
                confirmarContrasena = "Abc12345",
                requisitos = PasswordRequisitos(
                    tieneOchoCaracteres  = true,
                    tieneMayuscula       = true,
                    tieneNumero          = true,
                    contrasenasCoinciden = true,
                ),
            ),
            snackbarHostState           = remember { SnackbarHostState() },
            snackbarColor               = Sage,
            onNavigateBack              = {},
            onContrasenaActualChange    = {},
            onNuevaContrasenaChange     = {},
            onConfirmarContrasenaChange = {},
            onActualizarClick           = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true, name = "Requisitos pendientes")
@Composable
private fun CambiarContrasenaVacioPreview() {
    GpLeaderTheme {
        CambiarContrasenaContent(
            uiState = PerfilUiState(
                contrasenaActual    = "",
                nuevaContrasena     = "abc",
                confirmarContrasena = "ab",
                requisitos = PasswordRequisitos(
                    tieneOchoCaracteres  = false,
                    tieneMayuscula       = false,
                    tieneNumero          = false,
                    contrasenasCoinciden = false,
                ),
            ),
            snackbarHostState           = remember { SnackbarHostState() },
            snackbarColor               = Sage,
            onNavigateBack              = {},
            onContrasenaActualChange    = {},
            onNuevaContrasenaChange     = {},
            onConfirmarContrasenaChange = {},
            onActualizarClick           = {},
        )
    }
}
