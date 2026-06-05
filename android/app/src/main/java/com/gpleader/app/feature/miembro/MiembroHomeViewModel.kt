package com.gpleader.app.feature.miembro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.DuoRepository
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.SolicitudRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class MiembroHomeUiState(
    val miembroNombre:    String = "",
    val miembroIniciales: String = "",
    val grupoNombre:      String = "",
    val iglesiaNombre:    String = "",
    val districtNombre:   String = "",
    val campoNombre:      String = "",
    val isValidandoPerfil: Boolean = true,
    val isRefreshing:     Boolean = false,

    // Estadísticas del trimestre
    val cultosAsistidos:      Int     = 0,
    val totalCultosGP:        Int     = 0,
    val porcentajeAsistencia: Int     = 0,
    val isLoadingStats:       Boolean = false,

    // Dúo misionero
    val tieneDuo: Boolean = false,

    // Solicitud suplente
    val tieneSolicitudPendiente: Boolean = false,
    val solicitudPendienteId:    String  = "",

    // Navegación
    val navigateToLogin: Boolean = false,
)

@HiltViewModel
class MiembroHomeViewModel @Inject constructor(
    private val miembroRepo:   MiembroRepository,
    private val duoRepo:       DuoRepository,
    private val session:       SessionManager,
    private val supabase:      SupabaseClient,
    private val solicitudRepo: SolicitudRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiembroHomeUiState())
    val uiState: StateFlow<MiembroHomeUiState> = _uiState.asStateFlow()

    init {
        val nombre = session.miembroNombre
        _uiState.update {
            it.copy(
                miembroNombre    = nombre,
                miembroIniciales = calcularIniciales(nombre),
                grupoNombre    = session.grupoNombre,
                iglesiaNombre  = session.iglesiaNombre,
                districtNombre = session.districtNombre,
                campoNombre    = session.campoNombre,
            )
        }
        validarPerfilEnServidor()
        cargarEstadisticasTrimestre()
        verificarDuo()
        verificarSolicitudSuplente()
    }

    private fun verificarSolicitudSuplente() {
        viewModelScope.launch {
            runCatching { solicitudRepo.getSolicitudesAsignadas(session.miembroId) }
                .onSuccess { lista ->
                    val pendiente = lista.firstOrNull { it.status == "pending" }
                    _uiState.update {
                        it.copy(
                            tieneSolicitudPendiente = pendiente != null,
                            solicitudPendienteId    = pendiente?.id ?: "",
                        )
                    }
                }
        }
    }

    private fun verificarDuo() {
        viewModelScope.launch {
            val duo = duoRepo.getDuoPorMiembro(session.miembroId).getOrNull()
            _uiState.update { it.copy(tieneDuo = duo != null) }
        }
    }

    private fun validarPerfilEnServidor() {
        viewModelScope.launch {
            _uiState.update { it.copy(isValidandoPerfil = true) }
            val resultado = runCatching { miembroRepo.getMiembroById(session.miembroId) }
            when {
                resultado.isFailure -> {
                    _uiState.update { it.copy(isValidandoPerfil = false) }
                }
                resultado.getOrNull() == null -> {
                    session.cerrarSesionMiembro()
                    _uiState.update { it.copy(isValidandoPerfil = false, navigateToLogin = true) }
                }
                else -> {
                    _uiState.update { it.copy(isValidandoPerfil = false) }
                }
            }
        }
    }

    private fun cargarEstadisticasTrimestre() {
        val grupoId   = session.grupoId
        val miembroId = session.miembroId
        if (grupoId.isEmpty() || miembroId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true) }
            runCatching {
                val (start, end) = currentQuarterRange()

                // 1. Reuniones del GP en el trimestre
                val meetingsResp = supabase.postgrest.from("meeting")
                    .select(Columns.list("id")) {
                        filter {
                            eq("small_group_id", grupoId)
                            gte("meeting_date", start.toString())
                            lte("meeting_date", end.toString())
                            eq("registry_kind", "gp_meeting")
                        }
                    }
                val meetingIds = kotlinx.serialization.json.Json
                    .parseToJsonElement(meetingsResp.data).jsonArray
                    .mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content }
                val totalCultos = meetingIds.size

                // 2. Asistencias del miembro a esas reuniones
                val attendanceResp = supabase.postgrest.from("attendance")
                    .select(Columns.list("meeting_id")) {
                        filter {
                            eq("member_id", miembroId)
                            eq("status", "present")
                        }
                    }
                val attendedIds = kotlinx.serialization.json.Json
                    .parseToJsonElement(attendanceResp.data).jsonArray
                    .mapNotNull { it.jsonObject["meeting_id"]?.jsonPrimitive?.content }
                    .toSet()

                val asistidos  = meetingIds.count { it in attendedIds }
                val porcentaje = if (totalCultos > 0) (asistidos * 100) / totalCultos else 0

                _uiState.update {
                    it.copy(
                        cultosAsistidos      = asistidos,
                        totalCultosGP        = totalCultos,
                        porcentajeAsistencia = porcentaje,
                        isLoadingStats       = false,
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingStats = false) }
            }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            verificarDuo()
            cargarEstadisticasTrimestre()
            verificarSolicitudSuplente()
            _uiState.update { it.copy(isRefreshing = false) }
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

    private fun currentQuarterRange(): Pair<LocalDate, LocalDate> {
        val today        = LocalDate.now()
        val startMonth   = ((today.monthValue - 1) / 3) * 3 + 1
        val start        = LocalDate.of(today.year, startMonth, 1)
        val end          = start.plusMonths(3).minusDays(1)
        return start to end
    }

}
