package com.gpleader.app.feature.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.MemberEntryRepository
import com.gpleader.app.core.data.repository.MovimientoAprobacion
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Filtro por tipo de acción de la bitácora de movimientos. */
enum class MovFiltro(val label: String, val actions: Set<String>) {
    TODO("Todo", emptySet()),
    APROBADOS("Aprobados", setOf("approved", "board_approved")),
    RECHAZADOS("Rechazados", setOf("rejected")),
    EDITADOS("Editados", setOf("edited")),
    CREADOS("Creados", setOf("created")),
}

data class MovimientosUiState(
    val isLoading:   Boolean                    = true,
    val error:       String?                    = null,
    val movimientos: List<MovimientoAprobacion> = emptyList(),
    val filtro:      MovFiltro                  = MovFiltro.TODO,
) {
    /** Movimientos visibles según el filtro activo (conserva el orden por fecha del repo). */
    val visibles: List<MovimientoAprobacion>
        get() = if (filtro == MovFiltro.TODO) movimientos
                else movimientos.filter { it.action in filtro.actions }
}

@HiltViewModel
class MovimientosAprobacionViewModel @Inject constructor(
    private val memberEntryRepo: MemberEntryRepository,
    private val session:         SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovimientosUiState())
    val uiState: StateFlow<MovimientosUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            memberEntryRepo.getMovimientosGrupo(session.grupoId)
                .onSuccess { movs -> _uiState.update { it.copy(isLoading = false, movimientos = movs) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun onFiltroChange(f: MovFiltro) = _uiState.update { it.copy(filtro = f) }
}
