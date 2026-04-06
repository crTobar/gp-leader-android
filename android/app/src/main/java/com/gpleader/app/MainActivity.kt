package com.gpleader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gpleader.app.core.ui.navigation.AppNavGraph
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GpLeaderTheme {
                AppNavGraph()
            }
        }
    }
}
