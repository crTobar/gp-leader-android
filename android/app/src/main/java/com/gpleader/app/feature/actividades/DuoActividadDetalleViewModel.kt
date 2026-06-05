package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.DuoActividadRecord
import com.gpleader.app.core.data.repository.DuoActividadTipo
import com.gpleader.app.core.data.repository.DuoRepository
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DuoActividadDetalleUiState(
    val duoId:          String                   = "",
    val tipo:           DuoActividadTipo?         = null,
    val registroHoy:    DuoActividadRecord?        = null,
    val historial:      List<DuoActividadRecord>   = emptyList(),
    val miembroNombres: Map<String, String>        = emptyMap(),
    val showSheet:      Boolean                    = false,
    val inputValue:     String                     = "",
    val checkboxValue:  Boolean                    = false,
    val isGuardando:    Boolean                    = false,
    val isLoading:      Boolean                    = true,
    val error:          String?                    = null,
)

@HiltViewModel
class DuoActividadDetalleViewModel @Inject constructor(
    private val duoRepo: DuoRepository,
    private val session: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val duoId  = savedStateHandle.get<String>("duoId") ?: ""
    private val tipoId = savedStateHandle.get<String>("actividadTipoId") ?: ""

    private val _uiState = MutableStateFlow(DuoActividadDetalleUiState(duoId = duoId))
    val uiState: StateFlow<DuoActividadDetalleUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val tipos = duoRepo.getActividadesDuo(duoId).getOrElse { emptyList() }
            val tipo  = tipos.find { it.id == tipoId }

            val desde = LocalDate.now().minusDays(30)
            val registros = duoRepo.getRegistrosDuo(duoId, tipoId, desde).getOrElse { emptyList() }
            val hoy = LocalDate.now()
            val registroHoy = registros.find { it.recordDate == hoy }
            val historial   = registros.filter { it.recordDate != hoy }
                .sortedByDescending { it.recordDate }

            val duos = duoRepo.getDuosByGrupo(session.grupoId).getOrElse { emptyList() }
            val duo  = duos.find { it.id == duoId }
            val nombres = buildMap {
                duo?.member1?.let { put(it.id, it.nombreCompleto) }
                duo?.member2?.let { put(it.id, it.nombreCompleto) }
            }

            _uiState.update {
                it.copy(
                    tipo           = tipo,
                    registroHoy    = registroHoy,
                    historial      = historial,
                    miembroNombres = nombres,
                    inputValue     = registroHoy?.count?.toString() ?: "",
                    checkboxValue  = registroHoy?.isDone ?: false,
                    isLoading      = false,
                )
            }
        }
    }

    fun onRegistrarClick() {
        val s = _uiState.value
        val esCheckbox = s.tipo?.markerType == "checkbox"
        if (esCheckbox) {
            // Toggle directo sin sheet
            guardar(count = null, isDone = !(s.registroHoy?.isDone ?: false))
        } else {
            _uiState.update { it.copy(
                showSheet    = true,
                inputValue   = it.registroHoy?.count?.toString() ?: "",
                checkboxValue = it.registroHoy?.isDone ?: false,
            ) }
        }
    }

    fun onDismissSheet()          { _uiState.update { it.copy(showSheet = false) } }
    fun onInputChange(v: String)  { _uiState.update { it.copy(inputValue = v) } }
    fun onCheckboxChange(v: Boolean) { _uiState.update { it.copy(checkboxValue = v) } }

    fun onGuardar() {
        val s = _uiState.value
        val tipo = s.tipo ?: return
        val count = when (tipo.markerType) {
            "counter", "monetary" -> s.inputValue.trim().toIntOrNull() ?: 0
            else                  -> null
        }
        val isDone = when (tipo.markerType) {
            "checkbox" -> s.checkboxValue
            "counter", "monetary" -> (count ?: 0) > 0
            else -> false
        }
        guardar(count, isDone)
    }

    private fun guardar(count: Int?, isDone: Boolean) {
        if (_uiState.value.isGuardando) return
        _uiState.update { it.copy(isGuardando = true, showSheet = false) }
        viewModelScope.launch {
            duoRepo.upsertRegistroDuo(
                duoId           = duoId,
                actividadTipoId = tipoId,
                fecha           = LocalDate.now(),
                count           = count,
                isDone          = isDone,
                updatedBy       = session.miembroId.ifBlank { session.grupoId },
            ).onSuccess { cargar() }
             .onFailure { e -> _uiState.update { it.copy(isGuardando = false, error = e.message) } }
        }
    }
}
