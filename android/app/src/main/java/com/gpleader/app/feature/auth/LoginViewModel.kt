package com.gpleader.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.CampoItem
import com.gpleader.app.core.data.repository.DistritoItem
import com.gpleader.app.core.data.repository.GrupoItem
import com.gpleader.app.core.data.repository.GrupoRepository
import com.gpleader.app.core.data.repository.IglesiaItem
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class LoginUiState(
    // Datos cargados al inicio
    val allCampos:    List<CampoItem>    = emptyList(),
    val allDistritos: List<DistritoItem> = emptyList(),
    val allIglesias:  List<IglesiaItem>  = emptyList(),
    val allGrupos:    List<GrupoItem>    = emptyList(),

    // Listas filtradas que ve el usuario (dependen de la selección)
    val filteredDistritos: List<DistritoItem> = emptyList(),
    val filteredIglesias:  List<IglesiaItem>  = emptyList(),
    val filteredGrupos:    List<GrupoItem>    = emptyList(),

    // Selecciones actuales
    val selectedCampo:    CampoItem?    = null,
    val selectedDistrito: DistritoItem? = null,
    val selectedIglesia:  IglesiaItem?  = null,

    // Estado UI
    val isLoading: Boolean = false,
    val error:     String? = null,

    // Modo iglesia (DEV)
    val iglesiaMode:                Boolean      = false,
    val iglesiaSearchQuery:          String       = "",
    val showIglesiaPasswordDialog:   Boolean      = false,
    val pendingIglesiaLogin:         IglesiaItem? = null,

    // Navegación
    val navigateToQuienEres:    Boolean = false,
    val navigateToIglesiaHome:  Boolean = false,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val grupoRepo: GrupoRepository,
    private val session:   SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        cargarTodo()
    }

    // ── Carga inicial — todos los datos en paralelo ────────────────────────────

    private fun cargarTodo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val camposDeferred    = async { runCatching { grupoRepo.getCampos() }.getOrElse { emptyList() } }
            val distritosDeferred = async { runCatching { grupoRepo.getDistritos() }.getOrElse { emptyList() } }
            val iglesiasDeferred  = async { runCatching { grupoRepo.getIglesias() }.getOrElse { emptyList() } }
            val gruposDeferred    = async { runCatching { grupoRepo.getGrupos() }.getOrElse { emptyList() } }

            val campos    = camposDeferred.await()
            val distritos = distritosDeferred.await()
            val iglesias  = iglesiasDeferred.await()
            val grupos    = gruposDeferred.await()

            if (campos.isEmpty() && distritos.isEmpty() && iglesias.isEmpty() && grupos.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "Sin conexión. Verifica tu red e intenta de nuevo.") }
                return@launch
            }

            // Enriquecer iglesias con nombres de distrito y campo
            val camposMap    = campos.associateBy { it.id }
            val distritosMap = distritos.associateBy { it.id }
            val iglesiasEnriquecidas = iglesias.map { iglesia ->
                val distrito = distritosMap[iglesia.districtId]
                val campo    = distrito?.let { camposMap[it.campoId] }
                iglesia.copy(
                    districtNombre = distrito?.nombre ?: "",
                    campoNombre    = campo?.nombre ?: "",
                )
            }

            // Enriquecer grupos con nombres de iglesia, distrito y campo
            val iglesiasMap = iglesiasEnriquecidas.associateBy { it.id }
            val gruposEnriquecidos = grupos.map { grupo ->
                val iglesia = iglesiasMap[grupo.iglesiaId]
                grupo.copy(
                    iglesiaNombre  = iglesia?.nombre ?: "",
                    districtNombre = iglesia?.districtNombre ?: "",
                    campoNombre    = iglesia?.campoNombre ?: "",
                )
            }

            _uiState.update {
                it.copy(
                    isLoading         = false,
                    allCampos         = campos,
                    allDistritos      = distritos,
                    allIglesias       = iglesiasEnriquecidas,
                    allGrupos         = gruposEnriquecidos,
                    filteredDistritos = distritos,
                    filteredIglesias  = iglesiasEnriquecidas,
                    filteredGrupos    = gruposEnriquecidos,
                )
            }
        }
    }

    // ── Selecciones con filtrado inteligente ──────────────────────────────────

    fun onCampoSelected(campo: CampoItem?) {
        _uiState.update { state ->
            val filtrados = if (campo != null)
                state.allDistritos.filter { it.campoId == campo.id }
            else
                state.allDistritos
            state.copy(
                selectedCampo    = campo,
                selectedDistrito = null,
                selectedIglesia  = null,
                filteredDistritos = filtrados,
                filteredIglesias  = state.allIglesias,
                filteredGrupos    = state.allGrupos,
                error             = null,
            )
        }
    }

    fun onDistritoSelected(distrito: DistritoItem?) {
        _uiState.update { state ->
            val filtradas = if (distrito != null)
                state.allIglesias.filter { it.districtId == distrito.id }
            else
                state.allIglesias
            state.copy(
                selectedDistrito = distrito,
                selectedIglesia  = null,
                filteredIglesias = filtradas,
                filteredGrupos   = state.allGrupos,
                error            = null,
            )
        }
    }

    fun onIglesiaSelected(iglesia: IglesiaItem?) {
        _uiState.update { state ->
            val filtrados = if (iglesia != null)
                state.allGrupos.filter { it.iglesiaId == iglesia.id }
            else
                state.allGrupos
            state.copy(
                selectedIglesia = iglesia,
                filteredGrupos  = filtrados,
                error           = null,
            )
        }
    }

    fun onGrupoTap(grupo: GrupoItem) {
        if (grupo.username == null) {
            _uiState.update { it.copy(error = "Este grupo aún no tiene acceso digital. Contactá al administrador.") }
            return
        }
        val state    = _uiState.value
        val iglesia  = state.allIglesias.find { it.id == grupo.iglesiaId }
        val distrito = iglesia?.let { state.allDistritos.find { d -> d.id == it.districtId } }
        val campo    = distrito?.let { state.allCampos.find { c -> c.id == it.campoId } }
        session.grupoId          = grupo.id
        session.grupoNombre      = grupo.nombre
        session.grupoUsername    = grupo.username ?: ""
        session.grupoPasswordSet = grupo.passwordSet
        session.gpCode           = grupo.gpCode ?: ""
        session.iglesiaId        = grupo.iglesiaId
        session.iglesiaNombre    = iglesia?.nombre ?: ""
        session.districtId       = distrito?.id ?: ""
        session.districtNombre   = distrito?.nombre ?: ""
        session.campoId          = campo?.id ?: ""
        session.campoNombre      = campo?.nombre ?: ""
        _uiState.update { it.copy(navigateToQuienEres = true) }
    }

    fun consumeQuienEresNavigation() { _uiState.update { it.copy(navigateToQuienEres = false) } }

    // ── Modo Iglesia (DEV) ─────────────────────────────────────────────────────

    fun onIngresarComoIglesia() {
        _uiState.update { it.copy(iglesiaMode = true) }
    }

    fun onVolverDesdeModoIglesia() {
        _uiState.update { it.copy(iglesiaMode = false, iglesiaSearchQuery = "", showIglesiaPasswordDialog = false, pendingIglesiaLogin = null) }
    }

    fun onIglesiaSearchQueryChange(query: String) {
        _uiState.update { it.copy(iglesiaSearchQuery = query) }
    }

    fun onIglesiaParaLoginSelected(iglesia: IglesiaItem) {
        _uiState.update { it.copy(pendingIglesiaLogin = iglesia, showIglesiaPasswordDialog = true) }
    }

    fun onDismissIglesiaPasswordDialog() {
        _uiState.update { it.copy(showIglesiaPasswordDialog = false, pendingIglesiaLogin = null) }
    }

    fun onConfirmarAccesoIglesia() {
        val iglesia = _uiState.value.pendingIglesiaLogin ?: return
        val distrito = _uiState.value.allDistritos.find { it.id == iglesia.districtId }
        val campo    = distrito?.let { _uiState.value.allCampos.find { c -> c.id == it.campoId } }
        session.iglesiaId      = iglesia.id
        session.iglesiaNombre  = iglesia.nombre
        session.districtId     = distrito?.id ?: ""
        session.districtNombre = distrito?.nombre ?: ""
        session.campoId        = campo?.id ?: ""
        session.campoNombre    = campo?.nombre ?: ""
        session.isIglesiaLeader = true
        _uiState.update { it.copy(showIglesiaPasswordDialog = false, navigateToIglesiaHome = true) }
    }

    fun consumeIglesiaHomeNavigation() { _uiState.update { it.copy(navigateToIglesiaHome = false) } }
}
