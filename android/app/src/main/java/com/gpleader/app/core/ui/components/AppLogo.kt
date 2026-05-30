package com.gpleader.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.neuElevated

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    cornerRadius: Dp = 18.dp,
    iconSize: Dp = 28.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .neuElevated(cornerRadius = cornerRadius)
            .background(Background, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            tint               = Accent,
            modifier           = Modifier.size(iconSize),
        )
    }
}
