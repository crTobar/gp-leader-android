package com.gpleader.app.feature.miembros

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// ── Modelos ───────────────────────────────────────────────────────────────────

enum class EstadoMiembro { ACTIVO, ARCHIVADO }

data class AsistenciaResumen(
    val fecha:  String,   // "12 Mar"
    val estado: String,   // "P", "A", "J"
)

data class MiembroUi(
    val id:              String,
    val primerNombre:    String,
    val segundoNombre:   String = "",
    val primerApellido:  String,
    val segundoApellido: String = "",
    val telefono:        String = "",
    val correo:          String = "",
    val direccion:       String = "",
    val estado:          EstadoMiembro       = EstadoMiembro.ACTIVO,
    val mesIngreso:      String              = "Ene 2025",
    val historial:       List<AsistenciaResumen> = emptyList(),
) {
    val nombreCompleto: String
        get() = "$primerNombre $primerApellido"
    val iniciales: String
        get() = "${primerNombre.firstOrNull() ?: ""}${primerApellido.firstOrNull() ?: ""}".uppercase()
}

// ── Sample data ───────────────────────────────────────────────────────────────

private val sampleHistorial = listOf(
    AsistenciaResumen("12 Mar", "P"),
    AsistenciaResumen("05 Mar", "A"),
    AsistenciaResumen("26 Feb", "P"),
    AsistenciaResumen("19 Feb", "P"),
    AsistenciaResumen("12 Feb", "J"),
)

private val sampleMiembros = listOf(
    MiembroUi("1", "Carlos",   "",      "Ramírez",   "",       "8812-3456", "carlos.ramirez@gmail.com",  "San José, Costa Rica",     EstadoMiembro.ACTIVO,    "Mar 2024", sampleHistorial),
    MiembroUi("2", "Ana",      "María", "López",     "",       "8823-4567", "ana.lopez@gmail.com",       "Alajuela, Costa Rica",     EstadoMiembro.ACTIVO,    "Jun 2024", sampleHistorial),
    MiembroUi("3", "Luis",     "",      "Hernández", "Mora",   "8834-5678", "",                          "Heredia, Costa Rica",      EstadoMiembro.ACTIVO,    "Ene 2025", sampleHistorial),
    MiembroUi("4", "Sofía",    "",      "Vargas",    "",       "8845-6789", "sofia.vargas@gmail.com",    "Cartago, Costa Rica",      EstadoMiembro.ACTIVO,    "Feb 2025", sampleHistorial),
    MiembroUi("5", "Pedro",    "José",  "Castillo",  "",       "8856-7890", "",                          "San José, Costa Rica",     EstadoMiembro.ACTIVO,    "Ago 2024", sampleHistorial),
    MiembroUi("6", "Laura",    "",      "Jiménez",   "Solís",  "8867-8901", "laura.jimenez@gmail.com",   "San José, Costa Rica",     EstadoMiembro.ACTIVO,    "Nov 2024", sampleHistorial),
    MiembroUi("7", "Roberto",  "",      "Mora",      "",       "8878-9012", "roberto.mora@gmail.com",    "Desamparados, Costa Rica", EstadoMiembro.ACTIVO,    "Dic 2024", sampleHistorial),
    MiembroUi("8", "Carmen",   "",      "Torres",    "Bravo",  "8889-0123", "",                          "San José, Costa Rica",     EstadoMiembro.ACTIVO,    "Ene 2025", sampleHistorial),
    MiembroUi("9", "Miguel",   "",      "Soto",      "",       "",           "miguel.soto@gmail.com",     "Escazú, Costa Rica",       EstadoMiembro.ARCHIVADO, "Jul 2023", sampleHistorial),
    MiembroUi("10","Patricia", "",      "Rojas",     "",       "8800-1234", "patricia.rojas@gmail.com",  "",                         EstadoMiembro.ARCHIVADO, "Ene 2023", sampleHistorial),
)

// ── UI State ──────────────────────────────────────────────────────────────────

