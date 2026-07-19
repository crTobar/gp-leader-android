package com.gpleader.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.GroupLogRepository
import com.gpleader.app.core.data.repository.GrupoRepository
import com.gpleader.app.core.data.repository.MemberEntryRepository
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.ReunionRepository
import com.gpleader.app.core.data.repository.SabbathMeetingResumen
import com.gpleader.app.core.data.repository.SolicitudRepository
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

// ── Display models ────────────────────────────────────────────────────────────

data class GrupoInfo(
    val nombre:    String,
    val diaSemana: String,
    val horaInicio: String,
    val iglesia:   String,
)

enum class EstadoReunion { BORRADOR, PENDIENTE_SYNC, ENVIADA, APROBADA }

data class ReunionResumen(
    val id:        String,
    val fecha:     LocalDate,
    val estado:    EstadoReunion,
    val presentes: Int,
    val ausentes:  Int,
)

// ── UI state ──────────────────────────────────────────────────────────────────

data class HomeUiState(
    val nombreLider:          String              = "",
    val grupo:                GrupoInfo?          = null,
    val porcentajeAsistencia: Int                 = 0,
    val totalPresentes:       Int                 = 0,
    val totalAusentes:        Int                 = 0,
    val totalJustificados:    Int                 = 0,
    val totalMiembros:        Int                 = 0,
    val reunionesRecientes:        List<ReunionResumen>         = emptyList(),
    val reunionesSabadoRecientes:  List<SabbathMeetingResumen>  = emptyList(),
    val reunionGpHoy:        ReunionResumen?         = null,
    val reunionSabadoSemana: SabbathMeetingResumen?  = null,
    val isLoading:            Boolean                  = false,
    val isRefreshing:         Boolean                  = false,
    val error:                String?                  = null,
    val navigateToRegistro:   Boolean                  = false,
    val navigateToHistorial:  Boolean                  = false,
    val navigateToDetalle:    String?                  = null,
    val navigateToSabadoCulto: Boolean                 = false,
    val navigateToAprobaciones: Boolean                = false,

    // Aportes de miembros pendientes de aprobar (status="draft")
    val pendingMemberCount:   Int                      = 0,

    // ── Delegaciones activas (deputy_code con miembro asignado) ─────────────
    val delegaciones:          List<DelegacionActiva>   = emptyList(),
    val cancelandoCodeId:      String?                  = null,
    val delegacionError:       String?                  = null,
)

