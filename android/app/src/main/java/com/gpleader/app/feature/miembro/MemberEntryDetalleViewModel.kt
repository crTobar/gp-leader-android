package com.gpleader.app.feature.miembro

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.MemberEntry
import com.gpleader.app.core.data.repository.MemberEntryEvent
import com.gpleader.app.core.data.repository.MemberEntryRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberEntryDetalleUiState(
    val isLoading:    Boolean = true,
    val entry:        MemberEntry? = null,
    val eventos:      List<MemberEntryEvent> = emptyList(),
    val showEditDialog: Boolean = false,
    val editValue:    Int = 0,
    val isGuardando:  Boolean = false,
    val deleted:      Boolean = false,   // navegar atrás tras borrar
    val error:        String? = null,
) {
    val editable: Boolean get() = entry?.status == "draft" || entry?.status == "rejected"
}

@HiltViewModel
class MemberEntryDetalleViewModel @Inject constructor(
    savedStateHandle:          SavedStateHandle,
    private val memberEntryRepo: MemberEntryRepository,
    private val session:         SessionManager,
) : ViewModel() {

    private val entryId: String = checkNotNull(savedStateHandle["entryId"])

    private val _uiState = MutableStateFlow(MemberEntryDetalleUiState())
    val uiState: StateFlow<MemberEntryDetalleUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            memberEntryRepo.getEntry(entryId).onSuccess { e -> _uiState.update { it.copy(entry = e) } }
            memberEntryRepo.getEntryEvents(entryId).onSuccess { evs -> _uiState.update { it.copy(eventos = evs) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onShowEdit() = _uiState.update { it.copy(showEditDialog = true, editValue = (it.entry?.value ?: 0.0).toInt()) }
    fun onDismissEdit() = _uiState.update { it.copy(showEditDialog = false) }
    fun onEditValueChange(v: Int) = _uiState.update { it.copy(editValue = v.coerceAtLeast(0)) }

    fun onConfirmarEdicion() {
        val v = _uiState.value.editValue
        if (v <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isGuardando = true) }
            memberEntryRepo.editEntry(entryId, v.toDouble(), actorRole = "member", actorId = session.miembroId)
                .onSuccess { _uiState.update { it.copy(isGuardando = false, showEditDialog = false) }; cargar() }
                .onFailure { e -> _uiState.update { it.copy(isGuardando = false, error = e.message) } }
        }
    }

    fun onBorrar() {
        viewModelScope.launch {
            memberEntryRepo.deleteEntry(entryId, actorRole = "member", actorId = session.miembroId)
                .onSuccess { _uiState.update { it.copy(deleted = true) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun consumeDeleted() = _uiState.update { it.copy(deleted = false) }
}
