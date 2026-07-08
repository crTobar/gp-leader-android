package com.gpleader.app.feature.iglesia

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ApprovalLevel
import com.gpleader.app.core.data.repository.MoneyApprovalRepository
import com.gpleader.app.core.data.repository.NivelActividadDetalle
import com.gpleader.app.core.data.repository.NivelHijoPendiente
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Objetivo de aprobación en curso (un hijo dentro de una actividad). */
data class AprobObjetivo(
    val activityTypeId:  String,
    val actividadNombre: String,
    val markerType:      String,
    val unitLabel:       String,
    val childId:         String,
    val childNombre:     String,
    val pendiente:       Double,
)

data class AprobacionNivelUiState(
    val nivelTitulo: String                    = "Aprobaciones",
    val nodeNombre:  String                    = "",
    val nodeId:      String                    = "",
    val esChurch:    Boolean                   = false,  // solo iglesia muestra historial por miembro
    val sourceLevel: String                    = "gp",   // nivel de los hijos (para el historial)
    val items:       List<NivelActividadDetalle> = emptyList(),
    val isLoading:   Boolean                   = false,
    val error:       String?                   = null,
    val toastMsg:    String?                   = null,
    val procesando:  Set<String>               = emptySet(),   // "childId|activityId"
    val objetivo:    AprobObjetivo?            = null,
    val montoInput:  Int                       = 0,
)

@HiltViewModel
class AprobacionNivelViewModel @Inject constructor(
    savedStateHandle:           SavedStateHandle,
    private val moneyRepo:      MoneyApprovalRepository,
    private val session:        SessionManager,
) : ViewModel() {

    private val approver: ApprovalLevel = runCatching {
        ApprovalLevel.valueOf(savedStateHandle.get<String>("nivel") ?: "CHURCH")
    }.getOrDefault(ApprovalLevel.CHURCH)

    private val nodeId: String = when (approver) {
        ApprovalLevel.CHURCH   -> session.iglesiaId
        ApprovalLevel.DISTRICT -> session.districtId
        ApprovalLevel.CAMPO    -> session.campoId
        ApprovalLevel.UNION    -> session.unionId
    }

    private val nodeNombre: String = when (approver) {
        ApprovalLevel.CHURCH   -> session.iglesiaNombre
        ApprovalLevel.DISTRICT -> session.districtNombre
        ApprovalLevel.CAMPO    -> session.campoNombre
        ApprovalLevel.UNION    -> session.unionNombre
    }

    private val titulo: String = when (approver) {
        ApprovalLevel.CHURCH   -> "Aprobaciones de grupos"
        ApprovalLevel.DISTRICT -> "Aprobaciones de iglesias"
        ApprovalLevel.CAMPO    -> "Aprobaciones de distritos"
        ApprovalLevel.UNION    -> "Aprobaciones de asociaciones"
    }

    private val _uiState = MutableStateFlow(
        AprobacionNivelUiState(
            nivelTitulo = titulo,
            nodeNombre  = nodeNombre,
            nodeId      = nodeId,
            esChurch    = approver == ApprovalLevel.CHURCH,
            sourceLevel = approver.source,
        )
    )
    val uiState: StateFlow<AprobacionNivelUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            moneyRepo.getNivelDetalle(approver, nodeId)
                .onSuccess { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    // ── Diálogo de aprobación ───────────────────────────────────────────────────
    fun onShowApprove(detalle: NivelActividadDetalle, hijo: NivelHijoPendiente) {
        _uiState.update {
            it.copy(
                objetivo = AprobObjetivo(
                    activityTypeId  = detalle.actividad.activityTypeId,
                    actividadNombre = detalle.actividad.actividadNombre,
                    markerType      = detalle.actividad.markerType,
                    unitLabel       = detalle.actividad.unitLabel,
                    childId         = hijo.childId,
                    childNombre     = hijo.childNombre,
                    pendiente       = hijo.pendiente,
                ),
                montoInput = hijo.pendiente.toInt(),
            )
        }
    }

    fun onMontoChange(v: Int) {
        val max = _uiState.value.objetivo?.pendiente?.toInt() ?: return
        _uiState.update { it.copy(montoInput = v.coerceIn(0, max)) }
    }

    fun onDismiss() = _uiState.update { it.copy(objetivo = null, montoInput = 0) }

    fun onConfirmApprove() {
        val obj   = _uiState.value.objetivo ?: return
        val monto = _uiState.value.montoInput
        if (monto <= 0) return
        val key = "${obj.childId}|${obj.activityTypeId}"
        _uiState.update { it.copy(objetivo = null, procesando = it.procesando + key) }
        viewModelScope.launch {
            moneyRepo.approve(
                approver          = approver,
                childId           = obj.childId,
                activityTypeId    = obj.activityTypeId,
                requested         = obj.pendiente,
                approved          = monto.toDouble(),
                note              = null,
                approverProfileId = session.miembroId.takeIf { it.isNotBlank() },
            )
                .onSuccess {
                    _uiState.update { it.copy(procesando = it.procesando - key, toastMsg = "Aprobado") }
                    cargar()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(procesando = it.procesando - key, toastMsg = e.message ?: "Error al aprobar") }
                }
        }
    }

    fun consumeToast() { _uiState.update { it.copy(toastMsg = null) } }
}
