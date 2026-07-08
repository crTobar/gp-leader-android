package com.gpleader.app.feature.auth

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.GpAuthRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfirmarIdentidadUiState(
    val miembroNombre: String = "",
    val grupoNombre: String = "",
    val navigateToMiembroHome: Boolean = false,

    // Diálogo contraseña miembro
    val showPasswordDialog: Boolean = false,
    val passwordInput: String = "",
    val passwordError: String? = null,
    val isAuthenticating: Boolean = false,
)

@HiltViewModel
class ConfirmarIdentidadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gpAuthRepo: GpAuthRepository,
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

    // Al confirmar identidad ya no entra directo: pide contraseña.
    fun onConfirmarIdentidad() {
        _uiState.update { it.copy(showPasswordDialog = true, passwordInput = "", passwordError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(passwordInput = value, passwordError = null) }
    }

    fun onDismissPasswordDialog() {
        _uiState.update {
            it.copy(showPasswordDialog = false, passwordInput = "", passwordError = null, isAuthenticating = false)
        }
    }

    fun onSubmitPassword() {
        val password = _uiState.value.passwordInput
        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Ingresá tu contraseña") }
            return
        }
        _uiState.update { it.copy(isAuthenticating = true, passwordError = null) }
        viewModelScope.launch {
            gpAuthRepo.validateMemberPassword(miembroId, password)
                .onSuccess {
                    session.guardarPerfilMiembro(
                        miembroId     = miembroId,
                        nombre        = miembroNombre,
                        grupoId       = session.grupoId,
                        grupoNombre   = session.grupoNombre,
                        iglesiaId     = session.iglesiaId,
                        iglesiaNombre = session.iglesiaNombre,
                    )
                    _uiState.update {
                        it.copy(isAuthenticating = false, showPasswordDialog = false, navigateToMiembroHome = true)
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isAuthenticating = false, passwordError = "Contraseña incorrecta") }
                }
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigateToMiembroHome = false) }
    }
}
