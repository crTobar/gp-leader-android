package com.gpleader.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.GrupoRepository
import com.gpleader.app.core.data.repository.IglesiaItem
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.iniciales
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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

    // Sheet auto-marcado sábado
    val showSabadoSheet: Boolean = false,
    val sabadoMiembroNombre: String = "",
    val sabadoMiembroId: String = "",
    val sabadoIglesias: List<IglesiaItem> = emptyList(),
    val sabadoIglesiasLoading: Boolean = false,
    val sabadoBusqueda: String = "",
    val sabadoSelectedIglesiaId: String = "",
    val sabadoSelectedIglesiaNombre: String = "",
    val sabadoEnviando: Boolean = false,
    val sabadoError: String? = null,

    // Navegación
    val navigateToHome: Boolean = false,
    val navigateToCambiarContrasena: Boolean = false,
    val navigateToConfirmacion: String? = null, // iglesia nombre
    val showNoEsSabadoMsg: Boolean = false,
)

@HiltViewModel
class QuienEresViewModel @Inject constructor(
    private val miembroRepo: MiembroRepository,
    private val grupoRepo: GrupoRepository,
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

    // ── Selección de miembro ───────────────────────────────────────────────────

    fun onMiembroSelected(miembro: MiembroSesion) {
        if (miembro.isLider) {
            _uiState.update {
                it.copy(showPasswordDialog = true, pendingLider = miembro, authError = null, contrasenaDialog = "")
            }
        } else {
            if (esSabadoHoy()) {
                abrirSabadoSheet(miembro)
            } else {
                _uiState.update { it.copy(showNoEsSabadoMsg = true) }
                viewModelScope.launch {
                    delay(3_000)
                    _uiState.update { it.copy(showNoEsSabadoMsg = false) }
                }
            }
        }
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

    // ── Sheet sábado ───────────────────────────────────────────────────────────

    private fun abrirSabadoSheet(miembro: MiembroSesion) {
        _uiState.update {
            it.copy(
                showSabadoSheet           = true,
                sabadoMiembroId           = miembro.id,
                sabadoMiembroNombre       = miembro.nombre,
                sabadoSelectedIglesiaId   = session.iglesiaId,
                sabadoSelectedIglesiaNombre = session.iglesiaNombre,
                sabadoIglesias            = emptyList(),
                sabadoIglesiasLoading     = true,
                sabadoBusqueda            = "",
                sabadoError               = null,
            )
        }
        viewModelScope.launch {
            val iglesias = runCatching { grupoRepo.getIglesias() }.getOrElse { emptyList() }
            _uiState.update { it.copy(sabadoIglesias = iglesias, sabadoIglesiasLoading = false) }
        }
    }

    fun onSabadoIglesiaSelected(iglesia: IglesiaItem) {
        _uiState.update {
            it.copy(
                sabadoSelectedIglesiaId     = iglesia.id,
                sabadoSelectedIglesiaNombre = iglesia.nombre,
                sabadoBusqueda              = "",
            )
        }
    }

    fun onSabadoBusquedaChange(query: String) {
        _uiState.update { it.copy(sabadoBusqueda = query) }
    }

    fun onSabadoUsarIglesiaPropia() {
        _uiState.update {
            it.copy(
                sabadoSelectedIglesiaId     = session.iglesiaId,
                sabadoSelectedIglesiaNombre = session.iglesiaNombre,
                sabadoBusqueda              = "",
            )
        }
    }

    fun onSabadoMarcarClick() {
        val grupoId     = session.grupoId
        val mId         = _uiState.value.sabadoMiembroId.ifBlank { return }
        val iglesiaId   = _uiState.value.sabadoSelectedIglesiaId
        val iglesiaNombre = _uiState.value.sabadoSelectedIglesiaNombre

        _uiState.update { it.copy(sabadoEnviando = true, sabadoError = null) }
        viewModelScope.launch {
            runCatching {
                supabase.functions.invoke(
                    function = "sabbath-self-mark",
                    body     = buildJsonObject {
                        put("groupId",  grupoId)
                        put("memberId", mId)
                        if (iglesiaId.isNotBlank()) put("churchId", iglesiaId)
                    },
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(sabadoEnviando = false, showSabadoSheet = false, navigateToConfirmacion = iglesiaNombre)
                }
            }.onFailure {
                _uiState.update {
                    it.copy(sabadoEnviando = false, sabadoError = "No se pudo registrar. Intenta de nuevo.")
                }
            }
        }
    }

    fun onDismissSabadoSheet() {
        _uiState.update { it.copy(showSabadoSheet = false, sabadoError = null) }
    }

    // ── Consumidores de navegación ─────────────────────────────────────────────

    fun consumeHomeNavigation() {
        _uiState.update { it.copy(navigateToHome = false) }
    }

    fun consumeCambiarContrasenaNavigation() {
        _uiState.update { it.copy(navigateToCambiarContrasena = false) }
    }

    fun consumeConfirmacionNavigation() {
        _uiState.update { it.copy(navigateToConfirmacion = null) }
    }

    fun consumeNoEsSabadoMsg() {
        _uiState.update { it.copy(showNoEsSabadoMsg = false) }
    }

    private fun esSabadoHoy(): Boolean = true // TODO: restaurar → LocalDate.now().dayOfWeek == DayOfWeek.SATURDAY
}
