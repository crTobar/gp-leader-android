package com.gpleader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gpleader.app.core.data.session.SessionManager
import com.gpleader.app.core.ui.navigation.AppNavGraph
import com.gpleader.app.core.ui.navigation.NavRoutes
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Limpiar sesión de la app al arrancar — el usuario siempre debe hacer login.
        // La sesión de Supabase se mantiene para que los dropdowns del login funcionen.
        session.clear()
        enableEdgeToEdge()
        setContent {
            GpLeaderTheme {
                AppNavGraph(startDestination = NavRoutes.LOGIN)
            }
        }
    }
}
