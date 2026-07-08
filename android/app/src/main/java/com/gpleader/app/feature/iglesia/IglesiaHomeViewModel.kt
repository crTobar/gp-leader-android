package com.gpleader.app.feature.iglesia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ApprovalLevel
import com.gpleader.app.core.data.repository.IglesiaRepository
import com.gpleader.app.core.data.repository.MoneyApprovalRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IglesiaHomeUiState(
    val iglesiaNombre:     String  = "",
    val totalGrupos:       Int     = 0,
    val totalMiembros:     Int     = 0,
    val pendingBoardCount: Int     = 0,
    val isLoading:         Boolean = false,
    val error:             String? = null,
)

@HiltViewModel
class IglesiaHomeViewModel @Inject constructor(
    private val iglesiaRepo:     IglesiaRepository,
    private val moneyRepo:       MoneyApprovalRepository,
    private val session:         SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IglesiaHomeUiState())
    val uiState: StateFlow<IglesiaHomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(iglesiaNombre = session.iglesiaNombre, isLoading = true) }
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { iglesiaRepo.getGruposByIglesia(session.iglesiaId) }
                .onSuccess { grupos ->
                    val totalMiembros     = grupos.sumOf { it.totalMiembros }
                    // Nº de actividades monetarias con monto pendiente por aprobar (badge).
                    val pendingBoardCount = moneyRepo
                        .getMonetaryActivitiesWithPending(ApprovalLevel.CHURCH, session.iglesiaId)
                        .getOrDefault(emptyList())
                        .count { it.pendienteTotal > 0.0 }
                    _uiState.update {
                        it.copy(
                            isLoading         = false,
                            totalGrupos       = grupos.size,
                            totalMiembros     = totalMiembros,
                            pendingBoardCount = pendingBoardCount,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun cerrarSesion() {
        session.isIglesiaLeader = false
        session.clear()
    }
}
