package com.gpleader.app.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gpleader.app.feature.auth.ConfirmarIdentidadScreen
import com.gpleader.app.feature.auth.LoginScreen
import com.gpleader.app.feature.auth.QuienEresScreen
import com.gpleader.app.feature.auth.SabadoAutoMarcarScreen
import com.gpleader.app.feature.auth.SabadoConfirmacionScreen
import com.gpleader.app.feature.miembro.MiembroHomeScreen
import com.gpleader.app.feature.historial.HistorialScreen
import com.gpleader.app.feature.sabado.SabadoCultoScreen
import com.gpleader.app.feature.home.HomeScreen
import com.gpleader.app.feature.perfil.PerfilCambiarContrasenaScreen
import com.gpleader.app.feature.perfil.PerfilDatosGrupoScreen
import com.gpleader.app.feature.perfil.PerfilDatosPersonalesScreen
import com.gpleader.app.feature.perfil.PerfilPrincipalScreen
import com.gpleader.app.feature.perfil.RegistroActividadScreen
import com.gpleader.app.feature.registro.AgregarActividadScreen
import com.gpleader.app.feature.registro.AgregarActividadStandaloneScreen
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
import com.gpleader.app.feature.actividades.ActividadesMisionerasScreen
import com.gpleader.app.feature.actividades.ActividadesListScreen
import com.gpleader.app.feature.actividades.CampanaDetalleScreen
import com.gpleader.app.feature.actividades.ActividadHistorialScreen
import com.gpleader.app.feature.actividades.AgregarAporteScreen
import com.gpleader.app.feature.actividades.CrearActividadDuoScreen
import com.gpleader.app.feature.actividades.CrearDuoScreen
import com.gpleader.app.feature.actividades.DuoDetalleScreen
import com.gpleader.app.feature.actividades.DuosListScreen
import com.gpleader.app.feature.actividades.EstudiosBiblicosMiembrosScreen
import com.gpleader.app.feature.miembro.ActividadCampanaScreen
import com.gpleader.app.feature.miembro.MiembroActividadHistorialScreen
import com.gpleader.app.feature.miembro.MiembroActividadesScreen
import com.gpleader.app.feature.miembro.DuoMisioneroScreen
import com.gpleader.app.feature.miembro.EstudiosBiblicosListScreen
import com.gpleader.app.feature.miembro.EstudioDetalleScreen

object NavRoutes {
    const val LOGIN                      = "login"
    const val QUIEN_ERES                 = "quien_eres"
    const val CAMBIAR_CONTRASENA_INICIAL = "cambiar_contrasena_inicial"
    const val HOME                       = "home"
    const val HISTORIAL       = "historial"
    const val PERFIL                  = "perfil"
    const val PERFIL_DATOS_PERSONALES   = "perfil/datos_personales"
    const val PERFIL_CAMBIAR_CONTRASENA = "perfil/cambiar_contrasena"
    const val PERFIL_DATOS_GRUPO          = "perfil/datos_grupo"
    const val PERFIL_REGISTRO_ACTIVIDAD   = "perfil/registro_actividad"
    const val DETALLE_REUNION             = "detalle_reunion/{reunionId}"

    // ── Miembro regular (perfil guardado) ────────────────────────────────────
    const val CONFIRMAR_IDENTIDAD           = "confirmar_identidad/{miembroId}/{miembroNombre}"
    const val MIEMBRO_HOME                  = "miembro_home"
    const val MIEMBRO_DUO_MISIONERO         = "miembro_duo_misionero"
    const val MIEMBRO_ESTUDIOS_BIBLICOS     = "miembro_estudios_biblicos"
    const val MIEMBRO_ESTUDIOS_BIBLICOS_DE  = "miembro_estudios_biblicos/{miembroId}"
    const val MIEMBRO_ESTUDIO_DETALLE       = "miembro_estudio_detalle/{estudioId}"
    const val MIEMBRO_ESTUDIO_DETALLE_RO    = "miembro_estudio_detalle_ro/{estudioId}"
    const val MIEMBRO_ACTIVIDAD_CAMPANA     = "miembro_actividad_campana/{actividadTipoId}/{nombreCampana}/{desde}/{hasta}"
    const val MIEMBRO_ACTIVIDAD_HISTORIAL   = "miembro_actividad_historial/{actividadTipoId}"

