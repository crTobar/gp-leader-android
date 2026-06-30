package com.gpleader.app.feature.iglesia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.IglesiaRepository
import com.gpleader.app.core.data.repository.PendingBoardItem
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AprobacionesUiState(
    val items:      List<PendingBoardItem> = emptyList(),
    val isLoading:  Boolean                = false,
    val error:      String?                = null,
    val toastMsg:   String?                = null,
    val procesando: Set<String>            = emptySet(),
)

@HiltViewModel
class AprobacionesViewModel @Inject constructor(
    private val iglesiaRepo: IglesiaRepository,
    private val session:     SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AprobacionesUiState())
    val uiState: StateFlow<AprobacionesUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { iglesiaRepo.getPendingBoardActivities(session.iglesiaId) }
                .onSuccess { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun onAprobar(recordId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(procesando = it.procesando + recordId) }
            iglesiaRepo.approveMonetaryActivity(recordId)
                .onSuccess {
                    _uiState.update { s ->
                        s.copy(
                            items     = s.items.filter { it.recordId != recordId },
                            procesando = s.procesando - recordId,
                            toastMsg  = "Actividad aprobada",
                        )
                    }
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(procesando = s.procesando - recordId, toastMsg = "Error al aprobar")
                    }
                }
        }
    }

    fun onRechazar(recordId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(procesando = it.procesando + recordId) }
            iglesiaRepo.rejectMonetaryActivity(recordId)
                .onSuccess {
                    _uiState.update { s ->
                        s.copy(
                            items     = s.items.filter { it.recordId != recordId },
                            procesando = s.procesando - recordId,
                            toastMsg  = "Actividad rechazada",
                        )
                    }
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(procesando = s.procesando - recordId, toastMsg = "Error al rechazar")
                    }
                }
        }
    }

    fun consumeToast() { _uiState.update { it.copy(toastMsg = null) } }
}
