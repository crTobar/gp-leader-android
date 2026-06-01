package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.RegistroSemanalData
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActividadHistorialUiState(
    val actividadNombre: String                    = "",
    val actividadUnidad: String                    = "",
    val markerType:      String                    = "counter",
    val registros:       List<RegistroSemanalData> = emptyList(),
    val totalCantidad:   Int                       = 0,
    val montoTotal:      Double                    = 0.0,
    val editandoId:      String?                   = null,
    val isLoading:       Boolean                   = true,
    val isSaving:        Boolean                   = false,
    val error:           String?                   = null,
)

@HiltViewModel
class ActividadHistorialViewModel @Inject constructor(
    savedStateHandle:      SavedStateHandle,
    private val actividadRepo: ActividadRepository,
    private val session:       SessionManager,
) : ViewModel() {

    private val actividadTipoId: String = checkNotNull(savedStateHandle["actividadTipoId"])

    private val _uiState = MutableStateFlow(ActividadHistorialUiState())
    val uiState: StateFlow<ActividadHistorialUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            actividadRepo.getTodasActividadesTipo(session.iglesiaId, session.districtId, session.campoId, session.grupoId).onSuccess { tipos ->
                tipos.find { it.id == actividadTipoId }?.let { tipo ->
                    _uiState.update { s ->
                        s.copy(
                            actividadNombre = tipo.nombre,
                            actividadUnidad = tipo.unitLabel,
                            markerType      = tipo.markerType,
                        )
                    }
                }
            }

            actividadRepo.getRegistrosSemanal(session.grupoId, actividadTipoId).fold(
                onSuccess = { registros ->
                    _uiState.update { s ->
                        s.copy(
                            isLoading     = false,
                            registros     = registros,
                            totalCantidad = registros.sumOf { (it.cantidad ?: 0) + it.aportesMiembros },
                            montoTotal    = registros.sumOf { (it.monto ?: 0.0) + it.aportesMiembros },
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                },
            )
        }
    }

    fun onEditarClick(recordId: String) {
        _uiState.update { it.copy(editandoId = recordId) }
    }

    fun onCancelarEdicion() {
        _uiState.update { it.copy(editandoId = null) }
    }

    fun onGuardarEdicion(recordId: String, cantidad: Int?, monto: Double?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            actividadRepo.updateRegistro(recordId, cantidad, monto).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, editandoId = null) }
                    cargar()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                },
            )
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
