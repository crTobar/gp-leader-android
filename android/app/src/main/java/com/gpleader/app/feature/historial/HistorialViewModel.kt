package com.gpleader.app.feature.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ReunionRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val error:                String?           = null,
    val debugInfo:            String            = "",
    val busquedaActiva:       Boolean           = false,
    // Navegación
    val navigateToDetalle: String?              = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val reunionRepo: ReunionRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    /** Cache de todas las reuniones cargadas desde Supabase */
    private var todasLasReuniones: List<ReunionResumen> = emptyList()

    private val trimestresDelAnio: List<Trimestre> by lazy {
        val anio = LocalDate.now().year
        listOf(
            Trimestre("t1", "1er Ene-Mar", LocalDate.of(anio, 1, 1),  LocalDate.of(anio, 3, 31)),
            Trimestre("t2", "2do Abr-Jun", LocalDate.of(anio, 4, 1),  LocalDate.of(anio, 6, 30)),
            Trimestre("t3", "3er Jul-Sep", LocalDate.of(anio, 7, 1),  LocalDate.of(anio, 9, 30)),
            Trimestre("t4", "4to Oct-Dic", LocalDate.of(anio, 10, 1), LocalDate.of(anio, 12, 31)),
        )
    }

    /** Ordena: trimestre actual primero, luego futuros, luego pasados. */
    private val trimestresOrdenados: List<Trimestre> get() {
        val hoy     = LocalDate.now()
        val actual  = trimestresDelAnio.filter { !hoy.isBefore(it.fechaInicio) && !hoy.isAfter(it.fechaFin) }
        val futuros = trimestresDelAnio.filter { hoy.isBefore(it.fechaInicio) }
        val pasados = trimestresDelAnio.filter { hoy.isAfter(it.fechaFin) }
        return actual + futuros + pasados
    }

    private val trimestreActualId: String get() {
        val hoy = LocalDate.now()
        return trimestresDelAnio.find { !hoy.isBefore(it.fechaInicio) && !hoy.isAfter(it.fechaFin) }?.id ?: "t1"
    }

    init {
        val idActual = trimestreActualId
        _uiState.update { it.copy(trimestres = trimestresOrdenados, trimestreSeleccionado = idActual, isLoading = true, error = null) }
        cargarReuniones()
    }

    fun cargarReuniones() {
        val gId = session.grupoId
        _uiState.update { it.copy(isLoading = true, error = null, debugInfo = "grupoId=$gId") }
        viewModelScope.launch {
            reunionRepo.getReuniones(gId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar reuniones: ${e.message}\ngrupoId=$gId") }
                }
                .collect { reuniones ->
                    _uiState.update { it.copy(debugInfo = "grupoId=$gId total=${reuniones.size}") }
                    todasLasReuniones = reuniones.map { r ->
                        val total = r.presentes + r.ausentes + r.justificados
                        val pct   = if (total > 0) (r.presentes * 100 / total) else 0
                        val estado = when (r.estado.uppercase()) {
                            "ENVIADA", "SENT", "SUBMITTED", "APPROVED", "APROBADA" -> EstadoReunionHistorial.ENVIADA
                            else -> EstadoReunionHistorial.PENDIENTE_SYNC
                        }
                        ReunionResumen(
                            id                    = r.id,
                            fecha                 = r.fecha,
                            estado                = estado,
                            presentes             = r.presentes,
                            ausentes              = r.ausentes,
                            justificados          = r.justificados,
                            porcentajeAsistencia  = pct,
                            permitirEdicion       = estado != EstadoReunionHistorial.ENVIADA,
                        )
                    }
                    aplicarFiltro(_uiState.value.trimestreSeleccionado)
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
        }
    }

    private fun aplicarFiltro(trimestreId: String) {
        val filtradas = if (trimestreId == "todo") {
            todasLasReuniones
        } else {
            val trimestre = trimestresDelAnio.find { it.id == trimestreId }
            if (trimestre == null) todasLasReuniones
            else todasLasReuniones.filter { r ->
                !r.fecha.isBefore(trimestre.fechaInicio) && !r.fecha.isAfter(trimestre.fechaFin)
            }
        }

        val grupos = filtradas
            .groupBy { it.fecha.year * 100 + it.fecha.monthValue }
            .entries
            .sortedByDescending { it.key }
            .map { (key, reuniones) ->
                val anio = key / 100
                val mes  = Month.of(key % 100)
                GrupoMes(mes = mes, anio = anio, reuniones = reuniones.sortedByDescending { it.fecha })
            }

        val stats = HistorialStats(
            promedioAsistencia = if (filtradas.isEmpty()) 0
                                 else filtradas.map { it.porcentajeAsistencia }.average().toInt(),
            totalReuniones     = filtradas.size,
            enviadas           = filtradas.count { it.estado == EstadoReunionHistorial.ENVIADA },
            pendientes         = filtradas.count { it.estado == EstadoReunionHistorial.PENDIENTE_SYNC },
        )

        _uiState.update { it.copy(grupos = grupos, stats = stats) }
    }

    fun onTrimestralChange(trimestreId: String) {
        _uiState.update { it.copy(trimestreSeleccionado = trimestreId) }
        aplicarFiltro(trimestreId)
    }

    fun onVerTodoClick() {
        _uiState.update { it.copy(trimestreSeleccionado = "todo") }
        aplicarFiltro("todo")
    }

    fun onBuscarClick() {
        _uiState.update { it.copy(busquedaActiva = true) }
    }

    fun onEditarReunionClick(reunionId: String) {
        _uiState.update { it.copy(navigateToDetalle = reunionId) }
    }

    fun onReunionClick(reunionId: String) {
        _uiState.update { it.copy(navigateToDetalle = reunionId) }
    }

    fun consumeDetalleNavigation() {
        _uiState.update { it.copy(navigateToDetalle = null) }
    }
}
