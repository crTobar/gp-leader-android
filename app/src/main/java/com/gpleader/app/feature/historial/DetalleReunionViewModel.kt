package com.gpleader.app.feature.historial

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject

// ── Modelos ───────────────────────────────────────────────────────────────────

data class AsistenciaDetalle(
    val nombre:   String,
    val estado:   String,    // "P", "A", "J"
    val esVisita: Boolean = false,
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
    val isLoading:            Boolean                  = false,
)

// ── Sample data ───────────────────────────────────────────────────────────────

private val sampleAsistencias = listOf(
    AsistenciaDetalle("Carlos Ramírez",   "P"),
    AsistenciaDetalle("Ana López",        "P"),
    AsistenciaDetalle("Luis Hernández",   "A"),
    AsistenciaDetalle("Sofía Vargas",     "P"),
    AsistenciaDetalle("Pedro Castillo",   "J"),
    AsistenciaDetalle("Laura Jiménez",    "P"),
    AsistenciaDetalle("Roberto Mora",     "P"),
    AsistenciaDetalle("Carmen Torres",    "P"),
    AsistenciaDetalle("Juan García",      "P", esVisita = true),
)

private val sampleActividades = listOf(
    ActividadDetalle("Estudio bíblico",     "UNION",  1,    "sesión"),
    ActividadDetalle("Ofrendas",            "UNION",  null, "personas"),
    ActividadDetalle("Misioneros del mes",  "PASTOR", 3,    "personas"),
    ActividadDetalle("Visitas realizadas",  "GP",     2,    "personas"),
    ActividadDetalle("Llamadas de seguimiento", "GP", 5,    "llamadas"),
)

private val sampleDetalles = mapOf(
    "r1" to DetalleReunionUiState(
        reunionId            = "r1",
        fecha                = LocalDate.of(2026, 3, 4),
        estado               = EstadoReunionHistorial.ENVIADA,
        presentes            = 6,
        ausentes             = 1,
        justificados         = 1,
        porcentajeAsistencia = 75,
        asistencias          = sampleAsistencias,
        actividades          = sampleActividades,
    ),
    "r2" to DetalleReunionUiState(
        reunionId            = "r2",
        fecha                = LocalDate.of(2026, 2, 26),
        estado               = EstadoReunionHistorial.ENVIADA,
        presentes            = 7,
        ausentes             = 1,
        justificados         = 0,
        porcentajeAsistencia = 86,
        asistencias          = sampleAsistencias.dropLast(1),
        actividades          = sampleActividades,
    ),
    "r3" to DetalleReunionUiState(
        reunionId            = "r3",
        fecha                = LocalDate.of(2026, 2, 19),
        estado               = EstadoReunionHistorial.ENVIADA,
        presentes            = 6,
        ausentes             = 2,
        justificados         = 0,
        porcentajeAsistencia = 71,
        creadaPorSuplente    = true,
        suplementeNombre     = "José Ramírez",
        asistencias          = sampleAsistencias,
        actividades          = sampleActividades.take(3),
    ),
    "r4" to DetalleReunionUiState(
        reunionId            = "r4",
        fecha                = LocalDate.of(2026, 2, 12),
        estado               = EstadoReunionHistorial.PENDIENTE_SYNC,
        presentes            = 5,
        ausentes             = 3,
        justificados         = 0,
        porcentajeAsistencia = 62,
        asistencias          = sampleAsistencias.take(6),
        actividades          = sampleActividades,
    ),
)

private fun fallbackDetalle(id: String) = DetalleReunionUiState(
    reunionId            = id,
    fecha                = LocalDate.of(2026, 2, 26),
    estado               = EstadoReunionHistorial.ENVIADA,
    presentes            = 7,
    ausentes             = 1,
    justificados         = 0,
    porcentajeAsistencia = 86,
    asistencias          = sampleAsistencias,
    actividades          = sampleActividades,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class DetalleReunionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val reunionId: String = savedStateHandle["reunionId"] ?: ""

    private val _uiState = MutableStateFlow(
        sampleDetalles[reunionId] ?: fallbackDetalle(reunionId)
    )
    val uiState: StateFlow<DetalleReunionUiState> = _uiState.asStateFlow()
}
