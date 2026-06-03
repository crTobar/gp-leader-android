package com.gpleader.app.feature.miembro

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.RegistroDiario
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ActividadCampanaUiState(
    val isLoading:     Boolean          = true,
    val nombreCampana: String           = "",
    val dias:          List<RegistroDiario> = emptyList(),
    val togglingFecha: LocalDate?       = null,
    val error:         String?          = null,
)

@HiltViewModel
class ActividadCampanaViewModel @Inject constructor(
    private val actividadRepo: ActividadRepository,
    private val session:       SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val actividadTipoId: String = checkNotNull(savedStateHandle["actividadTipoId"])
    private val nombreCampana:   String = savedStateHandle["nombreCampana"] ?: ""
    private val desdeStr:        String = checkNotNull(savedStateHandle["desde"])
    private val hastaStr:        String = checkNotNull(savedStateHandle["hasta"])

    private val _uiState = MutableStateFlow(ActividadCampanaUiState(nombreCampana = nombreCampana))
    val uiState: StateFlow<ActividadCampanaUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val desde = runCatching { LocalDate.parse(desdeStr) }.getOrElse { LocalDate.now() }
            val hasta = runCatching { LocalDate.parse(hastaStr) }.getOrElse { LocalDate.now() }
            actividadRepo.getRegistrosCampana(session.miembroId, actividadTipoId, desde, hasta)
                .onSuccess { dias ->
                    _uiState.update { it.copy(isLoading = false, dias = dias) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, error = "No se pudo cargar la campaña") }
                }
        }
    }

    fun onToggleDia(fecha: LocalDate) {
        if (fecha.isAfter(LocalDate.now())) return
        if (_uiState.value.togglingFecha != null) return

        val actual = _uiState.value.dias.find { it.fecha == fecha } ?: return
        val nuevoEstado = !actual.marcada

        _uiState.update { it.copy(togglingFecha = fecha) }

        viewModelScope.launch {
            actividadRepo.toggleMiembroActividad(session.miembroId, actividadTipoId, fecha, nuevoEstado)
                .onSuccess { cargar() }
                .onFailure  { _uiState.update { it.copy(togglingFecha = null) } }
        }
    }
}
