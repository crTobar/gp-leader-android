package com.gpleader.app.feature.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.DuoMisioneroData
import com.gpleader.app.core.data.repository.DuoRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuosListUiState(
    val isLoading:    Boolean = true,
    val isRefreshing: Boolean = false,
    val duos: List<DuoMisioneroData> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class DuosListViewModel @Inject constructor(
    private val duoRepo: DuoRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuosListUiState())
    val uiState: StateFlow<DuosListUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            duoRepo.getDuosByGrupo(session.grupoId)
                .onSuccess { _uiState.value = _uiState.value.copy(isRefreshing = false, duos = it) }
                .onFailure { _uiState.value = _uiState.value.copy(isRefreshing = false, error = it.message) }
        }
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            duoRepo.getDuosByGrupo(session.grupoId)
                .onSuccess { _uiState.value = DuosListUiState(isLoading = false, duos = it) }
                .onFailure { _uiState.value = DuosListUiState(isLoading = false, error = it.message) }
        }
    }
}
