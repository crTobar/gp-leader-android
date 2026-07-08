package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.MemberEntryRepository
import com.gpleader.app.core.data.repository.MemberPendingEntry
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Aportes pendientes de un miembro para una actividad, agrupados: el líder aprueba la SUMA. */
data class AporteMiembro(
    val miembroId:       String,
    val miembroNombre:   String,
    val activityTypeId:  String,
    val actividadNombre: String,
    val markerType:      String,
    val unitLabel:       String,
    val total:           Double,
    val entryIds:        List<String>,
) {
    val key: String get() = "$miembroId|$activityTypeId"
}

data class LiderAprobacionesUiState(
    val grupoId:      String              = "",
    val items:        List<AporteMiembro> = emptyList(),
    val isLoading:    Boolean             = false,
    val error:        String?             = null,
    val toastMsg:     String?             = null,
    val procesando:   Set<String>         = emptySet(),   // por key
    val rechazando:   AporteMiembro?      = null,
    val rejectReason: String              = "",
)

@HiltViewModel
class LiderAprobacionesViewModel @Inject constructor(
    savedStateHandle:            SavedStateHandle,
    private val memberEntryRepo: MemberEntryRepository,
    private val session:         SessionManager,
) : ViewModel() {

    // Opcional: si viene, filtra las aprobaciones a una sola actividad.
    private val actividadTipoId: String? = savedStateHandle.get<String>("actividadTipoId")?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(LiderAprobacionesUiState())
    val uiState: StateFlow<LiderAprobacionesUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(grupoId = session.grupoId) }
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = if (actividadTipoId != null)
                memberEntryRepo.getPendingEntriesForActivity(session.grupoId, actividadTipoId)
            else
                memberEntryRepo.getPendingEntriesForGroup(session.grupoId)
            result
                .onSuccess { entries -> _uiState.update { it.copy(isLoading = false, items = agrupar(entries)) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    /** Agrupa los aportes por (miembro, actividad) sumando el valor. */
    private fun agrupar(entries: List<MemberPendingEntry>): List<AporteMiembro> =
        entries.groupBy { it.miembroId to it.activityTypeId }
            .map { (_, list) ->
                val first = list.first()
                AporteMiembro(
                    miembroId       = first.miembroId,
                    miembroNombre   = first.miembroNombre,
                    activityTypeId  = first.activityTypeId,
                    actividadNombre = first.actividadNombre,
                    markerType      = first.markerType,
                    unitLabel       = first.unitLabel,
                    total           = list.sumOf { it.value },
                    entryIds        = list.map { it.entryId },
                )
            }
            .sortedWith(compareBy({ it.actividadNombre }, { it.miembroNombre }))

    fun onAprobar(item: AporteMiembro) {
        viewModelScope.launch {
            _uiState.update { it.copy(procesando = it.procesando + item.key) }
            memberEntryRepo.approveMemberSum(
                miembroId       = item.miembroId,
                actividadTipoId = item.activityTypeId,
                actorId         = session.miembroId.takeIf { it.isNotBlank() },
            )
                .onSuccess { quitar(item.key, "Aprobado") }
                .onFailure { soloQuitarProcesando(item.key, "Error al aprobar") }
        }
    }

    // ── Rechazo con motivo opcional ────────────────────────────────────────────
    fun onShowReject(item: AporteMiembro) =
        _uiState.update { it.copy(rechazando = item, rejectReason = "") }
    fun onRejectReasonChange(v: String) = _uiState.update { it.copy(rejectReason = v) }
    fun onDismissReject() = _uiState.update { it.copy(rechazando = null, rejectReason = "") }

    fun onConfirmarRechazo() {
        val item = _uiState.value.rechazando ?: return
        val note = _uiState.value.rejectReason.trim().takeIf { it.isNotBlank() }
        _uiState.update { it.copy(rechazando = null, rejectReason = "") }
        viewModelScope.launch {
            _uiState.update { it.copy(procesando = it.procesando + item.key) }
            val actorId = session.miembroId.takeIf { it.isNotBlank() }
            val result = item.entryIds.fold(Result.success(Unit) as Result<Unit>) { acc, id ->
                if (acc.isFailure) acc else memberEntryRepo.rejectEntry(id, actorId = actorId, note = note)
            }
            result
                .onSuccess { quitar(item.key, "Rechazado") }
                .onFailure { soloQuitarProcesando(item.key, "Error al rechazar") }
        }
    }

    private fun quitar(key: String, msg: String) {
        _uiState.update { s ->
            s.copy(items = s.items.filter { it.key != key }, procesando = s.procesando - key, toastMsg = msg)
        }
    }

    private fun soloQuitarProcesando(key: String, msg: String) {
        _uiState.update { s -> s.copy(procesando = s.procesando - key, toastMsg = msg) }
    }

    fun consumeToast() { _uiState.update { it.copy(toastMsg = null) } }
}
