package com.gpleader.app.feature.auth

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ConfirmarIdentidadUiState(
    val miembroNombre: String = "",
    val grupoNombre: String = "",
    val navigateToMiembroHome: Boolean = false,
)

@HiltViewModel
class ConfirmarIdentidadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val session: SessionManager,
) : ViewModel() {

    private val miembroId: String     = Uri.decode(savedStateHandle["miembroId"]     ?: "")
    private val miembroNombre: String = Uri.decode(savedStateHandle["miembroNombre"] ?: "")

    private val _uiState = MutableStateFlow(
        ConfirmarIdentidadUiState(
            miembroNombre = miembroNombre,
            grupoNombre   = session.grupoNombre,
        )
    )
    val uiState: StateFlow<ConfirmarIdentidadUiState> = _uiState.asStateFlow()

    fun onConfirmarIdentidad() {
        session.guardarPerfilMiembro(
            miembroId     = miembroId,
            nombre        = miembroNombre,
            grupoId       = session.grupoId,
            grupoNombre   = session.grupoNombre,
            iglesiaId     = session.iglesiaId,
            iglesiaNombre = session.iglesiaNombre,
        )
        _uiState.update { it.copy(navigateToMiembroHome = true) }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigateToMiembroHome = false) }
    }
}
