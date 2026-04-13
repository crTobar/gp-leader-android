package com.gpleader.app.feature.registro

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuGlow
import java.time.LocalDate

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun DetalleActividadScreen(
    actividadId:   String,
    onNavigateBack: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val actividad = uiState.actividades.find { it.id == actividadId }

    if (actividad == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    // Intercepta el gesto/botón de sistema "atrás"
    BackHandler(onBack = onNavigateBack)

    DetalleActividadContent(
        actividad        = actividad,
        onNavigateBack   = onNavigateBack,
        onDesgloseChange = { miembroId, cant ->
            viewModel.onDesgloseChange(actividadId, miembroId, cant)
        },
        onGuardar        = { cantidad, notas ->
            viewModel.onCantidadChange(actividadId, cantidad)
            viewModel.onNotasChange(actividadId, notas)
            onNavigateBack()
        },
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@Composable
private fun DetalleActividadContent(
    actividad:        ActividadRegistro,
    onNavigateBack:   () -> Unit,
    onDesgloseChange: (miembroId: String, cantidad: Int) -> Unit = { _, _ -> },
    onGuardar:        (cantidad: Int?, notas: String) -> Unit,
) {
    var cantidad          by remember(actividad.id) { mutableStateOf(actividad.cantidad) }
    var notas             by remember(actividad.id) { mutableStateOf(actividad.notas ?: "") }
    var desgloseExpandido by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TopBar fuera del LazyColumn para evitar que el scroll intercepte el toque
            DetalleTopBar(onNavigateBack = onNavigateBack)

            LazyColumn(
                modifier       = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
            // ── Header de nivel ───────────────────────────────────────────────
            item {
                val (headerBg, labelNivel) = when (actividad.nivel) {
                    NivelActividad.UNION  -> BackgroundDeep to stringResource(R.string.detalle_actividad_nivel_union)
                    NivelActividad.PASTOR -> Mid             to stringResource(R.string.detalle_actividad_nivel_pastor)
                    NivelActividad.GP     -> BackgroundDeep  to stringResource(R.string.detalle_actividad_nivel_gp)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text  = labelNivel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (actividad.nivel == NivelActividad.PASTOR) Color.White else Ink,
                    )
                    if (actividad.esOficial) {
                        ActividadBadge(
                            texto = stringResource(R.string.registro_badge_oficial),
                            color = Gold,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── Card nombre ───────────────────────────────────────────────────
            item {
                NeuCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Ícono placeholder
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .neuElevatedSm(cornerRadius = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BackgroundDeep),
                        ) {
                            Text(text = "◆", style = MaterialTheme.typography.bodyMedium, color = Muted)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text  = actividad.nombre,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Ink,
                            )
                            Text(
                                text  = stringResource(R.string.detalle_actividad_unidad, actividad.unidad),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            // ── Total ─────────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text     = stringResource(R.string.detalle_actividad_label_total),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = Muted,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    ContadorGrande(
                        valor    = cantidad,
                        onChange = { cantidad = it },
                    )
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            // ── Notas ─────────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text     = stringResource(R.string.detalle_actividad_label_notas),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = Muted,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    OutlinedTextField(
                        value         = notas,
                        onValueChange = { notas = it },
                        placeholder   = {
                            Text(
                                text  = stringResource(R.string.detalle_actividad_hint_notas),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                            )
                        },
                        minLines  = 3,
                        shape     = RoundedCornerShape(14.dp),
                        colors    = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Background,
                            focusedContainerColor   = Background,
                            unfocusedBorderColor    = Muted.copy(alpha = 0.4f),
                            focusedBorderColor      = Accent,
                            cursorColor             = Accent,
                        ),
                        modifier  = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ── Desglose por miembro (colapsable) ────────────────────────────
            if (actividad.tieneDesglose && actividad.desgloseMiembros.isNotEmpty()) {
                item { Spacer(Modifier.height(20.dp)) }

                item {
                    val totalGeneral = cantidad ?: 0
                    val sumDesglose  = actividad.desgloseMiembros.sumOf { it.cantidad }
                    val disponibles  = totalGeneral - sumDesglose

                    NeuCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                        Column {
                            // ── Header tappable ───────────────────────────────
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { desgloseExpandido = !desgloseExpandido }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text  = "DETALLE POR MIEMBRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Ink,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (sumDesglose > 0) {
                                        Text(
                                            text  = "$sumDesglose / $totalGeneral",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Accent,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                        )
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    Icon(
                                        imageVector = if (desgloseExpandido)
                                            Icons.Default.KeyboardArrowUp
                                        else
                                            Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint     = Accent,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }

                            // ── Lista de miembros ─────────────────────────────
                            if (desgloseExpandido) {
                                HorizontalDivider(color = BackgroundDeep)
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    actividad.desgloseMiembros.forEachIndexed { idx, miembro ->
                                        MiembroDesgloseRow(
                                            miembro      = miembro,
                                            maxAdicional = disponibles,
                                            onChange     = { nuevaCant ->
                                                onDesgloseChange(miembro.miembroId, nuevaCant)
                                            },
                                            modifier = Modifier.padding(vertical = 6.dp),
                                        )
                                        if (idx < actividad.desgloseMiembros.lastIndex) {
                                            HorizontalDivider(color = BackgroundDeep)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        } // LazyColumn
        } // Column

        // ── Botón flotante ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Background)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            NeuButtonPrimary(
                text     = stringResource(R.string.detalle_actividad_btn_guardar),
                onClick  = { onGuardar(cantidad, notas) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun DetalleTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.detalle_actividad_titulo),
                tint               = Ink,
            )
        }
        Text(
            text      = stringResource(R.string.detalle_actividad_titulo),
            style     = MaterialTheme.typography.titleLarge,
            color     = Ink,
            textAlign = TextAlign.Center,
            modifier  = Modifier.weight(1f),
        )
        // Espaciador para centrar el título (mismo ancho que el IconButton = 48dp)
        Box(modifier = Modifier.size(48.dp))
    }
}

// ── Contador grande ───────────────────────────────────────────────────────────

@Composable
internal fun ContadorGrande(
    valor:    Int?,
    onChange: (Int?) -> Unit,
    enabled:  Boolean = true,
) {
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // − button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .neuElevatedSm(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Background)
                    .then(
                        if (enabled && valor != null) Modifier.clickable {
                            onChange(if (valor <= 0) null else valor - 1)
                        } else Modifier
                    ),
            ) {
                Text(
                    text  = "−",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (enabled && valor != null) Ink else Muted,
                )
            }

            // Valor
            Text(
                text  = valor?.toString() ?: "—",
                style = MaterialTheme.typography.displayLarge,
                color = if (enabled) Ink else Muted,
            )

            // + button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .then(if (enabled) Modifier.neuGlow(cornerRadius = 14.dp) else Modifier.neuElevatedSm(cornerRadius = 14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) Accent else BackgroundDeep)
                    .then(if (enabled) Modifier.clickable { onChange(valor?.plus(1) ?: 0) } else Modifier),
            ) {
                Text(
                    text       = "+",
                    style      = MaterialTheme.typography.titleLarge,
                    color      = if (enabled) Color.White else Muted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val detallePreviewActividad = ActividadRegistro(
    id           = "a3",
    nombre       = "Estudios Bíblicos",
    nivel        = NivelActividad.PASTOR,
    unidad       = "personas",
    esOficial    = true,
    esObligatoria = true,
    cantidad     = 3,
    notas        = null,
)

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "DetalleActividad — Pastor")
@Composable
private fun DetalleActividadPastorPreview() {
    GpLeaderTheme {
        DetalleActividadContent(
            actividad      = detallePreviewActividad,
            onNavigateBack = {},
            onGuardar      = { _, _ -> },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "DetalleActividad — GP Extra")
@Composable
private fun DetalleActividadGpPreview() {
    GpLeaderTheme {
        DetalleActividadContent(
            actividad = ActividadRegistro(
                id        = "a6",
                nombre    = "Oración especial",
                nivel     = NivelActividad.GP,
                unidad    = "veces",
                esOficial = false,
                esExtra   = true,
                cantidad  = 4,
            ),
            onNavigateBack = {},
            onGuardar      = { _, _ -> },
        )
    }
}
