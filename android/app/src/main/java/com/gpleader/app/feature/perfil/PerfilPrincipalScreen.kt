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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.AccentLight
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.data.repository.AsignadoPotencial
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Shadow
import com.gpleader.app.core.ui.components.FloatingNavScaffold
import com.gpleader.app.core.ui.components.NAV_TAB_ACTIVIDADES
import com.gpleader.app.core.ui.components.NAV_TAB_PERFIL
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.Sage

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun PerfilPrincipalScreen(
    onNavigateToHome:              () -> Unit,
    onNavigateToActividades:       () -> Unit = {},
    onNavigateToDatosPersonales:   () -> Unit,
    onNavigateToCambiarContrasena: () -> Unit,
    onNavigateToDatosGrupo:        () -> Unit,
    onNavigateToMiembros:          () -> Unit,
    onNavigateToLogin:             () -> Unit,
    onNavigateToQuienEres:         () -> Unit = {},
    onNavigateToRegistroActividad: () -> Unit = {},
    onNavigateToActividadesLista:  () -> Unit = {},
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
    LaunchedEffect(uiState.navigateToRegistroActividad) {
        if (uiState.navigateToRegistroActividad) {
            viewModel.consumeRegistroActividadNavigation()
            onNavigateToRegistroActividad()
        }
    }
    LaunchedEffect(uiState.navigateToActividadesLista) {
        if (uiState.navigateToActividadesLista) {
            viewModel.consumeActividadesListaNavigation()
            onNavigateToActividadesLista()
        }
    }

    PerfilContent(
        uiState                      = uiState,
        onNavigateToHome             = onNavigateToHome,
        onNavigateToActividades      = onNavigateToActividades,
        onDatosGrupoClick            = viewModel::onDatosGrupoClick,
        onMiembrosClick              = viewModel::onMiembrosClick,
        onRegistroActividadClick     = viewModel::onRegistroActividadClick,
        onActividadesListaClick      = viewModel::onActividadesListaClick,
        onCerrarSesionClick          = viewModel::onCerrarSesionClick,
        onDismissCerrarSesion        = viewModel::onDismissCerrarSesionDialog,
        onConfirmarCerrarSesion      = viewModel::onConfirmarCerrarSesion,
        onEditarAvatarClick          = viewModel::onEditarAvatarClick,
        onAsignarSuplenteClick       = viewModel::onAsignarSuplenteClick,
        onDismissDelegarSheet        = viewModel::onDismissDelegarSheet,
        onSeleccionarMiembro         = viewModel::onSeleccionarMiembroSuplente,
        onCrearSolicitud             = viewModel::onGuardarAsignacion,
        onDismissConfirmacion        = viewModel::onDismissConfirmacion,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PerfilContent(
    uiState:                  PerfilUiState,
    onNavigateToHome:         () -> Unit,
    onNavigateToActividades:  () -> Unit = {},
    onDatosGrupoClick:        () -> Unit,
    onMiembrosClick:          () -> Unit,
    onRegistroActividadClick: () -> Unit,
    onActividadesListaClick:  () -> Unit,
    onCerrarSesionClick:      () -> Unit,
    onDismissCerrarSesion:    () -> Unit,
    onConfirmarCerrarSesion:  () -> Unit,
    onEditarAvatarClick:      () -> Unit,
    onAsignarSuplenteClick:      () -> Unit = {},
    onDismissDelegarSheet:       () -> Unit = {},
    onSeleccionarMiembro:        (String) -> Unit = {},
    onCrearSolicitud:            () -> Unit = {},
    onDismissConfirmacion:       () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        FloatingNavScaffold(
            selectedTab        = NAV_TAB_PERFIL,
            onInicioClick      = onNavigateToHome,
            onActividadesClick = onNavigateToActividades,
            onPerfilClick      = { },
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

                // ── Asignar suplente ──────────────────────────────────────────
                item {
                    NeuButtonSecondary(
                        text     = "Asignar suplente",
                        onClick  = onAsignarSuplenteClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 8.dp),
                    )
                }

                // ── Sección MI GRUPO ──────────────────────────────────────────
                item {
                    SeccionLabel(stringResource(R.string.perfil_seccion_grupo))
                }
                item {
                    SeccionCard(modifier = Modifier.padding(bottom = 4.dp)) {
                        FilaMenu(
                            label   = stringResource(R.string.perfil_editar_miembros),
                            onClick = onMiembrosClick,
                            shape   = FilaShape.TOP,
                            badge   = uiState.totalMiembros.toString(),
                        )
                        HorizontalDivider(color = Shadow, thickness = 1.dp)
                        FilaMenu(
                            label   = stringResource(R.string.perfil_editar_datos_grupo),
                            onClick = onDatosGrupoClick,
                            shape   = FilaShape.MIDDLE,
                        )
                        HorizontalDivider(color = Shadow, thickness = 1.dp)
                        FilaMenu(
                            label   = stringResource(R.string.perfil_registro_actividad),
                            onClick = onRegistroActividadClick,
                            shape   = FilaShape.MIDDLE,
                        )
                        HorizontalDivider(color = Shadow, thickness = 1.dp)
                        FilaMenu(
                            label   = stringResource(R.string.perfil_actividades_lista),
                            onClick = onActividadesListaClick,
                            shape   = FilaShape.BOTTOM,
                        )
                    }
                }

                // ── Sección PREFERENCIAS ──────────────────────────────────────
                item {
                    SeccionLabel(stringResource(R.string.perfil_seccion_preferencias))
                }
                item {
                    SeccionCard(modifier = Modifier.padding(bottom = 4.dp)) {
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

        // ── Sheet: asignar suplente ───────────────────────────────────────────
        if (uiState.showDelegarSheet) {
            AsignarSuplenteSheet(
                asignados            = uiState.asignadosPotenciales,
                selectedId           = uiState.miembroSeleccionadoId,
                codigo               = uiState.codigoSuplente,
                isLoading            = uiState.isLoadingAsignados,
                isGenerando          = uiState.isGenerandoCodigo,
                error                = uiState.solicitudError,
                showConfirmacion     = uiState.showConfirmacion,
                confirmacionNombre   = uiState.confirmacionNombre,
                onSeleccionarMiembro = onSeleccionarMiembro,
                onCrear              = onCrearSolicitud,
                onDismiss            = onDismissDelegarSheet,
                onDismissConfirmacion = onDismissConfirmacion,
            )
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
                NeuAvatar(iniciales = iniciales, size = 80.dp)

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
            .background(Background, RoundedCornerShape(28.dp)),
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

    Box(modifier = Modifier.fillMaxWidth()) {
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
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = Muted,
                modifier           = Modifier.size(20.dp),
            )
        }

        if (badge != null) {
            BadgeNumero(
                numero   = badge,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-8).dp),
            )
        }
    }
}

@Composable
private fun FilaCerrarSesion(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
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
private fun BadgeNumero(numero: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AccentLight)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text       = numero,
            style      = MaterialTheme.typography.labelSmall,
            color      = Color.White,
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
    Box(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Background),
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

// ── Sheet: asignar suplente ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AsignarSuplenteSheet(
    asignados:             List<AsignadoPotencial>,
    selectedId:            String,
    codigo:                String,
    isLoading:             Boolean,
    isGenerando:           Boolean,
    error:                 String?,
    showConfirmacion:      Boolean,
    confirmacionNombre:    String,
    onSeleccionarMiembro:  (String) -> Unit,
    onCrear:               () -> Unit,
    onDismiss:             () -> Unit,
    onDismissConfirmacion: () -> Unit,
) {
    val sheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context     = androidx.compose.ui.platform.LocalContext.current

    ModalBottomSheet(
        onDismissRequest = if (showConfirmacion) onDismissConfirmacion else onDismiss,
        sheetState       = sheetState,
        containerColor   = Background,
    ) {

    // ── Pantalla de confirmación ──────────────────────────────────────────────
    if (showConfirmacion) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            Box(
                contentAlignment = androidx.compose.ui.Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Sage.copy(alpha = 0.15f)),
            ) {
                androidx.compose.material3.Icon(
                    imageVector        = Icons.Default.Person,
                    contentDescription = null,
                    tint               = Sage,
                    modifier           = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text       = "Delegación asignada",
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.Bold,
            )
            if (confirmacionNombre.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = confirmacionNombre,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Mid,
                )
            }
            Spacer(Modifier.height(20.dp))
            NeuCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Text(
                        text  = "CÓDIGO DE ACCESO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        codigo.forEach { digit ->
                            Box(
                                contentAlignment = androidx.compose.ui.Alignment.Center,
                                modifier = Modifier
                                    .neuElevatedSm(cornerRadius = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Background)
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                            ) {
                                Text(
                                    text       = digit.toString(),
                                    style      = MaterialTheme.typography.headlineMedium,
                                    color      = Ink,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text  = "Válido por 24 horas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            NeuButtonPrimary(
                text     = "Compartir código",
                onClick  = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, "Tu código de suplente para GP Los Olivos es: $codigo (válido 24h)")
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Compartir código"))
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            NeuButtonSecondary(
                text     = "Listo",
                onClick  = onDismissConfirmacion,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        return@ModalBottomSheet
    }

    // ── Selección de miembro ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text       = "Asignar suplente",
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Seleccioná a un miembro para que pueda registrar la reunión en las próximas 24 horas. Al guardar recibirá acceso en su app.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )
            Spacer(Modifier.height(16.dp))

            // ── Código de 6 dígitos ───────────────────────────────────────────
            AnimatedVisibility(visible = selectedId.isNotBlank() && (codigo.isNotBlank() || isGenerando)) {
                Column {
                    NeuCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text  = "CÓDIGO DE ACCESO",
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                codigo.forEach { digit ->
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .neuElevatedSm(cornerRadius = 8.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Background)
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                    ) {
                                        Text(
                                            text  = digit.toString(),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Ink,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                                Spacer(Modifier.width(4.dp))
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .neuElevatedSm(cornerRadius = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Background)
                                        .clickable {
                                            clipManager.setText(androidx.compose.ui.text.AnnotatedString(codigo))
                                        }
                                        .padding(8.dp),
                                ) {
                                    Icon(
                                        imageVector        = Icons.Default.Edit,
                                        contentDescription = "Copiar código",
                                        tint               = Accent,
                                        modifier           = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ── Lista de miembros ─────────────────────────────────────────────
            when {
                isLoading -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = Accent) }
                }
                asignados.isEmpty() -> {
                    Text(
                        text  = "No hay miembros registrados en este grupo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
                else -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(asignados) { asignado ->
                            val isSelected = asignado.profileId == selectedId
                            val iniciales  = asignado.nombre.split(" ")
                                .take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (isSelected) Modifier.neuElevatedSm(14.dp)
                                        else Modifier.neuElevated(14.dp)
                                    )
                                    .background(
                                        if (isSelected) Accent.copy(alpha = 0.08f) else Background,
                                        RoundedCornerShape(14.dp),
                                    )
                                    .clickable { onSeleccionarMiembro(asignado.profileId) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                NeuAvatar(iniciales = iniciales, size = 40.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text       = asignado.nombre,
                                    style      = MaterialTheme.typography.bodyLarge,
                                    color      = if (isSelected) Accent else Ink,
                                    fontWeight = FontWeight.Medium,
                                    modifier   = Modifier.weight(1f),
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint               = Accent,
                                        modifier           = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(text = error, style = MaterialTheme.typography.bodyMedium, color = Blush)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text  = "El permiso es válido por 24 horas a partir de guardar.",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Spacer(Modifier.height(12.dp))
            if (isGenerando) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Accent)
                }
            } else {
                NeuButtonPrimary(
                    text     = "Guardar",
                    enabled  = selectedId.isNotBlank() && codigo.isNotBlank(),
                    onClick  = onCrear,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    } // ModalBottomSheet
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
            onNavigateToActividades   = {},
            onDatosGrupoClick         = {},
            onMiembrosClick           = {},
            onRegistroActividadClick  = {},
            onActividadesListaClick   = {},
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
            onNavigateToActividades   = {},
            onDatosGrupoClick         = {},
            onMiembrosClick           = {},
            onRegistroActividadClick  = {},
            onActividadesListaClick   = {},
            onCerrarSesionClick       = {},
            onDismissCerrarSesion     = {},
            onConfirmarCerrarSesion   = {},
            onEditarAvatarClick       = {},
        )
    }
}
