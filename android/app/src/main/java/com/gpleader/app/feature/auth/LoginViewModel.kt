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
    val filteredDistritos: List<DistritoItem> = emptyList(), // filtrados por campo si hay selección
    val filteredIglesias:  List<IglesiaItem>  = emptyList(), // filtradas por distrito si hay selección
    val filteredGrupos:    List<GrupoItem>    = emptyList(), // filtrados por iglesia si hay selección

    // Selecciones actuales
    val selectedCampo:    CampoItem?    = null,
    val selectedDistrito: DistritoItem? = null,
    val selectedIglesia:  IglesiaItem?  = null,
    val selectedGrupo:    GrupoItem?    = null,

    // Estado UI
    val isLoading: Boolean = false,
    val error:     String? = null,

    // Navegación
    val navigateToQuienEres: Boolean = false,
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
            // Si hay campo → solo distritos de ese campo; si no → todos
            val filtrados = if (campo != null)
                state.allDistritos.filter { it.campoId == campo.id }
            else
                state.allDistritos
            state.copy(
                selectedCampo    = campo,
                selectedDistrito = null,
                selectedIglesia  = null,
                selectedGrupo    = null,
                filteredDistritos = filtrados,
                filteredIglesias  = state.allIglesias,
                filteredGrupos    = state.allGrupos,
                error             = null,
            )
        }
    }

    fun onDistritoSelected(distrito: DistritoItem?) {
        _uiState.update { state ->
            // Si hay distrito → solo iglesias de ese distrito; si no → todas
            val filtradas = if (distrito != null)
                state.allIglesias.filter { it.districtId == distrito.id }
            else
                state.allIglesias
            state.copy(
                selectedDistrito = distrito,
                selectedIglesia  = null,
                selectedGrupo    = null,
                filteredIglesias = filtradas,
                filteredGrupos   = state.allGrupos,
                error            = null,
            )
        }
    }

    fun onIglesiaSelected(iglesia: IglesiaItem?) {
        _uiState.update { state ->
            // Si hay iglesia → mostrar solo grupos de esa iglesia; si no → todos
            val filtrados = if (iglesia != null)
                state.allGrupos.filter { it.iglesiaId == iglesia.id }
            else
                state.allGrupos
            state.copy(
                selectedIglesia = iglesia,
                selectedGrupo   = null,
                filteredGrupos  = filtrados,
                error           = null,
            )
        }
    }

    fun onGrupoSelected(grupo: GrupoItem?) {
        if (grupo == null) {
            _uiState.update { it.copy(selectedGrupo = null, error = null) }
            return
        }
        // Auto-rellenar iglesia, distrito y campo basado en el GP seleccionado
        _uiState.update { state ->
            val iglesia  = state.allIglesias.find { it.id == grupo.iglesiaId }
            val distrito = iglesia?.let { state.allDistritos.find { d -> d.id == it.districtId } }
            val campo    = distrito?.let { state.allCampos.find { c -> c.id == it.campoId } }

            state.copy(
                selectedGrupo    = grupo,
                selectedIglesia  = iglesia,
                selectedDistrito = distrito,
                selectedCampo    = campo,
                filteredDistritos = if (campo != null) state.allDistritos.filter { it.campoId == campo.id } else state.allDistritos,
                filteredIglesias  = if (distrito != null) state.allIglesias.filter { it.districtId == distrito.id } else state.allIglesias,
                filteredGrupos    = if (iglesia != null) state.allGrupos.filter { it.iglesiaId == iglesia.id } else state.allGrupos,
                error             = null,
            )
        }
    }

    // ── Continuar ──────────────────────────────────────────────────────────────

    fun onContinuarClick() {
        val grupo = _uiState.value.selectedGrupo ?: run {
            _uiState.update { it.copy(error = "Seleccioná un grupo pequeño") }
            return
        }
        if (grupo.username == null) {
            _uiState.update { it.copy(error = "Este grupo aún no tiene acceso digital. Contactá al administrador.") }
            return
        }
        val iglesia = _uiState.value.selectedIglesia
        session.grupoId          = grupo.id
        session.grupoNombre      = grupo.nombre
        session.grupoUsername    = grupo.username
        session.grupoPasswordSet = grupo.passwordSet
        session.iglesiaId        = grupo.iglesiaId
        session.iglesiaNombre    = iglesia?.nombre ?: ""
        _uiState.update { it.copy(navigateToQuienEres = true) }
    }

    fun consumeQuienEresNavigation() { _uiState.update { it.copy(navigateToQuienEres = false) } }
}
