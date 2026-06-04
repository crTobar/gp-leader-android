package com.gpleader.app.feature.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.DuoRepository
import com.gpleader.app.core.data.repository.MiembroData
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CrearDuoUiState(
    val isLoading:  Boolean = true,
    val miembros:   List<MiembroData> = emptyList(),
    val member1Id:  String? = null,
    val member2Id:  String? = null,
    val isGuardando: Boolean = false,
    val savedOk:    Boolean = false,
    val error:      String? = null,
)

@HiltViewModel
class CrearDuoViewModel @Inject constructor(
    private val duoRepo:    DuoRepository,
    private val miembroRepo: MiembroRepository,
    private val session:    SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearDuoUiState())
    val uiState: StateFlow<CrearDuoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val duosActivos = duoRepo.getDuosByGrupo(session.grupoId).getOrDefault(emptyList())
            val idsBloqueados = duosActivos.flatMap { listOf(it.member1.id, it.member2.id) }.toSet()
            val todos = miembroRepo.getMiembrosActivos(session.grupoId).first()
            _uiState.value = CrearDuoUiState(
                isLoading = false,
                miembros  = todos.filter { it.id !in idsBloqueados },
            )
        }
    }

    fun onToggleMiembro(id: String) {
        val s = _uiState.value
        _uiState.value = when {
            s.member1Id == id -> s.copy(member1Id = null)
            s.member2Id == id -> s.copy(member2Id = null)
            s.member1Id == null -> s.copy(member1Id = id)
            s.member2Id == null -> s.copy(member2Id = id)
            else -> s.copy(error = "Ya seleccionaste 2 miembros")
        }
    }

    fun onCrear() {
        val s = _uiState.value
        val m1 = s.member1Id ?: return
        val m2 = s.member2Id ?: return
        viewModelScope.launch {
            _uiState.value = s.copy(isGuardando = true, error = null)
            duoRepo.crearDuo(session.grupoId, m1, m2)
                .onSuccess { _uiState.value = _uiState.value.copy(isGuardando = false, savedOk = true) }
                .onFailure { _uiState.value = _uiState.value.copy(isGuardando = false, error = it.message) }
        }
    }
}
