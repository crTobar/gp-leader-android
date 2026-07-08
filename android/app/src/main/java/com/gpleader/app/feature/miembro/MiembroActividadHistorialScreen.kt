package com.gpleader.app.feature.miembro

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.MemberEntry
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.OnResumeEffect
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInsetSm
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiembroActividadHistorialScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetalle: (String) -> Unit = {},
    viewModel: MiembroActividadHistorialViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResumeEffect { viewModel.cargar() }

    var pendingDelete by remember { mutableStateOf<MemberEntry?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
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
            Text(
                text      = "Aportes",
                style     = MaterialTheme.typography.titleLarge,
                color     = Ink,
                textAlign = TextAlign.Center,
                modifier  = Modifier.weight(1f),
            )
            Box(modifier = Modifier.size(40.dp))
        }

        // ── Card nombre + total ───────────────────────────────────────────────
        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text       = uiState.nombreActividad.ifBlank { "Actividad" },
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f),
                )
                val total = uiState.total.toLong()
                val totalLabel = when (uiState.markerType) {
                    "monetary" -> "₡${miles(total)}"
                    else       -> if (total > 0) "${miles(total)} ${uiState.unitLabel}".trim() else "—"
                }
                Text(
                    text       = totalLabel,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = if (total > 0) Accent else Muted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Card "Registrar aporte" (todos los tipos numéricos) ──────────────
        if (uiState.markerType != "realizado" && uiState.markerType != "checkbox") {
            NeuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Text(
                        text       = "Registrar aporte",
                        style      = MaterialTheme.typography.titleLarge,
                        color      = Ink,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = if (uiState.markerType == "monetary") "Agrega un monto aportado" else "Registra tu aporte",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                    Spacer(Modifier.height(16.dp))
                    NeuButtonPrimary(
                        text     = "Agregar",
                        onClick  = viewModel::onShowAddDialog,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Lista de aportes ──────────────────────────────────────────────────
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = viewModel::onRefresh,
            modifier     = Modifier.fillMaxSize(),
            indicator    = {},
        ) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }

                uiState.error != null -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(uiState.error!!, color = Blush, textAlign = TextAlign.Center)
                }

                uiState.entries.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sin aportes aún", style = MaterialTheme.typography.bodyLarge, color = Muted)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = "Toca \"Agregar\" para registrar tu primer aporte.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item(key = "_header") {
                        Text(
                            text     = "MIS APORTES · ${uiState.entries.size}",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = Muted,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    items(uiState.entries, key = { it.id }) { entry ->
                        EntryCard(
                            entry      = entry,
                            markerType = uiState.markerType,
                            unitLabel  = uiState.unitLabel,
                            onClick    = { onNavigateToDetalle(entry.id) },
                            onEditar   = { viewModel.onEditarEntry(entry) },
                            onBorrar   = { pendingDelete = entry },
                        )
                    }
                }
            }
        }
    }

    // ── Diálogo agregar / editar ──────────────────────────────────────────────
    if (uiState.showAddDialog) {
        val editando = uiState.editando != null
        AddRegistroDialog(
            markerType = uiState.markerType,
            unitLabel  = uiState.unitLabel,
            cantidad   = uiState.nuevaCantidad,
            isLoading  = uiState.isGuardando,
            isEdit     = editando,
            onCantidadChange = viewModel::onCantidadChange,
            onConfirmar      = { if (editando) viewModel.onConfirmarEdicion() else viewModel.onGuardar() },
            onDismiss        = viewModel::onDismissDialog,
        )
    }

    // ── Confirmación de borrado ───────────────────────────────────────────────
    val borrar = pendingDelete
    if (borrar != null) {
        val montoTxt = if (uiState.markerType == "monetary") "₡${borrar.value.toLong()}" else "${borrar.value.toLong()}"
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor   = Background,
            title = { Text("Borrar aporte", style = MaterialTheme.typography.titleLarge, color = Ink) },
            text  = { Text("¿Seguro que quieres borrar el aporte de $montoTxt? Esta acción no se puede deshacer.", style = MaterialTheme.typography.bodyMedium, color = Mid) },
            confirmButton = {
                TextButton(onClick = { viewModel.onBorrarEntry(borrar); pendingDelete = null }) {
                    Text("Borrar", color = Blush, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar", color = Mid) } },
        )
    }
}

@Composable
private fun EntryCard(
    entry:      MemberEntry,
    markerType: String,
    unitLabel:  String,
    onClick:    () -> Unit,
    onEditar:   () -> Unit,
    onBorrar:   () -> Unit,
) {
    val zone = java.time.ZoneId.systemDefault()
    val fmt  = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale("es")).withZone(zone)
    val statusColor = when (entry.status) {
        "approved"      -> Sage
        "pending_board" -> Accent
        "rejected"      -> Blush
        else            -> Muted
    }
    val statusLabel = when (entry.status) {
        "approved"      -> "Aprobado"
        "pending_board" -> "Pend. junta"
        "rejected"      -> "Rechazado"
        else            -> "Pendiente"
    }
    val editable = entry.status == "draft" || entry.status == "rejected"
    val aprobado = entry.status == "approved"

    @Composable
    fun CardContent(modifier: Modifier) {
        NeuCard(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .background(if (aprobado) Sage.copy(alpha = 0.08f) else Color.Transparent)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = entry.enteredAt?.let { fmt.format(it) } ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mid,
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.14f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(text = statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor)
                    }
                }
                val valorTxt = if (markerType == "monetary") "₡${miles(entry.value.toLong())}"
                               else "${miles(entry.value.toLong())} $unitLabel".trim()
                Text(
                    text       = valorTxt,
                    style      = MaterialTheme.typography.titleLarge,
                    color      = if (aprobado) Sage else Accent,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    if (!editable) {
        CardContent(Modifier.clickable(onClick = onClick))
        return
    }

    SwipeRevealEntry(
        onClick  = onClick,
        onEditar = onEditar,
        onBorrar = onBorrar,
    ) { dragMod -> CardContent(dragMod) }
}

/**
 * Tarjeta deslizable que **revela** las acciones Editar/Borrar (no las ejecuta al deslizar).
 * El usuario desliza para ver los botones y toca el que necesita.
 */
@Composable
private fun SwipeRevealEntry(
    onClick:  () -> Unit,
    onEditar: () -> Unit,
    onBorrar: () -> Unit,
    card: @Composable (Modifier) -> Unit,
) {
    val density   = LocalDensity.current
    val actionW   = 76.dp
    val revealPx  = with(density) { (actionW * 2).toPx() }
    val offsetX   = remember { Animatable(0f) }
    val scope     = rememberCoroutineScope()

    fun cerrar() = scope.launch { offsetX.animateTo(0f) }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Acciones reveladas detrás de la tarjeta (lado derecho)
        Row(
            modifier              = Modifier.matchParentSize().clip(RoundedCornerShape(20.dp)),
            horizontalArrangement = Arrangement.End,
        ) {
            RevealAction(Icons.Default.Edit, "Editar", Accent, actionW) { cerrar(); onEditar() }
            RevealAction(Icons.Default.Delete, "Borrar", Blush, actionW) { cerrar(); onBorrar() }
        }

        // Tarjeta en primer plano, arrastrable
        card(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch { offsetX.snapTo((offsetX.value + dragAmount).coerceIn(-revealPx, 0f)) }
                        },
                        onDragEnd = {
                            scope.launch { offsetX.animateTo(if (offsetX.value < -revealPx / 2f) -revealPx else 0f) }
                        },
                    )
                }
                .clickable { if (offsetX.value != 0f) cerrar() else onClick() },
        )
    }
}

