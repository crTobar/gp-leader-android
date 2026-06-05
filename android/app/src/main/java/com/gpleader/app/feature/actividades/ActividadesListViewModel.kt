package com.gpleader.app.feature.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.ActividadTipoData
import com.gpleader.app.core.data.repository.DuoActividadConTotal
import com.gpleader.app.core.data.repository.DuoMisioneroData
import com.gpleader.app.core.data.repository.DuoRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class FiltroNivel(val valor: String?, val label: String) {
    TODOS(null,       "Todo"),
    UNION("union",    "Unión"),
    PASTOR("pastor",  "Pastor"),
    MI_GP("my_group", "Mi GP"),
}

enum class FiltroEstado(val label: String) {
    TODAS("Todas"),
    ACTIVAS("Activas"),
    INACTIVAS("Inactivas"),
}

data class ActividadConResumen(
    val tipo:          ActividadTipoData,
    val totalCantidad: Int,
    val montoTotal:    Double,
    val esActiva:      Boolean,
    val esProxima:     Boolean,
)

data class ActividadesListUiState(
    val actividades:    List<ActividadConResumen>  = emptyList(),
    val visibles:       List<ActividadConResumen>  = emptyList(),
    val filtroNivel:    FiltroNivel                = FiltroNivel.TODOS,
    val filtroEstado:   FiltroEstado               = FiltroEstado.TODAS,
    val isLoading:      Boolean                    = true,
    val isRefreshing:   Boolean                    = false,
    val error:          String?                    = null,
    val modoGP:         Boolean                    = true,
    val duoActividades: List<DuoActividadConTotal> = emptyList(),
    val duosActivos:    List<DuoMisioneroData>     = emptyList(),
    val isDuoLoading:   Boolean                    = false,
)

@HiltViewModel
class ActividadesListViewModel @Inject constructor(
    private val actividadRepo: ActividadRepository,
    private val duoRepo:       DuoRepository,
    private val session:       SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActividadesListUiState())
    val uiState: StateFlow<ActividadesListUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            val tiposResult   = actividadRepo.getTodasActividadesTipo(session.iglesiaId, session.districtId, session.campoId, session.grupoId)
            val totalesResult = actividadRepo.getActividadesConTotales(session.grupoId)

            if (tiposResult.isFailure) {
                _uiState.update { it.copy(isRefreshing = false, error = tiposResult.exceptionOrNull()?.message) }
                return@launch
            }
            val hoy     = LocalDate.now()
            val tipos   = tiposResult.getOrThrow()
            val totales = totalesResult.getOrElse { emptyMap() }
            val combined = tipos.map { tipo ->
                val total      = totales[tipo.id]
                val esProxima  = tipo.startDate != null && hoy.isBefore(tipo.startDate)
                val esActiva   = !esProxima && (tipo.endDate == null || !hoy.isAfter(tipo.endDate))
                ActividadConResumen(tipo, total?.totalCantidad ?: 0, total?.montoTotal ?: 0.0, esActiva, esProxima)
            }
            _uiState.update { it.copy(isRefreshing = false, actividades = combined) }
            filtrar()
        }
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val tiposResult   = actividadRepo.getTodasActividadesTipo(session.iglesiaId, session.districtId, session.campoId, session.grupoId)
            val totalesResult = actividadRepo.getActividadesConTotales(session.grupoId)

            if (tiposResult.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = tiposResult.exceptionOrNull()?.message) }
                return@launch
            }
            val hoy     = LocalDate.now()
            val tipos   = tiposResult.getOrThrow()
            val totales = totalesResult.getOrElse { emptyMap() }
            val combined = tipos.map { tipo ->
                val total      = totales[tipo.id]
                val esProxima  = tipo.startDate != null && hoy.isBefore(tipo.startDate)
                val esActiva   = !esProxima && (tipo.endDate == null || !hoy.isAfter(tipo.endDate))
                ActividadConResumen(tipo, total?.totalCantidad ?: 0, total?.montoTotal ?: 0.0, esActiva, esProxima)
            }
            _uiState.update { it.copy(isLoading = false, actividades = combined) }
            filtrar()
        }
    }

    fun onModoChange(esGP: Boolean) {
        _uiState.update { it.copy(modoGP = esGP) }
        if (!esGP && _uiState.value.duoActividades.isEmpty()) {
            cargarDuoActividades()
        }
    }

    fun cargarDuoActividades() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDuoLoading = true) }
            val duosResult = duoRepo.getDuosByGrupo(session.grupoId)
            val duos = duosResult.getOrElse { emptyList() }.filter { it.isActive }
            val actResult = duoRepo.getActividadesConTotalesPorGrupo(session.grupoId)
            _uiState.update {
                it.copy(
                    isDuoLoading   = false,
                    duosActivos    = duos,
                    duoActividades = actResult.getOrElse { emptyList() },
                )
            }
        }
    }

    fun onFiltroNivel(filtro: FiltroNivel) {
        _uiState.update { it.copy(filtroNivel = filtro) }
        filtrar()
    }

    fun onFiltroEstado(filtro: FiltroEstado) {
        _uiState.update { it.copy(filtroEstado = filtro) }
        filtrar()
    }

    private fun filtrar() {
        val s = _uiState.value
        val resultado = s.actividades.filter { item ->
            val nivelOk  = s.filtroNivel.valor == null || item.tipo.level == s.filtroNivel.valor
            val estadoOk = when (s.filtroEstado) {
                FiltroEstado.TODAS     -> true
                FiltroEstado.ACTIVAS   -> item.esActiva || item.esProxima
                FiltroEstado.INACTIVAS -> !item.esActiva && !item.esProxima
            }
            nivelOk && estadoOk
        }
        _uiState.update { it.copy(visibles = resultado) }
    }
}