data class MiembrosUiState(
    val miembros: List<MiembroUi> = sampleMiembros,
    val query:    String          = "",

    // Miembro seleccionado (detalle / editar)
    val miembroId: String? = null,

    // Editar — campos
    val editPrimerNombre:    String = "",
    val editSegundoNombre:   String = "",
    val editPrimerApellido:  String = "",
    val editSegundoApellido: String = "",
    val editTelefono:        String = "",
    val editCorreo:          String = "",
    val editDireccion:       String = "",
    val editEstado:          EstadoMiembro = EstadoMiembro.ACTIVO,

    // Expandibles
    val editSegundoNombreExpandido:   Boolean = false,
    val editSegundoApellidoExpandido: Boolean = false,

    // Validación
    val editPrimerNombreError:   Boolean = false,
    val editPrimerApellidoError: Boolean = false,

    // Guardado
    val isSaving:     Boolean = false,
    val saveSuccess:  Boolean = false,

    // Agregar — campos
    val agregarPrimerNombre:    String = "",
    val agregarSegundoNombre:   String = "",
    val agregarPrimerApellido:  String = "",
    val agregarSegundoApellido: String = "",
    val agregarTelefono:        String = "",
    val agregarCorreo:          String = "",
    val agregarDireccion:       String = "",

    // Agregar — expandibles
    val agregarSegundoNombreExpandido:   Boolean = false,
    val agregarSegundoApellidoExpandido: Boolean = false,

    // Agregar — validación
    val agregarPrimerNombreError:   Boolean = false,
    val agregarPrimerApellidoError: Boolean = false,

    // Navegación
    val navigateToDetalle:  Boolean = false,
    val navigateToEditar:   Boolean = false,
    val navigateEditarBack: Boolean = false,
    val navigateListaBack:  Boolean = false,
    val navigateAgregarBack: Boolean = false,
) {
    val miembroSeleccionado: MiembroUi?
        get() = miembros.find { it.id == miembroId }

    val activosFiltrados: List<MiembroUi>
        get() = miembros.filter {
            it.estado == EstadoMiembro.ACTIVO && matchesQuery(it, query)
        }

    val archivadosFiltrados: List<MiembroUi>
        get() = miembros.filter {
            it.estado == EstadoMiembro.ARCHIVADO && matchesQuery(it, query)
        }

    val agregarInicialesPreview: String
        get() {
            val n = agregarPrimerNombre.trim().firstOrNull() ?: return ""
            val a = agregarPrimerApellido.trim().firstOrNull()
            return if (a != null) "$n$a".uppercase() else n.uppercaseChar().toString()
        }
}

