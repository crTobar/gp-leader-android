package com.gpleader.app.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Shadow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ── Modelo de acción ──────────────────────────────────────────────────────────

data class SwipeAction(
    val label:     String,
    val color:     Color,
    val textColor: Color = Color.White,
    val onClick:   () -> Unit,
)

// ── Componente ────────────────────────────────────────────────────────────────
// swipeActions → se revelan al deslizar a la izquierda
// onItemClick  → se invoca al tocar la card (reemplaza .clickable en el contenido)
// menuActions  → aparecen en DropdownMenu al mantener presionado (opcional)
// itemKey      → clave única para resetear el Animatable al cambiar de ítem
// actionWidth  → ancho de cada acción revelada (default 88dp)
//
// Patrón de gestos: detectTapGestures y detectHorizontalDragGestures están en el
// MISMO composable (content slider Box), en bloques pointerInput separados.
// En Compose, para el Main pass el bloque más interno de la cadena procesa primero:
//   - "sw" (drag) es el último en la cadena → procesa PRIMERO
//   - "tap" (tap/longpress) es el primero → procesa DESPUÉS
// Para un drag: drag-detector consume eventos → tap-detector los ve consumidos → no dispara
// Para un tap: drag-detector no detecta slop → tap-detector dispara onTap normalmente

@Composable
fun SwipeableItem(
    swipeActions:    List<SwipeAction>,
    onItemClick:     (() -> Unit)? = null,
    modifier:        Modifier = Modifier,
    menuActions:     List<SwipeAction> = emptyList(),
    itemKey:         Any = Unit,
    actionWidth:     Dp = 88.dp,
    dimOnSwipe:      Boolean = false,
    clipCornerRadius: Dp = 0.dp,
    openKey:         Any? = null,
    onOpen:          () -> Unit = {},
    content:         @Composable () -> Unit,
) {
    // Sin acciones: comportamiento simple con tap opcional
    if (swipeActions.isEmpty() && menuActions.isEmpty()) {
        Box(
            modifier = modifier.then(
                if (onItemClick != null) Modifier.clickable(onClick = onItemClick) else Modifier
            ),
        ) { content() }
        return
    }

    val density  = LocalDensity.current
    val revealDp = actionWidth * swipeActions.size
    val revealPx = with(density) { revealDp.toPx() }
    val offsetX  = remember(itemKey) { Animatable(0f) }
    val scope    = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    // Cerrar este ítem cuando otro se abre (openKey cambia y no es este ítem)
    LaunchedEffect(openKey) {
        if (openKey != itemKey && offsetX.value < -1f) {
            offsetX.animateTo(0f, tween(220))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .then(
                if (clipCornerRadius > 0.dp)
                    Modifier.clip(RoundedCornerShape(clipCornerRadius))
                else Modifier
            ),
    ) {
        // ── Paneles de acción (detrás) ────────────────────────────────────────
        // Cuando clipCornerRadius > 0: tarjeta completa del mismo tamaño, oculta detrás.
        //   El contenido (content) se desliza encima revelando la tarjeta de acción.
        // Sin clip: panel dinámico que crece desde la derecha al deslizar.
        if (swipeActions.isNotEmpty()) {
            if (clipCornerRadius > 0.dp) {
                // Tarjeta completa oculta detrás de la tarjeta de contenido.
                // matchParentSize() llena el outer Box sin afectar su IntrinsicSize.
                Box(modifier = Modifier.matchParentSize()) {
                    // Fondo completo con el color de la última acción
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(swipeActions.last().color),
                    )
                    // Etiquetas de cada acción, alineadas en la zona derecha revelable
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(revealDp)
                            .fillMaxHeight(),
                    ) {
                        swipeActions.forEach { action ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        action.onClick()
                                        scope.launch { offsetX.animateTo(0f, tween(200)) }
                                    },
                            ) {
                                Text(
                                    text  = action.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = action.textColor,
                                )
                            }
                        }
                    }
                }
            } else {
                // Panel dinámico: crece desde la derecha al deslizar (sin clip externo).
                val currentRevealPx by remember {
                    derivedStateOf { (-offsetX.value).coerceIn(0f, revealPx) }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(with(density) { currentRevealPx.toDp() })
                        .fillMaxHeight(),
                ) {
                    swipeActions.forEach { action ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp))
                                .background(action.color)
                                .clickable {
                                    action.onClick()
                                    scope.launch { offsetX.animateTo(0f, tween(200)) }
                                },
                        ) {
                            Text(
                                text  = action.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = action.textColor,
                            )
                        }
                    }
                }
            }
        }

        // ── Menú contextual ───────────────────────────────────────────────────
        if (menuActions.isNotEmpty()) {
            DropdownMenu(
                expanded         = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
                menuActions.forEach { action ->
                    DropdownMenuItem(
                        text    = {
                            Text(
                                action.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (action.textColor == Color.White) Ink else action.textColor,
                            )
                        },
                        onClick = {
                            action.onClick()
                            showMenu = false
                            scope.launch { offsetX.animateTo(0f, tween(200)) }
                        },
                    )
                }
            }
        }

        // ── Contenido deslizable ──────────────────────────────────────────────
        // "tap" primero en cadena (procesa SEGUNDO en Main pass)
        // "sw" segundo en cadena (procesa PRIMERO en Main pass → consume drags antes que tap)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput("tap_$itemKey") {
                    detectTapGestures(
                        onLongPress = if (menuActions.isNotEmpty()) {
                            {
                                scope.launch { offsetX.animateTo(0f, tween(200)) }
                                showMenu = true
                            }
                        } else null,
                        onTap = {
                            if (offsetX.value < -1f)
                                scope.launch { offsetX.animateTo(0f, tween(220)) }
                            else
                                onItemClick?.invoke()
                        },
                    )
                }
                .pointerInput("sw_$itemKey") {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < -(revealPx * 0.4f)) {
                                    onOpen()
                                    offsetX.animateTo(
                                        -revealPx,
                                        spring(Spring.DampingRatioMediumBouncy),
                                    )
                                } else {
                                    offsetX.animateTo(0f, tween(220))
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, tween(220)) }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo(
                                    (offsetX.value + dragAmount).coerceIn(-revealPx, 0f)
                                )
                            }
                        },
                    )
                },
        ) {
            content()

            // Capa gris progresiva al deslizar: opaca (no transparenta el card).
            // Se clipea al mismo cornerRadius que el componente para que las esquinas
            // redondeadas no muestren bordes cuadrados translúcidos sobre el panel detrás.
            if (dimOnSwipe && revealPx > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .then(
                            if (clipCornerRadius > 0.dp)
                                Modifier.clip(RoundedCornerShape(clipCornerRadius))
                            else Modifier
                        )
                        .graphicsLayer {
                            alpha = (-offsetX.value / revealPx).coerceIn(0f, 1f) * 0.30f
                        }
                        .background(Shadow),
                )
            }
        }
    }
}