data class DelegacionActiva(
    val codeId:        String,
    val nombreAsignado: String,
    val expiresAt:     String?,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reunionRepo:   ReunionRepository,
    private val grupoRepo:     GrupoRepository,
    private val miembroRepo:   MiembroRepository,
    private val solicitudRepo: SolicitudRepository,
    private val groupLogRepo:  GroupLogRepository,
    private val memberEntryRepo: MemberEntryRepository,
    private val supabase:      SupabaseClient,
    private val session:       SessionManager,
    private val offlinePreloader: com.gpleader.app.core.data.sync.OfflinePreloader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Precarga el caché offline del grupo en background (sesión ya lista al llegar al Home).
        offlinePreloader.start()

        _uiState.update {
            it.copy(
                nombreLider = session.miembroNombre,
                grupo       = GrupoInfo(
                    nombre     = session.grupoNombre,
                    diaSemana  = "",
                    horaInicio = "",
                    iglesia    = "",
                ),
                isLoading = true,
            )
        }
        cargarReuniones()
        cargarSabadoRecientes()
        cargarGrupoDetalle()
        cargarTotalMiembros()
        cargarSolicitudes()
        cargarPendientes()
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            cargarReuniones()
            cargarSabadoRecientes()
            cargarGrupoDetalle()
            cargarSolicitudes()
            cargarPendientes()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun cargarPendientes() {
        viewModelScope.launch {
            // El badge cuenta las aprobaciones que verá el líder: una por (miembro, actividad),
            // igual que las tarjetas de LiderAprobacionesScreen (que agrupa los aportes draft).
            val total = memberEntryRepo.getPendingEntriesForGroup(session.grupoId)
                .getOrDefault(emptyList())
                .distinctBy { it.miembroId to it.activityTypeId }
                .size
            _uiState.update { it.copy(pendingMemberCount = total) }
        }
    }

    private fun cargarTotalMiembros() {
        viewModelScope.launch {
            miembroRepo.getMiembrosActivos(session.grupoId).collect { miembros ->
                _uiState.update { it.copy(totalMiembros = miembros.size) }
            }
        }
    }

    private fun cargarGrupoDetalle() {
        viewModelScope.launch {
            val detalle = grupoRepo.getGrupoDetalle(session.grupoId) ?: return@launch
            _uiState.update { state ->
                state.copy(
                    grupo = state.grupo?.copy(
                        diaSemana  = detalle.meetingDay,
                        horaInicio = detalle.meetingTime,
                    )
                )
            }
        }
    }

    private fun cargarSabadoRecientes() {
        viewModelScope.launch {
            val lista = runCatching {
                reunionRepo.getReunionesRecientesSabado(session.grupoId, limit = 3).getOrDefault(emptyList())
            }.getOrDefault(emptyList())
            // Ventana de la semana litúrgica: del sábado al viernes siguiente.
            // El culto de sábado vale desde ese sábado hasta el viernes a medianoche.
            // Se renueva al pasar a un nuevo sábado (medianoche viernes→sábado),
            // permitiendo registrar el culto de la semana siguiente.
            val hoy          = LocalDate.now()
            val inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))
            val finSemana    = inicioSemana.plusDays(6)   // viernes siguiente
            val registroSab  = lista.firstOrNull {
                !it.fecha.isBefore(inicioSemana) && !it.fecha.isAfter(finSemana)
            }
            _uiState.update { it.copy(reunionesSabadoRecientes = lista, reunionSabadoSemana = registroSab) }
        }
    }

    private fun cargarReuniones() {
        viewModelScope.launch {
            // getReuniones es un flow de una sola emisión (consulta puntual a Supabase);
            // tomamos firstOrNull y lo recargamos manualmente al volver al Home.
            val reuniones = runCatching {
                reunionRepo.getReuniones(grupoId = session.grupoId, limit = 3).firstOrNull()
            }.getOrNull() ?: emptyList()

            val totalP = reuniones.sumOf { it.presentes }
            val totalA = reuniones.sumOf { it.ausentes }
            val totalJ = reuniones.sumOf { it.justificados }
            val totalAsistentes = totalP + totalA + totalJ
            val pct = if (totalAsistentes > 0) (totalP * 100) / totalAsistentes else 0

            val hoy = LocalDate.now()
            val mapped = reuniones.map { r ->
                ReunionResumen(
                    id        = r.id,
                    fecha     = r.fecha,
                    estado    = runCatching { EstadoReunion.valueOf(r.estado) }
                        .getOrElse { EstadoReunion.BORRADOR },
                    presentes = r.presentes,
                    ausentes  = r.ausentes,
                )
            }
            _uiState.update {
                it.copy(
                    isLoading            = false,
                    reunionesRecientes   = mapped,
                    reunionGpHoy         = mapped.firstOrNull { r -> r.fecha == hoy },
                    totalPresentes       = totalP,
                    totalAusentes        = totalA,
                    totalJustificados    = totalJ,
                    porcentajeAsistencia = pct,
                )
            }
        }
    }

    fun onRegistrarClick() {
        _uiState.update { it.copy(navigateToRegistro = true) }
    }

    fun onVerTodasClick() {
        _uiState.update { it.copy(navigateToHistorial = true) }
    }

    fun onReunionClick(reunionId: String) {
        _uiState.update { it.copy(navigateToDetalle = reunionId) }
    }

    fun consumeRegistroNavigation() {
        _uiState.update { it.copy(navigateToRegistro = false) }
    }

    fun consumeHistorialNavigation() {
        _uiState.update { it.copy(navigateToHistorial = false) }
    }

    fun consumeDetalleNavigation() {
        _uiState.update { it.copy(navigateToDetalle = null) }
    }

    /** Recarga GP de hoy + culto de sábado al volver al Home. */
    fun reloadHome() {
        cargarReuniones()
        cargarSabadoRecientes()
        cargarPendientes()
    }

    fun onAprobacionesClick() {
        _uiState.update { it.copy(navigateToAprobaciones = true) }
    }

    fun consumeAprobacionesNavigation() {
        _uiState.update { it.copy(navigateToAprobaciones = false) }
    }

    fun onSabadoCultoClick() {
        _uiState.update { it.copy(navigateToSabadoCulto = true) }
    }

    fun consumeSabadoCultoNavigation() {
        _uiState.update { it.copy(navigateToSabadoCulto = false) }
    }

    // ── Delegaciones (deputy_code con miembro asignado) ──────────────────────

    fun cargarSolicitudes() {
        viewModelScope.launch {
            runCatching {
                val resp = supabase.from("deputy_code").select(
                    columns = Columns.raw("id, expires_at, assigned_member_id, member!deputy_code_assigned_member_id_fkey(first_name, last_name)")
                ) {
                    filter {
                        eq("small_group_id", session.grupoId)
                        gt("expires_at", Instant.now().toString())
                    }
                }.data
                Json.parseToJsonElement(resp).jsonArray.mapNotNull { el ->
                    val obj        = el.jsonObject
                    val id         = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                    val assignedId = obj["assigned_member_id"]?.jsonPrimitive?.contentOrNull
                    if (assignedId.isNullOrBlank()) return@mapNotNull null  // solo códigos asignados
                    val usedAt     = obj["used_at"]?.jsonPrimitive?.contentOrNull
                    if (!usedAt.isNullOrBlank()) return@mapNotNull null     // no usados
                    val exp        = obj["expires_at"]?.jsonPrimitive?.contentOrNull
                    val member     = obj["member"]?.jsonObject
                    val nombre     = listOfNotNull(
                        member?.get("first_name")?.jsonPrimitive?.contentOrNull,
                        member?.get("last_name")?.jsonPrimitive?.contentOrNull,
                    ).joinToString(" ")
                    DelegacionActiva(codeId = id, nombreAsignado = nombre, expiresAt = exp)
                }
            }.onSuccess { lista ->
                _uiState.update { it.copy(delegaciones = lista) }
            }
        }
    }

    fun onCancelarSolicitud(codeId: String) {
        _uiState.update { it.copy(cancelandoCodeId = codeId, delegacionError = null) }
        viewModelScope.launch {
            runCatching {
                solicitudRepo.revokeDeputyCode(session.grupoId)
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        cancelandoCodeId = null,
                        delegaciones     = state.delegaciones.filter { it.codeId != codeId },
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(cancelandoCodeId = null, delegacionError = "No se pudo cancelar. Intentá de nuevo.") }
            }
        }
    }
}