private fun matchesQuery(m: MiembroUi, q: String): Boolean =
    q.isBlank() ||
    m.primerNombre.contains(q, ignoreCase = true) ||
    m.primerApellido.contains(q, ignoreCase = true) ||
    m.nombreCompleto.contains(q, ignoreCase = true)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class MiembrosViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MiembrosUiState())
    val uiState: StateFlow<MiembrosUiState> = _uiState.asStateFlow()

    // ── Lista ─────────────────────────────────────────────────────────────────

    fun onQueryChange(q: String) {
        _uiState.update { it.copy(query = q) }
    }

    fun onToggleEstadoDesdeListado(miembroId: String) {
        _uiState.update { s ->
            s.copy(
                miembros = s.miembros.map { m ->
                    if (m.id == miembroId)
                        m.copy(estado = if (m.estado == EstadoMiembro.ACTIVO) EstadoMiembro.ARCHIVADO else EstadoMiembro.ACTIVO)
                    else m
                }
            )
        }
    }

    fun onMiembroClick(id: String) {
        _uiState.update { it.copy(miembroId = id, navigateToDetalle = true) }
    }

    fun consumeNavigateToDetalle() {
        _uiState.update { it.copy(navigateToDetalle = false) }
    }

    // ── Detalle ───────────────────────────────────────────────────────────────

    fun onEditarClick() {
        _uiState.value.miembroSeleccionado?.let { m ->
            _uiState.update {
                it.copy(
                    editPrimerNombre    = m.primerNombre,
                    editSegundoNombre   = m.segundoNombre,
                    editPrimerApellido  = m.primerApellido,
                    editSegundoApellido = m.segundoApellido,
                    editTelefono        = m.telefono,
                    editCorreo          = m.correo,
                    editDireccion       = m.direccion,
                    editEstado          = m.estado,
                    editSegundoNombreExpandido   = m.segundoNombre.isNotBlank(),
                    editSegundoApellidoExpandido = m.segundoApellido.isNotBlank(),
                    editPrimerNombreError   = false,
                    editPrimerApellidoError = false,
                    navigateToEditar = true,
                )
            }
        }
    }

    fun consumeNavigateToEditar() {
        _uiState.update { it.copy(navigateToEditar = false) }
    }

    // ── Editar ────────────────────────────────────────────────────────────────

    fun onEditPrimerNombreChange(v: String)    { _uiState.update { it.copy(editPrimerNombre    = v, editPrimerNombreError   = false) } }
    fun onEditSegundoNombreChange(v: String)   { _uiState.update { it.copy(editSegundoNombre   = v) } }
    fun onEditPrimerApellidoChange(v: String)  { _uiState.update { it.copy(editPrimerApellido  = v, editPrimerApellidoError = false) } }
    fun onEditSegundoApellidoChange(v: String) { _uiState.update { it.copy(editSegundoApellido = v) } }
    fun onEditTelefonoChange(v: String)        { _uiState.update { it.copy(editTelefono        = v) } }
    fun onEditCorreoChange(v: String)          { _uiState.update { it.copy(editCorreo          = v) } }
    fun onEditDireccionChange(v: String)       { _uiState.update { it.copy(editDireccion       = v) } }

    fun onToggleEditSegundoNombre()   { _uiState.update { it.copy(editSegundoNombreExpandido   = !it.editSegundoNombreExpandido) } }
    fun onToggleEditSegundoApellido() { _uiState.update { it.copy(editSegundoApellidoExpandido = !it.editSegundoApellidoExpandido) } }

    fun onToggleEstado() {
        _uiState.update {
            it.copy(editEstado = if (it.editEstado == EstadoMiembro.ACTIVO) EstadoMiembro.ARCHIVADO else EstadoMiembro.ACTIVO)
        }
    }

    fun onGuardarEdicion() {
        val nombreError   = _uiState.value.editPrimerNombre.isBlank()
        val apellidoError = _uiState.value.editPrimerApellido.isBlank()
        if (nombreError || apellidoError) {
            _uiState.update { it.copy(editPrimerNombreError = nombreError, editPrimerApellidoError = apellidoError) }
            return
        }
        val id = _uiState.value.miembroId ?: return
        _uiState.update { state ->
            val updated = state.miembros.map { m ->
                if (m.id == id) m.copy(
                    primerNombre    = state.editPrimerNombre.trim(),
                    segundoNombre   = state.editSegundoNombre.trim(),
                    primerApellido  = state.editPrimerApellido.trim(),
                    segundoApellido = state.editSegundoApellido.trim(),
                    telefono        = state.editTelefono.trim(),
                    correo          = state.editCorreo.trim(),
                    direccion       = state.editDireccion.trim(),
                    estado          = state.editEstado,
                ) else m
            }
            state.copy(miembros = updated, navigateEditarBack = true)
        }
    }

    fun consumeNavigateEditarBack() {
        _uiState.update { it.copy(navigateEditarBack = false) }
    }

    fun onNavigateListaBack() {
        _uiState.update { it.copy(navigateListaBack = true) }
    }

    fun consumeNavigateListaBack() {
        _uiState.update { it.copy(navigateListaBack = false) }
    }

    // ── Agregar ───────────────────────────────────────────────────────────────

    fun onPrepararAgregar() {
        _uiState.update {
            it.copy(
                agregarPrimerNombre             = "",
                agregarSegundoNombre            = "",
                agregarPrimerApellido            = "",
                agregarSegundoApellido           = "",
                agregarTelefono                  = "",
                agregarCorreo                    = "",
                agregarDireccion                 = "",
                agregarSegundoNombreExpandido    = false,
                agregarSegundoApellidoExpandido  = false,
                agregarPrimerNombreError         = false,
                agregarPrimerApellidoError       = false,
            )
        }
    }

    fun onAgregarPrimerNombreChange(v: String)    { _uiState.update { it.copy(agregarPrimerNombre    = v, agregarPrimerNombreError   = false) } }
    fun onAgregarSegundoNombreChange(v: String)   { _uiState.update { it.copy(agregarSegundoNombre   = v) } }
    fun onAgregarPrimerApellidoChange(v: String)  { _uiState.update { it.copy(agregarPrimerApellido  = v, agregarPrimerApellidoError = false) } }
    fun onAgregarSegundoApellidoChange(v: String) { _uiState.update { it.copy(agregarSegundoApellido = v) } }
    fun onAgregarTelefonoChange(v: String)        { _uiState.update { it.copy(agregarTelefono        = v) } }
    fun onAgregarCorreoChange(v: String)          { _uiState.update { it.copy(agregarCorreo          = v) } }
    fun onAgregarDireccionChange(v: String)       { _uiState.update { it.copy(agregarDireccion       = v) } }

    fun onToggleAgregarSegundoNombre()   { _uiState.update { it.copy(agregarSegundoNombreExpandido   = !it.agregarSegundoNombreExpandido) } }
    fun onToggleAgregarSegundoApellido() { _uiState.update { it.copy(agregarSegundoApellidoExpandido = !it.agregarSegundoApellidoExpandido) } }

    fun onAgregarMiembro() {
        val nombreError   = _uiState.value.agregarPrimerNombre.isBlank()
        val apellidoError = _uiState.value.agregarPrimerApellido.isBlank()
        if (nombreError || apellidoError) {
            _uiState.update { it.copy(agregarPrimerNombreError = nombreError, agregarPrimerApellidoError = apellidoError) }
            return
        }
        _uiState.update { state ->
            val nuevoId = (state.miembros.size + 1).toString()
            val nuevo = MiembroUi(
                id              = nuevoId,
                primerNombre    = state.agregarPrimerNombre.trim(),
                segundoNombre   = state.agregarSegundoNombre.trim(),
                primerApellido  = state.agregarPrimerApellido.trim(),
                segundoApellido = state.agregarSegundoApellido.trim(),
                telefono        = state.agregarTelefono.trim(),
                correo          = state.agregarCorreo.trim(),
                direccion       = state.agregarDireccion.trim(),
                estado          = EstadoMiembro.ACTIVO,   // SIEMPRE ACTIVO al agregar
                mesIngreso      = "Mar 2026",
            )
            // TODO: supabaseClient.insert(nuevo)
            state.copy(miembros = state.miembros + nuevo, navigateAgregarBack = true)
        }
    }

    fun consumeNavigateAgregarBack() {
        _uiState.update { it.copy(navigateAgregarBack = false) }
    }
}
