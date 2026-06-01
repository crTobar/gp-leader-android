package com.gpleader.app.feature.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.AsignadoPotencial
import com.gpleader.app.core.data.repository.GroupLogRepository
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.SolicitudRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Modelos ───────────────────────────────────────────────────────────────────

data class PasswordRequisitos(
    val tieneOchoCaracteres:  Boolean = false,
    val tieneMayuscula:       Boolean = false,
    val tieneNumero:          Boolean = false,
    val contrasenasCoinciden: Boolean = false,
) {
    val todosCompletos: Boolean
        get() = tieneOchoCaracteres && tieneMayuscula && tieneNumero && contrasenasCoinciden
}

enum class DiaSemana(val display: String) {
    LUNES("Lunes"), MARTES("Martes"), MIERCOLES("Miércoles"),
    JUEVES("Jueves"), VIERNES("Viernes"), SABADO("Sábado"), DOMINGO("Domingo")
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun generarHorasOpciones(): List<String> {
    val result = mutableListOf<String>()
    for (totalMin in (6 * 60)..(23 * 60) step 30) {
        val h = totalMin / 60
        val m = totalMin % 60
        val amPm = if (h < 12) "AM" else "PM"
        val h12  = when {
            h == 0  -> 12
            h == 12 -> 12
            h > 12  -> h - 12
            else    -> h
        }
        result.add("$h12:${m.toString().padStart(2, '0')} $amPm")
    }
    return result
}

private val DIAS_SEMANA_OPCIONES   = DiaSemana.entries.map { it.display }
private val HORAS_OPCIONES         = generarHorasOpciones()

// ── UI State ──────────────────────────────────────────────────────────────────

data class PerfilUiState(
    // Avatar / identificación
    val nombreCompleto:  String  = "Maria Garcia",
    val iniciales:       String  = "MG",
    val rol:             String  = "Líder de grupo pequeño",
    val nombreGrupo:     String  = "GP Los Olivos",
    val totalMiembros:   Int     = 8,

    // Datos personales — campos editables
    val primerNombre:    String  = "Maria",
    val segundoNombre:   String  = "",
    val primerApellido:  String  = "Garcia",
    val segundoApellido: String  = "",
    val telefono:        String  = "8888-1234",
    val correo:          String  = "maria.garcia@gmail.com",   // readonly
    val direccion:       String  = "San José, Costa Rica",
    val iglesia:         String  = "Iglesia Central · San José", // readonly

    // Expandibles datos personales
    val segundoNombreExpandido:   Boolean = false,
    val segundoApellidoExpandido: Boolean = false,

    // Estados guardado datos personales
    val isSaving:             Boolean = false,
    val saveError:            String? = null,
    val primerNombreError:    Boolean = false,
    val primerApellidoError:  Boolean = false,

    // Cambiar contraseña
    val contrasenaActual:      String  = "",
    val nuevaContrasena:       String  = "",
    val confirmarContrasena:   String  = "",
    val requisitos:            PasswordRequisitos = PasswordRequisitos(),
    val isUpdatingPassword:    Boolean = false,
    val passwordUpdateSuccess: Boolean = false,
    val passwordUpdateError:   String? = null,

    // Datos del grupo — editables
    val descripcion:        String  = "Grupo jóvenes adultos zona norte",
    val lugarReunion:       String  = "Casa de Maria Garcia, San José",
    val cantoFavorito:      String  = "",
    val versiculo:          String  = "",
    val personajeBiblico:   String  = "",

    // Datos del grupo — readonly
    val campo:    String  = "Asociación Central Costarricense",
    val distrito: String  = "Distrito 3 — San José Central",

    // Horario de reunión
    val diaSemana:  DiaSemana = DiaSemana.MIERCOLES,
    val horaInicio: String    = "7:00 PM",
    val horaFin:    String    = "9:00 PM",

    // Listas estáticas
    val diasSemanaOpciones: List<String> = DIAS_SEMANA_OPCIONES,
    val horasOpciones:      List<String> = HORAS_OPCIONES,

    // Visibilidad dropdowns
    val showDiaDropdown:        Boolean = false,
    val showHoraInicioDropdown: Boolean = false,
    val showHoraFinDropdown:    Boolean = false,

    // Errores y guardado grupo
    val nombreGrupoError: Boolean = false,
    val isSavingGrupo:    Boolean = false,

    // Dialog
    val showCerrarSesionDialog: Boolean = false,

    // Navegación — PerfilPrincipal
    val navigateToDatosPersonales:   Boolean = false,
    val navigateToCambiarContrasena: Boolean = false,
    val navigateToDatosGrupo:        Boolean = false,
    val navigateToMiembros:          Boolean = false,
    val navigateToNotificaciones:    Boolean = false,
    val navigateToLogin:             Boolean = false,
    val navigateToQuienEres:         Boolean = false,
    val navigateToRegistroActividad: Boolean = false,

    // Navegación — pantallas de retorno
    val navigateDatosPersonalesBack: Boolean = false,
    val navigateDatosGrupoBack:      Boolean = false,

    val navigateToActividadesLista:  Boolean = false,

    // Asignar suplente (sheet)
    val showDelegarSheet:     Boolean                 = false,
    val asignadosPotenciales: List<AsignadoPotencial> = emptyList(),
    val isLoadingAsignados:   Boolean                 = false,
    val isCreandoSolicitud:   Boolean                 = false,
    val solicitudError:       String?                 = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val supabase:       SupabaseClient,
    private val session:        SessionManager,
    private val miembroRepo:    MiembroRepository,
    private val solicitudRepo:  SolicitudRepository,
    private val groupLogRepo:   GroupLogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init { cargarTotalMiembros() }

    private fun cargarTotalMiembros() {
        viewModelScope.launch {
            miembroRepo.getMiembrosActivos(session.grupoId)
                .catch { }
                .collect { lista ->
                    _uiState.update { it.copy(totalMiembros = lista.size) }
                }
        }
    }

    // ── PerfilPrincipal ───────────────────────────────────────────────────────

    fun onDatosPersonalesClick()   { _uiState.update { it.copy(navigateToDatosPersonales = true) } }
    fun onCambiarContrasenaClick() { _uiState.update { it.copy(navigateToCambiarContrasena = true) } }
    fun onDatosGrupoClick()        { _uiState.update { it.copy(navigateToDatosGrupo = true) } }
    fun onMiembrosClick()          { _uiState.update { it.copy(navigateToMiembros = true) } }
    fun onNotificacionesClick()    { _uiState.update { it.copy(navigateToNotificaciones = true) } }

    fun onCerrarSesionClick()        { _uiState.update { it.copy(showCerrarSesionDialog = true) } }
    fun onDismissCerrarSesionDialog() { _uiState.update { it.copy(showCerrarSesionDialog = false) } }
    fun onConfirmarCerrarSesion() {
        _uiState.update { it.copy(showCerrarSesionDialog = false) }
        viewModelScope.launch {
            runCatching { supabase.auth.signOut() }
            session.clear()
            _uiState.update { it.copy(navigateToLogin = true) }
        }
    }
    fun onEditarAvatarClick()        { /* TODO */ }
    fun onCambiarQuienUsaClick()          { _uiState.update { it.copy(navigateToQuienEres = true) } }
    fun consumeQuienEresNavigation()      { _uiState.update { it.copy(navigateToQuienEres = false) } }
    fun onRegistroActividadClick()           { _uiState.update { it.copy(navigateToRegistroActividad = true) } }
    fun consumeRegistroActividadNavigation() { _uiState.update { it.copy(navigateToRegistroActividad = false) } }
    fun onActividadesListaClick()            { _uiState.update { it.copy(navigateToActividadesLista = true) } }
    fun consumeActividadesListaNavigation()  { _uiState.update { it.copy(navigateToActividadesLista = false) } }

    // ── DatosPersonales ───────────────────────────────────────────────────────

    fun onPrimerNombreChange(v: String)    { _uiState.update { it.copy(primerNombre = v, primerNombreError = false) } }
    fun onSegundoNombreChange(v: String)   { _uiState.update { it.copy(segundoNombre = v) } }
    fun onPrimerApellidoChange(v: String)  { _uiState.update { it.copy(primerApellido = v, primerApellidoError = false) } }
    fun onSegundoApellidoChange(v: String) { _uiState.update { it.copy(segundoApellido = v) } }
    fun onTelefonoChange(v: String)        { _uiState.update { it.copy(telefono = v) } }
    fun onDireccionChange(v: String)       { _uiState.update { it.copy(direccion = v) } }
    fun onToggleSegundoNombre()    { _uiState.update { it.copy(segundoNombreExpandido = !it.segundoNombreExpandido) } }
    fun onToggleSegundoApellido()  { _uiState.update { it.copy(segundoApellidoExpandido = !it.segundoApellidoExpandido) } }

    fun onGuardarDatosPersonales() {
        val nombreError   = _uiState.value.primerNombre.isBlank()
        val apellidoError = _uiState.value.primerApellido.isBlank()
        if (nombreError || apellidoError) {
            _uiState.update { it.copy(primerNombreError = nombreError, primerApellidoError = apellidoError) }
            return
        }
        val nombre    = "${_uiState.value.primerNombre.trim()} ${_uiState.value.primerApellido.trim()}"
        val iniciales = "${_uiState.value.primerNombre.trim().firstOrNull() ?: ""}${_uiState.value.primerApellido.trim().firstOrNull() ?: ""}".uppercase()
        _uiState.update { it.copy(nombreCompleto = nombre, iniciales = iniciales, navigateDatosPersonalesBack = true) }
    }

    // ── CambiarContraseña ─────────────────────────────────────────────────────

    fun onContrasenaActualChange(v: String) { _uiState.update { it.copy(contrasenaActual = v) } }

    fun onNuevaContrasenaChange(v: String) {
        val confirmar  = _uiState.value.confirmarContrasena
        val req = PasswordRequisitos(
            tieneOchoCaracteres  = v.length >= 8,
            tieneMayuscula       = v.any { it.isUpperCase() },
            tieneNumero          = v.any { it.isDigit() },
            contrasenasCoinciden = v.isNotEmpty() && v == confirmar,
        )
        _uiState.update { it.copy(nuevaContrasena = v, requisitos = req) }
    }

    fun onConfirmarContrasenaChange(v: String) {
        val nueva = _uiState.value.nuevaContrasena
        _uiState.update {
            it.copy(
                confirmarContrasena = v,
                requisitos = it.requisitos.copy(contrasenasCoinciden = nueva.isNotEmpty() && nueva == v),
            )
        }
    }

    fun onActualizarContrasenaClick(esPrimerLogin: Boolean = false) {
        val state = _uiState.value
        if (!state.requisitos.todosCompletos) return
        if (!esPrimerLogin && state.contrasenaActual.isBlank()) return

        _uiState.update { it.copy(isUpdatingPassword = true, passwordUpdateError = null) }
        viewModelScope.launch {
            runCatching {
                val nuevaContrasena = state.nuevaContrasena

                val token: String = if (esPrimerLogin) {
                    // Usar el token ya guardado al hacer login
                    session.sessionToken.takeIf { it.isNotBlank() } ?: error("Sin sesión activa")
                } else {
                    // Re-autenticar con la contraseña actual para obtener token fresco
                    val gpCode = session.gpCode
                    if (gpCode.isBlank()) error("Este grupo no tiene código de acceso configurado")
                    val resp = supabase.postgrest.rpc("gp_login", buildJsonObject {
                        put("p_gp_code",    gpCode)
                        put("p_password",   state.contrasenaActual)
                        put("p_device_info", "Android")
                    })
                    val row = Json.parseToJsonElement(resp.data).jsonArray.firstOrNull()?.jsonObject
                    row?.get("session_token")?.jsonPrimitive?.contentOrNull
                        ?: error("Contraseña actual incorrecta")
                }

                // 1. Actualizar gp_password en small_group vía función segura
                supabase.postgrest.rpc("gp_set_password", buildJsonObject {
                    put("p_token",        token)
                    put("p_new_password", nuevaContrasena)
                })

                // 2. Actualizar contraseña en Supabase Auth (para future signIn)
                supabase.auth.updateUser { password = nuevaContrasena }

                // 3. Marcar contraseña como establecida en sesión local
                session.grupoPasswordSet = true
                session.sessionToken     = ""
            }.onSuccess {
                _uiState.update { it.copy(isUpdatingPassword = false, passwordUpdateSuccess = true) }
            }.onFailure { e ->
                val msg = when {
                    e.message?.contains("invalid_session") == true  -> "La sesión expiró. Intentá cerrar e ingresar de nuevo."
                    e.message?.contains("Contraseña actual") == true -> "Contraseña actual incorrecta"
                    else -> "Error al actualizar la contraseña"
                }
                _uiState.update { it.copy(isUpdatingPassword = false, passwordUpdateError = msg) }
            }
        }
    }

    fun consumePasswordUpdateSuccess() {
        _uiState.update { it.copy(passwordUpdateSuccess = false, contrasenaActual = "", nuevaContrasena = "", confirmarContrasena = "", requisitos = PasswordRequisitos()) }
    }
    fun consumePasswordUpdateError() { _uiState.update { it.copy(passwordUpdateError = null) } }

    // ── DatosGrupo ────────────────────────────────────────────────────────────

    fun onNombreGrupoChange(v: String)      { _uiState.update { it.copy(nombreGrupo = v, nombreGrupoError = false) } }
    fun onDescripcionChange(v: String)      { _uiState.update { it.copy(descripcion = v) } }
    fun onLugarReunionChange(v: String)     { _uiState.update { it.copy(lugarReunion = v) } }
    fun onCantoFavoritoChange(v: String)    { _uiState.update { it.copy(cantoFavorito = v) } }
    fun onVersiculoChange(v: String)        { _uiState.update { it.copy(versiculo = v) } }
    fun onPersonajeBiblicoChange(v: String) { _uiState.update { it.copy(personajeBiblico = v) } }

    fun onDiaSemanaChange(dia: String) {
        val diaEnum = DiaSemana.entries.find { it.display == dia } ?: return
        _uiState.update { it.copy(diaSemana = diaEnum, showDiaDropdown = false) }
    }
    fun onHoraInicioChange(hora: String) { _uiState.update { it.copy(horaInicio = hora, showHoraInicioDropdown = false) } }
    fun onHoraFinChange(hora: String)    { _uiState.update { it.copy(horaFin = hora, showHoraFinDropdown = false) } }

    fun onToggleDiaDropdown()        { _uiState.update { it.copy(showDiaDropdown = !it.showDiaDropdown, showHoraInicioDropdown = false, showHoraFinDropdown = false) } }
    fun onToggleHoraInicioDropdown() { _uiState.update { it.copy(showHoraInicioDropdown = !it.showHoraInicioDropdown, showDiaDropdown = false, showHoraFinDropdown = false) } }
    fun onToggleHoraFinDropdown()    { _uiState.update { it.copy(showHoraFinDropdown = !it.showHoraFinDropdown, showDiaDropdown = false, showHoraInicioDropdown = false) } }

    fun onGuardarDatosGrupo() {
        if (_uiState.value.nombreGrupo.isBlank()) {
            _uiState.update { it.copy(nombreGrupoError = true) }
            return
        }
        // TODO: supabaseClient update grupo data
        _uiState.update { it.copy(isSavingGrupo = false, navigateDatosGrupoBack = true) }
    }

    // ── Consumidores de navegación ────────────────────────────────────────────

    fun consumeDatosPersonalesNavigation()   { _uiState.update { it.copy(navigateToDatosPersonales = false) } }
    fun consumeCambiarContrasenaNavigation() { _uiState.update { it.copy(navigateToCambiarContrasena = false) } }
    fun consumeDatosGrupoNavigation()        { _uiState.update { it.copy(navigateToDatosGrupo = false) } }
    fun consumeMiembrosNavigation()          { _uiState.update { it.copy(navigateToMiembros = false) } }
    fun consumeNotificacionesNavigation()    { _uiState.update { it.copy(navigateToNotificaciones = false) } }
    fun consumeLoginNavigation()             { _uiState.update { it.copy(navigateToLogin = false) } }
    fun consumeDatosPersonalesBackNavigation() { _uiState.update { it.copy(navigateDatosPersonalesBack = false) } }
    fun consumeDatosGrupoBackNavigation()    { _uiState.update { it.copy(navigateDatosGrupoBack = false) } }

    // ── Asignar suplente ──────────────────────────────────────────────────────

    fun onAsignarSuplenteClick() {
        _uiState.update { it.copy(showDelegarSheet = true, isLoadingAsignados = true, solicitudError = null) }
        viewModelScope.launch {
            runCatching { solicitudRepo.getAsignadosPotenciales(session.grupoId) }
                .onSuccess { lista ->
                    val sinLider = lista.filter { it.profileId != session.miembroId }
                    _uiState.update { it.copy(asignadosPotenciales = sinLider, isLoadingAsignados = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingAsignados = false) }
                }
        }
    }

    fun onDismissDelegarSheet() {
        _uiState.update { it.copy(showDelegarSheet = false, solicitudError = null) }
    }

    fun onCrearSolicitud(assignedToId: String, nota: String?) {
        _uiState.update { it.copy(isCreandoSolicitud = true, solicitudError = null) }
        viewModelScope.launch {
            runCatching {
                solicitudRepo.createSolicitud(assignedToId, session.grupoId, nota)
            }.onSuccess {
                val nombreDelegado = _uiState.value.asignadosPotenciales
                    .find { it.profileId == assignedToId }?.nombre ?: "miembro"
                groupLogRepo.logAccion(session.grupoId, "deputy_submission_created", "Delegación creada para $nombreDelegado")
                _uiState.update { it.copy(isCreandoSolicitud = false, showDelegarSheet = false) }
            }.onFailure { e ->
                val msg = when {
                    e.message?.contains("duplicate_solicitude") == true ->
                        "Esta persona ya tiene una solicitud pendiente para este grupo"
                    else -> "No se pudo crear la delegación"
                }
                _uiState.update { it.copy(isCreandoSolicitud = false, solicitudError = msg) }
            }
        }
    }
}
