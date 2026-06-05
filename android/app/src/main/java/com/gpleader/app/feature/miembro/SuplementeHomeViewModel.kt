package com.gpleader.app.feature.miembro

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SuplementeHomeUiState(
    val grupoNombre:  String = "",
    val deputyCodeId: String = "",
)

@HiltViewModel
class SuplementeHomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SuplementeHomeUiState(
            grupoNombre  = session.grupoNombre,
            deputyCodeId = savedStateHandle.get<String>("deputyCodeId") ?: "",
        )
    )
    val uiState: StateFlow<SuplementeHomeUiState> = _uiState.asStateFlow()
}
