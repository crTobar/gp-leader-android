package com.gpleader.app.feature.miembros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.GroupLogRepository
import com.gpleader.app.core.data.repository.MiembroData
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// ── Modelos ───────────────────────────────────────────────────────────────────

enum class EstadoMiembro { ACTIVO, ARCHIVADO }

data class AsistenciaResumen(
    val fecha:  String,
    val estado: String,
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
    val estado:          EstadoMiembro           = EstadoMiembro.ACTIVO,
    val mesIngreso:      String                  = "",
    val historial:       List<AsistenciaResumen> = emptyList(),
    val isLider:         Boolean                 = false,
) {
    val nombreCompleto: String
        get() = "$primerNombre $primerApellido"
    val iniciales: String
        get() = "${primerNombre.firstOrNull() ?: ""}${primerApellido.firstOrNull() ?: ""}".uppercase()
}

// ── UI State ──────────────────────────────────────────────────────────────────

data class MiembrosUiState(
    val miembros:     List<MiembroUi> = emptyList(),
    val query:        String          = "",
    val isLoading:    Boolean         = false,
    val isRefreshing: Boolean         = false,
    val error:        String?         = null,

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

    val editSegundoNombreExpandido:   Boolean = false,
    val editSegundoApellidoExpandido: Boolean = false,

    val editPrimerNombreError:   Boolean = false,
    val editPrimerApellidoError: Boolean = false,

    val isSaving:    Boolean = false,
    val saveSuccess: Boolean = false,

    // Agregar — campos
    val agregarPrimerNombre:    String = "",
    val agregarSegundoNombre:   String = "",
    val agregarPrimerApellido:  String = "",
    val agregarSegundoApellido: String = "",
    val agregarTelefono:        String = "",
    val agregarCorreo:          String = "",
    val agregarDireccion:       String = "",

    val agregarSegundoNombreExpandido:   Boolean = false,
    val agregarSegundoApellidoExpandido: Boolean = false,

    val agregarPrimerNombreError:   Boolean = false,
    val agregarPrimerApellidoError: Boolean = false,

    // Navegación
    val navigateToDetalle:   Boolean = false,
    val navigateToEditar:    Boolean = false,
    val navigateEditarBack:  Boolean = false,
    val navigateListaBack:   Boolean = false,
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
class MiembrosViewModel @Inject constructor(
    private val miembroRepo:   MiembroRepository,
    private val groupLogRepo:  GroupLogRepository,
    private val session:       SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiembrosUiState())
    val uiState: StateFlow<MiembrosUiState> = _uiState.asStateFlow()

    init { cargarMiembros() }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            miembroRepo.getMiembros(session.grupoId)
                .catch { e -> _uiState.update { it.copy(isRefreshing = false, error = e.message) } }
                .collect { lista ->
                    _uiState.update { it.copy(isRefreshing = false, miembros = lista.map { m -> m.toUi() }) }
                }
        }
    }

    private fun cargarMiembros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            miembroRepo.getMiembros(session.grupoId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { lista ->
                    _uiState.update { it.copy(isLoading = false, miembros = lista.map { m -> m.toUi() }) }
                }
        }
    }

    // ── Lista ─────────────────────────────────────────────────────────────────

    fun onQueryChange(q: String) {
        _uiState.update { it.copy(query = q) }
    }

    fun onToggleEstadoDesdeListado(miembroId: String) {
        val miembro = _uiState.value.miembros.find { it.id == miembroId } ?: return
        if (miembro.isLider) return
        val nuevoActivo = miembro.estado != EstadoMiembro.ACTIVO
        _uiState.update { s ->
            s.copy(miembros = s.miembros.map { m ->
                if (m.id == miembroId) m.copy(
                    estado = if (nuevoActivo) EstadoMiembro.ACTIVO else EstadoMiembro.ARCHIVADO
                ) else m
            })
        }
        viewModelScope.launch {
            runCatching { miembroRepo.toggleActivoMiembro(miembroId, nuevoActivo) }
                .onSuccess {
                    val accion = if (nuevoActivo) "member_unarchived" else "member_archived"
                    val verbo  = if (nuevoActivo) "restaurado" else "archivado"
                    val nombre = miembro.nombreCompleto
                    groupLogRepo.logAccion(session.grupoId, accion, "$nombre fue $verbo")
                }
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
        val miembro = _uiState.value.miembros.find { it.id == _uiState.value.miembroId }
        if (miembro?.isLider == true) return
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
        val id    = _uiState.value.miembroId ?: return
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                miembroRepo.actualizarMiembro(
                    miembroId       = id,
                    primerNombre    = state.editPrimerNombre.trim(),
                    segundoNombre   = state.editSegundoNombre.trim().takeIf { it.isNotBlank() },
                    primerApellido  = state.editPrimerApellido.trim(),
                    segundoApellido = state.editSegundoApellido.trim().takeIf { it.isNotBlank() },
                    telefono        = state.editTelefono.trim().takeIf { it.isNotBlank() },
                    correo          = state.editCorreo.trim().takeIf { it.isNotBlank() },
                    isActive        = state.editEstado == EstadoMiembro.ACTIVO,
                )
            }.onSuccess { updated ->
                // Log cambio de estado si fue modificado
                val anterior = _uiState.value.miembros.find { it.id == id }
                if (anterior != null && anterior.estado != state.editEstado) {
                    val accion = if (state.editEstado == EstadoMiembro.ACTIVO) "member_unarchived" else "member_archived"
                    val verbo  = if (state.editEstado == EstadoMiembro.ACTIVO) "restaurado" else "archivado"
                    groupLogRepo.logAccion(session.grupoId, accion, "${updated.toUi().nombreCompleto} fue $verbo")
                }
                _uiState.update { s ->
                    val historialExistente = s.miembros.find { it.id == id }?.historial ?: emptyList()
                    s.copy(
                        isSaving           = false,
                        saveSuccess        = true,
                        navigateEditarBack = true,
                        miembros           = s.miembros.map {
                            if (it.id == id) updated.toUi().copy(historial = historialExistente) else it
                        },
                    )
                }
                cargarMiembros()
            }.onFailure {
                _uiState.update { it.copy(isSaving = false, error = "Error al guardar los cambios") }
            }
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
                agregarPrimerNombre            = "",
                agregarSegundoNombre           = "",
                agregarPrimerApellido          = "",
                agregarSegundoApellido         = "",
                agregarTelefono                = "",
                agregarCorreo                  = "",
                agregarDireccion               = "",
                agregarSegundoNombreExpandido  = false,
                agregarSegundoApellidoExpandido = false,
                agregarPrimerNombreError       = false,
                agregarPrimerApellidoError     = false,
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
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                miembroRepo.agregarMiembro(
                    grupoId         = session.grupoId,
                    primerNombre    = state.agregarPrimerNombre.trim(),
                    segundoNombre   = state.agregarSegundoNombre.trim().takeIf { it.isNotBlank() },
                    primerApellido  = state.agregarPrimerApellido.trim(),
                    segundoApellido = state.agregarSegundoApellido.trim().takeIf { it.isNotBlank() },
                    telefono        = state.agregarTelefono.trim().takeIf { it.isNotBlank() },
                    correo          = state.agregarCorreo.trim().takeIf { it.isNotBlank() },
                )
            }.onSuccess { nuevo ->
                val nombre = nuevo.toUi().nombreCompleto
                groupLogRepo.logAccion(session.grupoId, "member_added", "$nombre fue agregado al grupo")
                _uiState.update { s ->
                    s.copy(
                        isSaving            = false,
                        miembros            = s.miembros + nuevo.toUi(),
                        navigateAgregarBack = true,
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isSaving = false, error = "Error al agregar el miembro") }
            }
        }
    }

    fun consumeNavigateAgregarBack() {
        _uiState.update { it.copy(navigateAgregarBack = false) }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}

// ── Mapping ───────────────────────────────────────────────────────────────────

private fun MiembroData.toUi(): MiembroUi = MiembroUi(
    id              = id,
    primerNombre    = primerNombre,
    segundoNombre   = segundoNombre   ?: "",
    primerApellido  = primerApellido,
    segundoApellido = segundoApellido ?: "",
    telefono        = telefono        ?: "",
    correo          = correo          ?: "",
    estado          = if (estado == "ARCHIVADO") EstadoMiembro.ARCHIVADO else EstadoMiembro.ACTIVO,
    mesIngreso      = createdAt?.let { formatMes(it) } ?: "",
    isLider         = isLider,
)

private fun formatMes(iso: String): String = runCatching {
    val date  = LocalDate.parse(iso.take(10))
    val meses = arrayOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
    "${meses[date.monthValue - 1]} ${date.year}"
}.getOrElse { "" }
