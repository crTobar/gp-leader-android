package com.gpleader.app.feature.perfil

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Shadow
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun PerfilPrincipalScreen(
    onNavigateToHome:             () -> Unit,
    onNavigateToHistorial:        () -> Unit,
    onNavigateToDatosPersonales:  () -> Unit,
    onNavigateToCambiarContrasena: () -> Unit,
    onNavigateToDatosGrupo:       () -> Unit,
    onNavigateToMiembros:         () -> Unit,
    onNavigateToLogin:            () -> Unit,
    onNavigateToQuienEres:        () -> Unit = {},
    viewModel: PerfilViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToDatosPersonales) {
        if (uiState.navigateToDatosPersonales) {
            viewModel.consumeDatosPersonalesNavigation()
            onNavigateToDatosPersonales()
        }
    }
    LaunchedEffect(uiState.navigateToCambiarContrasena) {
        if (uiState.navigateToCambiarContrasena) {
            viewModel.consumeCambiarContrasenaNavigation()
            onNavigateToCambiarContrasena()
        }
    }
    LaunchedEffect(uiState.navigateToDatosGrupo) {
        if (uiState.navigateToDatosGrupo) {
            viewModel.consumeDatosGrupoNavigation()
            onNavigateToDatosGrupo()
        }
    }
    LaunchedEffect(uiState.navigateToMiembros) {
        if (uiState.navigateToMiembros) {
            viewModel.consumeMiembrosNavigation()
            onNavigateToMiembros()
        }
    }
    LaunchedEffect(uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            viewModel.consumeLoginNavigation()
            onNavigateToLogin()
        }
    }
    LaunchedEffect(uiState.navigateToQuienEres) {
        if (uiState.navigateToQuienEres) {
            viewModel.consumeQuienEresNavigation()
            onNavigateToQuienEres()
        }
    }

    PerfilContent(
        uiState                    = uiState,
        onNavigateToHome           = onNavigateToHome,
        onNavigateToHistorial      = onNavigateToHistorial,
        onDatosPersonalesClick     = viewModel::onDatosPersonalesClick,
        onCambiarContrasenaClick   = viewModel::onCambiarContrasenaClick,
        onDatosGrupoClick          = viewModel::onDatosGrupoClick,
        onMiembrosClick            = viewModel::onMiembrosClick,
        onNotificacionesClick      = viewModel::onNotificacionesClick,
        onCambiarQuienUsaClick     = viewModel::onCambiarQuienUsaClick,
        onCerrarSesionClick        = viewModel::onCerrarSesionClick,
        onDismissCerrarSesion      = viewModel::onDismissCerrarSesionDialog,
        onConfirmarCerrarSesion    = viewModel::onConfirmarCerrarSesion,
        onEditarAvatarClick        = viewModel::onEditarAvatarClick,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun PerfilContent(
    uiState:                  PerfilUiState,
    onNavigateToHome:         () -> Unit,
    onNavigateToHistorial:    () -> Unit,
    onDatosPersonalesClick:   () -> Unit,
    onCambiarContrasenaClick: () -> Unit,
    onDatosGrupoClick:        () -> Unit,
    onMiembrosClick:          () -> Unit,
    onNotificacionesClick:    () -> Unit,
    onCambiarQuienUsaClick:   () -> Unit,
    onCerrarSesionClick:      () -> Unit,
    onDismissCerrarSesion:    () -> Unit,
    onConfirmarCerrarSesion:  () -> Unit,
    onEditarAvatarClick:      () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                PerfilBottomNavBar(
                    onInicioClick    = onNavigateToHome,
                    onHistorialClick = onNavigateToHistorial,
                    onPerfilClick    = { },
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier       = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start  = 20.dp,
                    end    = 20.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // ── Avatar card ───────────────────────────────────────────────
                item {
                    Spacer(Modifier.height(16.dp).statusBarsPadding())
                    AvatarCard(
                        iniciales       = uiState.iniciales,
                        nombreCompleto  = uiState.nombreCompleto,
                        rol             = uiState.rol,
                        nombreGrupo     = uiState.nombreGrupo,
                        onEditarClick   = onEditarAvatarClick,
                        modifier        = Modifier.padding(vertical = 8.dp),
                    )
                }

                // ── Sección MI CUENTA ─────────────────────────────────────────
                item {
                    SeccionLabel(stringResource(R.string.perfil_seccion_cuenta))
                }
                item {
                    SeccionCard(modifier = Modifier.padding(bottom = 4.dp)) {
                        FilaMenu(
                            label   = stringResource(R.string.perfil_datos_personales),
                            onClick = onDatosPersonalesClick,
                            shape   = FilaShape.TOP,
                        )
                        HorizontalDivider(color = Shadow, thickness = 1.dp)
                        FilaMenu(
                            label   = stringResource(R.string.perfil_cambiar_contrasena),
                            onClick = onCambiarContrasenaClick,
                            shape   = FilaShape.BOTTOM,
                        )
                    }
                }

                // ── Sección MI GRUPO ──────────────────────────────────────────
                item {
                    SeccionLabel(stringResource(R.string.perfil_seccion_grupo))
                }
                item {
                    SeccionCard(modifier = Modifier.padding(bottom = 4.dp)) {
                        FilaMenu(
                            label   = stringResource(R.string.perfil_datos_grupo),
                            onClick = onDatosGrupoClick,
                            shape   = FilaShape.TOP,
                        )
                        HorizontalDivider(color = Shadow, thickness = 1.dp)
                        FilaMenu(
                            label   = stringResource(R.string.perfil_miembros),
                            onClick = onMiembrosClick,
                            shape   = FilaShape.BOTTOM,
                            badge   = uiState.totalMiembros.toString(),
                        )
                    }
                }

                // ── Sección PREFERENCIAS ──────────────────────────────────────
                item {
                    SeccionLabel(stringResource(R.string.perfil_seccion_preferencias))
                }
                item {
                    SeccionCard(modifier = Modifier.padding(bottom = 4.dp)) {
                        FilaMenu(
                            label   = stringResource(R.string.perfil_notificaciones),
                            onClick = onNotificacionesClick,
                            shape   = FilaShape.SINGLE,
                        )
                    }
                }

                // ── Sección HERRAMIENTAS ──────────────────────────────────────
                item {
                    SeccionLabel(stringResource(R.string.perfil_seccion_herramientas))
                }
                item {
                    SeccionCard(modifier = Modifier.padding(bottom = 4.dp)) {
                        FilaMenu(
                            label   = stringResource(R.string.perfil_cambiar_quien_usa),
                            onClick = onCambiarQuienUsaClick,
                            shape   = FilaShape.TOP,
                        )
                        HorizontalDivider(color = Shadow, thickness = 1.dp)
                        FilaCerrarSesion(onClick = onCerrarSesionClick)
                    }
                }

                // ── Footer ────────────────────────────────────────────────────
                item {
                    Text(
                        text      = stringResource(R.string.perfil_footer),
                        style     = MaterialTheme.typography.labelSmall,
                        color     = Muted,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 8.dp),
                    )
                }
            }
        }

        // ── Dialog cerrar sesión ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.showCerrarSesionDialog,
            enter   = fadeIn(),
            exit    = fadeOut(),
        ) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(onClick = onDismissCerrarSesion),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = uiState.showCerrarSesionDialog,
                    enter   = scaleIn(initialScale = 0.92f) + fadeIn(),
                    exit    = scaleOut(targetScale = 0.92f) + fadeOut(),
                ) {
                    CerrarSesionDialog(
                        onConfirmar = onConfirmarCerrarSesion,
                        onCancelar  = onDismissCerrarSesion,
                    )
                }
            }
        }
    }
}

