package com.gpleader.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gpleader.app.R
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuInset

const val NAV_TAB_INICIO    = 0
const val NAV_TAB_HISTORIAL = 1
const val NAV_TAB_PERFIL    = 2

@Composable
fun AppBottomNavBar(
    selectedTab: Int,
    onInicioClick: () -> Unit,
    onHistorialClick: () -> Unit,
    onPerfilClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .neuElevated(cornerRadius = 26.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Background)
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NavTabItem(
                icon     = Icons.Default.Home,
                label    = stringResource(R.string.home_nav_inicio),
                isActive = selectedTab == NAV_TAB_INICIO,
                onClick  = onInicioClick,
            )
            NavTabItem(
                icon     = Icons.Default.DateRange,
                label    = stringResource(R.string.home_nav_historial),
                isActive = selectedTab == NAV_TAB_HISTORIAL,
                onClick  = onHistorialClick,
            )
            NavTabItem(
                icon     = Icons.Default.Person,
                label    = stringResource(R.string.home_nav_perfil),
                isActive = selectedTab == NAV_TAB_PERFIL,
                onClick  = onPerfilClick,
            )
        }
    }
}

@Composable
private fun NavTabItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(
                if (isActive) Modifier.neuInset(cornerRadius = 20.dp)
                else Modifier
            )
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) BackgroundDeep else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = if (isActive) Accent else Muted,
            modifier           = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (isActive) Accent else Muted,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun AppBottomNavBarPreview() {
    GpLeaderTheme {
        AppBottomNavBar(
            selectedTab      = NAV_TAB_INICIO,
            onInicioClick    = {},
            onHistorialClick = {},
            onPerfilClick    = {},
        )
    }
}
