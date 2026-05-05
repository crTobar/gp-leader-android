package com.gpleader.app.feature.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.IglesiaItem
import com.gpleader.app.core.data.repository.GrupoRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class SabadoAutoMarcarUiState(
    val miembroId: String = "",
    val iglesiaPropiaId: String = "",
    val iglesiaPropiaName: String = "",
    val iglesias: List<IglesiaItem> = emptyList(),
    val selectedIglesiaId: String = "",
    val selectedIglesiaNombre: String = "",
    val busqueda: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val navigateToConfirmacion: Boolean = false,
    val iglesiaConfirmadaNombre: String = "",
    val esSabado: Boolean = false,
)

@HiltViewModel
class SabadoAutoMarcarViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val grupoRepo: GrupoRepository,
    private val session: SessionManager,
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val miembroId: String = savedStateHandle["miembroId"] ?: ""

    private val _uiState = MutableStateFlow(SabadoAutoMarcarUiState(miembroId = miembroId))
    val uiState: StateFlow<SabadoAutoMarcarUiState> = _uiState.asStateFlow()

    init {
        val iglesiaId     = session.iglesiaId
        val iglesiaNombre = session.iglesiaNombre
        val esSabado      = LocalDate.now().dayOfWeek == DayOfWeek.SATURDAY
        _uiState.update {
            it.copy(
                iglesiaPropiaId       = iglesiaId,
                iglesiaPropiaName     = iglesiaNombre,
                selectedIglesiaId     = iglesiaId,
                selectedIglesiaNombre = iglesiaNombre,
                esSabado              = esSabado,
            )
        }
        if (esSabado) cargarIglesias()
    }

    private fun cargarIglesias() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val iglesias = runCatching { grupoRepo.getIglesias() }.getOrElse { emptyList() }
            _uiState.update { it.copy(isLoading = false, iglesias = iglesias) }
        }
    }

    fun onBusquedaChange(query: String) {
        _uiState.update { it.copy(busqueda = query) }
    }

    fun onIglesiaSelected(iglesia: IglesiaItem) {
        _uiState.update {
            it.copy(
                selectedIglesiaId     = iglesia.id,
                selectedIglesiaNombre = iglesia.nombre,
                busqueda              = "",
            )
        }
    }

    fun onUsarIglesiaPropia() {
        _uiState.update {
            it.copy(
                selectedIglesiaId     = it.iglesiaPropiaId,
                selectedIglesiaNombre = it.iglesiaPropiaName,
                busqueda              = "",
            )
        }
    }

    fun onMarcarAsistencia() {
        val grupoId   = session.grupoId
        val mId       = _uiState.value.miembroId.ifBlank { return }
        val iglesiaId = _uiState.value.selectedIglesiaId
        val iglesiaName = _uiState.value.selectedIglesiaNombre

        _uiState.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            try {
                supabase.functions.invoke(
                    function = "sabbath-self-mark",
                    body     = buildJsonObject {
                        put("groupId",  grupoId)
                        put("memberId", mId)
                        if (iglesiaId.isNotBlank()) put("churchId", iglesiaId)
                    },
                )
                _uiState.update {
                    it.copy(
                        isSending              = false,
                        navigateToConfirmacion = true,
                        iglesiaConfirmadaNombre = iglesiaName,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSending = false, error = "No se pudo registrar la asistencia. Intenta de nuevo.")
                }
            }
        }
    }

    fun consumeConfirmacionNavigation() {
        _uiState.update { it.copy(navigateToConfirmacion = false) }
    }

    fun iglesiasFiltradas(): List<IglesiaItem> {
        val q = _uiState.value.busqueda.trim()
        if (q.isBlank()) return _uiState.value.iglesias
        return _uiState.value.iglesias.filter { it.nombre.contains(q, ignoreCase = true) }
    }
}
