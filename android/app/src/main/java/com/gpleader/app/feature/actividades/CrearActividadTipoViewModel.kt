package com.gpleader.app.feature.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CrearActividadTipoUiState(
    val nombre: String = "",
    val level: String = "my_group",           // "union" | "pastor" | "my_group"
    val markerType: String = "counter",        // "counter" | "checkbox" | "monetary" | "participants"
    val frecuencia: String = "semanal",        // "diaria" | "semanal"
    val unitLabel: String = "",
    val isMemberAccessible: Boolean = false,
    val startDate: LocalDate? = LocalDate.now(),
    val endDate: LocalDate? = null,
    val isSaving: Boolean = false,
    val savedOk: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CrearActividadTipoViewModel @Inject constructor(
    private val actividadRepo: ActividadRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearActividadTipoUiState())
    val uiState: StateFlow<CrearActividadTipoUiState> = _uiState.asStateFlow()

    fun onNombreChange(v: String) = _uiState.update { it.copy(nombre = v, error = null) }
    fun onLevelChange(v: String)  = _uiState.update { it.copy(level = v) }
    fun onMarkerTypeChange(v: String) = _uiState.update { it.copy(markerType = v) }
    fun onFrecuenciaChange(v: String) = _uiState.update { it.copy(frecuencia = v) }
    fun onUnitLabelChange(v: String)  = _uiState.update { it.copy(unitLabel = v) }
    fun onMemberAccessibleChange(v: Boolean) = _uiState.update { it.copy(isMemberAccessible = v) }
    fun onStartDateChange(v: LocalDate?)  = _uiState.update { it.copy(startDate = v) }
    fun onEndDateChange(v: LocalDate?)    = _uiState.update { it.copy(endDate = v) }

    fun onGuardar() {
        val s = _uiState.value
        if (s.nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        val scope = when (s.level) {
            "union"  -> "global"
            "pastor" -> "church"
            else     -> "group"
        }

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            actividadRepo.saveActividadTipo(
                nombre              = s.nombre.trim(),
                level               = s.level,
                markerType          = s.markerType,
                frecuencia          = s.frecuencia,
                unitLabel           = s.unitLabel.trim(),
                isMemberAccessible  = s.isMemberAccessible,
                iglesiaId           = session.iglesiaId,
                grupoId             = if (scope == "group") session.grupoId else null,
                scope               = scope,
                startDate           = s.startDate,
                endDate             = s.endDate,
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false, savedOk = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = "Error al guardar: ${e.message}") }
            }
        }
    }
}