    fun confirmarIdentidad(miembroId: String, miembroNombre: String) =
        "confirmar_identidad/${android.net.Uri.encode(miembroId)}/${android.net.Uri.encode(miembroNombre)}"
    fun miembroEstudiosBiblicos(miembroId: String) = "miembro_estudios_biblicos/$miembroId"
    fun miembroEstudioDetalle(estudioId: String) = "miembro_estudio_detalle/$estudioId"
    fun miembroEstudioDetalleRo(estudioId: String) = "miembro_estudio_detalle_ro/$estudioId"
    fun miembroActividadCampana(tipoId: String, nombre: String, desde: String, hasta: String) =
        "miembro_actividad_campana/$tipoId/${android.net.Uri.encode(nombre)}/$desde/$hasta"
    fun miembroActividadHistorial(tipoId: String) = "miembro_actividad_historial/$tipoId"

    // ── Sábado ────────────────────────────────────────────────────────────────
    const val SABADO_AUTOMARCAR   = "sabado_automarcar/{miembroId}"
    const val SABADO_CONFIRMACION = "sabado_confirmacion/{iglesiaName}"
    const val SABADO_CULTO        = "sabado_culto"

    // ── Miembros nested graph ─────────────────────────────────────────────────
    const val MIEMBROS_GRAPH   = "miembros_graph"
    const val MIEMBROS_LISTA   = "miembros"
    const val MIEMBROS_DETALLE = "miembros/detalle"
    const val MIEMBROS_EDITAR  = "miembros/editar"
    const val MIEMBROS_AGREGAR = "miembros/agregar"

    // ── Actividades ───────────────────────────────────────────────────────────
    const val ACTIVIDADES_LISTA           = "actividades_lista"
    const val ACTIVIDAD_HISTORIAL         = "actividad_historial/{actividadTipoId}"
    const val AGREGAR_APORTE              = "agregar_aporte/{actividadTipoId}"
    const val CREAR_ACTIVIDAD_TIPO        = "crear_actividad_tipo"
    const val MIEMBRO_ACTIVIDADES         = "miembro_actividades"
    const val CAMPANA_DETALLE             = "campana_detalle/{actividadTipoId}/{nombreCampana}/{desde}/{hasta}"

    // ── Dúos Misioneros ───────────────────────────────────────────────────────
    const val ACTIVIDADES_MISIONERAS      = "actividades_misioneras"
    const val DUOS_LISTA                  = "duos_lista"
    const val CREAR_DUO                   = "crear_duo"
    const val DUO_DETALLE                 = "duo_detalle/{duoId}"
    const val CREAR_ACTIVIDAD_DUO         = "crear_actividad_duo/{duoId}"
    const val ESTUDIOS_BIBLICOS_MIEMBROS  = "estudios_biblicos_miembros"

    fun actividadHistorial(actividadTipoId: String) = "actividad_historial/$actividadTipoId"
    fun agregarAporte(actividadTipoId: String)      = "agregar_aporte/$actividadTipoId"
    fun campanaDetalle(tipoId: String, nombre: String, desde: String, hasta: String) =
        "campana_detalle/$tipoId/${android.net.Uri.encode(nombre)}/$desde/$hasta"
    fun duoDetalle(duoId: String) = "duo_detalle/$duoId"
    fun crearActividadDuo(duoId: String) = "crear_actividad_duo/$duoId"

    // ── Registro nested graph ─────────────────────────────────────────────────
    const val REGISTRO_GRAPH         = "registro"
    const val REGISTRO_GRAPH_ROUTE   = "registro?kind={kind}"
    const val REGISTRO_PASO1         = "registro/paso1"
    const val REGISTRO_PASO2         = "registro/paso2"
    const val REGISTRO_PASO3         = "registro/paso3"
    const val DETALLE_ACTIVIDAD      = "registro/detalle/{actividadId}"
    const val AGREGAR_ACTIVIDAD      = "registro/agregar_actividad"
    const val EXITO_ENVIADO          = "registro/exito_enviado"
    const val EXITO_OFFLINE          = "registro/exito_offline"

