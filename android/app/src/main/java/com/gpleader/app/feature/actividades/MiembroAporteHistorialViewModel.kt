package com.gpleader.app.feature.actividades

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.MemberEntry
import com.gpleader.app.core.data.repository.MemberEntryRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MiembroAporteHistorialUiState(
    val miembroNombre: String = "",
    val markerType:    String = "counter",
    val unitLabel:     String = "",
    val entries:       List<MemberEntry> = emptyList(),
    val isLoading:     Boolean = true,
    val error:         String? = null,
)

@HiltViewModel
class MiembroAporteHistorialViewModel @Inject constructor(
    savedStateHandle:            SavedStateHandle,
    private val actividadRepo:   ActividadRepository,
    private val memberEntryRepo: MemberEntryRepository,
    private val session:         SessionManager,
) : ViewModel() {

    private val miembroId:       String = checkNotNull(savedStateHandle["miembroId"])
    private val actividadTipoId: String = checkNotNull(savedStateHandle["actividadTipoId"])
    private val miembroNombre:   String = Uri.decode(savedStateHandle["nombre"] ?: "")

    private val _uiState = MutableStateFlow(MiembroAporteHistorialUiState(miembroNombre = miembroNombre))
    val uiState: StateFlow<MiembroAporteHistorialUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            actividadRepo.getTodasActividadesTipo(
                session.iglesiaId, session.districtId, session.campoId, session.grupoId
            ).onSuccess { tipos ->
                tipos.find { it.id == actividadTipoId }?.let { t ->
                    _uiState.update { it.copy(markerType = t.markerType, unitLabel = t.unitLabel) }
                }
            }
            memberEntryRepo.getEntries(miembroId, actividadTipoId)
                .onSuccess { entries -> _uiState.update { it.copy(entries = entries, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
        }
    }
}
