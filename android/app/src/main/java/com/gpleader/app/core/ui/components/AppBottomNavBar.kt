package com.gpleader.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInset

const val NAV_TAB_INICIO      = 0
const val NAV_TAB_ACTIVIDADES = 1
const val NAV_TAB_PERFIL      = 2

/**
 * Scaffold con menú flotante superpuesto sobre el contenido (sin reservar banda).
 * El contenido recibe [PaddingValues] con el inset superior (status bar) y el
 * espacio inferior para librar el menú flotante. El fondo gris siempre queda detrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingNavScaffold(
    selectedTab:        Int,
    onInicioClick:      () -> Unit,
    onActividadesClick: () -> Unit,
    onPerfilClick:      () -> Unit,
    topBar:  @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(containerColor = Background, topBar = topBar) { innerPadding ->
        val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
        ) {
            content(
                PaddingValues(
                    top    = innerPadding.calculateTopPadding(),
                    bottom = navInset + 96.dp,
                )
            )
            AppBottomNavBar(
                selectedTab        = selectedTab,
                onInicioClick      = onInicioClick,
                onActividadesClick = onActividadesClick,
                onPerfilClick      = onPerfilClick,
                modifier           = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
fun AppBottomNavBar(
    selectedTab:       Int,
    onInicioClick:     () -> Unit,
    onActividadesClick: () -> Unit,
    onPerfilClick:     () -> Unit,
    modifier:          Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .neuElevatedSm(cornerRadius = 28.dp)
                .clip(RoundedCornerShape(28.dp))
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
                icon     = Icons.AutoMirrored.Filled.Assignment,
                label    = stringResource(R.string.home_nav_actividades),
                isActive = selectedTab == NAV_TAB_ACTIVIDADES,
                onClick  = onActividadesClick,
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
    icon:       ImageVector,
    label:      String,
    isActive:   Boolean,
    onClick:    () -> Unit,
    badgeCount: Int = 0,
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
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(containerColor = Blush) {
                        Text(badgeCount.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = if (isActive) Accent else Muted,
                modifier           = Modifier.size(22.dp),
            )
        }
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
            selectedTab        = NAV_TAB_INICIO,
            onInicioClick      = {},
            onActividadesClick = {},
            onPerfilClick      = {},
        )
    }
}
