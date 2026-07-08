package com.gpleader.app.feature.nivel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ApprovalLevel
import com.gpleader.app.core.data.repository.MoneyApprovalRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NivelHomeUiState(
    val nivelLabel:     String  = "",     // "Pastor" | "Asociación" | "Unión"
    val nodeNombre:     String  = "",
    val nivel:          String  = "DISTRICT",
    val pendingCount:   Int     = 0,
    val isLoading:      Boolean = false,
    val error:          String? = null,
)

@HiltViewModel
class NivelHomeViewModel @Inject constructor(
    savedStateHandle:      SavedStateHandle,
    private val moneyRepo: MoneyApprovalRepository,
    private val session:   SessionManager,
) : ViewModel() {

    private val approver: ApprovalLevel = runCatching {
        ApprovalLevel.valueOf(savedStateHandle.get<String>("nivel") ?: "DISTRICT")
    }.getOrDefault(ApprovalLevel.DISTRICT)

    private val nodeId: String = when (approver) {
        ApprovalLevel.CHURCH   -> session.iglesiaId
        ApprovalLevel.DISTRICT -> session.districtId
        ApprovalLevel.CAMPO    -> session.campoId
        ApprovalLevel.UNION    -> session.unionId
    }

    private val label: String = when (approver) {
        ApprovalLevel.CHURCH   -> "Iglesia"
        ApprovalLevel.DISTRICT -> "Pastor"
        ApprovalLevel.CAMPO    -> "Asociación"
        ApprovalLevel.UNION    -> "Unión"
    }

    private val nodeNombre: String = when (approver) {
        ApprovalLevel.CHURCH   -> session.iglesiaNombre
        ApprovalLevel.DISTRICT -> session.districtNombre
        ApprovalLevel.CAMPO    -> session.campoNombre
        ApprovalLevel.UNION    -> session.unionNombre
    }

    private val _uiState = MutableStateFlow(
        NivelHomeUiState(nivelLabel = label, nodeNombre = nodeNombre, nivel = approver.name)
    )
    val uiState: StateFlow<NivelHomeUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            moneyRepo.getMonetaryActivitiesWithPending(approver, nodeId)
                .onSuccess { acts ->
                    _uiState.update { it.copy(isLoading = false, pendingCount = acts.count { a -> a.pendienteTotal > 0.0 }) }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun cerrarSesion() {
        session.isIglesiaLeader = false
        session.clear()
    }
}
