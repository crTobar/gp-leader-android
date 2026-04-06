package com.gpleader.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val correo: String = "",
    val contrasena: String = "",
    val loginSuccess: Boolean = false,
    val navigateToSuplente: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(value: String) {
        _uiState.update { it.copy(correo = value, error = null) }
    }

    fun onContrasenaChange(value: String) {
        _uiState.update { it.copy(contrasena = value, error = null) }
    }

    fun onLoginClick() {
        // TODO: reemplazar con autenticación real cuando haya usuario disponible
        _uiState.update { it.copy(loginSuccess = true) }
    }

    fun onSuplementeClick() {
        _uiState.update { it.copy(navigateToSuplente = true) }
    }

    fun consumeLoginSuccess() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    fun consumeSuplementeNavigation() {
        _uiState.update { it.copy(navigateToSuplente = false) }
    }
}
