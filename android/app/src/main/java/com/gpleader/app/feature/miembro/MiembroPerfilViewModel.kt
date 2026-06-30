package com.gpleader.app.feature.miembro

import androidx.lifecycle.ViewModel
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MiembroPerfilUiState(
    val miembroNombre:    String  = "",
    val miembroIniciales: String  = "",
    val grupoNombre:      String  = "",
    val iglesiaNombre:    String  = "",
    val districtNombre:   String  = "",
    val campoNombre:      String  = "",
    val navigateToLogin:  Boolean = false,
)

@HiltViewModel
class MiembroPerfilViewModel @Inject constructor(
    private val session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiembroPerfilUiState())
    val uiState: StateFlow<MiembroPerfilUiState> = _uiState.asStateFlow()

    init {
        val nombre = session.miembroNombre
        _uiState.update {
            it.copy(
                miembroNombre    = nombre,
                miembroIniciales = calcularIniciales(nombre),
                grupoNombre      = session.grupoNombre,
                iglesiaNombre    = session.iglesiaNombre,
                districtNombre   = session.districtNombre,
                campoNombre      = session.campoNombre,
            )
        }
    }

    fun onCerrarSesion() {
        session.cerrarSesionMiembro()
        _uiState.update { it.copy(navigateToLogin = true) }
    }

    fun consumeLoginNavigation() {
        _uiState.update { it.copy(navigateToLogin = false) }
    }

    private fun calcularIniciales(nombre: String): String {
        val partes = nombre.trim().split(" ")
        return when {
            partes.size >= 2 -> "${partes[0].firstOrNull() ?: ""}${partes[1].firstOrNull() ?: ""}".uppercase()
            partes.size == 1 -> partes[0].take(2).uppercase()
            else             -> "??"
        }
    }
}
