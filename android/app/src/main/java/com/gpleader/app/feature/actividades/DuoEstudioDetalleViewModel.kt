package com.gpleader.app.feature.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.DuoRepository
import com.gpleader.app.core.ui.estudios.EstudioBiblicoItem
import com.gpleader.app.core.ui.estudios.asItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuoEstudioDetalleUiState(
    val isLoading:      Boolean = true,
    val isRefreshing:   Boolean = false,
    val estudio:        EstudioBiblicoItem? = null,
    val togglingLesson: Int? = null,
    val error:          String? = null,
)

@HiltViewModel
class DuoEstudioDetalleViewModel @Inject constructor(
    private val duoRepo: DuoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuoEstudioDetalleUiState())
    val uiState: StateFlow<DuoEstudioDetalleUiState> = _uiState.asStateFlow()

    fun cargar(estudioId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            duoRepo.getEstudioDuoById(estudioId)
                .onSuccess { estudio ->
                    _uiState.update { it.copy(isLoading = false, estudio = estudio?.asItem()) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun onRefresh(estudioId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            duoRepo.getEstudioDuoById(estudioId)
                .onSuccess { estudio ->
                    _uiState.update { it.copy(isRefreshing = false, estudio = estudio?.asItem()) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isRefreshing = false, error = e.message) }
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

        _uiState.update { it.copy(togglingLesson = lessonNumber, estudio = estudio.copy(completedLessons = newCompleted)) }

        viewModelScope.launch {
            duoRepo.toggleLeccionDuo(estudioId, lessonNumber, !wasCompleted)
                .onFailure { _uiState.update { it.copy(togglingLesson = null, estudio = estudio) } }
                .onSuccess { _uiState.update { it.copy(togglingLesson = null) } }
        }
    }
}
