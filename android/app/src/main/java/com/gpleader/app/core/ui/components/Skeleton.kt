package com.gpleader.app.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Shadow

@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue  = 1000f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerOffset",
    )
    return Brush.linearGradient(
        colors = listOf(BackgroundDeep, Shadow.copy(alpha = 0.55f), BackgroundDeep),
        start  = Offset(offset - 300f, 0f),
        end    = Offset(offset, 0f),
    )
}

@Composable
fun SkeletonBox(
    modifier:     Modifier = Modifier,
    cornerRadius: Dp       = 14.dp,
) {
    val brush = rememberShimmerBrush()
    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius)).background(brush))
}

@Composable
fun SkeletonText(
    width:        Dp,
    modifier:     Modifier = Modifier,
    height:       Dp       = 12.dp,
    cornerRadius: Dp       = 6.dp,
) {
    SkeletonBox(
        modifier     = modifier.width(width).height(height),
        cornerRadius = cornerRadius,
    )
}
