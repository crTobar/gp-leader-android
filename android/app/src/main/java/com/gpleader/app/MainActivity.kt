package com.gpleader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gpleader.app.core.data.session.SessionManager
import com.gpleader.app.core.data.sync.OfflinePreloader
import com.gpleader.app.core.ui.navigation.AppNavGraph
import com.gpleader.app.core.ui.navigation.NavRoutes
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var session: SessionManager
    @Inject lateinit var offlinePreloader: OfflinePreloader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Precarga el caché offline del grupo en segundo plano si hay sesión de líder + internet.
        offlinePreloader.start()
val startDestination = when {
            session.isMiembroGuardado -> NavRoutes.MIEMBRO_HOME
            session.isIglesiaLeader   -> NavRoutes.IGLESIA_HOME
            session.isLoggedIn        -> NavRoutes.HOME
            else                      -> NavRoutes.LOGIN
        }
        enableEdgeToEdge()
        setContent {
            GpLeaderTheme {
                AppNavGraph(startDestination = startDestination)
            }
        }
    }
}
