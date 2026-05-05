package com.gpleader.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ReunionRepository
import com.gpleader.app.core.data.repository.SabbathMeetingResumen
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

// ── Display models ────────────────────────────────────────────────────────────

data class GrupoInfo(
    val nombre:    String,
    val diaSemana: String,
    val horaInicio: String,
    val iglesia:   String,
)

enum class EstadoReunion { BORRADOR, PENDIENTE_SYNC, ENVIADA, APROBADA }

data class ReunionResumen(
    val id:        String,
    val fecha:     LocalDate,
    val estado:    EstadoReunion,
    val presentes: Int,
    val ausentes:  Int,
)

// ── UI state ──────────────────────────────────────────────────────────────────

data class HomeUiState(
    val nombreLider:          String              = "",
    val grupo:                GrupoInfo?          = null,
    val porcentajeAsistencia: Int                 = 0,
    val totalPresentes:       Int                 = 0,
    val totalAusentes:        Int                 = 0,
    val totalJustificados:    Int                 = 0,
    val totalMiembros:        Int                 = 0,
    val reunionesRecientes:   List<ReunionResumen>      = emptyList(),
    val sabbathMeeting:       SabbathMeetingResumen?   = null,
    val isLoading:            Boolean                  = false,
    val error:                String?                  = null,
    val navigateToRegistro:   Boolean                  = false,
    val navigateToHistorial:  Boolean                  = false,
    val navigateToDetalle:    String?                  = null,
    val navigateToSabadoCulto: Boolean                 = false,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reunionRepo: ReunionRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                nombreLider = session.miembroNombre,
                grupo       = GrupoInfo(
                    nombre     = session.grupoNombre,
                    diaSemana  = "",
                    horaInicio = "",
                    iglesia    = "",
                ),
                isLoading = true,
            )
        }
        observarReuniones()
        cargarSabbath()
    }

    private fun cargarSabbath() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val sabado = if (today.dayOfWeek == DayOfWeek.SATURDAY) today
                         else today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
            runCatching {
                reunionRepo.getSabbathMeeting(session.grupoId, sabado).getOrNull()
            }.onSuccess { resumen ->
                _uiState.update { it.copy(sabbathMeeting = resumen) }
            }
        }
    }

    private fun observarReuniones() {
        viewModelScope.launch {
            reunionRepo.getReuniones(grupoId = session.grupoId, limit = 2)
                .collect { reuniones ->
                    val totalP = reuniones.sumOf { it.presentes }
                    val totalA = reuniones.sumOf { it.ausentes }
                    val totalJ = reuniones.sumOf { it.justificados }
                    val totalAsistentes = totalP + totalA + totalJ
                    val pct = if (totalAsistentes > 0) (totalP * 100) / totalAsistentes else 0

                    _uiState.update {
                        it.copy(
                            isLoading            = false,
                            reunionesRecientes   = reuniones.map { r ->
                                ReunionResumen(
                                    id        = r.id,
                                    fecha     = r.fecha,
                                    estado    = runCatching { EstadoReunion.valueOf(r.estado) }
                                        .getOrElse { EstadoReunion.BORRADOR },
                                    presentes = r.presentes,
                                    ausentes  = r.ausentes,
                                )
                            },
                            totalPresentes       = totalP,
                            totalAusentes        = totalA,
                            totalJustificados    = totalJ,
                            totalMiembros        = totalAsistentes,
                            porcentajeAsistencia = pct,
                        )
                    }
                }
        }
    }

    fun onRegistrarClick() {
        _uiState.update { it.copy(navigateToRegistro = true) }
    }

    fun onVerTodasClick() {
        _uiState.update { it.copy(navigateToHistorial = true) }
    }

    fun onReunionClick(reunionId: String) {
        _uiState.update { it.copy(navigateToDetalle = reunionId) }
    }

    fun consumeRegistroNavigation() {
        _uiState.update { it.copy(navigateToRegistro = false) }
    }

    fun consumeHistorialNavigation() {
        _uiState.update { it.copy(navigateToHistorial = false) }
    }

    fun consumeDetalleNavigation() {
        _uiState.update { it.copy(navigateToDetalle = null) }
    }

    fun reloadSabbath() {
        cargarSabbath()
    }

    fun onSabadoCultoClick() {
        _uiState.update { it.copy(navigateToSabadoCulto = true) }
    }

    fun consumeSabadoCultoNavigation() {
        _uiState.update { it.copy(navigateToSabadoCulto = false) }
    }
}
