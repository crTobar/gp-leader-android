package com.gpleader.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.iniciales
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MiembroSesion(
    val id: String,
    val nombre: String,
    val iniciales: String,
)

data class QuienEresUiState(
    val grupoNombre: String = "",
    val miembros: List<MiembroSesion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateToHome: Boolean = false,
)

@HiltViewModel
class QuienEresViewModel @Inject constructor(
    private val miembroRepo: MiembroRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuienEresUiState())
    val uiState: StateFlow<QuienEresUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(grupoNombre = session.grupoNombre, isLoading = true) }
        viewModelScope.launch {
            miembroRepo.getMiembrosActivos(session.grupoId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar miembros: ${e.message}") }
                }
                .collect { miembros ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error     = null,
                            miembros  = miembros.map { m ->
                                MiembroSesion(
                                    id        = m.id,
                                    nombre    = m.nombreCompleto,
                                    iniciales = m.iniciales,
                                )
                            },
                        )
                    }
                }
        }
    }

    fun onMiembroSelected(miembro: MiembroSesion) {
        session.miembroId     = miembro.id
        session.miembroNombre = miembro.nombre
        _uiState.update { it.copy(navigateToHome = true) }
    }

    fun consumeHomeNavigation() {
        _uiState.update { it.copy(navigateToHome = false) }
    }
}