// ── Avatar card ───────────────────────────────────────────────────────────────

@Composable
private fun AvatarCard(
    iniciales:      String,
    nombreCompleto: String,
    rol:            String,
    nombreGrupo:    String,
    onEditarClick:  () -> Unit,
    modifier:       Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        NeuCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Avatar cuadrado con iniciales
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Ink),
                ) {
                    Text(
                        text  = iniciales,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text  = nombreCompleto,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = rol,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = nombreGrupo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Accent,
                )
            }
        }

        // Botón editar alineado arriba-derecha
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .neuElevatedSm(cornerRadius = 10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Background)
                .clickable(onClick = onEditarClick)
                .padding(8.dp),
        ) {
            Icon(
                imageVector        = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.perfil_editar_avatar),
                tint               = Muted,
                modifier           = Modifier.size(16.dp),
            )
        }
    }
}

// ── Sección label ─────────────────────────────────────────────────────────────

@Composable
private fun SeccionLabel(text: String) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelSmall,
        color    = Muted,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
    )
}

// ── Sección card (wrapper manual para filas) ──────────────────────────────────

@Composable
private fun SeccionCard(
    modifier: Modifier = Modifier,
    content:  @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 28.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Background),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

// ── Fila de menú ──────────────────────────────────────────────────────────────

private enum class FilaShape { TOP, BOTTOM, SINGLE, MIDDLE }

