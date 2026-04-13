package com.gpleader.app.feature.perfil

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    // Navegación — pantallas de retorno
    val navigateDatosPersonalesBack: Boolean = false,
    val navigateDatosGrupoBack:      Boolean = false,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class PerfilViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    // ── PerfilPrincipal ───────────────────────────────────────────────────────

    fun onDatosPersonalesClick()   { _uiState.update { it.copy(navigateToDatosPersonales = true) } }
    fun onCambiarContrasenaClick() { _uiState.update { it.copy(navigateToCambiarContrasena = true) } }
    fun onDatosGrupoClick()        { _uiState.update { it.copy(navigateToDatosGrupo = true) } }
    fun onMiembrosClick()          { _uiState.update { it.copy(navigateToMiembros = true) } }
    fun onNotificacionesClick()    { _uiState.update { it.copy(navigateToNotificaciones = true) } }

    fun onCerrarSesionClick()        { _uiState.update { it.copy(showCerrarSesionDialog = true) } }
    fun onDismissCerrarSesionDialog() { _uiState.update { it.copy(showCerrarSesionDialog = false) } }
    fun onConfirmarCerrarSesion()    { _uiState.update { it.copy(showCerrarSesionDialog = false, navigateToLogin = true) } }
    fun onEditarAvatarClick()        { /* TODO */ }
    fun onCambiarQuienUsaClick()     { _uiState.update { it.copy(navigateToQuienEres = true) } }
    fun consumeQuienEresNavigation() { _uiState.update { it.copy(navigateToQuienEres = false) } }

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

    fun onActualizarContrasenaClick() {
        if (!_uiState.value.requisitos.todosCompletos || _uiState.value.contrasenaActual.isBlank()) return
        // TODO: supabaseClient.auth.updateUser { password = nuevaContrasena }
        _uiState.update { it.copy(isUpdatingPassword = false, passwordUpdateSuccess = true) }
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
}
