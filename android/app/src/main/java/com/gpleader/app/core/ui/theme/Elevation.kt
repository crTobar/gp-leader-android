package com.gpleader.app.core.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Internal palette aliases ──────────────────────────────────────────────────
private val NeuBg          = Color(0xFFECEEF1)             // Same as Background
private val NeuDark        = Color(0xFFC2C8D4)             // Same as Shadow
private val NeuLight       = Color(0xFFFFFFFF)             // Same as Light
private val NeuAccentGlow  = Color(0x404A7FD4)             // rgba(74,127,212, 0.25)

// ── Helper ────────────────────────────────────────────────────────────────────
private fun Modifier.drawNeuShadows(
    cornerRadius: Dp,
    blurRadius: Float,
    darkOffsetX: Float,
    darkOffsetY: Float,
    lightOffsetX: Float,
    lightOffsetY: Float,
    glowColor: Color = Color.Transparent,
    bgColor: Color = NeuBg,
): Modifier = this.drawBehind {
    val rPx = cornerRadius.toPx()

    fun paint(color: Color, dx: Float, dy: Float) = Paint().apply {
        asFrameworkPaint().apply {
            isAntiAlias = true
            this.color = bgColor.toArgb()          // Must match actual background to avoid visible fill
            setShadowLayer(blurRadius, dx, dy, color.toArgb())
        }
    }

    drawIntoCanvas { c ->
        // Optional glow ring (used by neuGlow)
        if (glowColor != Color.Transparent) {
            c.drawRoundRect(0f, 0f, size.width, size.height, rPx, rPx,
                paint(glowColor, 0f, 0f).also {
                    it.asFrameworkPaint().setShadowLayer(blurRadius * 1.5f, 0f, 0f, glowColor.toArgb())
                })
        }
        // Dark shadow
        c.drawRoundRect(0f, 0f, size.width, size.height, rPx, rPx,
            paint(NeuDark, darkOffsetX, darkOffsetY))
        // Light shadow
        c.drawRoundRect(0f, 0f, size.width, size.height, rPx, rPx,
            paint(NeuLight, lightOffsetX, lightOffsetY))
    }
}

// ── Public modifiers ──────────────────────────────────────────────────────────

/**
 * Card flotante — sombra oscura abajo-derecha, sombra clara arriba-izquierda.
 * Nivel normal (neu-up). cornerRadius debe coincidir con el shape del composable.
 */
fun Modifier.neuElevated(cornerRadius: Dp = 28.dp, bgColor: Color = NeuBg): Modifier =
    drawNeuShadows(
        cornerRadius  = cornerRadius,
        blurRadius    = 20f,
        darkOffsetX   =  8f, darkOffsetY  =  8f,
        lightOffsetX  = -8f, lightOffsetY = -8f,
        bgColor       = bgColor,
    )

/**
 * Elemento pequeño flotante (neu-up-sm). Sombras más sutiles.
 */
fun Modifier.neuElevatedSm(cornerRadius: Dp = 14.dp, bgColor: Color = NeuBg): Modifier =
    drawNeuShadows(
        cornerRadius  = cornerRadius,
        blurRadius    = 12f,
        darkOffsetX   =  5f, darkOffsetY  =  5f,
        lightOffsetX  = -5f, lightOffsetY = -5f,
        bgColor       = bgColor,
    )

/**
 * Campo de entrada / estado presionado (neu-in).
 * Sombras invertidas: oscura arriba-izquierda, clara abajo-derecha.
 */
fun Modifier.neuInset(cornerRadius: Dp = 14.dp): Modifier =
    drawNeuShadows(
        cornerRadius  = cornerRadius,
        blurRadius    = 14f,
        darkOffsetX   = -6f, darkOffsetY  = -6f,
        lightOffsetX  =  6f, lightOffsetY =  6f,
    )

/**
 * Campo o elemento pequeño presionado (neu-in-sm). Desplazamiento reducido.
 */
fun Modifier.neuInsetSm(cornerRadius: Dp = 8.dp): Modifier =
    drawNeuShadows(
        cornerRadius  = cornerRadius,
        blurRadius    = 8f,
        darkOffsetX   = -4f, darkOffsetY  = -4f,
        lightOffsetX  =  4f, lightOffsetY =  4f,
    )

/**
 * Botón principal activo (neu-up + halo azul rgba(74,127,212,0.25)).
 */
fun Modifier.neuGlow(cornerRadius: Dp = 14.dp): Modifier =
    drawNeuShadows(
        cornerRadius  = cornerRadius,
        blurRadius    = 20f,
        darkOffsetX   =  8f, darkOffsetY  =  8f,
        lightOffsetX  = -8f, lightOffsetY = -8f,
        glowColor     = NeuAccentGlow,
    )