@Composable
private fun FilaMenu(
    label:   String,
    onClick: () -> Unit,
    shape:   FilaShape = FilaShape.MIDDLE,
    badge:   String?   = null,
) {
    val clipShape = when (shape) {
        FilaShape.TOP    -> RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        FilaShape.BOTTOM -> RoundedCornerShape(topStart = 0.dp,  topEnd = 0.dp,  bottomStart = 28.dp, bottomEnd = 28.dp)
        FilaShape.SINGLE -> RoundedCornerShape(28.dp)
        FilaShape.MIDDLE -> RoundedCornerShape(0.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(clipShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (badge != null) {
                BadgeNumero(numero = badge)
            }
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = Muted,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun FilaCerrarSesion(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 28.dp, bottomEnd = 28.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text  = stringResource(R.string.perfil_cerrar_sesion),
            style = MaterialTheme.typography.titleLarge,
            color = Blush,
        )
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = Blush,
            modifier           = Modifier.size(20.dp),
        )
    }
}

// ── Badge número de miembros ──────────────────────────────────────────────────

@Composable
private fun BadgeNumero(numero: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Ink)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text  = numero,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Dialog cerrar sesión ──────────────────────────────────────────────────────

@Composable
private fun CerrarSesionDialog(
    onConfirmar: () -> Unit,
    onCancelar:  () -> Unit,
) {
    NeuCard(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth(),
    ) {
        Column(
            modifier            = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text      = stringResource(R.string.perfil_cerrar_sesion_titulo),
                style     = MaterialTheme.typography.headlineMedium,
                color     = Ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.perfil_cerrar_sesion_cuerpo),
                style     = MaterialTheme.typography.bodyMedium,
                color     = Mid,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))

            // Botón confirmar (Blush)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .neuElevated(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Blush)
                    .clickable(onClick = onConfirmar)
                    .padding(vertical = 14.dp),
            ) {
                Text(
                    text  = stringResource(R.string.perfil_cerrar_sesion_confirmar),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(10.dp))

            NeuButtonSecondary(
                text     = stringResource(R.string.perfil_cerrar_sesion_cancelar),
                onClick  = onCancelar,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Bottom nav bar ────────────────────────────────────────────────────────────

@Composable
private fun PerfilBottomNavBar(
    onInicioClick:    () -> Unit,
    onHistorialClick: () -> Unit,
    onPerfilClick:    () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        NeuCard(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                NavTabItem(Icons.Default.Home,      stringResource(R.string.home_nav_inicio),    false, onInicioClick)
                NavTabItem(Icons.Default.DateRange, stringResource(R.string.home_nav_historial), false, onHistorialClick)
                NavTabItem(Icons.Default.Person,    stringResource(R.string.home_nav_perfil),    true,  onPerfilClick)
            }
        }
    }
}

@Composable
private fun NavTabItem(
    icon:     ImageVector,
    label:    String,
    isActive: Boolean,
    onClick:  () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = if (isActive) Accent else Muted,
            modifier           = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Accent else Muted,
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun PerfilPreview() {
    GpLeaderTheme {
        PerfilContent(
            uiState = PerfilUiState(
                nombreCompleto = "Maria Garcia",
                iniciales      = "MG",
                rol            = "Líder de grupo pequeño",
                nombreGrupo    = "GP Los Olivos",
                totalMiembros  = 8,
            ),
            onNavigateToHome          = {},
            onNavigateToHistorial     = {},
            onDatosPersonalesClick    = {},
            onCambiarContrasenaClick  = {},
            onDatosGrupoClick         = {},
            onMiembrosClick           = {},
            onNotificacionesClick     = {},
            onCambiarQuienUsaClick    = {},
            onCerrarSesionClick       = {},
            onDismissCerrarSesion     = {},
            onConfirmarCerrarSesion   = {},
            onEditarAvatarClick       = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true, name = "Dialog abierto")
@Composable
private fun PerfilDialogPreview() {
    GpLeaderTheme {
        PerfilContent(
            uiState = PerfilUiState(
                nombreCompleto         = "Maria Garcia",
                iniciales              = "MG",
                rol                    = "Líder de grupo pequeño",
                nombreGrupo            = "GP Los Olivos",
                totalMiembros          = 8,
                showCerrarSesionDialog = true,
            ),
            onNavigateToHome          = {},
            onNavigateToHistorial     = {},
            onDatosPersonalesClick    = {},
            onCambiarContrasenaClick  = {},
            onDatosGrupoClick         = {},
            onMiembrosClick           = {},
            onNotificacionesClick     = {},
            onCambiarQuienUsaClick    = {},
            onCerrarSesionClick       = {},
            onDismissCerrarSesion     = {},
            onConfirmarCerrarSesion   = {},
            onEditarAvatarClick       = {},
        )
    }
}
