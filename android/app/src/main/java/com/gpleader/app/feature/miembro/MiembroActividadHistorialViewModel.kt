package com.gpleader.app.feature.miembro

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.MemberEntry
import com.gpleader.app.core.data.repository.MemberEntryRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MiembroActividadHistorialUiState(
    val isLoading:       Boolean = true,
    val isRefreshing:    Boolean = false,
    val nombreActividad: String = "",
    val markerType:      String = "counter",
    val unitLabel:       String = "",
    val total:           Double = 0.0,
    val entries:         List<MemberEntry> = emptyList(),
    val showAddDialog:   Boolean = false,
    val nuevaCantidad:   Int = 1,
    val editando:        MemberEntry? = null,   // entry en edición (null = no)
    val isGuardando:     Boolean = false,
    val error:           String? = null,
)

@HiltViewModel
class MiembroActividadHistorialViewModel @Inject constructor(
    savedStateHandle:          SavedStateHandle,
    private val actividadRepo:   ActividadRepository,
    private val memberEntryRepo: MemberEntryRepository,
    private val session:         SessionManager,
) : ViewModel() {

    private val actividadTipoId: String = checkNotNull(savedStateHandle["actividadTipoId"])

    private val _uiState = MutableStateFlow(MiembroActividadHistorialUiState())
    val uiState: StateFlow<MiembroActividadHistorialUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            cargarInterno()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            cargarInterno()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun cargarInterno() {
        actividadRepo.getTodasActividadesTipo(
            session.iglesiaId, session.districtId, session.campoId, session.grupoId
        ).onSuccess { tipos ->
            tipos.find { it.id == actividadTipoId }?.let { tipo ->
                _uiState.update { s ->
                    s.copy(nombreActividad = tipo.nombre, markerType = tipo.markerType, unitLabel = tipo.unitLabel)
                }
            }
        }
        memberEntryRepo.getEntries(session.miembroId, actividadTipoId).onSuccess { entries ->
            _uiState.update { it.copy(entries = entries) }
        }.onFailure { e ->
            _uiState.update { it.copy(error = e.message) }
        }
        memberEntryRepo.getEntryTotal(session.miembroId, actividadTipoId).onSuccess { total ->
            _uiState.update { it.copy(total = total) }
        }
    }

    // ── Agregar ────────────────────────────────────────────────────────────────
    fun onShowAddDialog()        = _uiState.update { it.copy(showAddDialog = true, nuevaCantidad = 1, editando = null) }
    fun onDismissDialog()        = _uiState.update { it.copy(showAddDialog = false, editando = null) }
    fun onCantidadChange(v: Int) = _uiState.update { it.copy(nuevaCantidad = v.coerceAtLeast(0)) }

    fun onGuardar() {
        val cantidad = _uiState.value.nuevaCantidad
        if (cantidad <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isGuardando = true) }
            memberEntryRepo.addEntry(
                miembroId       = session.miembroId,
                actividadTipoId = actividadTipoId,
                grupoId         = session.grupoId,
                value           = cantidad.toDouble(),
            ).fold(
                onSuccess = { _uiState.update { it.copy(isGuardando = false, showAddDialog = false) }; cargarInterno() },
                onFailure = { e -> _uiState.update { it.copy(isGuardando = false, error = e.message) } },
            )
        }
    }

    // ── Editar ───────────────────────────────────────────────────────────────
    fun onEditarEntry(entry: MemberEntry) =
        _uiState.update { it.copy(editando = entry, nuevaCantidad = entry.value.toInt(), showAddDialog = true) }

    fun onConfirmarEdicion() {
        val entry    = _uiState.value.editando ?: return
        val cantidad = _uiState.value.nuevaCantidad
        if (cantidad <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isGuardando = true) }
            memberEntryRepo.editEntry(
                entryId   = entry.id,
                newValue  = cantidad.toDouble(),
                actorRole = "member",
                actorId   = session.miembroId,
            ).fold(
                onSuccess = { _uiState.update { it.copy(isGuardando = false, showAddDialog = false, editando = null) }; cargarInterno() },
                onFailure = { e -> _uiState.update { it.copy(isGuardando = false, error = e.message) } },
            )
        }
    }

    // ── Borrar ───────────────────────────────────────────────────────────────
    fun onBorrarEntry(entry: MemberEntry) {
        viewModelScope.launch {
            memberEntryRepo.deleteEntry(entry.id, actorRole = "member", actorId = session.miembroId)
                .onSuccess { cargarInterno() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }
}
