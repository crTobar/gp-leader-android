package com.gpleader.app.feature.sabado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.AsistenciaParaGuardar
import com.gpleader.app.core.data.repository.GrupoRepository
import com.gpleader.app.core.data.repository.IglesiaItem
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.ReunionRepository
import com.gpleader.app.core.data.repository.iniciales
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class MiembroSabado(
    val id: String,
    val nombre: String,
    val iniciales: String,
    val presente: Boolean = false,
    val iglesiaVisitadaId: String? = null,
    val iglesiaVisitadaNombre: String? = null,
)

data class SabadoCultoUiState(
    val meetingId: String = "",
    val fecha: LocalDate = LocalDate.now(),
    val miembros: List<MiembroSabado> = emptyList(),
    val iglesias: List<IglesiaItem> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null,
    val showIglesiaSheetForMiembro: String? = null,
    val navigateToExito: Boolean = false,
)

@HiltViewModel
class SabadoCultoViewModel @Inject constructor(
    private val miembroRepo: MiembroRepository,
    private val reunionRepo: ReunionRepository,
    private val grupoRepo: GrupoRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SabadoCultoUiState())
    val uiState: StateFlow<SabadoCultoUiState> = _uiState.asStateFlow()

    private val sabadoDeEstaSemana: LocalDate = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Cargar o crear el meeting de sábado de esta semana
            val meetingId = run {
                val existente = runCatching {
                    reunionRepo.getSabbathMeeting(session.grupoId, sabadoDeEstaSemana).getOrNull()
                }.getOrNull()
                if (existente != null) {
                    existente.id
                } else if (LocalDate.now().dayOfWeek == DayOfWeek.SATURDAY) {
                    // Solo crear borrador si hoy es sábado
                    runCatching {
                        reunionRepo.saveReunion(
                            grupoId       = session.grupoId,
                            fecha         = sabadoDeEstaSemana,
                            noHuboReunion = false,
                            asistencias   = emptyList(),
                            tipoReunion   = "saturday_worship",
                            status        = "draft",
                        ).getOrNull()
                    }.getOrNull() ?: ""
                } else {
                    ""
                }
            }

            // Cargar iglesias para el sheet
            val iglesias = runCatching { grupoRepo.getIglesias() }.getOrElse { emptyList() }

            // Si hay meeting, cargar qué miembros ya se marcaron (presentes)
            // presentesMap: memberId → iglesiaVisitadaNombre (null si no visitó otra iglesia)
            val presentesMap: Map<String, String?> = if (meetingId.isNotBlank()) {
                val detalle = runCatching {
                    reunionRepo.getDetalleReunion(meetingId).getOrNull()
                }.getOrNull()
                detalle?.asistencias
                    ?.filter { it.estado == "P" && it.memberId != null }
                    ?.associate { it.memberId!! to it.iglesiaVisitadaNombre }
                    ?: emptyMap()
            } else emptyMap()

            // Cargar miembros activos
            miembroRepo.getMiembrosActivos(session.grupoId)
                .catch { }
                .collect { miembros ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            meetingId = meetingId,
                            fecha     = sabadoDeEstaSemana,
                            iglesias  = iglesias,
                            miembros  = miembros.map { m ->
                                val iglesiaVisitadaNombre = presentesMap[m.id]
                                MiembroSabado(
                                    id                    = m.id,
                                    nombre                = m.nombreCompleto,
                                    iniciales             = m.iniciales,
                                    presente              = presentesMap.containsKey(m.id),
                                    iglesiaVisitadaNombre = iglesiaVisitadaNombre,
                                    iglesiaVisitadaId     = iglesias.find { ig -> ig.nombre == iglesiaVisitadaNombre }?.id,
                                )
                            },
                        )
                    }
                }
        }
    }

    fun onTogglePresencia(miembroId: String) {
        var nuevoPresente = false
        _uiState.update { state ->
            state.copy(
                miembros = state.miembros.map { m ->
                    if (m.id == miembroId) {
                        nuevoPresente = !m.presente
                        m.copy(presente = nuevoPresente)
                    } else m
                }
            )
        }
        val meetingId = _uiState.value.meetingId
        if (meetingId.isBlank()) return
        val iglesiaId = _uiState.value.miembros.find { it.id == miembroId }?.iglesiaVisitadaId
        viewModelScope.launch {
            reunionRepo.saveDraftAttendance(meetingId, miembroId, nuevoPresente, iglesiaId)
        }
    }

    fun onShowIglesiaSheet(miembroId: String) {
        _uiState.update { it.copy(showIglesiaSheetForMiembro = miembroId) }
    }

    fun onDismissIglesiaSheet() {
        _uiState.update { it.copy(showIglesiaSheetForMiembro = null) }
    }

    fun onIglesiaSeleccionada(miembroId: String, iglesia: IglesiaItem) {
        _uiState.update { state ->
            state.copy(
                showIglesiaSheetForMiembro = null,
                miembros = state.miembros.map { m ->
                    if (m.id == miembroId) m.copy(
                        iglesiaVisitadaId     = iglesia.id,
                        iglesiaVisitadaNombre = iglesia.nombre,
                    ) else m
                }
            )
        }
        val meetingId = _uiState.value.meetingId
        if (meetingId.isBlank()) return
        val presente = _uiState.value.miembros.find { it.id == miembroId }?.presente ?: false
        viewModelScope.launch {
            reunionRepo.saveDraftAttendance(meetingId, miembroId, presente, iglesia.id)
        }
    }

    fun onEnviarAlPastor() {
        val meetingId = _uiState.value.meetingId
        if (meetingId.isBlank()) {
            _uiState.update { it.copy(error = "No hay reunión de sábado para esta semana") }
            return
        }
        _uiState.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            val asistencias = _uiState.value.miembros.map { m ->
                AsistenciaParaGuardar(
                    miembroId         = m.id,
                    nombreVisita      = null,
                    esVisita          = false,
                    estado            = if (m.presente) "PRESENTE" else "AUSENTE",
                    iglesiaVisitadaId = m.iglesiaVisitadaId,
                )
            }
            reunionRepo.submitSabbathMeeting(meetingId, asistencias)
                .onSuccess {
                    _uiState.update { it.copy(isSending = false, navigateToExito = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSending = false, error = "Error al enviar: ${e.message}") }
                }
        }
    }

    fun consumeExitoNavigation() {
        _uiState.update { it.copy(navigateToExito = false) }
    }
}
