package com.gpleader.app.feature.miembro

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.RegistroHistorial
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MiembroActividadHistorialUiState(
    val isLoading:      Boolean = true,
    val isRefreshing:   Boolean = false,
    val nombreActividad: String = "",
    val markerType:     String = "counter",
    val unitLabel:      String = "",
    val totalHistorico: Int = 0,
    val registros:      List<RegistroHistorial> = emptyList(),
    val showAddDialog:  Boolean = false,
    val nuevaCantidad:  Int = 1,
    val isGuardando:    Boolean = false,
    val error:          String? = null,
)

@HiltViewModel
class MiembroActividadHistorialViewModel @Inject constructor(
    savedStateHandle:      SavedStateHandle,
    private val actividadRepo: ActividadRepository,
    private val session:       SessionManager,
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
                    s.copy(
                        nombreActividad = tipo.nombre,
                        markerType      = tipo.markerType,
                        unitLabel       = tipo.unitLabel,
                    )
                }
            }
        }
        actividadRepo.getMiembroActividadHistorial(session.miembroId, actividadTipoId).onSuccess { registros ->
            _uiState.update { it.copy(registros = registros) }
        }.onFailure { e ->
            _uiState.update { it.copy(error = e.message) }
        }
        actividadRepo.getMiembroActividadTotalHistorico(session.miembroId, actividadTipoId).onSuccess { total ->
            _uiState.update { it.copy(totalHistorico = total) }
        }
    }

    fun onShowAddDialog()  = _uiState.update { it.copy(showAddDialog = true, nuevaCantidad = 1) }
    fun onDismissDialog()  = _uiState.update { it.copy(showAddDialog = false) }
    fun onCantidadChange(v: Int) = _uiState.update { it.copy(nuevaCantidad = v.coerceAtLeast(1)) }

    fun onGuardar() {
        val state    = _uiState.value
        val cantidad = state.nuevaCantidad
        if (cantidad <= 0) return
        val autoApprove = state.markerType != "monetary"
        viewModelScope.launch {
            _uiState.update { it.copy(isGuardando = true) }
            actividadRepo.agregarRegistroMiembro(
                miembroId       = session.miembroId,
                actividadTipoId = actividadTipoId,
                fecha           = LocalDate.now(),
                count           = cantidad,
                autoApprove     = autoApprove,
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(isGuardando = false, showAddDialog = false) }
                    cargarInterno()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isGuardando = false, error = e.message) }
                },
            )
        }
    }
}
