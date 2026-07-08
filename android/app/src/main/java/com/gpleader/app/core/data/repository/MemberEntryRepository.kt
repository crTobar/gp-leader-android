package com.gpleader.app.core.data.repository

import java.time.Instant

/** Un aporte individual de un miembro (monetario o contador). */
data class MemberEntry(
    val id:         String,
    val value:      Double,
    val enteredAt:  Instant?,
    val status:     String,   // draft | approved | pending_board | rejected
    val approvedAt: Instant? = null,
)

/** Filtro de trimestre para el historial de aportes aprobados. */
enum class HistFiltroTrimestre { ACTUAL, ANTERIORES, TODOS }

/** Una actividad con su total aprobado, para el historial. */
data class HistActividad(
    val activityId:    String,
    val nombre:        String,
    val markerType:    String,
    val unitLabel:     String,
    val totalAprobado: Double,
)

/** Un miembro con su total aprobado en una actividad. */
data class HistMiembro(
    val miembroId:     String,
    val nombre:        String,
    val totalAprobado: Double,
    val count:         Int,
)

/** Un lote de aportes aprobados juntos (una "aprobación"). */
data class HistAprobacion(
    val approvedAtKey: String,          // clave de agrupación (approved_at o entered_at fallback)
    val fecha:         Instant?,
    val total:         Double,
    val count:         Int,
    val aportes:       List<MemberEntry>,
)

/** Una acción registrada sobre un aporte (auditoría). */
data class MemberEntryEvent(
    val id:        String,
    val action:    String,   // created | edited | deleted | approved | rejected | board_approved
    val oldValue:  Double?,
    val newValue:  Double?,
    val actorRole: String,   // member | leader | church
    val actorName: String?,  // nombre del miembro/líder que hizo la acción (null para iglesia)
    val note:      String?,
    val createdAt: Instant?,
)

/** Agregado por miembro para la pantalla de actividad del líder. */
data class MemberEntryAggregate(
    val miembroId:     String,
    val miembroNombre: String,
    val total:         Double,   // suma no rechazada
    val count:         Int,
)

/** Resumen de aportes de una actividad para el líder (dos totales + por miembro). */
data class ActivityEntrySummary(
    val approvedTotal: Double,   // approved + pending_board
    val pendingTotal:  Double,   // draft
    val pendingCount:  Int,
    val perMember:     List<MemberEntryAggregate>,
)

/** Aporte pendiente, enriquecido para las pantallas de aprobación. */
data class MemberPendingEntry(
    val entryId:         String,
    val miembroId:       String,
    val miembroNombre:   String,
    val activityTypeId:  String,
    val actividadNombre: String,
    val markerType:      String,
    val unitLabel:       String,
    val value:           Double,
    val enteredAt:       Instant?,
    val grupoNombre:     String = "",
)

interface MemberEntryRepository {
    // ── Miembro ───────────────────────────────────────────────────────────────
    suspend fun getEntries(miembroId: String, actividadTipoId: String): Result<List<MemberEntry>>
    suspend fun getEntry(entryId: String): Result<MemberEntry?>
    suspend fun getEntryTotal(miembroId: String, actividadTipoId: String): Result<Double>
    suspend fun getEntryEvents(entryId: String): Result<List<MemberEntryEvent>>
    suspend fun addEntry(
        miembroId: String,
        actividadTipoId: String,
        grupoId: String,
        value: Double,
        status: String = "draft",
        actorRole: String = "member",
        actorId: String? = null,
    ): Result<Unit>
    suspend fun editEntry(entryId: String, newValue: Double, actorRole: String, actorId: String?): Result<Unit>
    suspend fun deleteEntry(entryId: String, actorRole: String, actorId: String?): Result<Unit>
    /** Aprueba de una sola vez la suma de los aportes `draft` de un miembro en una actividad. */
    suspend fun approveMemberSum(miembroId: String, actividadTipoId: String, actorId: String?): Result<Unit>

    // ── Historial (líder scope="gp" grupoId · iglesia scope="church" iglesiaId) ──
    suspend fun getHistorialActividades(scopeLevel: String, scopeId: String, filtro: HistFiltroTrimestre): Result<List<HistActividad>>
    suspend fun getHistorialMiembros(scopeLevel: String, scopeId: String, activityId: String, filtro: HistFiltroTrimestre): Result<List<HistMiembro>>
    suspend fun getHistorialMiembroAprobaciones(miembroId: String, activityId: String, filtro: HistFiltroTrimestre): Result<List<HistAprobacion>>

    // ── Líder ─────────────────────────────────────────────────────────────────
    suspend fun getActivityMemberSummary(grupoId: String, actividadTipoId: String): Result<ActivityEntrySummary>
    suspend fun getPendingEntriesForGroup(grupoId: String): Result<List<MemberPendingEntry>>
    suspend fun getPendingEntriesForActivity(grupoId: String, actividadTipoId: String): Result<List<MemberPendingEntry>>
    suspend fun getPendingEntriesCount(grupoId: String): Result<Int>
    suspend fun approveEntry(entryId: String, correctedValue: Double?, isMonetary: Boolean, actorId: String?): Result<Unit>
    suspend fun rejectEntry(entryId: String, actorId: String?, note: String? = null): Result<Unit>

    // ── Iglesia ───────────────────────────────────────────────────────────────
    suspend fun getPendingBoardEntries(iglesiaId: String): Result<List<MemberPendingEntry>>
    suspend fun getPendingBoardCount(iglesiaId: String): Result<Int>
    suspend fun approveBoardEntry(entryId: String, actorId: String?): Result<Unit>
    suspend fun rejectBoardEntry(entryId: String, actorId: String?, note: String? = null): Result<Unit>
}
