package com.gpleader.app.feature.actividades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.DiaStat
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CampanaDetalleUiState(
    val isLoading:     Boolean       = true,
    val nombreCampana: String        = "",
    val dias:          List<DiaStat> = emptyList(),
    val expandedFecha: LocalDate?    = null,
    val error:         String?       = null,
)

@HiltViewModel
class CampanaDetalleViewModel @Inject constructor(
    private val actividadRepo: ActividadRepository,
    private val session:       SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val actividadTipoId: String = checkNotNull(savedStateHandle["actividadTipoId"])
    private val nombreCampana:   String = savedStateHandle["nombreCampana"] ?: ""
    private val desdeStr:        String = checkNotNull(savedStateHandle["desde"])
    private val hastaStr:        String = checkNotNull(savedStateHandle["hasta"])

    private val _uiState = MutableStateFlow(CampanaDetalleUiState(nombreCampana = nombreCampana))
    val uiState: StateFlow<CampanaDetalleUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val desde = runCatching { LocalDate.parse(desdeStr) }.getOrElse { LocalDate.now() }
            val hasta = runCatching { LocalDate.parse(hastaStr) }.getOrElse { LocalDate.now() }
            actividadRepo.getDiasCompletionStats(session.grupoId, actividadTipoId, desde, hasta)
                .onSuccess { dias ->
                    _uiState.update { it.copy(isLoading = false, dias = dias) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, error = "No se pudieron cargar los datos") }
                }
        }
    }

    fun onToggleDia(fecha: LocalDate) {
        val actual = _uiState.value.expandedFecha
        _uiState.update { it.copy(expandedFecha = if (actual == fecha) null else fecha) }
    }
}