    fun detalleReunion(reunionId: String)     = "detalle_reunion/$reunionId"
    fun detalleActividad(actividadId: String) = "registro/detalle/$actividadId"
    fun sabadoAutoMarcar(miembroId: String)   = "sabado_automarcar/$miembroId"
    fun sabadoConfirmacion(iglesiaName: String) = "sabado_confirmacion/${android.net.Uri.encode(iglesiaName)}"
    fun registroGraph(kind: String = "gp_meeting") = "registro?kind=$kind"
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
            )
        }

        composable(NavRoutes.QUIEN_ERES) {
            QuienEresScreen(
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.QUIEN_ERES) { inclusive = true }
                    }
                },
                onNavigateToCambiarContrasena = {
                    navController.navigate(NavRoutes.CAMBIAR_CONTRASENA_INICIAL)
                },
                onNavigateToConfirmarIdentidad = { miembroId, miembroNombre ->
                    navController.navigate(NavRoutes.confirmarIdentidad(miembroId, miembroNombre))
                },
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route     = NavRoutes.CONFIRMAR_IDENTIDAD,
            arguments = listOf(
                navArgument("miembroId")    { type = NavType.StringType },
                navArgument("miembroNombre") { type = NavType.StringType },
            ),
        ) {
            ConfirmarIdentidadScreen(
                onConfirmado = {
                    navController.navigate(NavRoutes.MIEMBRO_HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNoSoyYo = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.MIEMBRO_HOME) {
            MiembroHomeScreen(
                onCerrarSesion = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToActividades      = { navController.navigate(NavRoutes.MIEMBRO_ACTIVIDADES) },
                onNavigateToDuoMisionero     = { navController.navigate(NavRoutes.MIEMBRO_DUO_MISIONERO) },
                onNavigateToEstudiosBiblicos = { navController.navigate(NavRoutes.MIEMBRO_ESTUDIOS_BIBLICOS) },
            )
        }

        composable(NavRoutes.MIEMBRO_ACTIVIDADES) {
            MiembroActividadesScreen(
                onNavigateBack        = { navController.popBackStack() },
                onNavigateToCampana   = { tipoId, nombre, desde, hasta ->
                    navController.navigate(NavRoutes.miembroActividadCampana(tipoId, nombre, desde, hasta))
                },
                onNavigateToHistorial = { tipoId ->
                    navController.navigate(NavRoutes.miembroActividadHistorial(tipoId))
                },
            )
        }

        composable(
            route     = NavRoutes.MIEMBRO_ACTIVIDAD_HISTORIAL,
            arguments = listOf(navArgument("actividadTipoId") { type = NavType.StringType }),
        ) {
            MiembroActividadHistorialScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route     = NavRoutes.MIEMBRO_ACTIVIDAD_CAMPANA,
            arguments = listOf(
                navArgument("actividadTipoId") { type = NavType.StringType },
                navArgument("nombreCampana")   { type = NavType.StringType },
                navArgument("desde")           { type = NavType.StringType },
                navArgument("hasta")           { type = NavType.StringType },
            ),
        ) {
            ActividadCampanaScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.MIEMBRO_DUO_MISIONERO) {
            DuoMisioneroScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.MIEMBRO_ESTUDIOS_BIBLICOS) {
            EstudiosBiblicosListScreen(
                onNavigateBack       = { navController.popBackStack() },
                onNavigateToDetalle  = { id -> navController.navigate(NavRoutes.miembroEstudioDetalle(id)) },
            )
        }

        composable(
            route     = NavRoutes.MIEMBRO_ESTUDIOS_BIBLICOS_DE,
            arguments = listOf(navArgument("miembroId") { type = NavType.StringType }),
        ) {
            EstudiosBiblicosListScreen(
                onNavigateBack      = { navController.popBackStack() },
                onNavigateToDetalle = { id -> navController.navigate(NavRoutes.miembroEstudioDetalleRo(id)) },
                soloLectura         = true,
            )
        }

        composable(
            route     = NavRoutes.MIEMBRO_ESTUDIO_DETALLE,
            arguments = listOf(navArgument("estudioId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val estudioId = backStackEntry.arguments?.getString("estudioId") ?: ""
            EstudioDetalleScreen(
                estudioId      = estudioId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route     = NavRoutes.MIEMBRO_ESTUDIO_DETALLE_RO,
            arguments = listOf(navArgument("estudioId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val estudioId = backStackEntry.arguments?.getString("estudioId") ?: ""
            EstudioDetalleScreen(
                estudioId      = estudioId,
                onNavigateBack = { navController.popBackStack() },
                soloLectura    = true,
            )
        }

        composable(
            route     = NavRoutes.SABADO_AUTOMARCAR,
            arguments = listOf(navArgument("miembroId") { type = NavType.StringType }),
        ) {
            SabadoAutoMarcarScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToConfirmacion = { iglesiaName ->
                    navController.navigate(NavRoutes.sabadoConfirmacion(iglesiaName)) {
                        popUpTo(NavRoutes.QUIEN_ERES) { inclusive = false }
                    }
                },
            )
        }

        composable(
            route     = NavRoutes.SABADO_CONFIRMACION,
            arguments = listOf(navArgument("iglesiaName") { type = NavType.StringType }),
        ) { backStackEntry ->
            val iglesiaName = backStackEntry.arguments?.getString("iglesiaName")?.let {
                android.net.Uri.decode(it)
            } ?: ""
            SabadoConfirmacionScreen(
                iglesiaNombre = iglesiaName,
                onCerrar = {
                    if (!navController.popBackStack(NavRoutes.MIEMBRO_HOME, inclusive = false)) {
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(NavRoutes.CAMBIAR_CONTRASENA_INICIAL) {
            PerfilCambiarContrasenaScreen(
                esPrimerLogin  = true,
                onNavigateBack = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToRegistro               = { kind -> navController.navigate(NavRoutes.registroGraph(kind)) },
                onNavigateToHistorial              = { navController.navigate(NavRoutes.HISTORIAL) },
                onNavigateToDetalle                = { id -> navController.navigate(NavRoutes.detalleReunion(id)) },
                onNavigateToPerfil                 = { navController.navigate(NavRoutes.PERFIL) },
                onNavigateToActividades            = { navController.navigate(NavRoutes.ACTIVIDADES_LISTA) },
                onNavigateToSabadoCulto            = { navController.navigate(NavRoutes.SABADO_CULTO) },
                onNavigateToActividadesMisioneras  = { navController.navigate(NavRoutes.ACTIVIDADES_MISIONERAS) },
            )
        }

        composable(NavRoutes.SABADO_CULTO) {
            SabadoCultoScreen(
                onNavigateBack   = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
            )
        }

        composable(NavRoutes.HISTORIAL) {
            HistorialScreen(
                onNavigateToHome        = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
                onNavigateToDetalle     = { id -> navController.navigate(NavRoutes.detalleReunion(id)) },
                onNavigateToActividades = { navController.navigate(NavRoutes.ACTIVIDADES_LISTA) },
                onNavigateToPerfil      = {
                    navController.navigate(NavRoutes.PERFIL) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
                onNavigateToRegistro    = { navController.navigate(NavRoutes.registroGraph()) },
            )
        }

        composable(NavRoutes.PERFIL) {
            PerfilPrincipalScreen(
                onNavigateToHome              = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
                onNavigateToActividades       = { navController.navigate(NavRoutes.ACTIVIDADES_LISTA) },
                onNavigateToDatosPersonales   = { navController.navigate(NavRoutes.PERFIL_DATOS_PERSONALES) },
                onNavigateToCambiarContrasena = { navController.navigate(NavRoutes.PERFIL_CAMBIAR_CONTRASENA) },
                onNavigateToDatosGrupo        = { navController.navigate(NavRoutes.PERFIL_DATOS_GRUPO) },
                onNavigateToMiembros          = { navController.navigate(NavRoutes.MIEMBROS_GRAPH) },
                onNavigateToRegistroActividad = { navController.navigate(NavRoutes.PERFIL_REGISTRO_ACTIVIDAD) },
                onNavigateToActividadesLista  = { navController.navigate(NavRoutes.ACTIVIDADES_LISTA) },
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

        composable(NavRoutes.PERFIL_REGISTRO_ACTIVIDAD) {
            RegistroActividadScreen(
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
                    onNavigateToHome         = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = false }
                        }
                    },
                    onNavigateToActividades  = { navController.navigate(NavRoutes.ACTIVIDADES_LISTA) },
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

        composable(
            route     = NavRoutes.DETALLE_REUNION,
            arguments = listOf(navArgument("reunionId") { type = NavType.StringType }),
        ) {
            DetalleReunionScreen(
                onNavigateBack          = { navController.popBackStack() },
                onNavigateToHome        = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
                onNavigateToActividades = { navController.navigate(NavRoutes.ACTIVIDADES_LISTA) },
                onNavigateToPerfil      = {
                    navController.navigate(NavRoutes.PERFIL) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
            )
        }

        composable(NavRoutes.ACTIVIDADES_LISTA) {
            ActividadesListScreen(
                onNavigateBack       = { navController.popBackStack() },
                onNavigateToHistorial = { tipoId ->
                    navController.navigate(NavRoutes.actividadHistorial(tipoId))
                },
                onNavigateToCrear    = { navController.navigate(NavRoutes.CREAR_ACTIVIDAD_TIPO) },
                onNavigateToHome     = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
                onNavigateToPerfil   = {
                    navController.navigate(NavRoutes.PERFIL) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
                onNavigateToCampana  = { tipoId, nombre, desde, hasta ->
                    navController.navigate(NavRoutes.campanaDetalle(tipoId, nombre, desde, hasta))
                },
            )
        }

        composable(NavRoutes.CREAR_ACTIVIDAD_TIPO) {
            AgregarActividadStandaloneScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route     = NavRoutes.ACTIVIDAD_HISTORIAL,
            arguments = listOf(navArgument("actividadTipoId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val tipoId = backStackEntry.arguments?.getString("actividadTipoId") ?: return@composable
            ActividadHistorialScreen(
                onNavigateBack             = { navController.popBackStack() },
                onNavigateToAgregarAporte  = { navController.navigate(NavRoutes.agregarAporte(tipoId)) },
            )
        }

        composable(
            route     = NavRoutes.AGREGAR_APORTE,
            arguments = listOf(navArgument("actividadTipoId") { type = NavType.StringType }),
        ) {
            AgregarAporteScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route     = NavRoutes.CAMPANA_DETALLE,
            arguments = listOf(
                navArgument("actividadTipoId") { type = NavType.StringType },
                navArgument("nombreCampana")   { type = NavType.StringType },
                navArgument("desde")           { type = NavType.StringType },
                navArgument("hasta")           { type = NavType.StringType },
            ),
        ) {
            CampanaDetalleScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Dúos Misioneros ───────────────────────────────────────────────────
        composable(NavRoutes.ACTIVIDADES_MISIONERAS) {
            ActividadesMisionerasScreen(
                onNavigateBack               = { navController.popBackStack() },
                onNavigateToDuos             = { navController.navigate(NavRoutes.DUOS_LISTA) },
                onNavigateToEstudiosBiblicos = { navController.navigate(NavRoutes.ESTUDIOS_BIBLICOS_MIEMBROS) },
            )
        }

        composable(NavRoutes.DUOS_LISTA) {
            DuosListScreen(
                onNavigateBack      = { navController.popBackStack() },
                onNavigateToCrear   = { navController.navigate(NavRoutes.CREAR_DUO) },
                onNavigateToDetalle = { duoId -> navController.navigate(NavRoutes.duoDetalle(duoId)) },
            )
        }

        composable(NavRoutes.CREAR_DUO) {
            CrearDuoScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route     = NavRoutes.DUO_DETALLE,
            arguments = listOf(navArgument("duoId") { type = NavType.StringType }),
        ) {
            DuoDetalleScreen(
                onNavigateBack             = { navController.popBackStack() },
                onNavigateToCrearActividad = { duoId ->
                    navController.navigate(NavRoutes.crearActividadDuo(duoId))
                },
            )
        }

        composable(
            route     = NavRoutes.CREAR_ACTIVIDAD_DUO,
            arguments = listOf(navArgument("duoId") { type = NavType.StringType }),
        ) {
            CrearActividadDuoScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.ESTUDIOS_BIBLICOS_MIEMBROS) {
            EstudiosBiblicosMiembrosScreen(
                onNavigateBack       = { navController.popBackStack() },
                onNavigateToEstudios = { miembroId ->
                    navController.navigate(NavRoutes.miembroEstudiosBiblicos(miembroId))
                },
            )
        }

        // ── Registro nested graph (ViewModel compartido) ───────────────────────
        navigation(
            route            = NavRoutes.REGISTRO_GRAPH_ROUTE,
            startDestination = NavRoutes.REGISTRO_PASO1,
            arguments        = listOf(navArgument("kind") { defaultValue = "gp_meeting" }),
        ) {
            composable(NavRoutes.REGISTRO_PASO1) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH_ROUTE)
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
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH_ROUTE)
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
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH_ROUTE)
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
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH_ROUTE)
                }
                val sharedVm: RegistroViewModel = hiltViewModel(graphEntry)
                AgregarActividadScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel      = sharedVm,
                )
            }

            composable(NavRoutes.REGISTRO_PASO3) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH_ROUTE)
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
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH_ROUTE)
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
                    navController.getBackStackEntry(NavRoutes.REGISTRO_GRAPH_ROUTE)
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

