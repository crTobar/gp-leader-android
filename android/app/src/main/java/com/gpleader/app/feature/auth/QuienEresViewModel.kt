package com.gpleader.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.iniciales
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class MiembroSesion(
    val id: String,
    val nombre: String,
    val iniciales: String,
    val isLider: Boolean = false,
)

data class QuienEresUiState(
    val grupoNombre: String = "",
    val miembros: List<MiembroSesion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,

    // Diálogo contraseña líder
    val showPasswordDialog: Boolean = false,
    val pendingLider: MiembroSesion? = null,
    val contrasenaDialog: String = "",
    val isAuthenticating: Boolean = false,
    val authError: String? = null,

    // Navegación
    val navigateToHome: Boolean = false,
    val navigateToSabadoAutoMarcar: String? = null,  // miembroId
    val showNoEsSabadoMsg: Boolean = false,
)

@HiltViewModel
class QuienEresViewModel @Inject constructor(
    private val miembroRepo: MiembroRepository,
    private val session: SessionManager,
    private val supabase: SupabaseClient,
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
                                    isLider   = m.isLider,
                                )
                            },
                        )
                    }
                }
        }
    }

    fun onMiembroSelected(miembro: MiembroSesion) {
        if (miembro.isLider) {
            _uiState.update { it.copy(showPasswordDialog = true, pendingLider = miembro, authError = null, contrasenaDialog = "") }
        } else {
            if (esSabadoHoy()) {
                _uiState.update { it.copy(navigateToSabadoAutoMarcar = miembro.id) }
            } else {
                _uiState.update { it.copy(showNoEsSabadoMsg = true) }
                viewModelScope.launch {
                    delay(3_000)
                    _uiState.update { it.copy(showNoEsSabadoMsg = false) }
                }
            }
        }
    }

    fun onContrasenaDialogChange(value: String) {
        _uiState.update { it.copy(contrasenaDialog = value, authError = null) }
    }

    fun onConfirmarContrasena() {
        val contrasena = _uiState.value.contrasenaDialog
        if (contrasena.isBlank()) {
            _uiState.update { it.copy(authError = "Ingresá la contraseña") }
            return
        }
        val username = session.grupoUsername
        if (username.isBlank()) {
            _uiState.update { it.copy(authError = "Este grupo no tiene acceso digital configurado") }
            return
        }
        val lider = _uiState.value.pendingLider ?: return

        _uiState.update { it.copy(isAuthenticating = true, authError = null) }
        viewModelScope.launch {
            runCatching {
                supabase.auth.signInWith(Email) {
                    email    = "$username@login.presencia.app"
                    password = contrasena
                }
            }.onSuccess {
                session.miembroId     = lider.id
                session.miembroNombre = lider.nombre
                _uiState.update { it.copy(isAuthenticating = false, showPasswordDialog = false, navigateToHome = true) }
            }.onFailure {
                _uiState.update { it.copy(isAuthenticating = false, authError = "Contraseña incorrecta") }
            }
        }
    }

    fun onDismissPasswordDialog() {
        _uiState.update { it.copy(showPasswordDialog = false, pendingLider = null, contrasenaDialog = "", authError = null) }
    }

    fun consumeHomeNavigation() {
        _uiState.update { it.copy(navigateToHome = false) }
    }

    fun consumeSabadoAutoMarcarNavigation() {
        _uiState.update { it.copy(navigateToSabadoAutoMarcar = null) }
    }

    fun consumeNoEsSabadoMsg() {
        _uiState.update { it.copy(showNoEsSabadoMsg = false) }
    }

    private fun esSabadoHoy(): Boolean =
        LocalDate.now().dayOfWeek == DayOfWeek.SATURDAY
}