@Composable
private fun RevealAction(
    icon:  androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(color.copy(alpha = 0.16f))
            .clickable(onClick = onClick),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AddRegistroDialog(
    markerType:      String,
    unitLabel:       String,
    cantidad:        Int,
    isLoading:       Boolean,
    isEdit:          Boolean = false,
    onCantidadChange: (Int) -> Unit,
    onConfirmar:     () -> Unit,
    onDismiss:       () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Background,
        title = {
            Text(
                text  = if (isEdit) "Editar aporte" else "Agregar aporte",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
        },
        text = {
            if (markerType == "realizado" || markerType == "checkbox") {
                Text(
                    text  = "Se registrará que realizaste esta actividad hoy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mid,
                )
            } else {
                MontoInput(
                    markerType = markerType,
                    unitLabel  = unitLabel,
                    cantidad   = cantidad,
                    onChange   = onCantidadChange,
                    onDone     = onConfirmar,
                )
            }
        },
        confirmButton = {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Accent)
            } else {
                TextButton(onClick = onConfirmar, enabled = cantidad > 0) {
                    Text("Guardar", color = if (cantidad > 0) Accent else Muted, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Mid)
            }
        },
    )
}

// ── Campo para escribir el monto/cantidad con separación de miles ─────────────

private val milesFmt = java.text.DecimalFormat(
    "#,###",
    java.text.DecimalFormatSymbols(Locale("es")).apply { groupingSeparator = '.' },
)

