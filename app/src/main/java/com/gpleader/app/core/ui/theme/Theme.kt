package com.gpleader.app.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Custom color palette exposed via CompositionLocal ─────────────────────────
@Immutable
data class GpColors(
    val background:     Color = Background,
    val backgroundDeep: Color = BackgroundDeep,
    val shadow:         Color = Shadow,
    val light:          Color = Light,
    val accent:         Color = Accent,
    val accentLight:    Color = AccentLight,
    val gold:           Color = Gold,
    val sage:           Color = Sage,
    val blush:          Color = Blush,
    val ink:            Color = Ink,
    val mid:            Color = Mid,
    val muted:          Color = Muted,
)

val LocalGpColors = staticCompositionLocalOf { GpColors() }

/** Acceso rápido desde cualquier composable: MaterialTheme.gpColors.accent */
val MaterialTheme.gpColors: GpColors
    @Composable get() = LocalGpColors.current

// ── Material3 color scheme ────────────────────────────────────────────────────
private val GpColorScheme = lightColorScheme(
    primary          = Accent,
    onPrimary        = Color.White,
    primaryContainer = AccentLight,
    secondary        = Sage,
    onSecondary      = Color.White,
    tertiary         = Gold,
    background       = Background,
    onBackground     = Ink,
    surface          = Background,
    onSurface        = Ink,
    surfaceVariant   = BackgroundDeep,
    onSurfaceVariant = Mid,
    error            = Blush,
    onError          = Color.White,
    outline          = Muted,
)

// ── Root theme ────────────────────────────────────────────────────────────────
@Composable
fun GpLeaderTheme(content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalGpColors provides GpColors(),
    ) {
        MaterialTheme(
            colorScheme = GpColorScheme,
            typography  = GpTypography,
            shapes      = GpShapes,
            content     = content,
        )
    }
}
