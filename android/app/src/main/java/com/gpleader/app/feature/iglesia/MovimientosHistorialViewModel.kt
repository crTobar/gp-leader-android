package com.gpleader.app.feature.iglesia

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.MoneyApprovalRepository
import com.gpleader.app.core.data.repository.MoneyMovimiento
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovimientosHistorialUiState(
    val titulo:      String                = "Historial de aprobaciones",
    val markerType:  String                = "monetary",
    val unitLabel:   String                = "",
    val movimientos: List<MoneyMovimiento> = emptyList(),
    val isLoading:   Boolean               = false,
    val error:       String?               = null,
)

@HiltViewModel
class MovimientosHistorialViewModel @Inject constructor(
    savedStateHandle:      SavedStateHandle,
    private val moneyRepo: MoneyApprovalRepository,
) : ViewModel() {

    private val sourceLevel: String = savedStateHandle.get<String>("sourceLevel") ?: "gp"
    private val sourceId:    String = savedStateHandle.get<String>("sourceId") ?: ""
    private val activityId:  String? = savedStateHandle.get<String>("activityId")?.takeIf { it.isNotBlank() && it != "-" }

    private val _uiState = MutableStateFlow(
        MovimientosHistorialUiState(
            titulo     = savedStateHandle.get<String>("titulo")?.takeIf { it.isNotBlank() } ?: "Historial de aprobaciones",
            markerType = savedStateHandle.get<String>("marker") ?: "monetary",
        )
    )
    val uiState: StateFlow<MovimientosHistorialUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            moneyRepo.getHistory(sourceLevel, sourceId, activityId)
                .onSuccess { movs -> _uiState.update { it.copy(isLoading = false, movimientos = movs) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