fun formatMiles(n: Int): String = if (n <= 0) "" else milesFmt.format(n.toLong())

/** Para mostrar (no input): 0 → "0", con separación de miles. */
fun miles(n: Long): String = milesFmt.format(n)

fun parseMonto(s: String): Int =
    s.filter { it.isDigit() }.take(12).toLongOrNull()?.coerceAtMost(Int.MAX_VALUE.toLong())?.toInt() ?: 0

/** Formatea solo la VISTA con separación de miles; el texto real son dígitos (cursor estable). */
private val milesVisualTransformation = androidx.compose.ui.text.input.VisualTransformation { text ->
    val digits = text.text.filter { it.isDigit() }
    val formatted = if (digits.isEmpty()) "" else milesFmt.format(digits.toLong())
    // pos[k] = índice en 'formatted' justo después de k dígitos
    val pos = IntArray(digits.length + 1)
    var d = 0
    formatted.forEachIndexed { idx, c -> if (c.isDigit()) { d++; pos[d] = idx + 1 } }
    val mapping = object : androidx.compose.ui.text.input.OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = pos[offset.coerceIn(0, digits.length)]
        override fun transformedToOriginal(offset: Int): Int =
            formatted.take(offset.coerceIn(0, formatted.length)).count { it.isDigit() }
    }
    androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(formatted), mapping)
}

@Composable
fun MontoInput(
    markerType: String,
    unitLabel:  String,
    cantidad:   Int,
    onChange:   (Int) -> Unit,
    onDone:     () -> Unit = {},
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = if (markerType == "monetary") "Monto" else unitLabel.ifBlank { "Cantidad" },
            style = MaterialTheme.typography.bodyMedium,
            color = Mid,
        )
        Spacer(Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .neuInsetSm(cornerRadius = 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Background)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            if (markerType == "monetary") {
                Text("₡", style = MaterialTheme.typography.headlineMedium, color = Accent)
                Spacer(Modifier.width(6.dp))
            }
            BasicTextField(
                value                = if (cantidad > 0) cantidad.toString() else "",
                onValueChange        = { onChange(parseMonto(it)) },
                singleLine           = true,
                textStyle            = MaterialTheme.typography.headlineMedium.copy(color = Accent, textAlign = TextAlign.Start),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions      = KeyboardActions(onDone = { onDone() }),
                visualTransformation = milesVisualTransformation,
                decorationBox        = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (cantidad <= 0) {
                            Text("0", style = MaterialTheme.typography.headlineMedium, color = Muted)
                        }
                        inner()
                    }
                },
                modifier = Modifier.widthIn(min = 80.dp),
            )
            if (markerType != "monetary" && unitLabel.isNotBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(unitLabel, style = MaterialTheme.typography.bodyMedium, color = Mid)
            }
        }
    }
}

/** Campo de monto compacto (una línea) para filas, con ₡ y miles. Cursor estable. */
@Composable
fun MontoFieldCompact(
    markerType: String,
    value:      Int,
    onChange:   (Int) -> Unit,
    modifier:   Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .neuInsetSm(cornerRadius = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        if (markerType == "monetary") {
            Text("₡", style = MaterialTheme.typography.titleLarge, color = Accent)
            Spacer(Modifier.width(4.dp))
        }
        BasicTextField(
            value                = if (value > 0) value.toString() else "",
            onValueChange        = { onChange(parseMonto(it)) },
            singleLine           = true,
            textStyle            = MaterialTheme.typography.titleLarge.copy(color = Accent, textAlign = TextAlign.End),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = milesVisualTransformation,
            decorationBox        = { inner ->
                Box(contentAlignment = Alignment.CenterEnd) {
                    if (value <= 0) Text("0", style = MaterialTheme.typography.titleLarge, color = Muted)
                    inner()
                }
            },
            modifier = Modifier.widthIn(min = 56.dp),
        )
    }
}
