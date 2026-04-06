package com.gpleader.app.feature.historial

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.Month
import javax.inject.Inject

// ── Domain models ──────────────────────────────────────────────────────────────

data class Trimestre(
    val id:          String,
    val nombre:      String,  // "1er Ene-Mar"
    val fechaInicio: LocalDate,
    val fechaFin:    LocalDate,
)

enum class EstadoReunionHistorial { ENVIADA, PENDIENTE_SYNC }

data class ReunionResumen(
    val id:                  String,
    val fecha:               LocalDate,
    val estado:              EstadoReunionHistorial,
    val presentes:           Int,
    val ausentes:            Int,
    val justificados:        Int,
    val porcentajeAsistencia: Int,
    val permitirEdicion:     Boolean = false,
)

data class HistorialStats(
    val promedioAsistencia: Int,
    val totalReuniones:     Int,
    val enviadas:           Int,
    val pendientes:         Int,
)

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Agrupa reuniones por (año, mes) manteniendo el orden cronológico descendente. */
data class GrupoMes(
    val mes:      Month,
    val anio:     Int,
    val reuniones: List<ReunionResumen>,
)

// ── UI State ──────────────────────────────────────────────────────────────────

data class HistorialUiState(
    val trimestres:           List<Trimestre>   = emptyList(),
    val trimestreSeleccionado: String           = "todo",   // id o "todo"
    val grupos:               List<GrupoMes>    = emptyList(),
    val stats:                HistorialStats    = HistorialStats(0, 0, 0, 0),
    val isLoading:            Boolean           = false,
    val busquedaActiva:       Boolean           = false,
    // Navegación
    val navigateToDetalle: String?              = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class HistorialViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        // TODO: reemplazar con carga real desde PowerSync
        val todasLasReuniones = listOf(
            ReunionResumen(
                id                   = "r1",
                fecha                = LocalDate.of(2026, 3, 4),
                estado               = EstadoReunionHistorial.ENVIADA,
                presentes            = 6,
                ausentes             = 1,
                justificados         = 1,
                porcentajeAsistencia = 75,
                permitirEdicion      = true,
            ),
            ReunionResumen(
                id                   = "r2",
                fecha                = LocalDate.of(2026, 2, 26),
                estado               = EstadoReunionHistorial.ENVIADA,
                presentes            = 7,
                ausentes             = 1,
                justificados         = 0,
                porcentajeAsistencia = 86,
            ),
            ReunionResumen(
                id                   = "r3",
                fecha                = LocalDate.of(2026, 2, 19),
                estado               = EstadoReunionHistorial.ENVIADA,
                presentes            = 6,
                ausentes             = 2,
                justificados         = 0,
                porcentajeAsistencia = 71,
            ),
            ReunionResumen(
                id                   = "r4",
                fecha                = LocalDate.of(2026, 2, 12),
                estado               = EstadoReunionHistorial.PENDIENTE_SYNC,
                presentes            = 5,
                ausentes             = 3,
                justificados         = 0,
                porcentajeAsistencia = 62,
            ),
        )

        val grupos = todasLasReuniones
            .groupBy { it.fecha.year * 100 + it.fecha.monthValue }
            .entries
            .sortedByDescending { it.key }
            .map { (key, reuniones) ->
                val anio = key / 100
                val mes  = Month.of(key % 100)
                GrupoMes(mes = mes, anio = anio, reuniones = reuniones.sortedByDescending { it.fecha })
            }

        _uiState.update {
            it.copy(
                trimestres = listOf(
                    Trimestre("t1", "1er Ene-Mar", LocalDate.of(2026, 1, 1),  LocalDate.of(2026, 3, 31)),
                    Trimestre("t2", "2do Abr-Jun", LocalDate.of(2026, 4, 1),  LocalDate.of(2026, 6, 30)),
                    Trimestre("t3", "3er Jul-Sep", LocalDate.of(2026, 7, 1),  LocalDate.of(2026, 9, 30)),
                    Trimestre("t4", "4to Oct-Dic", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 31)),
                ),
                trimestreSeleccionado = "todo",
                grupos  = grupos,
                stats   = HistorialStats(
                    promedioAsistencia = 78,
                    totalReuniones     = 8,
                    enviadas           = 7,
                    pendientes         = 1,
                ),
            )
        }
    }

    fun onTrimestralChange(trimestreId: String) {
        _uiState.update { it.copy(trimestreSeleccionado = trimestreId) }
    }

    fun onVerTodoClick() {
        _uiState.update { it.copy(trimestreSeleccionado = "todo") }
    }

    fun onBuscarClick() {
        _uiState.update { it.copy(busquedaActiva = true) }
    }

    fun onEditarReunionClick(reunionId: String) {
        // TODO: navegar a edición de reunión cuando esté implementada;
        // por ahora abre el detalle
        _uiState.update { it.copy(navigateToDetalle = reunionId) }
    }

    fun onReunionClick(reunionId: String) {
        _uiState.update { it.copy(navigateToDetalle = reunionId) }
    }

    fun consumeDetalleNavigation() {
        _uiState.update { it.copy(navigateToDetalle = null) }
    }
}
