package com.gpleader.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Modelos ───────────────────────────────────────────────────────────────────

data class GrupoInfoSuplente(
    val nombre:      String,
    val diaSemana:   String,
    val horaInicio:  String,
    val iglesia:     String,
    val liderNombre: String,
)

// ── UI State ──────────────────────────────────────────────────────────────────

data class SuplementeUiState(
    // ── Entrada de código (SuplementeCodigoScreen) ──────────────────────────
    val codigo:       String  = "",
    val isValidating: Boolean = false,
    val codigoError:  String? = null,
    val codigoValido: Boolean = false,

    // ── Bienvenida (SuplementeBienvenidaScreen) ──────────────────────────────
    val nombreSuplente: String             = "",
    val grupoInfo:      GrupoInfoSuplente? = null,

    // ── Navegación ───────────────────────────────────────────────────────────
    val navigateToBienvenida: Boolean = false,
    val navigateToRegistro:   Boolean = false,

    // ── Generación de código (SheetGenerarCodigoSuplente — líder) ────────────
    val codigoGenerado:    String  = "",
    val codigoExpiraEn:    Int     = 1440,  // minutos restantes (24h)
    val isGenerating:      Boolean = false,
    val showRevocarDialog: Boolean = false,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class SuplementeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SuplementeUiState())
    val uiState: StateFlow<SuplementeUiState> = _uiState.asStateFlow()

    init {
        // Genera un código de suplente al crear el ViewModel (para el líder)
        generarCodigoInterno()
    }

    // ── Código screen ─────────────────────────────────────────────────────────

    fun onCodigoChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(codigo = value, codigoError = null) }
            if (value.length == 6) validarCodigo(value)
        }
    }

    private fun validarCodigo(codigo: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isValidating = true, codigoError = null) }
            delay(1200) // simula llamada de red
            if (codigo == "123456") {
                _uiState.update {
                    it.copy(
                        isValidating      = false,
                        codigoValido      = true,
                        grupoInfo         = GrupoInfoSuplente(
                            nombre       = "GP Los Olivos",
                            diaSemana    = "Miércoles",
                            horaInicio   = "7:00 PM",
                            iglesia      = "Iglesia Central",
                            liderNombre  = "Maria Garcia",
                        ),
                        navigateToBienvenida = true,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        codigoError  = "Código inválido o expirado.",
                        codigo       = "",
                    )
                }
            }
        }
    }

    fun consumeBienvenidaNavigation() {
        _uiState.update { it.copy(navigateToBienvenida = false) }
    }

    // ── Bienvenida screen ─────────────────────────────────────────────────────

    fun onNombreSuplementeChange(value: String) {
        _uiState.update { it.copy(nombreSuplente = value) }
    }

    fun onComenzarRegistro() {
        if (_uiState.value.nombreSuplente.isBlank()) return
        _uiState.update { it.copy(navigateToRegistro = true) }
    }

    fun consumeRegistroNavigation() {
        _uiState.update { it.copy(navigateToRegistro = false) }
    }

    // ── Sheet generar código (líder) ──────────────────────────────────────────

    fun onGenerarCodigo() {
        generarCodigoInterno()
    }

    private fun generarCodigoInterno() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            delay(600) // simula llamada de red
            val nuevo = (100000..999999).random().toString()
            _uiState.update {
                it.copy(
                    isGenerating   = false,
                    codigoGenerado = nuevo,
                    codigoExpiraEn = 1440,
                )
            }
        }
    }

    fun onShowRevocarDialog() {
        _uiState.update { it.copy(showRevocarDialog = true) }
    }

    fun onDismissRevocarDialog() {
        _uiState.update { it.copy(showRevocarDialog = false) }
    }

    fun onRevocarCodigoConfirm() {
        _uiState.update {
            it.copy(
                showRevocarDialog = false,
                codigoGenerado    = "",
                codigoExpiraEn    = 0,
            )
        }
    }
}
