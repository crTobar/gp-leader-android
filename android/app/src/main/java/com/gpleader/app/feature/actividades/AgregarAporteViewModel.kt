package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.MemberEntryRepository
import com.gpleader.app.core.data.repository.MiembroData
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgregarAporteUiState(
    val actividadNombre: String            = "",
    val markerType:      String            = "counter",
    val unitLabel:       String            = "",
    val miembros:        List<MiembroData> = emptyList(),
    val valores:         Map<String, Int>  = emptyMap(),   // miembroId → monto/cantidad
    val toggleados:      Set<String>       = emptySet(),   // miembroId (checkbox)
    val isLoading:       Boolean           = true,
    val isGuardando:     Boolean           = false,
    val guardadoOk:      Boolean           = false,
    val error:           String?           = null,
)

@HiltViewModel
class AgregarAporteViewModel @Inject constructor(
    savedStateHandle:            SavedStateHandle,
    private val actividadRepo:   ActividadRepository,
    private val memberEntryRepo: MemberEntryRepository,
    private val miembroRepo:     MiembroRepository,
    private val session:         SessionManager,
) : ViewModel() {

    private val actividadTipoId: String = checkNotNull(savedStateHandle["actividadTipoId"])

    private val _uiState = MutableStateFlow(AgregarAporteUiState())
    val uiState: StateFlow<AgregarAporteUiState> = _uiState.asStateFlow()

    private val isCheckbox: Boolean get() = _uiState.value.markerType.let { it == "checkbox" || it == "realizado" }
    private val isMonetary: Boolean get() = _uiState.value.markerType == "monetary"

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            actividadRepo.getTodasActividadesTipo(
                session.iglesiaId, session.districtId, session.campoId, session.grupoId
            ).onSuccess { tipos ->
                tipos.find { it.id == actividadTipoId }?.let { tipo ->
                    _uiState.update { s ->
                        s.copy(actividadNombre = tipo.nombre, markerType = tipo.markerType, unitLabel = tipo.unitLabel)
                    }
                }
            }
            val miembros = runCatching { miembroRepo.getMiembrosActivos(session.grupoId).first() }.getOrDefault(emptyList())
            _uiState.update { it.copy(miembros = miembros, isLoading = false) }
        }
    }

    fun onValorChange(miembroId: String, valor: Int) {
        _uiState.update { s ->
            val v = valor.coerceAtLeast(0)
            s.copy(valores = if (v == 0) s.valores - miembroId else s.valores + (miembroId to v))
        }
    }

    fun onToggle(miembroId: String) {
        _uiState.update { s ->
            s.copy(toggleados = if (miembroId in s.toggleados) s.toggleados - miembroId else s.toggleados + miembroId)
        }
    }

    fun onGuardar() {
        val state = _uiState.value
        // Líder registra → aprobado directo. Monetario mantiene el paso de Junta.
        val status = if (isMonetary) "pending_board" else "approved"
        val aportes: List<Pair<String, Double>> = if (isCheckbox) {
            state.toggleados.map { it to 1.0 }
        } else {
            state.valores.filter { it.value > 0 }.map { it.key to it.value.toDouble() }
        }
        if (aportes.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isGuardando = true, error = null) }
            var huboError = false
            for ((miembroId, valor) in aportes) {
                memberEntryRepo.addEntry(
                    miembroId       = miembroId,
                    actividadTipoId = actividadTipoId,
                    grupoId         = session.grupoId,
                    value           = valor,
                    status          = status,
                    actorRole       = "leader",
                    actorId         = session.miembroId.takeIf { it.isNotBlank() },
                ).onFailure { huboError = true }
            }
            _uiState.update {
                it.copy(
                    isGuardando = false,
                    guardadoOk  = !huboError,
                    error       = if (huboError) "Hubo un error al guardar algunos aportes" else null,
                )
            }
        }
    }

    val hayValores: Boolean
        get() {
            val s = _uiState.value
            return if (isCheckbox) s.toggleados.isNotEmpty() else s.valores.values.any { it > 0 }
        }
}
