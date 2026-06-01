package com.gpleader.app.feature.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.GroupLogEntry
import com.gpleader.app.core.data.repository.GroupLogRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistroActividadUiState(
    val isLoading: Boolean          = true,
    val entradas:  List<GroupLogEntry> = emptyList(),
    val error:     String?          = null,
)

@HiltViewModel
class RegistroActividadViewModel @Inject constructor(
    private val groupLogRepo: GroupLogRepository,
    private val session:      SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroActividadUiState())
    val uiState: StateFlow<RegistroActividadUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            groupLogRepo.getEntradas(session.grupoId)
                .onSuccess { lista ->
                    _uiState.update { it.copy(isLoading = false, entradas = lista) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, error = "No se pudo cargar el registro") }
                }
        }
    }
}
