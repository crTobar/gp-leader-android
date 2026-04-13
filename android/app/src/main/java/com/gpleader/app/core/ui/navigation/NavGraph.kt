package com.gpleader.app.core.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.feature.auth.LoginScreen
import com.gpleader.app.feature.auth.QuienEresScreen
import com.gpleader.app.feature.historial.HistorialScreen
import com.gpleader.app.feature.home.HomeScreen
import com.gpleader.app.feature.perfil.PerfilCambiarContrasenaScreen
import com.gpleader.app.feature.perfil.PerfilDatosGrupoScreen
import com.gpleader.app.feature.perfil.PerfilDatosPersonalesScreen
import com.gpleader.app.feature.perfil.PerfilPrincipalScreen
import com.gpleader.app.feature.registro.AgregarActividadScreen
import com.gpleader.app.feature.registro.DetalleActividadScreen
import com.gpleader.app.feature.registro.ExitoEnviadoScreen
import com.gpleader.app.feature.registro.ExitoOfflineScreen
import com.gpleader.app.feature.registro.RegistroPaso1Screen
import com.gpleader.app.feature.registro.RegistroPaso2Screen
import com.gpleader.app.feature.registro.RegistroPaso3Screen
import com.gpleader.app.feature.miembros.MiembroDetalleScreen
import com.gpleader.app.feature.miembros.MiembroEditarScreen
import com.gpleader.app.feature.miembros.MiembroAgregarScreen
import com.gpleader.app.feature.miembros.MiembrosListaScreen
import com.gpleader.app.feature.miembros.MiembrosViewModel
import com.gpleader.app.feature.historial.DetalleReunionScreen
import com.gpleader.app.feature.registro.RegistroViewModel

object NavRoutes {
    const val LOGIN                      = "login"
    const val QUIEN_ERES                 = "quien_eres"
    const val CAMBIAR_CONTRASENA_INICIAL = "cambiar_contrasena_inicial"
    const val HOME                       = "home"
    const val HISTORIAL       = "historial"
    const val PERFIL                  = "perfil"
    const val PERFIL_DATOS_PERSONALES   = "perfil/datos_personales"
    const val PERFIL_CAMBIAR_CONTRASENA = "perfil/cambiar_contrasena"
    const val PERFIL_DATOS_GRUPO        = "perfil/datos_grupo"
    const val DETALLE_REUNION         = "detalle_reunion/{reunionId}"

    // ── Miembros nested graph ─────────────────────────────────────────────────
    const val MIEMBROS_GRAPH   = "miembros_graph"
    const val MIEMBROS_LISTA   = "miembros"
    const val MIEMBROS_DETALLE = "miembros/detalle"
    const val MIEMBROS_EDITAR  = "miembros/editar"
    const val MIEMBROS_AGREGAR = "miembros/agregar"

    // ── Registro nested graph ─────────────────────────────────────────────────
    const val REGISTRO_GRAPH         = "registro"
    const val REGISTRO_PASO1         = "registro/paso1"
    const val REGISTRO_PASO2         = "registro/paso2"
    const val REGISTRO_PASO3         = "registro/paso3"
    const val DETALLE_ACTIVIDAD      = "registro/detalle/{actividadId}"
    const val AGREGAR_ACTIVIDAD      = "registro/agregar_actividad"
    const val EXITO_ENVIADO          = "registro/exito_enviado"
    const val EXITO_OFFLINE          = "registro/exito_offline"

    fun detalleReunion(reunionId: String)     = "detalle_reunion/$reunionId"
    fun detalleActividad(actividadId: String) = "registro/detalle/$actividadId"
}

