package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.DuoActividadRecord
import com.gpleader.app.core.data.repository.DuoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DuoActividadRegistroUiState(
    val actividadTipoId: String                  = "",
    val duoId:           String                  = "",
    val nombre:          String                  = "",
    val registros:       List<DuoActividadRecord> = emptyList(),
    val isLoading:       Boolean                 = true,
    val error:           String?                 = null,
)

@HiltViewModel
class DuoActividadRegistroViewModel @Inject constructor(
    private val duoRepo: DuoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val tipoId = savedStateHandle.get<String>("actividadTipoId") ?: ""
    private val duoId  = savedStateHandle.get<String>("duoId") ?: ""
    private val nombre = savedStateHandle.get<String>("nombreActividad") ?: ""

    private val _uiState = MutableStateFlow(DuoActividadRegistroUiState(
        actividadTipoId = tipoId,
        duoId           = duoId,
        nombre          = nombre,
    ))
    val uiState: StateFlow<DuoActividadRegistroUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val desde = LocalDate.now().minusDays(90)
            duoRepo.getRegistrosPorTipoActividad(tipoId, duoId, desde)
                .onSuccess { registros ->
                    _uiState.value = _uiState.value.copy(
                        registros = registros.sortedByDescending { it.recordDate },
                        isLoading = false,
                        error     = null,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }
}
