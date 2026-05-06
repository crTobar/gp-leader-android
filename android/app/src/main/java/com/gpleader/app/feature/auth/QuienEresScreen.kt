package com.gpleader.app.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.data.repository.IglesiaItem
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Violet
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuInset

// ── Entry point (Hilt) ────────────────────────────────────────────────────────

@Composable
fun QuienEresScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCambiarContrasena: () -> Unit = {},
    onNavigateToConfirmacion: (String) -> Unit = {},
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

    LaunchedEffect(uiState.navigateToConfirmacion) {
        val iglesiaNombre = uiState.navigateToConfirmacion
        if (iglesiaNombre != null) {
            onNavigateToConfirmacion(iglesiaNombre)
            viewModel.consumeConfirmacionNavigation()
        }
    }

    QuienEresContent(
        uiState               = uiState,
        onMiembroClick        = viewModel::onMiembroSelected,
        onContrasenaChange    = viewModel::onContrasenaDialogChange,
        onConfirmarContrasena = viewModel::onConfirmarContrasena,
        onDismissDialog       = viewModel::onDismissPasswordDialog,
        onDismissSabadoSheet  = viewModel::onDismissSabadoSheet,
        onSabadoBusqueda      = viewModel::onSabadoBusquedaChange,
        onSabadoIglesiaSelect = viewModel::onSabadoIglesiaSelected,
        onSabadoUsarPropia    = viewModel::onSabadoUsarIglesiaPropia,
        onSabadoMarcar        = viewModel::onSabadoMarcarClick,
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
    onDismissSabadoSheet: () -> Unit = {},
    onSabadoBusqueda: (String) -> Unit = {},
    onSabadoIglesiaSelect: (IglesiaItem) -> Unit = {},
    onSabadoUsarPropia: () -> Unit = {},
    onSabadoMarcar: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))

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
            text     = "MIEMBROS DEL GRUPO",
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
                LazyColumn {
                    items(uiState.miembros) { miembro ->
                        MiembroRow(
                            miembro = miembro,
                            onClick = { onMiembroClick(miembro) },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        if (uiState.showNoEsSabadoMsg) {
            Spacer(Modifier.height(16.dp))
            Text(
                text      = stringResource(R.string.quien_eres_no_sabado),
                style     = MaterialTheme.typography.bodyMedium,
                color     = Mid,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDeep, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
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

    // ── Bottom sheet sábado ───────────────────────────────────────────────────
    if (uiState.showSabadoSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var mostrarBusqueda by remember { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest   = onDismissSabadoSheet,
            sheetState         = sheetState,
            containerColor     = Background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
            ) {
                // ── Header ────────────────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Violet.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Church,
                            contentDescription = null,
                            tint               = Violet,
                            modifier           = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text       = uiState.sabadoMiembroNombre,
                            style      = MaterialTheme.typography.titleLarge,
                            color      = Ink,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text  = "¿A qué iglesia fuiste hoy?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mid,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Iglesia seleccionada ───────────────────────────────────────
                Text(
                    text     = "IGLESIA",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Muted,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                IglesiaSeleccionadaCard(
                    nombre   = uiState.sabadoSelectedIglesiaNombre,
                )

                Spacer(Modifier.height(12.dp))

                // ── Buscar otra iglesia ────────────────────────────────────────
                TextButton(
                    onClick  = { mostrarBusqueda = !mostrarBusqueda },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(
                        text  = if (mostrarBusqueda) "Cerrar búsqueda" else "Cambiar iglesia",
                        color = Violet,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                AnimatedVisibility(visible = mostrarBusqueda) {
                    Column {
                        Spacer(Modifier.height(4.dp))

                        // Buscador
                        TextField(
                            value         = uiState.sabadoBusqueda,
                            onValueChange = onSabadoBusqueda,
                            placeholder   = { Text("Buscar iglesia…", color = Muted) },
                            leadingIcon   = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Muted)
                            },
                            singleLine    = true,
                            colors        = TextFieldDefaults.colors(
                                focusedContainerColor   = BackgroundDeep,
                                unfocusedContainerColor = BackgroundDeep,
                                focusedIndicatorColor   = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor             = Violet,
                            ),
                            shape    = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(8.dp))

                        if (uiState.sabadoIglesiasLoading) {
                            Box(
                                modifier         = Modifier.fillMaxWidth().height(80.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = Violet, modifier = Modifier.size(24.dp))
                            }
                        } else {
                            val filtradas = uiState.sabadoIglesias.filter {
                                uiState.sabadoBusqueda.isBlank() ||
                                it.nombre.contains(uiState.sabadoBusqueda, ignoreCase = true)
                            }
                            Column {
                                filtradas.take(8).forEach { iglesia ->
                                    val isSelected = iglesia.id == uiState.sabadoSelectedIglesiaId
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Violet.copy(alpha = 0.08f) else BackgroundDeep)
                                            .then(if (isSelected) Modifier.drawWithContent {
                                                drawContent()
                                                val s = 1.5.dp.toPx()
                                                drawRoundRect(
                                                    color        = Violet,
                                                    topLeft      = Offset(s / 2, s / 2),
                                                    size         = Size(size.width - s, size.height - s),
                                                    cornerRadius = CornerRadius(10.dp.toPx()),
                                                    style        = Stroke(s),
                                                )
                                            } else Modifier)
                                            .clickable { onSabadoIglesiaSelect(iglesia) }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text       = iglesia.nombre,
                                            style      = MaterialTheme.typography.bodyMedium,
                                            color      = if (isSelected) Violet else Ink,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            modifier   = Modifier.weight(1f),
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector        = Icons.Default.Check,
                                                contentDescription = null,
                                                tint               = Violet,
                                                modifier           = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Error ─────────────────────────────────────────────────────
                if (uiState.sabadoError != null) {
                    Text(
                        text      = uiState.sabadoError,
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = Blush,
                        modifier  = Modifier.padding(bottom = 8.dp),
                    )
                }

                // ── Botón marcar ──────────────────────────────────────────────
                if (uiState.sabadoEnviando) {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(52.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Violet, modifier = Modifier.size(28.dp))
                    }
                } else {
                    NeuButtonPrimary(
                        text     = stringResource(R.string.sabado_automarcar_btn),
                        onClick  = onSabadoMarcar,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// ── Tarjeta iglesia seleccionada ──────────────────────────────────────────────

@Composable
private fun IglesiaSeleccionadaCard(nombre: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuInset(cornerRadius = 14.dp)
            .background(Violet.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            .drawWithContent {
                drawContent()
                val s = 1.5.dp.toPx()
                drawRoundRect(
                    color        = Violet,
                    topLeft      = Offset(s / 2, s / 2),
                    size         = Size(size.width - s, size.height - s),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style        = Stroke(s),
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = Icons.Default.Church,
            contentDescription = null,
            tint               = Violet,
            modifier           = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text       = nombre.ifBlank { "Sin iglesia seleccionada" },
            style      = MaterialTheme.typography.bodyLarge,
            color      = Violet,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.weight(1f),
        )
        Icon(
            imageVector        = Icons.Default.Check,
            contentDescription = null,
            tint               = Violet,
            modifier           = Modifier.size(18.dp),
        )
    }
}

// ── Fila de miembro ───────────────────────────────────────────────────────────

@Composable
private fun MiembroRow(
    miembro: MiembroSesion,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 16.dp)
            .background(Background, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(BackgroundDeep, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = miembro.iniciales,
                style      = MaterialTheme.typography.labelSmall,
                color      = if (miembro.isLider) Gold else Accent,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text       = miembro.nombre,
            style      = MaterialTheme.typography.bodyLarge,
            color      = Ink,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.weight(1f),
        )

        if (miembro.isLider) {
            Icon(
                imageVector        = Icons.Default.Star,
                contentDescription = null,
                tint               = Gold,
                modifier           = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
        }

        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = Muted,
            modifier           = Modifier.size(20.dp),
        )
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
            onMiembroClick = {},
        )
    }
}
