package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.HistFiltroTrimestre
import com.gpleader.app.core.data.repository.HistMiembro
import com.gpleader.app.core.data.repository.MemberEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistorialMiembrosUiState(
    val titulo:    String            = "",
    val marker:    String            = "monetary",
    val items:     List<HistMiembro> = emptyList(),
    val isLoading: Boolean           = false,
    val error:     String?           = null,
)

@HiltViewModel
class HistorialAportesMiembrosViewModel @Inject constructor(
    savedStateHandle:            SavedStateHandle,
    private val memberEntryRepo: MemberEntryRepository,
) : ViewModel() {

    private val scope:      String = savedStateHandle.get<String>("scope") ?: "gp"
    private val scopeId:    String = savedStateHandle.get<String>("scopeId") ?: ""
    private val activityId: String = savedStateHandle.get<String>("activityId") ?: ""
    private val filtro: HistFiltroTrimestre = runCatching {
        HistFiltroTrimestre.valueOf(savedStateHandle.get<String>("filtro") ?: "ACTUAL")
    }.getOrDefault(HistFiltroTrimestre.ACTUAL)

    private val _uiState = MutableStateFlow(
        HistorialMiembrosUiState(
            titulo = savedStateHandle.get<String>("titulo") ?: "",
            marker = savedStateHandle.get<String>("marker") ?: "monetary",
        )
    )
    val uiState: StateFlow<HistorialMiembrosUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            memberEntryRepo.getHistorialMiembros(scope, scopeId, activityId, filtro)
                .onSuccess { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
