package com.gpleader.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.GpAuthRepository
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
    val isLider: Boolean = false,
)

data class QuienEresUiState(
    val grupoNombre:    String = "",
    val iglesiaNombre:  String = "",
    val districtNombre: String = "",
    val campoNombre:    String = "",
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
    val navigateToCambiarContrasena: Boolean = false,
    val navigateToConfirmarIdentidad: MiembroSesion? = null,
)

@HiltViewModel
class QuienEresViewModel @Inject constructor(
    private val miembroRepo: MiembroRepository,
    private val gpAuthRepo: GpAuthRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuienEresUiState())
    val uiState: StateFlow<QuienEresUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                grupoNombre    = session.grupoNombre,
                iglesiaNombre  = session.iglesiaNombre,
                districtNombre = session.districtNombre,
                campoNombre    = session.campoNombre,
                isLoading      = true,
            )
        }
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

    // ── Selección de miembro ───────────────────────────────────────────────────

    fun onMiembroSelected(miembro: MiembroSesion) {
        if (miembro.isLider) {
            _uiState.update {
                it.copy(showPasswordDialog = true, pendingLider = miembro, authError = null, contrasenaDialog = "")
            }
        } else {
            _uiState.update { it.copy(navigateToConfirmarIdentidad = miembro) }
        }
    }

    fun consumeConfirmarIdentidadNavigation() {
        _uiState.update { it.copy(navigateToConfirmarIdentidad = null) }
    }

    // ── Contraseña líder ───────────────────────────────────────────────────────

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
        val gpCode = session.gpCode
        if (gpCode.isBlank()) {
            _uiState.update { it.copy(authError = "Este grupo no tiene código de acceso configurado") }
            return
        }
        val lider = _uiState.value.pendingLider ?: return

        _uiState.update { it.copy(isAuthenticating = true, authError = null) }
        viewModelScope.launch {
            gpAuthRepo.validateGroupPassword(gpCode, username, contrasena)
                .onSuccess { result ->
                    session.miembroId        = lider.id
                    session.miembroNombre    = lider.nombre
                    session.grupoPasswordSet = result.passwordSet
                    session.sessionToken     = result.sessionToken ?: ""
                    val canChangePassword    = !result.passwordSet && !result.sessionToken.isNullOrBlank()
                    _uiState.update {
                        it.copy(
                            isAuthenticating       = false,
                            showPasswordDialog       = false,
                            navigateToCambiarContrasena = canChangePassword,
                            navigateToHome           = !canChangePassword,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isAuthenticating = false, authError = "Contraseña incorrecta") }
                }
        }
    }

    fun onDismissPasswordDialog() {
        _uiState.update { it.copy(showPasswordDialog = false, pendingLider = null, contrasenaDialog = "", authError = null) }
    }

    // ── Consumidores de navegación ─────────────────────────────────────────────

    fun consumeHomeNavigation() {
        _uiState.update { it.copy(navigateToHome = false) }
    }

    fun consumeCambiarContrasenaNavigation() {
        _uiState.update { it.copy(navigateToCambiarContrasena = false) }
    }
}