@Composable
fun AppNavGraph(
    startDestination: String = NavRoutes.LOGIN,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
    ) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onNavigateToQuienEres = {
                    navController.navigate(NavRoutes.QUIEN_ERES) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToCambiarContrasena = {
                    navController.navigate(NavRoutes.CAMBIAR_CONTRASENA_INICIAL)
                },
            )
        }

        composable(NavRoutes.QUIEN_ERES) {
            QuienEresScreen(
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.QUIEN_ERES) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.CAMBIAR_CONTRASENA_INICIAL) {
            PerfilCambiarContrasenaScreen(
                onNavigateBack = {
                    navController.navigate(NavRoutes.QUIEN_ERES) {
                        popUpTo(NavRoutes.CAMBIAR_CONTRASENA_INICIAL) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToRegistro  = { navController.navigate(NavRoutes.REGISTRO_GRAPH) },
                onNavigateToHistorial = { navController.navigate(NavRoutes.HISTORIAL) },
                onNavigateToDetalle   = { id -> navController.navigate(NavRoutes.detalleReunion(id)) },
                onNavigateToPerfil    = { navController.navigate(NavRoutes.PERFIL) },
            )
        }

        composable(NavRoutes.HISTORIAL) {
            HistorialScreen(
                onNavigateToHome    = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
                onNavigateToDetalle = { id -> navController.navigate(NavRoutes.detalleReunion(id)) },
                onNavigateToPerfil  = { navController.navigate(NavRoutes.PERFIL) },
            )
        }

        composable(NavRoutes.PERFIL) {
            PerfilPrincipalScreen(
                onNavigateToHome              = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
                onNavigateToHistorial         = {
                    navController.navigate(NavRoutes.HISTORIAL) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
                onNavigateToDatosPersonales   = { navController.navigate(NavRoutes.PERFIL_DATOS_PERSONALES) },
                onNavigateToCambiarContrasena = { navController.navigate(NavRoutes.PERFIL_CAMBIAR_CONTRASENA) },
                onNavigateToDatosGrupo        = { navController.navigate(NavRoutes.PERFIL_DATOS_GRUPO) },
                onNavigateToMiembros          = { navController.navigate(NavRoutes.MIEMBROS_GRAPH) },
                onNavigateToLogin             = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToQuienEres         = {
                    navController.navigate(NavRoutes.QUIEN_ERES) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
            )
        }

        composable(NavRoutes.PERFIL_DATOS_PERSONALES) {
            PerfilDatosPersonalesScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.PERFIL_CAMBIAR_CONTRASENA) {
            PerfilCambiarContrasenaScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.PERFIL_DATOS_GRUPO) {
            PerfilDatosGrupoScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Miembros nested graph (ViewModel compartido) ──────────────────────
        navigation(
            route            = NavRoutes.MIEMBROS_GRAPH,
            startDestination = NavRoutes.MIEMBROS_LISTA,
        ) {
            composable(NavRoutes.MIEMBROS_LISTA) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.MIEMBROS_GRAPH)
                }
                val sharedVm: MiembrosViewModel = hiltViewModel(graphEntry)
                MiembrosListaScreen(
                    onNavigateBack        = { navController.popBackStack() },
                    onNavigateToDetalle   = { navController.navigate(NavRoutes.MIEMBROS_DETALLE) },
                    onNavigateToAgregar   = {
                        sharedVm.onPrepararAgregar()
                        navController.navigate(NavRoutes.MIEMBROS_AGREGAR)
                    },
                    onNavigateToHome      = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = false }
                        }
                    },
                    onNavigateToHistorial = {
                        navController.navigate(NavRoutes.HISTORIAL) {
                            popUpTo(NavRoutes.HOME) { inclusive = false }
                        }
                    },
                    viewModel = sharedVm,
                )
            }

            composable(NavRoutes.MIEMBROS_DETALLE) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.MIEMBROS_GRAPH)
                }
                val sharedVm: MiembrosViewModel = hiltViewModel(graphEntry)
                MiembroDetalleScreen(
                    onNavigateBack      = { navController.popBackStack() },
                    onNavigateToEditar  = { navController.navigate(NavRoutes.MIEMBROS_EDITAR) },
                    viewModel           = sharedVm,
                )
            }

            composable(NavRoutes.MIEMBROS_EDITAR) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.MIEMBROS_GRAPH)
                }
                val sharedVm: MiembrosViewModel = hiltViewModel(graphEntry)
                MiembroEditarScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel      = sharedVm,
                )
            }

            composable(NavRoutes.MIEMBROS_AGREGAR) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.MIEMBROS_GRAPH)
                }
                val sharedVm: MiembrosViewModel = hiltViewModel(graphEntry)
                MiembroAgregarScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel      = sharedVm,
                )
            }
        }

        // Placeholder — DetalleReunionScreen
        composable(
            route     = NavRoutes.DETALLE_REUNION,
            arguments = listOf(navArgument("reunionId") { type = NavType.StringType }),
        ) {
            DetalleReunionScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Registro nested graph (ViewModel compartido) ───────────────────────
        navigation(
            route            = NavRoutes.REGISTRO_GRAPH,
            startDestination = NavRoutes.REGISTRO_PASO1,
        ) {
            composable(NavRoutes.REGISTRO_PASO1) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH)
                }
                val sharedVm: RegistroViewModel = hiltViewModel(graphEntry)
                RegistroPaso1Screen(
                    onNavigateBack    = { navController.popBackStack() },
                    onNavigateToPaso2 = { navController.navigate(NavRoutes.REGISTRO_PASO2) },
                    onNavigateToPaso3 = { navController.navigate(NavRoutes.REGISTRO_PASO3) },
                    viewModel         = sharedVm,
                )
            }

            composable(NavRoutes.REGISTRO_PASO2) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH)
                }
                val sharedVm: RegistroViewModel = hiltViewModel(graphEntry)
                RegistroPaso2Screen(
                    onNavigateBack      = { navController.popBackStack() },
                    onNavigateToDetalle = { id ->
                        navController.navigate(NavRoutes.detalleActividad(id))
                    },
                    onNavigateToAgregar = {
                        navController.navigate(NavRoutes.AGREGAR_ACTIVIDAD)
                    },
                    onNavigateToPaso3   = { navController.navigate(NavRoutes.REGISTRO_PASO3) },
                    viewModel           = sharedVm,
                )
            }

            composable(
                route     = NavRoutes.DETALLE_ACTIVIDAD,
                arguments = listOf(navArgument("actividadId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH)
                }
                val sharedVm: RegistroViewModel = hiltViewModel(graphEntry)
                val actividadId = backStackEntry.arguments?.getString("actividadId") ?: ""
                DetalleActividadScreen(
                    actividadId     = actividadId,
                    onNavigateBack  = { navController.popBackStack() },
                    viewModel       = sharedVm,
                )
            }

            composable(NavRoutes.AGREGAR_ACTIVIDAD) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH)
                }
                val sharedVm: RegistroViewModel = hiltViewModel(graphEntry)
                AgregarActividadScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel      = sharedVm,
                )
            }

            composable(NavRoutes.REGISTRO_PASO3) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH)
                }
                val sharedVm: RegistroViewModel = hiltViewModel(graphEntry)
                RegistroPaso3Screen(
                    onNavigateBack           = { navController.popBackStack() },
                    onNavigateToExitoEnviado = { navController.navigate(NavRoutes.EXITO_ENVIADO) },
                    onNavigateToExitoOffline = { navController.navigate(NavRoutes.EXITO_OFFLINE) },
                    viewModel                = sharedVm,
                )
            }

            composable(NavRoutes.EXITO_ENVIADO) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH)
                }
                val sharedVm: RegistroViewModel = hiltViewModel(graphEntry)
                ExitoEnviadoScreen(
                    onNavigateToHome = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = false }
                        }
                    },
                    onNavigateToHistorial = {
                        navController.navigate(NavRoutes.HISTORIAL) {
                            popUpTo(NavRoutes.HOME) { inclusive = false }
                        }
                    },
                    viewModel = sharedVm,
                )
            }

            composable(NavRoutes.EXITO_OFFLINE) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH)
                }
                val sharedVm: RegistroViewModel = hiltViewModel(graphEntry)
                ExitoOfflineScreen(
                    onNavigateToHome = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = false }
                        }
                    },
                    viewModel = sharedVm,
                )
            }
        }
    }
}

@Composable
private fun Placeholder(label: String) {
    Box(
        modifier         = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
    }
}
