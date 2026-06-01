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
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.Json
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
    private val session: SessionManager,
    private val supabase: SupabaseClient,
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
                // Crear sesión gp_sessions para gp_set_password (si el grupo tiene gp_code)
                val gpCode = session.gpCode
                if (gpCode.isNotBlank()) {
                    runCatching {
                        val resp = supabase.postgrest.rpc("gp_login", buildJsonObject {
                            put("p_gp_code",    gpCode)
                            put("p_password",   contrasena)
                            put("p_device_info", "Android")
                        })
                        val row = Json.parseToJsonElement(resp.data).jsonArray.firstOrNull()?.jsonObject
                        row?.get("session_token")?.jsonPrimitive?.contentOrNull
                    }.getOrNull()?.let { token ->
                        session.sessionToken = token
                    }
                }
                if (!session.grupoPasswordSet) {
                    _uiState.update { it.copy(isAuthenticating = false, showPasswordDialog = false, navigateToCambiarContrasena = true) }
                } else {
                    _uiState.update { it.copy(isAuthenticating = false, showPasswordDialog = false, navigateToHome = true) }
                }
            }.onFailure {
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
