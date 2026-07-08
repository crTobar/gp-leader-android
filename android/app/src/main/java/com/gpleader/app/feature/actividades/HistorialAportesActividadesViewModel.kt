package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.HistActividad
import com.gpleader.app.core.data.repository.HistFiltroTrimestre
import com.gpleader.app.core.data.repository.MemberEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistorialActividadesUiState(
    val filtro:    HistFiltroTrimestre  = HistFiltroTrimestre.ACTUAL,
    val items:     List<HistActividad>  = emptyList(),
    val isLoading: Boolean              = false,
    val error:     String?              = null,
)

@HiltViewModel
class HistorialAportesActividadesViewModel @Inject constructor(
    savedStateHandle:            SavedStateHandle,
    private val memberEntryRepo: MemberEntryRepository,
) : ViewModel() {

    private val scope:   String = savedStateHandle.get<String>("scope") ?: "gp"
    private val scopeId: String = savedStateHandle.get<String>("scopeId") ?: ""

    private val _uiState = MutableStateFlow(HistorialActividadesUiState())
    val uiState: StateFlow<HistorialActividadesUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun onFiltroChange(filtro: HistFiltroTrimestre) {
        _uiState.update { it.copy(filtro = filtro) }
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            memberEntryRepo.getHistorialActividades(scope, scopeId, _uiState.value.filtro)
                .onSuccess { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
