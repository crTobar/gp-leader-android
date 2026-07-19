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
import java.time.Instant
import javax.inject.Inject

/** Una línea del detalle: un aporte del miembro o la línea "Ajuste del líder". */
data class AporteLinea(
    val entryId:      String,
    val value:        Double,
    val enteredAt:    Instant?,
    val isAdjustment: Boolean,
)

data class LiderAporteDetalleUiState(
    val isLoading:       Boolean            = true,
    val error:           String?            = null,
    val miembroNombre:   String             = "",
    val actividadNombre: String             = "",
    val markerType:      String             = "counter",
    val unitLabel:       String             = "",
    val lineas:          List<AporteLinea>  = emptyList(),
    val total:           Double             = 0.0,
    val procesando:      Boolean            = false,
    val done:            Boolean            = false,   // aprobado/rechazado → volver
    val toastMsg:        String?            = null,
    // Rechazo
    val rechazando:      Boolean            = false,
    val rejectReason:    String             = "",
    // Editar total
    val showEditDialog:  Boolean            = false,
    val editValue:       Int                = 0,
) {
    val entryIds: List<String> get() = lineas.map { it.entryId }
}

@HiltViewModel
class LiderAporteDetalleViewModel @Inject constructor(
    savedStateHandle:            SavedStateHandle,
    private val memberEntryRepo: MemberEntryRepository,
    private val session:         SessionManager,
) : ViewModel() {

    private val miembroId:       String = savedStateHandle.get<String>("miembroId").orEmpty()
    private val actividadTipoId: String = savedStateHandle.get<String>("actividadTipoId").orEmpty()

    private val _uiState = MutableStateFlow(LiderAporteDetalleUiState())
    val uiState: StateFlow<LiderAporteDetalleUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            memberEntryRepo.getPendingEntriesForActivity(session.grupoId, actividadTipoId)
                .onSuccess { entries ->
                    val mios = entries.filter { it.miembroId == miembroId }
                    if (mios.isEmpty()) {
                        _uiState.update { it.copy(isLoading = false, done = true) }
                        return@onSuccess
                    }
                    val first = mios.first()
                    val lineas = mios
                        .sortedWith(compareBy({ it.isAdjustment }, { it.enteredAt ?: Instant.EPOCH }))
                        .map { AporteLinea(it.entryId, it.value, it.enteredAt, it.isAdjustment) }
                    _uiState.update {
                        it.copy(
                            isLoading       = false,
                            miembroNombre   = first.miembroNombre,
                            actividadNombre = first.actividadNombre,
                            markerType      = first.markerType,
                            unitLabel       = first.unitLabel,
                            lineas          = lineas,
                            total           = lineas.sumOf { l -> l.value },
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    private val actorId: String? get() = session.miembroId.takeIf { it.isNotBlank() }

    fun onAprobar() {
        viewModelScope.launch {
            _uiState.update { it.copy(procesando = true) }
            memberEntryRepo.approveMemberSum(miembroId, actividadTipoId, actorId)
                .onSuccess { _uiState.update { it.copy(procesando = false, done = true, toastMsg = "Aprobado") } }
                .onFailure { _uiState.update { it.copy(procesando = false, toastMsg = "Error al aprobar") } }
        }
    }

    // ── Rechazo ────────────────────────────────────────────────────────────────
    fun onShowReject()               = _uiState.update { it.copy(rechazando = true, rejectReason = "") }
    fun onRejectReasonChange(v: String) = _uiState.update { it.copy(rejectReason = v) }
    fun onDismissReject()            = _uiState.update { it.copy(rechazando = false, rejectReason = "") }

    fun onConfirmarRechazo() {
        val note = _uiState.value.rejectReason.trim().takeIf { it.isNotBlank() }
        val ids  = _uiState.value.entryIds
        _uiState.update { it.copy(rechazando = false, rejectReason = "", procesando = true) }
        viewModelScope.launch {
            val result = ids.fold(Result.success(Unit) as Result<Unit>) { acc, id ->
                if (acc.isFailure) acc else memberEntryRepo.rejectEntry(id, actorId = actorId, note = note)
            }
            result
                .onSuccess { _uiState.update { it.copy(procesando = false, done = true, toastMsg = "Rechazado") } }
                .onFailure { _uiState.update { it.copy(procesando = false, toastMsg = "Error al rechazar") } }
        }
    }

    // ── Editar total ─────────────────────────────────────────────────────────────
    fun onShowEdit()              = _uiState.update { it.copy(showEditDialog = true, editValue = it.total.toInt()) }
    fun onEditValueChange(v: Int) = _uiState.update { it.copy(editValue = v) }
    fun onDismissEdit()           = _uiState.update { it.copy(showEditDialog = false) }

    fun onConfirmarEdicion() {
        val nuevoTotal = _uiState.value.editValue.toDouble()
        _uiState.update { it.copy(showEditDialog = false, procesando = true) }
        viewModelScope.launch {
            memberEntryRepo.setMemberDraftTotal(miembroId, actividadTipoId, session.grupoId, nuevoTotal, actorId)
                .onSuccess { _uiState.update { it.copy(procesando = false, toastMsg = "Total actualizado") }; cargar() }
                .onFailure { _uiState.update { it.copy(procesando = false, toastMsg = "Error al editar") } }
        }
    }

    fun consumeToast()   { _uiState.update { it.copy(toastMsg = null) } }
    fun consumeDone()    { _uiState.update { it.copy(done = false) } }
}
