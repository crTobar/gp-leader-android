package com.gpleader.app.feature.miembro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.ActividadTipoData
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
import javax.inject.Inject

sealed class ActividadMiembroUi {
    abstract val tipo: ActividadTipoData
    abstract val isToggling: Boolean

    data class Diaria(
        override val tipo: ActividadTipoData,
        val marcadaHoy: Boolean,
        val horaMarcada: LocalTime? = null,
        override val isToggling: Boolean = false,
    ) : ActividadMiembroUi()

    data class Semanal(
        override val tipo: ActividadTipoData,
        val contadorSemana: Int,
        override val isToggling: Boolean = false,
    ) : ActividadMiembroUi()
}

data class MiembroActividadesUiState(
    val isLoading:   Boolean = true,
    val actividades: List<ActividadMiembroUi> = emptyList(),
    val error:       String? = null,
)

@HiltViewModel
class MiembroActividadesViewModel @Inject constructor(
    private val actividadRepo: ActividadRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiembroActividadesUiState())
    val uiState: StateFlow<MiembroActividadesUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val iglesiaId = session.iglesiaId
            val miembroId = session.miembroId

            actividadRepo.getActividadesMiembro(iglesiaId, session.districtId, session.campoId, session.grupoId)
                .onSuccess { tipos ->
                    val hoy        = LocalDate.now()
                    val lunes      = lunesDeSemana(hoy)
                    val desde7dias = hoy.minusDays(6)

                    val actividades = coroutineScope {
                        tipos.map { tipo ->
                            async {
                                if (tipo.frecuencia == "diaria") {
                                    val fechas = actividadRepo
                                        .getRegistrosMiembro(miembroId, tipo.id, desde7dias)
                                        .getOrElse { emptyList() }
                                        .toSet()
                                    ActividadMiembroUi.Diaria(
                                        tipo        = tipo,
                                        marcadaHoy  = fechas.contains(hoy),
                                        horaMarcada = null,
                                    )
                                } else {
                                    val count = actividadRepo
                                        .getContadorSemanalMiembro(miembroId, tipo.id, lunes)
                                        .getOrElse { 0 }
                                    ActividadMiembroUi.Semanal(
                                        tipo            = tipo,
                                        contadorSemana  = count,
                                    )
                                }
                            }
                        }.map { it.await() }
                    }
                    _uiState.update { it.copy(isLoading = false, actividades = actividades) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, error = "No se pudieron cargar las actividades") }
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
            actividadRepo.toggleMiembroActividad(session.miembroId, tipoId, hoy, nuevoEstado)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(actividades = state.actividades.map { item ->
                            if (item.tipo.id == tipoId && item is ActividadMiembroUi.Diaria)
                                item.copy(marcadaHoy = nuevoEstado, isToggling = false)
                            else item
                        })
                    }
                }
                .onFailure { setToggling(tipoId, false) }
        }
    }

    fun onIncrementarSemanal(tipoId: String) {
        val actual = _uiState.value.actividades.find { it.tipo.id == tipoId } as? ActividadMiembroUi.Semanal ?: return
        if (actual.isToggling) return
        actualizarContador(tipoId, actual.contadorSemana + 1)
    }

    fun onDecrementarSemanal(tipoId: String) {
        val actual = _uiState.value.actividades.find { it.tipo.id == tipoId } as? ActividadMiembroUi.Semanal ?: return
        if (actual.isToggling || actual.contadorSemana <= 0) return
        actualizarContador(tipoId, actual.contadorSemana - 1)
    }

    fun onMontoSemanalChange(tipoId: String, nuevoMonto: Int) {
        actualizarContador(tipoId, nuevoMonto.coerceAtLeast(0))
    }

    private fun actualizarContador(tipoId: String, nuevoCount: Int) {
        setToggling(tipoId, true)
        viewModelScope.launch {
            val lunes = lunesDeSemana(LocalDate.now())
            actividadRepo.upsertContadorSemanalMiembro(session.miembroId, tipoId, lunes, nuevoCount)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(actividades = state.actividades.map { item ->
                            if (item.tipo.id == tipoId && item is ActividadMiembroUi.Semanal)
                                item.copy(contadorSemana = nuevoCount, isToggling = false)
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
