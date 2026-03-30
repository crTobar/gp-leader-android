package com.gpleader.app.feature.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
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
    val reunionesRecientes:   List<ReunionResumen> = emptyList(),
    val isLoading:            Boolean             = false,
    val error:                String?             = null,
    // Navegación
    val showCodigoSuplementeSheet: Boolean = false,
    val navigateToRegistro:        Boolean = false,
    val navigateToHistorial:       Boolean = false,
    val navigateToDetalle:         String? = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // TODO: reemplazar con carga real desde PowerSync
        _uiState.update {
            it.copy(
                nombreLider = "Maria Garcia",
                grupo = GrupoInfo(
                    nombre     = "GP Los Olivos",
                    diaSemana  = "Miércoles",
                    horaInicio = "7:00 PM",
                    iglesia    = "Iglesia Central",
                ),
                porcentajeAsistencia = 85,
                totalPresentes       = 12,
                totalAusentes        = 2,
                reunionesRecientes   = listOf(
                    ReunionResumen("r1", LocalDate.of(2026, 2, 26), EstadoReunion.ENVIADA, 12, 2),
                    ReunionResumen("r2", LocalDate.of(2026, 2, 19), EstadoReunion.ENVIADA, 10, 3),
                ),
            )
        }
    }

    fun onRegistrarClick() {
        _uiState.update { it.copy(navigateToRegistro = true) }
    }

    fun onSuplementeClick() {
        _uiState.update { it.copy(showCodigoSuplementeSheet = true) }
    }

    fun onCerrarCodigoSuplente() {
        _uiState.update { it.copy(showCodigoSuplementeSheet = false) }
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
}
