package com.gpleader.app.feature.miembro

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

data class EstudioDetalleUiState(
    val isLoading:      Boolean = true,
    val isRefreshing:   Boolean = false,
    val estudio:        EstudioBiblico? = null,
    val togglingLesson: Int? = null,
    val error:          String? = null,
)

@HiltViewModel
class EstudioDetalleViewModel @Inject constructor(
    private val repo:    BibleStudyRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstudioDetalleUiState())
    val uiState: StateFlow<EstudioDetalleUiState> = _uiState.asStateFlow()

    fun onRefresh(estudioId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            repo.getEstudioById(estudioId)
                .onSuccess { estudio ->
                    _uiState.update { it.copy(isRefreshing = false, estudio = estudio) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isRefreshing = false, error = e.message) }
                }
        }
    }

    fun cargar(estudioId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.getEstudioById(estudioId)
                .onSuccess { estudio ->
                    _uiState.update { it.copy(isLoading = false, estudio = estudio) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun onToggleLesson(estudioId: String, lessonNumber: Int) {
        val estudio = _uiState.value.estudio ?: return
        if (_uiState.value.togglingLesson != null) return

        val wasCompleted = lessonNumber in estudio.completedLessons
        val newCompleted = if (wasCompleted) {
            estudio.completedLessons.filter { it != lessonNumber }
        } else {
            (estudio.completedLessons + lessonNumber).sorted()
        }

        // Actualización optimista
        _uiState.update { s ->
            s.copy(
                togglingLesson = lessonNumber,
                estudio        = estudio.copy(completedLessons = newCompleted),
            )
        }

        viewModelScope.launch {
            repo.toggleLesson(estudioId, lessonNumber, !wasCompleted)
                .onFailure {
                    // Revertir en caso de error
                    _uiState.update { s ->
                        s.copy(
                            togglingLesson = null,
                            estudio        = estudio,
                        )
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(togglingLesson = null) }
                }
        }
    }
}
