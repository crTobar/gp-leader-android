package com.gpleader.app.feature.miembro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.ActividadTipoData
import com.gpleader.app.core.data.repository.MemberEntryRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

sealed class ActividadMiembroUi {
    abstract val tipo: ActividadTipoData
    abstract val isToggling: Boolean

    data class Diaria(
        override val tipo: ActividadTipoData,
        val marcadaHoy: Boolean,
        val diasMarcadosSemana: Set<LocalDate> = emptySet(),
        val horaMarcada: LocalTime? = null,
        override val isToggling: Boolean = false,
    ) : ActividadMiembroUi()

    data class Semanal(
        override val tipo: ActividadTipoData,
        val totalHistorico: Int,
        override val isToggling: Boolean = false,
    ) : ActividadMiembroUi()
}

data class MiembroActividadesUiState(
    val isLoading:    Boolean = true,
    val isRefreshing: Boolean = false,
    val actividades: List<ActividadMiembroUi> = emptyList(),
    val error:       String? = null,
)

@HiltViewModel
class MiembroActividadesViewModel @Inject constructor(
    private val actividadRepo:   ActividadRepository,
    private val memberEntryRepo: MemberEntryRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiembroActividadesUiState())
    val uiState: StateFlow<MiembroActividadesUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
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
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            val iglesiaId = session.iglesiaId
            val miembroId = session.miembroId

            actividadRepo.getActividadesMiembro(iglesiaId, session.districtId, session.campoId, session.grupoId)
                .onSuccess { tipos ->
                    val hoy     = LocalDate.now()
                    val lunes   = lunesDeSemana(hoy)
                    val domingo = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

                    val actividades = coroutineScope {
                        tipos.map { tipo ->
                            async {
                                if (tipo.frecuencia == "diaria") {
                                    val fechas = actividadRepo
                                        .getRegistrosMiembro(miembroId, tipo.id, domingo)
                                        .getOrElse { emptyList() }
                                        .toSet()
                                    ActividadMiembroUi.Diaria(
                                        tipo                = tipo,
                                        marcadaHoy          = fechas.contains(hoy),
                                        diasMarcadosSemana  = fechas,
                                        horaMarcada         = null,
                                    )
                                } else {
                                    val total = memberEntryRepo
                                        .getEntryTotal(miembroId, tipo.id)
                                        .getOrElse { 0.0 }
                                    ActividadMiembroUi.Semanal(
                                        tipo           = tipo,
                                        totalHistorico = total.toInt(),
                                    )
                                }
                            }
                        }.map { it.await() }
                    }
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, actividades = actividades) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = "No se pudieron cargar las actividades") }
                }
        }
    }

    fun onToggleDiaria(tipoId: String) {
        val actual = _uiState.value.actividades.find { it.tipo.id == tipoId } as? ActividadMiembroUi.Diaria ?: return
        if (actual.isToggling) return

        setToggling(tipoId, true)

        viewModelScope.launch {
            val hoy         = LocalDate.now()
            val nuevoEstado = !actual.marcadaHoy
            actividadRepo.toggleMiembroActividad(session.miembroId, tipoId, hoy, nuevoEstado, autoApprove = true)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(actividades = state.actividades.map { item ->
                            if (item.tipo.id == tipoId && item is ActividadMiembroUi.Diaria)
                                item.copy(
                                    marcadaHoy         = nuevoEstado,
                                    diasMarcadosSemana = if (nuevoEstado) item.diasMarcadosSemana + hoy else item.diasMarcadosSemana - hoy,
                                    isToggling         = false,
                                )
                            else item
                        })
                    }
                }
                .onFailure { setToggling(tipoId, false) }
        }
    }

    private fun setToggling(tipoId: String, value: Boolean) {
        _uiState.update { state ->
            state.copy(actividades = state.actividades.map { item ->
                when {
                    item.tipo.id != tipoId -> item
                    item is ActividadMiembroUi.Diaria  -> item.copy(isToggling = value)
                    item is ActividadMiembroUi.Semanal -> item.copy(isToggling = value)
                    else -> item
                }
            })
        }
    }

    private fun lunesDeSemana(date: LocalDate): LocalDate {
        var d = date
        while (d.dayOfWeek != DayOfWeek.MONDAY) d = d.minusDays(1)
        return d
    }
}
