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

/**
 * Filtro por tipo de acción de la bitácora de movimientos.
 * Los eventos "created" (aporte creado) no se muestran en Movimientos — solo aprobaciones,
 * rechazos y ediciones. Se excluyen al cargar, así que no hay filtro "Creados".
 */
enum class MovFiltro(val label: String, val actions: Set<String>) {
    TODO("Todo", emptySet()),
    APROBADOS("Aprobados", setOf("approved", "board_approved")),
    RECHAZADOS("Rechazados", setOf("rejected")),
    EDITADOS("Editados", setOf("edited")),
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
                .onSuccess { movs ->
                    // Ocultar los "aporte creado": Movimientos solo muestra aprobaciones/rechazos/ediciones.
                    val visibles = movs.filter { it.action != "created" }
                    _uiState.update { it.copy(isLoading = false, movimientos = visibles) }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun onFiltroChange(f: MovFiltro) = _uiState.update { it.copy(filtro = f) }
}
