package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.DuoActividadRecord
import com.gpleader.app.core.data.repository.DuoActividadTipo
import com.gpleader.app.core.data.repository.DuoBibleStudy
import com.gpleader.app.core.data.repository.DuoMisioneroData
import com.gpleader.app.core.data.repository.DuoRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DuoDetalleUiState(
    val isLoading:    Boolean = true,
    val isRefreshing: Boolean = false,
    val duo:         DuoMisioneroData? = null,
    val actividades: List<DuoActividadTipo> = emptyList(),
    val registrosHoy: Map<String, DuoActividadRecord> = emptyMap(), // tipoId → record
    val estudios:    List<DuoBibleStudy> = emptyList(),
    val tabActivo:   DuoDetalleTab = DuoDetalleTab.ACTIVIDADES,
    val error:       String? = null,
)

enum class DuoDetalleTab { ACTIVIDADES, ESTUDIOS }

@HiltViewModel
class DuoDetalleViewModel @Inject constructor(
    private val duoRepo: DuoRepository,
    private val session: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val duoId: String = checkNotNull(savedStateHandle["duoId"])

    private val _uiState = MutableStateFlow(DuoDetalleUiState())
    val uiState: StateFlow<DuoDetalleUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            cargarInterno(isRefresh = true)
        }
    }

    fun cargar() {
        viewModelScope.launch {
            cargarInterno(isRefresh = false)
        }
    }

    private fun cargarInterno(isRefresh: Boolean) {
        viewModelScope.launch {
            if (!isRefresh) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }
            runCatching {
                coroutineScope {
                    val duos     = async { duoRepo.getDuosByGrupo(session.grupoId).getOrThrow() }
                    val acts     = async { duoRepo.getActividadesDuo(duoId).getOrThrow() }
                    val estudios = async { duoRepo.getEstudiosDuo(duoId).getOrThrow() }
                    val duo = duos.await().find { it.id == duoId }
                    val actividades = acts.await()
                    val hoy = LocalDate.now()
                    val registros = actividades.map { tipo ->
                        async {
                            tipo.id to duoRepo.getRegistrosDuo(duoId, tipo.id, hoy)
                                .getOrDefault(emptyList())
                                .find { it.recordDate == hoy }
                        }
                    }.associate { it.await().first to it.await().second }

                    Triple(duo, actividades to estudios.await(), registros)
                }
            }.onSuccess { (duo, actsEstudios, registros) ->
                val (acts, estudios) = actsEstudios
                _uiState.value = DuoDetalleUiState(
                    isLoading    = false,
                    isRefreshing = false,
                    duo          = duo,
                    actividades  = acts,
                    registrosHoy = registros.filterValues { it != null } as Map<String, DuoActividadRecord>,
                    estudios     = estudios,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = it.message)
            }
        }
    }

    fun onTabChange(tab: DuoDetalleTab) {
        _uiState.value = _uiState.value.copy(tabActivo = tab)
    }

    fun onToggleActividad(tipoId: String, markerType: String) {
        val s = _uiState.value
        val actual = s.registrosHoy[tipoId]
        val isDone = !(actual?.isDone ?: false)
        viewModelScope.launch {
            duoRepo.upsertRegistroDuo(
                duoId           = duoId,
                actividadTipoId = tipoId,
                fecha           = LocalDate.now(),
                count           = if (markerType == "counter") (actual?.count ?: 0) else null,
                isDone          = isDone,
                updatedBy       = session.miembroId,
            ).onSuccess { cargar() }
        }
    }

    fun onIncrementar(tipoId: String) = updateContador(tipoId, +1)
    fun onDecrementar(tipoId: String) = updateContador(tipoId, -1)

    private fun updateContador(tipoId: String, delta: Int) {
        val actual = _uiState.value.registrosHoy[tipoId]
        val nuevo  = ((actual?.count ?: 0) + delta).coerceAtLeast(0)
        viewModelScope.launch {
            duoRepo.upsertRegistroDuo(
                duoId           = duoId,
                actividadTipoId = tipoId,
                fecha           = LocalDate.now(),
                count           = nuevo,
                isDone          = nuevo > 0,
                updatedBy       = session.miembroId,
            ).onSuccess { cargar() }
        }
    }

    fun onToggleLeccion(estudioId: String, leccion: Int, completado: Boolean) {
        viewModelScope.launch {
            duoRepo.toggleLeccionDuo(estudioId, leccion, completado).onSuccess { cargar() }
        }
    }

    fun onCrearEstudio(studentName: String) {
        viewModelScope.launch {
            duoRepo.crearEstudioDuo(duoId, studentName).onSuccess { cargar() }
        }
    }
}
