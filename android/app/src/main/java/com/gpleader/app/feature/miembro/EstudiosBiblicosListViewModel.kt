package com.gpleader.app.feature.miembro

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.BibleStudyRepository
import com.gpleader.app.core.data.repository.EstudioBiblico
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EstudiosBiblicosUiState(
    val isLoading:     Boolean = true,
    val isRefreshing:  Boolean = false,
    val estudios:      List<EstudioBiblico> = emptyList(),
    val error:         String? = null,
    val showAddDialog: Boolean = false,
    val nuevoNombre:   String = "",
    val isCreating:    Boolean = false,
)

@HiltViewModel
class EstudiosBiblicosListViewModel @Inject constructor(
    private val repo:              BibleStudyRepository,
    private val session:           SessionManager,
    private val savedStateHandle:  SavedStateHandle,
) : ViewModel() {

    private val miembroId: String
        get() = savedStateHandle.get<String>("miembroId") ?: session.miembroId

    private val _uiState = MutableStateFlow(EstudiosBiblicosUiState())
    val uiState: StateFlow<EstudiosBiblicosUiState> = _uiState.asStateFlow()

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            repo.getEstudios(miembroId)
                .onSuccess { list -> _uiState.update { it.copy(isRefreshing = false, estudios = list) } }
                .onFailure { e -> _uiState.update { it.copy(isRefreshing = false, error = e.message) } }
        }
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.getEstudios(miembroId)
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, estudios = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun onShowAddDialog() = _uiState.update { it.copy(showAddDialog = true, nuevoNombre = "") }
    fun onDismissDialog() = _uiState.update { it.copy(showAddDialog = false, nuevoNombre = "") }
    fun onNombreChange(v: String) = _uiState.update { it.copy(nuevoNombre = v) }

    fun onCrearEstudio() {
        val nombre = _uiState.value.nuevoNombre.trim()
        if (nombre.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            repo.createEstudio(miembroId, nombre)
                .onSuccess { nuevo ->
                    _uiState.update { s ->
                        s.copy(
                            isCreating    = false,
                            showAddDialog = false,
                            nuevoNombre   = "",
                            estudios      = (s.estudios + nuevo).sortedBy { it.studentName },
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isCreating = false, error = e.message) }
                }
        }
    }
}
