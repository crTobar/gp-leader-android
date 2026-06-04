package com.gpleader.app.feature.miembro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.DuoActividadRecord
import com.gpleader.app.core.data.repository.DuoActividadTipo
import com.gpleader.app.core.data.repository.DuoBibleStudy
import com.gpleader.app.core.data.repository.DuoRepository
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.data.repository.iniciales
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

enum class DuoMiembroTab { ACTIVIDADES, ESTUDIOS }

data class DuoMisioneroUiState(
    val isLoading:      Boolean = true,
    val isRefreshing:   Boolean = false,
    val duoId:          String? = null,
    val parejaNombre:   String? = null,
    val parejaIniciales: String = "??",
    val actividades:    List<DuoActividadTipo> = emptyList(),
    val registrosHoy:   Map<String, DuoActividadRecord> = emptyMap(),
    val estudios:       List<DuoBibleStudy> = emptyList(),
    val tabActivo:      DuoMiembroTab = DuoMiembroTab.ACTIVIDADES,
    val error:          String? = null,
)

@HiltViewModel
class DuoMisioneroViewModel @Inject constructor(
    private val duoRepo: DuoRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuoMisioneroUiState())
    val uiState: StateFlow<DuoMisioneroUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            cargarInterno(isRefresh = true)
        }
    }

    private fun cargar() {
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
                val duo = duoRepo.getDuoPorMiembro(session.miembroId).getOrThrow()
                    ?: error("No perteneces a ningún dúo activo")

                val pareja = if (duo.member1.id == session.miembroId) duo.member2 else duo.member1
                val hoy = LocalDate.now()

                coroutineScope {
                    val acts     = async { duoRepo.getActividadesDuo(duo.id).getOrThrow() }
                    val estudios = async { duoRepo.getEstudiosDuo(duo.id).getOrThrow() }
                    val actividades = acts.await()
                    val registros = actividades.map { tipo ->
                        async {
                            tipo.id to duoRepo.getRegistrosDuo(duo.id, tipo.id, hoy)
                                .getOrDefault(emptyList())
                                .find { it.recordDate == hoy }
                        }
                    }.associate { it.await() }

                    DuoMisioneroUiState(
                        isLoading       = false,
                        isRefreshing    = false,
                        duoId           = duo.id,
                        parejaNombre    = pareja.nombreCompleto,
                        parejaIniciales = pareja.iniciales,
                        actividades     = actividades,
                        registrosHoy    = registros.filterValues { it != null } as Map<String, DuoActividadRecord>,
                        estudios        = estudios.await(),
                        tabActivo       = _uiState.value.tabActivo,
                    )
                }
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure {
                _uiState.value = DuoMisioneroUiState(isLoading = false, isRefreshing = false, error = it.message)
            }
        }
    }

    fun onTabChange(tab: DuoMiembroTab) {
        _uiState.value = _uiState.value.copy(tabActivo = tab)
    }

    fun onToggleActividad(tipoId: String, markerType: String) {
        val s = _uiState.value
        val duoId = s.duoId ?: return
        val actual = s.registrosHoy[tipoId]
        val isDone = !(actual?.isDone ?: false)
        viewModelScope.launch {
            duoRepo.upsertRegistroDuo(
                duoId           = duoId,
                actividadTipoId = tipoId,
                fecha           = LocalDate.now(),
                count           = if (markerType == "counter") actual?.count else null,
                isDone          = isDone,
                updatedBy       = session.miembroId,
            ).onSuccess { cargar() }
        }
    }

    fun onIncrementar(tipoId: String) = updateContador(tipoId, +1)
    fun onDecrementar(tipoId: String) = updateContador(tipoId, -1)

    private fun updateContador(tipoId: String, delta: Int) {
        val s = _uiState.value
        val duoId = s.duoId ?: return
        val actual = s.registrosHoy[tipoId]
        val nuevo = ((actual?.count ?: 0) + delta).coerceAtLeast(0)
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
        val duoId = _uiState.value.duoId ?: return
        viewModelScope.launch {
            duoRepo.crearEstudioDuo(duoId, studentName).onSuccess { cargar() }
        }
    }
}
