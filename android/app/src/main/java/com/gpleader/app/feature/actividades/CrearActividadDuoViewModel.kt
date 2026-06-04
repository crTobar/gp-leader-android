package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.DuoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CrearActividadDuoUiState(
    val nombre:      String  = "",
    val markerType:  String  = "counter",
    val unitLabel:   String  = "veces",
    val isGuardando: Boolean = false,
    val savedOk:     Boolean = false,
    val error:       String? = null,
)

@HiltViewModel
class CrearActividadDuoViewModel @Inject constructor(
    private val duoRepo: DuoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val duoId: String = checkNotNull(savedStateHandle["duoId"])

    private val _uiState = MutableStateFlow(CrearActividadDuoUiState())
    val uiState: StateFlow<CrearActividadDuoUiState> = _uiState.asStateFlow()

    fun onNombreChange(v: String)      { _uiState.value = _uiState.value.copy(nombre = v) }
    fun onMarkerTypeChange(v: String)  { _uiState.value = _uiState.value.copy(markerType = v) }
    fun onUnitLabelChange(v: String)   { _uiState.value = _uiState.value.copy(unitLabel = v) }

    fun onGuardar() {
        val s = _uiState.value
        if (s.nombre.isBlank()) return
        viewModelScope.launch {
            _uiState.value = s.copy(isGuardando = true, error = null)
            duoRepo.crearActividadDuo(duoId, s.nombre.trim(), s.markerType, s.unitLabel.ifBlank { "veces" })
                .onSuccess { _uiState.value = _uiState.value.copy(isGuardando = false, savedOk = true) }
                .onFailure { _uiState.value = _uiState.value.copy(isGuardando = false, error = it.message) }
        }
    }
}
