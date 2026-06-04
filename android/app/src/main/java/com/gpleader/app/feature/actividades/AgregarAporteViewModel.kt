package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.MiembroData
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AgregarAporteUiState(
    val actividadNombre: String          = "",
    val markerType:      String          = "counter",
    val unitLabel:       String          = "",
    val miembros:        List<MiembroData> = emptyList(),
    val contadores:      Map<String, Int>  = emptyMap(),   // miembroId → count
    val toggleados:      Set<String>       = emptySet(),   // miembroId (para checkbox)
    val isLoading:       Boolean           = true,
    val isGuardando:     Boolean           = false,
    val guardadoOk:      Boolean           = false,
    val error:           String?           = null,
)

@HiltViewModel
class AgregarAporteViewModel @Inject constructor(
    savedStateHandle:      SavedStateHandle,
    private val actividadRepo: ActividadRepository,
    private val miembroRepo:   MiembroRepository,
    private val session:       SessionManager,
) : ViewModel() {

    private val actividadTipoId: String = checkNotNull(savedStateHandle["actividadTipoId"])

    private val _uiState = MutableStateFlow(AgregarAporteUiState())
    val uiState: StateFlow<AgregarAporteUiState> = _uiState.asStateFlow()

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            actividadRepo.getTodasActividadesTipo(
                session.iglesiaId, session.districtId, session.campoId, session.grupoId
            ).onSuccess { tipos ->
                tipos.find { it.id == actividadTipoId }?.let { tipo ->
                    _uiState.update { s ->
                        s.copy(
                            actividadNombre = tipo.nombre,
                            markerType      = tipo.markerType,
                            unitLabel       = tipo.unitLabel,
                        )
                    }
                }
            }

            val miembros = miembroRepo.getMiembrosActivos(session.grupoId).first()
            _uiState.update { it.copy(miembros = miembros, isLoading = false) }
        }
    }

    fun onIncrement(miembroId: String) {
        _uiState.update { s ->
            val current = s.contadores[miembroId] ?: 0
            s.copy(contadores = s.contadores + (miembroId to current + 1))
        }
    }

    fun onDecrement(miembroId: String) {
        _uiState.update { s ->
            val current = s.contadores[miembroId] ?: 0
            if (current <= 0) return@update s
            val newVal = current - 1
            val newMap = if (newVal == 0) s.contadores - miembroId else s.contadores + (miembroId to newVal)
            s.copy(contadores = newMap)
        }
    }

    fun onToggle(miembroId: String) {
        _uiState.update { s ->
            val newSet = if (miembroId in s.toggleados) s.toggleados - miembroId
                         else s.toggleados + miembroId
            s.copy(toggleados = newSet)
        }
    }

    fun onGuardar() {
        val state = _uiState.value
        val isCheckbox = state.markerType == "checkbox" || state.markerType == "realizado"
        val hoy = LocalDate.now()

        viewModelScope.launch {
            _uiState.update { it.copy(isGuardando = true, error = null) }
            var huboError = false

            if (isCheckbox) {
                for (miembroId in state.toggleados) {
                    actividadRepo.toggleMiembroActividad(
                        miembroId       = miembroId,
                        actividadTipoId = actividadTipoId,
                        fecha           = hoy,
                        isDone          = true,
                        autoApprove     = true,
                    ).onFailure { huboError = true }
                }
            } else {
                for ((miembroId, count) in state.contadores) {
                    if (count <= 0) continue
                    actividadRepo.agregarRegistroMiembro(
                        miembroId       = miembroId,
                        actividadTipoId = actividadTipoId,
                        fecha           = hoy,
                        count           = count,
                        autoApprove     = true,
                    ).onFailure { huboError = true }
                }
            }

            _uiState.update { it.copy(isGuardando = false, guardadoOk = !huboError, error = if (huboError) "Hubo un error al guardar algunos registros" else null) }
        }
    }

    val hayValores: Boolean
        get() {
            val s = _uiState.value
            val isCheckbox = s.markerType == "checkbox" || s.markerType == "realizado"
            return if (isCheckbox) s.toggleados.isNotEmpty() else s.contadores.values.any { it > 0 }
        }
}
