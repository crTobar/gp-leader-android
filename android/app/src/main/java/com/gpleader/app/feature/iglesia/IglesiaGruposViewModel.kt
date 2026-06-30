package com.gpleader.app.feature.iglesia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.GrupoResumen
import com.gpleader.app.core.data.repository.IglesiaRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IglesiaGruposUiState(
    val grupos:    List<GrupoResumen> = emptyList(),
    val isLoading: Boolean            = false,
    val error:     String?            = null,
)

@HiltViewModel
class IglesiaGruposViewModel @Inject constructor(
    private val iglesiaRepo: IglesiaRepository,
    private val session:     SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IglesiaGruposUiState())
    val uiState: StateFlow<IglesiaGruposUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { iglesiaRepo.getGruposByIglesia(session.iglesiaId) }
                .onSuccess { grupos ->
                    _uiState.update { it.copy(isLoading = false, grupos = grupos) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
