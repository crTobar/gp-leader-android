package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.DuoRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Detalle de una actividad de dúos: total agregado + desglose por dúo.
 * Recibe el nombre de la actividad (clave de agrupación) y reconstruye el agregado desde el repo.
 */
data class DuoReporteActividadUiState(
    val nombre:    String                = "",
    val agregada:  DuoActividadAgregada? = null,
    val isLoading: Boolean               = true,
    val error:     String?               = null,
)

@HiltViewModel
class DuoReporteActividadViewModel @Inject constructor(
    private val duoRepo: DuoRepository,
    private val session: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val nombre = savedStateHandle.get<String>("nombreActividad") ?: ""

    private val _uiState = MutableStateFlow(DuoReporteActividadUiState(nombre = nombre))
    val uiState: StateFlow<DuoReporteActividadUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            duoRepo.getActividadesConTotalesPorGrupo(session.grupoId)
                .onSuccess { lista ->
                    val agregada = agregarDuoActividades(lista).firstOrNull { it.nombre == nombre }
                    _uiState.update { it.copy(isLoading = false, agregada = agregada) }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
