package com.gpleader.app.feature.historial

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ReunionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// ── Modelos de UI ─────────────────────────────────────────────────────────────

data class AsistenciaDetalle(
    val nombre:          String,
    val estado:          String,    // "P", "A", "J"
    val esVisita:        Boolean = false,
    val iglesiaVisitada: String? = null,
)

data class ActividadDetalle(
    val nombre:   String,
    val nivel:    String,    // "UNION", "PASTOR", "GP"
    val cantidad: Int?,
    val unidad:   String,
)

data class DetalleReunionUiState(
    val reunionId:            String                   = "",
    val fecha:                LocalDate                = LocalDate.now(),
    val estado:               EstadoReunionHistorial   = EstadoReunionHistorial.ENVIADA,
    val presentes:            Int                      = 0,
    val ausentes:             Int                      = 0,
    val justificados:         Int                      = 0,
    val porcentajeAsistencia: Int                      = 0,
    val creadaPorSuplente:    Boolean                  = false,
    val suplementeNombre:     String?                  = null,
    val asistencias:          List<AsistenciaDetalle>  = emptyList(),
    val actividades:          List<ActividadDetalle>   = emptyList(),
    val tipoReunion:          String                   = "gp_meeting",
    val isLoading:            Boolean                  = false,
    val isRefreshing:         Boolean                  = false,
    val error:                String?                  = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class DetalleReunionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reunionRepo: ReunionRepository,
) : ViewModel() {

    private val reunionId: String = savedStateHandle["reunionId"] ?: ""

    private val _uiState = MutableStateFlow(DetalleReunionUiState(isLoading = true))
    val uiState: StateFlow<DetalleReunionUiState> = _uiState.asStateFlow()

    init {
        cargarDetalle()
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            cargarDetalleInterno(isRefresh = true)
        }
    }

    private fun cargarDetalle() {
        cargarDetalleInterno(isRefresh = false)
    }

    private fun cargarDetalleInterno(isRefresh: Boolean) {
        if (reunionId.isBlank()) {
            _uiState.value = DetalleReunionUiState(isLoading = false, error = "ID de reunión inválido")
            return
        }
        if (!isRefresh) {
            _uiState.value = _uiState.value.copy(isLoading = true)
        }
        viewModelScope.launch {
            reunionRepo.getDetalleReunion(reunionId)
                .onSuccess { data ->
                    val total = data.presentes + data.ausentes + data.justificados
                    val pct   = if (total > 0) (data.presentes * 100 / total) else 0
                    val estado = when (data.estado.uppercase()) {
                        "SUBMITTED", "SENT", "APPROVED", "ENVIADA", "APROBADA" ->
                            EstadoReunionHistorial.ENVIADA
                        else ->
                            EstadoReunionHistorial.PENDIENTE_SYNC
                    }
                    _uiState.value = DetalleReunionUiState(
                        reunionId            = data.id,
                        fecha                = data.fecha,
                        estado               = estado,
                        presentes            = data.presentes,
                        ausentes             = data.ausentes,
                        justificados         = data.justificados,
                        porcentajeAsistencia = pct,
                        tipoReunion          = data.tipoReunion,
                        asistencias          = data.asistencias.map { a ->
                            AsistenciaDetalle(
                                nombre          = a.nombre,
                                estado          = a.estado,
                                esVisita        = a.esVisita,
                                iglesiaVisitada = a.iglesiaVisitadaNombre,
                            )
                        },
                        actividades = data.actividades.map { act ->
                            ActividadDetalle(
                                nombre   = act.nombre,
                                nivel    = when (act.nivel) {
                                    "union"    -> "UNION"
                                    "pastor"   -> "PASTOR"
                                    "my_group" -> "GP"
                                    else       -> "GP"
                                },
                                cantidad = act.cantidad,
                                unidad   = act.unidad,
                            )
                        },
                        isLoading    = false,
                        isRefreshing = false,
                    )
                }
                .onFailure { e ->
                    _uiState.value = DetalleReunionUiState(
                        reunionId    = reunionId,
                        isLoading    = false,
                        isRefreshing = false,
                        error        = "Error: ${e.message}",
                    )
                }
        }
    }
}
