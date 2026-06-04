package com.gpleader.app.feature.auth

import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuButtonPrimary

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.gpleader.app.core.ui.theme.neuElevated

// ── Entry point (Hilt) ────────────────────────────────────────────────────────

@Composable
fun QuienEresScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCambiarContrasena: () -> Unit = {},
    onNavigateToConfirmarIdentidad: (miembroId: String, miembroNombre: String) -> Unit = { _, _ -> },
    onNavigateToLogin: () -> Unit = {},
    viewModel: QuienEresViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            onNavigateToHome()
            viewModel.consumeHomeNavigation()
        }
    }

    LaunchedEffect(uiState.navigateToCambiarContrasena) {
        if (uiState.navigateToCambiarContrasena) {
            onNavigateToCambiarContrasena()
            viewModel.consumeCambiarContrasenaNavigation()
        }
    }

    LaunchedEffect(uiState.navigateToConfirmarIdentidad) {
        val miembro = uiState.navigateToConfirmarIdentidad
        if (miembro != null) {
            onNavigateToConfirmarIdentidad(miembro.id, miembro.nombre)
            viewModel.consumeConfirmarIdentidadNavigation()
        }
    }

    QuienEresContent(
        uiState               = uiState,
        onMiembroClick        = viewModel::onMiembroSelected,
        onContrasenaChange    = viewModel::onContrasenaDialogChange,
        onConfirmarContrasena = viewModel::onConfirmarContrasena,
        onDismissDialog       = viewModel::onDismissPasswordDialog,
        onNavigateToLogin     = onNavigateToLogin,
    )
}

// ── Content composable ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuienEresContent(
    uiState: QuienEresUiState,
    onMiembroClick: (MiembroSesion) -> Unit,
    onContrasenaChange: (String) -> Unit = {},
    onConfirmarContrasena: () -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // Scrollable: header + cards
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .neuElevated(cornerRadius = 20.dp)
                    .background(Background, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Default.Group,
                    contentDescription = null,
                    tint               = Accent,
                    modifier           = Modifier.size(32.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text       = "¿Quién entra?",
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text      = "Selecciona tu nombre. Los líderes necesitan contraseña.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = Mid,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text     = "INICIAR SESIÓN",
                style    = MaterialTheme.typography.labelSmall,
                color    = Accent,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = "Bienvenido",
                style      = MaterialTheme.typography.headlineMedium,
                color      = Ink,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text     = "Elige tu GP y toca tu nombre para continuar.",
                style    = MaterialTheme.typography.bodyMedium,
                color    = Mid,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            // ── Tarjeta del GP ────────────────────────────────────────────────
            Text(
                text     = "TU GP",
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuElevated(cornerRadius = 14.dp)
                    .background(Background, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column {
                    Text(
                        text       = uiState.grupoNombre,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = Ink,
                    )
                    val ubicacion = listOf(
                        uiState.iglesiaNombre,
                        uiState.districtNombre,
                        uiState.campoNombre,
                    ).filter { it.isNotBlank() }.joinToString(" · ")
                    if (ubicacion.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = ubicacion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Lista de miembros ─────────────────────────────────────────────
            Text(
                text     = "MIEMBROS",
                style    = MaterialTheme.typography.labelSmall,
                color    = Muted,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))

            when {
                uiState.isLoading -> {
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator(color = Accent)
                }
                uiState.error != null -> {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text      = uiState.error,
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = Blush,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        uiState.miembros.forEach { miembro ->
                            MiembroRow(
                                miembro = miembro,
                                onClick = { onMiembroClick(miembro) },
                            )
                        }
                    }
                }
            }
        }

        // Botón fijo al fondo
        NeuButtonSecondary(
            text     = "Cambiar de grupo",
            onClick  = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        )
    }

    // ── Dialog contraseña líder ───────────────────────────────────────────────
    if (uiState.showPasswordDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            containerColor   = Background,
            title = {
                Text(
                    text  = stringResource(R.string.quien_eres_password_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
            },
            text = {
                Column {
                    Text(
                        text  = "Hola, ${uiState.pendingLider?.nombre ?: ""}. Ingresá la contraseña del grupo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mid,
                    )
                    Spacer(Modifier.height(16.dp))
                    NeuTextField(
                        value           = uiState.contrasenaDialog,
                        onValueChange   = onContrasenaChange,
                        label           = null,
                        placeholder     = "Contraseña",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isPassword      = true,
                        isError         = uiState.authError != null,
                        modifier        = Modifier.fillMaxWidth(),
                    )
                    if (uiState.authError != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text  = uiState.authError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Blush,
                        )
                    }
                }
            },
            confirmButton = {
                if (uiState.isAuthenticating) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(24.dp))
                } else {
                    NeuButtonPrimary(text = "Ingresar", onClick = onConfirmarContrasena)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) {
                    Text(text = "Cancelar", color = Mid)
                }
            },
        )
    }

}

// ── Fila de miembro ───────────────────────────────────────────────────────────

@Composable
private fun MiembroRow(
    miembro: MiembroSesion,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 14.dp)
            .background(Background, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeuAvatar(iniciales = miembro.iniciales, size = 40.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = miembro.nombre,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = Ink,
                )
                if (miembro.isLider) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = "Líder",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun QuienEresPreview() {
    GpLeaderTheme {
        QuienEresContent(
            uiState = QuienEresUiState(
                grupoNombre = "GP Los Olivos",
                miembros = listOf(
                    MiembroSesion("m1", "Ana Martínez López", "AM", isLider = true),
                    MiembroSesion("m2", "Juan Carlos Pérez",  "JP"),
                    MiembroSesion("m3", "María García",       "MG"),
                ),
            ),
            onMiembroClick  = {},
        )
    }
}
