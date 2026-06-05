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
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CrearActividadDuoUiState(
    val nombre:      String     = "",
    val markerType:  String     = "counter",   // "counter"|"checkbox"|"monetary"
    val unitLabel:   String     = "veces",
    val esDiario:    Boolean    = false,
    val startDate:   LocalDate? = LocalDate.now(),
    val endDate:     LocalDate? = null,
    val isGuardando: Boolean    = false,
    val savedOk:     Boolean    = false,
    val error:       String?    = null,
)

@HiltViewModel
class CrearActividadDuoViewModel @Inject constructor(
    private val duoRepo: DuoRepository,
    private val session: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val duoId: String = checkNotNull(savedStateHandle["duoId"])
    private val crearParaTodos: Boolean = duoId == "todos"

    private val _uiState = MutableStateFlow(CrearActividadDuoUiState())
    val uiState: StateFlow<CrearActividadDuoUiState> = _uiState.asStateFlow()

    fun onNombreChange(v: String)         { _uiState.value = _uiState.value.copy(nombre = v) }
    fun onMarkerTypeChange(v: String)     { _uiState.value = _uiState.value.copy(markerType = v) }
    fun onUnitLabelChange(v: String)      { _uiState.value = _uiState.value.copy(unitLabel = v) }
    fun onEsDiarioChange(v: Boolean)      { _uiState.value = _uiState.value.copy(esDiario = v) }
    fun onStartDateChange(v: LocalDate?)  { _uiState.value = _uiState.value.copy(startDate = v) }
    fun onEndDateChange(v: LocalDate?)    { _uiState.value = _uiState.value.copy(endDate = v) }

    fun onGuardar() {
        val s = _uiState.value
        if (s.nombre.isBlank()) return
        val effectiveType  = if (s.esDiario) "daily_checker" else s.markerType
        val effectiveLabel = when {
            s.esDiario               -> ""
            s.markerType == "monetary" -> "₡"
            else                     -> s.unitLabel.ifBlank { "veces" }
        }
        viewModelScope.launch {
            _uiState.value = s.copy(isGuardando = true, error = null)
            if (crearParaTodos) {
                val duos = duoRepo.getDuosByGrupo(session.grupoId)
                    .getOrElse { emptyList() }
                    .filter { it.isActive }
                var lastError: String? = null
                duos.forEach { duo ->
                    duoRepo.crearActividadDuo(duo.id, s.nombre.trim(), effectiveType, effectiveLabel, s.startDate, s.endDate)
                        .onFailure { lastError = it.message }
                }
                if (lastError != null) {
                    _uiState.value = _uiState.value.copy(isGuardando = false, error = lastError)
                } else {
                    _uiState.value = _uiState.value.copy(isGuardando = false, savedOk = true)
                }
            } else {
                duoRepo.crearActividadDuo(duoId, s.nombre.trim(), effectiveType, effectiveLabel, s.startDate, s.endDate)
                    .onSuccess { _uiState.value = _uiState.value.copy(isGuardando = false, savedOk = true) }
                    .onFailure { _uiState.value = _uiState.value.copy(isGuardando = false, error = it.message) }
            }
        }
    }
}
