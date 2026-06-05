package com.gpleader.app.feature.miembro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject

data class SuplementeCodigoEntryUiState(
    val digitos:      List<String> = List(6) { "" },
    val isValidando:  Boolean      = false,
    val error:        String?      = null,
    val errorTrigger: Int          = 0,
    val deputyCodeId: String       = "",
)

@HiltViewModel
class SuplementeCodigoEntryViewModel @Inject constructor(
    private val supabase: SupabaseClient,
    private val session:  SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuplementeCodigoEntryUiState())
    val uiState: StateFlow<SuplementeCodigoEntryUiState> = _uiState.asStateFlow()

    fun onDigito(d: String) {
        val current = _uiState.value.digitos
        val idx = current.indexOfFirst { it.isEmpty() }
        if (idx == -1) return
        _uiState.update { it.copy(digitos = current.toMutableList().also { l -> l[idx] = d }, error = null) }
        if (idx == 5) onConfirmar()
    }

    fun onBorrar() {
        val current = _uiState.value.digitos
        val idx = (current.indexOfLast { it.isNotEmpty() }).coerceAtLeast(0)
        _uiState.update { it.copy(digitos = current.toMutableList().also { l -> l[idx] = "" }, error = null) }
    }

    fun onConfirmar() {
        val code = _uiState.value.digitos.joinToString("")
        if (code.length < 6) return
        viewModelScope.launch {
            _uiState.update { it.copy(isValidando = true, error = null) }
            runCatching {
                val resp = supabase.postgrest.rpc("validate_deputy_code", buildJsonObject {
                    put("p_small_group_id", session.grupoId)
                    put("p_code",           code)
                })
                val arr = Json.parseToJsonElement(resp.data).jsonArray
                if (arr.isEmpty()) error("Código inválido o vencido")
                arr.first().jsonObject["deputy_code_id"]?.jsonPrimitive?.contentOrNull ?: error("Código inválido")
            }.onSuccess { id ->
                _uiState.update { it.copy(isValidando = false, deputyCodeId = id) }
            }.onFailure { e ->
                val msg = when {
                    e.message?.contains("inválido") == true || e.message?.contains("invalid") == true -> "Código incorrecto o vencido"
                    else -> "Código incorrecto o vencido"
                }
                _uiState.update { it.copy(isValidando = false, error = msg, errorTrigger = it.errorTrigger + 1, digitos = List(6) { "" }) }
            }
        }
    }
}
