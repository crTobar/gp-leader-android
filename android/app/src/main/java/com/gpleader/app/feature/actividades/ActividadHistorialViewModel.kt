package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.MemberActivitySubmission
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

data class ActividadHistorialUiState(
    val actividadNombre: String                         = "",
    val actividadUnidad: String                         = "",
    val markerType:      String                         = "counter",
    val submissions:     List<MemberActivitySubmission> = emptyList(),
    val grupoTotal:      Int                            = 0,
    val grupoMonto:      Double                         = 0.0,
    val isLoading:       Boolean                        = true,
    val isRefreshing:    Boolean                        = false,
    val error:           String?                        = null,
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
                        actividadNombre = tipo.nombre,
                        actividadUnidad = tipo.unitLabel,
                        markerType      = tipo.markerType,
                    )
                }
            }
        }

        actividadRepo.getActividadesConTotales(session.grupoId).onSuccess { totales ->
            totales[actividadTipoId]?.let { t ->
                _uiState.update { it.copy(grupoTotal = t.totalCantidad, grupoMonto = t.montoTotal) }
            }
        }

        actividadRepo.getActividadSubmissions(actividadTipoId, session.grupoId).fold(
            onSuccess = { subs ->
                val sorted = subs.sortedByDescending {
                    it.markedAt ?: it.recordDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                }
                _uiState.update { it.copy(submissions = sorted) }
            },
            onFailure = { e ->
                _uiState.update { it.copy(error = e.message) }
            },
        )
    }
}
