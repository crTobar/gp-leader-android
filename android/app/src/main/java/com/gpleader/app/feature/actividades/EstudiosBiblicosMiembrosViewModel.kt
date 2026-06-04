package com.gpleader.app.feature.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.BibleStudyRepository
import com.gpleader.app.core.data.repository.MiembroData
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EstudiosMiembrosUiState(
    val isLoading:    Boolean = true,
    val isRefreshing: Boolean = false,
    val miembros:  List<Pair<MiembroData, Int>> = emptyList(),
    val error:     String? = null,
)

@HiltViewModel
class EstudiosBiblicosMiembrosViewModel @Inject constructor(
    private val miembroRepo:   MiembroRepository,
    private val bibleStudyRepo: BibleStudyRepository,
    private val session:       SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstudiosMiembrosUiState())
    val uiState: StateFlow<EstudiosMiembrosUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            runCatching {
                val miembros = miembroRepo.getMiembrosActivos(session.grupoId).first()
                coroutineScope {
                    miembros.map { m ->
                        async {
                            val estudios = bibleStudyRepo.getEstudios(m.id).getOrDefault(emptyList())
                            m to estudios.size
                        }
                    }.map { it.await() }
                }
            }.onSuccess { lista ->
                _uiState.value = _uiState.value.copy(isRefreshing = false, miembros = lista)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isRefreshing = false, error = it.message)
            }
        }
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching {
                val miembros = miembroRepo.getMiembrosActivos(session.grupoId).first()
                coroutineScope {
                    miembros.map { m ->
                        async {
                            val estudios = bibleStudyRepo.getEstudios(m.id).getOrDefault(emptyList())
                            m to estudios.size
                        }
                    }.map { it.await() }
                }
            }.onSuccess { lista ->
                _uiState.value = EstudiosMiembrosUiState(isLoading = false, miembros = lista)
            }.onFailure {
                _uiState.value = EstudiosMiembrosUiState(isLoading = false, error = it.message)
            }
        }
    }
}
