package com.gpleader.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.AsignadoPotencial
import com.gpleader.app.core.data.repository.GroupLogRepository
import com.gpleader.app.core.data.repository.GrupoRepository
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.ReunionRepository
import com.gpleader.app.core.data.repository.SabbathMeetingResumen
import com.gpleader.app.core.data.repository.Solicitud
import com.gpleader.app.core.data.repository.SolicitudRepository
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
    val reunionesRecientes:        List<ReunionResumen>         = emptyList(),
    val reunionesSabadoRecientes:  List<SabbathMeetingResumen>  = emptyList(),
    val reunionGpHoy:        ReunionResumen?         = null,
    val reunionSabadoSemana: SabbathMeetingResumen?  = null,
    val isLoading:            Boolean                  = false,
    val isRefreshing:         Boolean                  = false,
    val error:                String?                  = null,
    val navigateToRegistro:   Boolean                  = false,
    val navigateToHistorial:  Boolean                  = false,
    val navigateToDetalle:    String?                  = null,
    val navigateToSabadoCulto: Boolean                 = false,

    // ── Solicitudes ──────────────────────────────────────────────────────────
    val solicitudesActivas:    List<Solicitud>          = emptyList(),
    val solicitudAsignada:     Solicitud?               = null,  // pendiente para este usuario
    val showDelegarSheet:      Boolean                  = false,
    val asignadosPotenciales:  List<AsignadoPotencial>  = emptyList(),
    val isLoadingAsignados:    Boolean                  = false,
    val isCreandoSolicitud:    Boolean                  = false,
    val solicitudError:        String?                  = null,
    val showActivarDialog:     Boolean                  = false,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reunionRepo:   ReunionRepository,
    private val grupoRepo:     GrupoRepository,
    private val miembroRepo:   MiembroRepository,
    private val solicitudRepo: SolicitudRepository,
    private val groupLogRepo:  GroupLogRepository,
    private val session:       SessionManager,
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
        cargarSabadoRecientes()
        cargarGrupoDetalle()
        cargarTotalMiembros()
        cargarSolicitudes()
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            cargarSabadoRecientes()
            cargarGrupoDetalle()
            cargarSolicitudes()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun cargarTotalMiembros() {
        viewModelScope.launch {
            miembroRepo.getMiembrosActivos(session.grupoId).collect { miembros ->
                _uiState.update { it.copy(totalMiembros = miembros.size) }
            }
        }
    }

    private fun cargarGrupoDetalle() {
        viewModelScope.launch {
            val detalle = grupoRepo.getGrupoDetalle(session.grupoId) ?: return@launch
            _uiState.update { state ->
                state.copy(
                    grupo = state.grupo?.copy(
                        diaSemana  = detalle.meetingDay,
                        horaInicio = detalle.meetingTime,
                    )
                )
            }
        }
    }

    private fun cargarSabadoRecientes() {
        viewModelScope.launch {
            val lista = runCatching {
                reunionRepo.getReunionesRecientesSabado(session.grupoId, limit = 3).getOrDefault(emptyList())
            }.getOrDefault(emptyList())
            val hoy          = LocalDate.now()
            val ultimoSabado = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))
            val sabadoSemana = lista.firstOrNull {
                !it.fecha.isBefore(ultimoSabado) && !it.fecha.isAfter(hoy)
            }
            _uiState.update { it.copy(reunionesSabadoRecientes = lista, reunionSabadoSemana = sabadoSemana) }
        }
    }

    private fun observarReuniones() {
        viewModelScope.launch {
            reunionRepo.getReuniones(grupoId = session.grupoId, limit = 3)
                .collect { reuniones ->
                    val totalP = reuniones.sumOf { it.presentes }
                    val totalA = reuniones.sumOf { it.ausentes }
                    val totalJ = reuniones.sumOf { it.justificados }
                    val totalAsistentes = totalP + totalA + totalJ
                    val pct = if (totalAsistentes > 0) (totalP * 100) / totalAsistentes else 0

                    val hoy = LocalDate.now()
                    val mapped = reuniones.map { r ->
                        ReunionResumen(
                            id        = r.id,
                            fecha     = r.fecha,
                            estado    = runCatching { EstadoReunion.valueOf(r.estado) }
                                .getOrElse { EstadoReunion.BORRADOR },
                            presentes = r.presentes,
                            ausentes  = r.ausentes,
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isLoading            = false,
                            reunionesRecientes   = mapped,
                            reunionGpHoy         = mapped.firstOrNull { r -> r.fecha == hoy },
                            totalPresentes       = totalP,
                            totalAusentes        = totalA,
                            totalJustificados    = totalJ,
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
        cargarSabadoRecientes()
    }

    fun onSabadoCultoClick() {
        _uiState.update { it.copy(navigateToSabadoCulto = true) }
    }

    fun consumeSabadoCultoNavigation() {
        _uiState.update { it.copy(navigateToSabadoCulto = false) }
    }

    // ── Solicitudes ───────────────────────────────────────────────────────────

    fun cargarSolicitudes() {
        viewModelScope.launch {
            val grupoId = session.grupoId
            // Solicitudes que el líder creó (pendientes o activas)
            runCatching { solicitudRepo.getSolicitudesCreadas(grupoId) }
                .onSuccess { lista ->
                    _uiState.update { it.copy(solicitudesActivas = lista) }
                }
            // Solicitudes asignadas al usuario autenticado (pendientes)
            runCatching { solicitudRepo.getSolicitudesAsignadas() }
                .onSuccess { lista ->
                    _uiState.update { it.copy(
                        solicitudAsignada = lista.firstOrNull { it.smallGroupId != grupoId },
                        showActivarDialog = lista.any { it.smallGroupId != grupoId },
                    ) }
                }
        }
    }

    fun onDelegarClick() {
        _uiState.update { it.copy(showDelegarSheet = true, isLoadingAsignados = true, solicitudError = null) }
        viewModelScope.launch {
            runCatching { solicitudRepo.getAsignadosPotenciales(session.grupoId) }
                .onSuccess { lista ->
                    val sinLider = lista.filter { it.profileId != session.miembroId }
                    _uiState.update { it.copy(asignadosPotenciales = sinLider, isLoadingAsignados = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingAsignados = false) }
                }
        }
    }

    fun onDismissDelegarSheet() {
        _uiState.update { it.copy(showDelegarSheet = false, solicitudError = null) }
    }

    fun onCrearSolicitud(assignedToId: String, nota: String?) {
        _uiState.update { it.copy(isCreandoSolicitud = true, solicitudError = null) }
        viewModelScope.launch {
            runCatching {
                solicitudRepo.createSolicitud(assignedToId, session.grupoId, nota)
            }.onSuccess { nueva ->
                // Buscar nombre del delegado para el log
                val nombreDelegado = _uiState.value.asignadosPotenciales
                    .find { it.profileId == assignedToId }?.nombre ?: "miembro"
                groupLogRepo.logAccion(session.grupoId, "deputy_submission_created", "Delegación creada para $nombreDelegado")
                _uiState.update { state ->
                    state.copy(
                        isCreandoSolicitud = false,
                        showDelegarSheet   = false,
                        solicitudesActivas = state.solicitudesActivas + nueva,
                    )
                }
            }.onFailure { e ->
                val msg = when {
                    e.message?.contains("duplicate_solicitude") == true ->
                        "Esta persona ya tiene una solicitud pendiente para este grupo"
                    else -> "No se pudo crear la delegación"
                }
                _uiState.update { it.copy(isCreandoSolicitud = false, solicitudError = msg) }
            }
        }
    }

    fun onCancelarSolicitud(solicitudId: String) {
        viewModelScope.launch {
            runCatching { solicitudRepo.cancelSolicitud(solicitudId) }
                .onSuccess { _ ->
                    _uiState.update { state ->
                        state.copy(
                            solicitudesActivas = state.solicitudesActivas
                                .filter { it.id != solicitudId }
                        )
                    }
                }
        }
    }

    fun onActivarSolicitud(solicitudId: String) {
        viewModelScope.launch {
            runCatching { solicitudRepo.activateSolicitud(solicitudId) }
                .onSuccess { activada ->
                    _uiState.update { it.copy(
                        showActivarDialog  = false,
                        solicitudAsignada  = activada,
                    ) }
                }
                .onFailure {
                    _uiState.update { it.copy(showActivarDialog = false) }
                }
        }
    }

    fun onDismissActivarDialog() {
        _uiState.update { it.copy(showActivarDialog = false) }
    }

    fun consumeSolicitudError() {
        _uiState.update { it.copy(solicitudError = null) }
    }
}
