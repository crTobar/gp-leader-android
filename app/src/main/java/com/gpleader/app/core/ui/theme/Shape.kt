package com.gpleader.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val GpShapes = Shapes(
    extraLarge = RoundedCornerShape(28.dp), // Cards principales
    large      = RoundedCornerShape(20.dp), // Cards internas
    medium     = RoundedCornerShape(14.dp), // Botones, avatares
    small      = RoundedCornerShape(8.dp),  // Chips, badges
    extraSmall = RoundedCornerShape(4.dp),  // Tags pequeños
)
