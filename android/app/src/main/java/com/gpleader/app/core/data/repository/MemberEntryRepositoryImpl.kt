package com.gpleader.app.core.data.repository

import com.gpleader.app.core.data.local.room.dao.ActivityTypeDao
import com.gpleader.app.core.data.local.room.dao.MemberDao
import com.gpleader.app.core.data.local.room.dao.MemberEntryDao
import com.gpleader.app.core.data.local.room.dao.MemberEntryEventDao
import com.gpleader.app.core.data.local.room.dao.QuarterDao
import com.gpleader.app.core.data.local.room.dao.SmallGroupDao
import com.gpleader.app.core.data.local.room.entity.MemberEntryEntity
import com.gpleader.app.core.data.local.room.entity.MemberEntryEventEntity
import com.gpleader.app.core.data.local.room.entity.QuarterEntity
import com.gpleader.app.core.data.network.NetworkMonitor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.OffsetDateTime
import javax.inject.Inject

class MemberEntryRepositoryImpl @Inject constructor(
    private val supabase:            SupabaseClient,
    private val network:             NetworkMonitor,
    private val memberEntryDao:      MemberEntryDao,
    private val memberEntryEventDao: MemberEntryEventDao,
    private val memberDao:           MemberDao,
    private val activityTypeDao:     ActivityTypeDao,
    private val smallGroupDao:       SmallGroupDao,
    private val quarterDao:          QuarterDao,
) : MemberEntryRepository {

    // ── Miembro ────────────────────────────────────────────────────────────────

    override suspend fun getEntries(miembroId: String, actividadTipoId: String): Result<List<MemberEntry>> = offlineSafe(emptyList()) {
        val data = supabase.from("member_entry").select(
            Columns.raw("id, value, entered_at, status, is_adjustment")
        ) {
            filter {
                eq("member_id", miembroId)
                eq("activity_type_id", actividadTipoId)
                eq("is_deleted", false)
            }
        }.data
        Json.parseToJsonElement(data).jsonArray.mapNotNull { it.jsonObject.toMemberEntry() }
            .sortedByDescending { it.enteredAt ?: Instant.EPOCH }
    }

    override suspend fun getEntry(entryId: String): Result<MemberEntry?> = offlineSafe(null) {
        val data = supabase.from("member_entry").select(Columns.raw("id, value, entered_at, status, approved_at")) {
            filter { eq("id", entryId) }
            limit(1)
        }.data
        Json.parseToJsonElement(data).jsonArray.firstOrNull()?.jsonObject?.toMemberEntry()
    }

    override suspend fun getEntryTotal(miembroId: String, actividadTipoId: String): Result<Double> = offlineSafe(0.0) {
        val data = supabase.from("member_entry").select(Columns.raw("value, status")) {
            filter {
                eq("member_id", miembroId)
                eq("activity_type_id", actividadTipoId)
                eq("is_deleted", false)
                neq("status", "rejected")
            }
        }.data
        Json.parseToJsonElement(data).jsonArray.sumOf {
            it.jsonObject["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        }
    }

    override suspend fun getEntryEvents(entryId: String): Result<List<MemberEntryEvent>> = offlineSafe(emptyList()) {
        val data = supabase.from("member_entry_event").select(
            Columns.raw("id, action, old_value, new_value, actor_role, actor_id, note, created_at")
        ) {
            filter { eq("entry_id", entryId) }
        }.data

        // actor_id no tiene FK a member (y es null para iglesia) → resolvemos nombres aparte.
        data class RawEvent(
            val id: String, val action: String, val old: Double?, val new: Double?,
            val role: String, val actorId: String?, val note: String?, val createdAt: Instant?,
        )
        val raws = Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj = elem.jsonObject
            RawEvent(
                id        = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                action    = obj["action"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                old       = obj["old_value"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.doubleOrNull,
                new       = obj["new_value"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.doubleOrNull,
                role      = obj["actor_role"]?.jsonPrimitive?.contentOrNull ?: "member",
                actorId   = obj["actor_id"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
                note      = obj["note"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
                createdAt = obj["created_at"]?.jsonPrimitive?.contentOrNull?.let(::parseInstant),
            )
        }

        val nombres = resolveMemberNames(raws.mapNotNull { it.actorId }.distinct())

        raws.map { r ->
            MemberEntryEvent(
                id        = r.id,
                action    = r.action,
                oldValue  = r.old,
                newValue  = r.new,
                actorRole = r.role,
                actorName = r.actorId?.let { nombres[it] },
                note      = r.note,
                createdAt = r.createdAt,
            )
        }.sortedByDescending { it.createdAt ?: Instant.EPOCH }
    }

    /** Resuelve id → "Nombre Apellido" para los actores (miembro/líder). */
    private suspend fun resolveMemberNames(ids: List<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        return runCatching {
            val data = supabase.from("member").select(Columns.raw("id, first_name, last_name")) {
                filter { isIn("id", ids) }
            }.data
            Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
                val obj    = elem.jsonObject
                val id     = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val nombre = "${obj["first_name"]?.jsonPrimitive?.contentOrNull ?: ""} ${obj["last_name"]?.jsonPrimitive?.contentOrNull ?: ""}".trim()
                nombre.ifBlank { null }?.let { id to it }
            }.toMap()
        }.getOrDefault(emptyMap())
    }

    /** Lee online (y cachea); si no hay red o la llamada falla, cae al caché de Room. */
    private suspend fun <T> cachedRead(offline: suspend () -> T, online: suspend () -> T): T {
        if (!network.isOnline()) return offline()
        return try { online() }
        catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) { offline() }
    }

    /** Degradación sin error: offline (o fallo de red) devuelve `fallback` en Result (lecturas sin caché real). */
    private suspend fun <T> offlineSafe(fallback: T, block: suspend () -> T): Result<T> =
        runCatching { block() }.recoverCatching { if (!network.isOnline()) fallback else throw it }

    override suspend fun getMovimientosGrupo(grupoId: String): Result<List<MovimientoAprobacion>> = runCatching {
        cachedRead(offline = { movimientosDesdeRoom(grupoId) }, online = { getMovimientosOnline(grupoId) })
    }

    private suspend fun getMovimientosOnline(grupoId: String): List<MovimientoAprobacion> {
        val data = supabase.from("member_entry_event").select(
            Columns.raw(
                "id, action, old_value, new_value, actor_role, actor_id, note, created_at, " +
                    "member_entry!inner(small_group_id, value, member!inner(first_name, last_name), " +
                    "activity_type!inner(name, marker_type, unit_label))"
            )
        ) {
            filter { eq("member_entry.small_group_id", grupoId) }
        }.data

        data class RawMov(
            val id: String, val action: String, val old: Double?, val new: Double?, val entry: Double?,
            val role: String, val actorId: String?, val note: String?, val createdAt: Instant?,
            val miembro: String, val actividad: String, val marker: String, val unit: String,
        )
        val raws = Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj   = elem.jsonObject
            val entry = obj["member_entry"]?.takeIf { it !is JsonNull }?.jsonObject ?: return@mapNotNull null
            val mem   = entry["member"]?.takeIf { it !is JsonNull }?.jsonObject
            val act   = entry["activity_type"]?.takeIf { it !is JsonNull }?.jsonObject
            val nombre = "${mem?.get("first_name")?.jsonPrimitive?.contentOrNull ?: ""} ${mem?.get("last_name")?.jsonPrimitive?.contentOrNull ?: ""}".trim()
            RawMov(
                id        = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                action    = obj["action"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                old       = obj["old_value"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.doubleOrNull,
                new       = obj["new_value"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.doubleOrNull,
                entry     = entry["value"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.doubleOrNull,
                role      = obj["actor_role"]?.jsonPrimitive?.contentOrNull ?: "member",
                actorId   = obj["actor_id"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
                note      = obj["note"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
                createdAt = obj["created_at"]?.jsonPrimitive?.contentOrNull?.let(::parseInstant),
                miembro   = nombre,
                actividad = act?.get("name")?.jsonPrimitive?.contentOrNull ?: "",
                marker    = act?.get("marker_type")?.jsonPrimitive?.contentOrNull ?: "counter",
                unit      = act?.get("unit_label")?.jsonPrimitive?.contentOrNull ?: "",
            )
        }
        val nombres = resolveMemberNames(raws.mapNotNull { it.actorId }.distinct())
        val movimientos = raws.map { r ->
            MovimientoAprobacion(
                id = r.id, action = r.action, actorRole = r.role,
                actorName = r.actorId?.let { nombres[it] },
                oldValue = r.old, newValue = r.new, entryValue = r.entry, note = r.note, createdAt = r.createdAt,
                miembroNombre = r.miembro, actividadNombre = r.actividad,
                markerType = r.marker, unitLabel = r.unit,
            )
        }.sortedByDescending { it.createdAt ?: Instant.EPOCH }
        cacheMovimientos(grupoId, movimientos)
        return movimientos
    }

    // ── Caché offline (Room) ─────────────────────────────────────────────────────

    private suspend fun cacheMovimientos(grupoId: String, movs: List<MovimientoAprobacion>) {
        val rows = movs.map { m ->
            MemberEntryEventEntity(
                id = m.id, entryId = "", smallGroupId = grupoId,
                action = m.action, oldValue = m.oldValue, newValue = m.newValue, entryValue = m.entryValue,
                actorRole = m.actorRole, actorId = null, actorName = m.actorName,
                note = m.note, createdAt = m.createdAt?.toString() ?: "",
                miembroNombre = m.miembroNombre, actividadNombre = m.actividadNombre,
                markerType = m.markerType, unitLabel = m.unitLabel,
            )
        }
        if (rows.isNotEmpty()) memberEntryEventDao.upsert(rows)
    }

    private suspend fun movimientosDesdeRoom(grupoId: String): List<MovimientoAprobacion> =
        memberEntryEventDao.getByGroup(grupoId).map { e ->
            MovimientoAprobacion(
                id = e.id, action = e.action, actorRole = e.actorRole, actorName = e.actorName,
                oldValue = e.oldValue, newValue = e.newValue, entryValue = e.entryValue, note = e.note,
                createdAt = e.createdAt.takeIf { it.isNotBlank() }?.let(::parseInstant),
                miembroNombre = e.miembroNombre, actividadNombre = e.actividadNombre,
                markerType = e.markerType, unitLabel = e.unitLabel,
            )
        }

    override suspend fun addEntry(
        miembroId: String,
        actividadTipoId: String,
        grupoId: String,
        value: Double,
        status: String,
        actorRole: String,
        actorId: String?,
    ): Result<Unit> = runCatching {
        val inserted = supabase.from("member_entry").insert(
            buildJsonObject {
                put("member_id", miembroId)
                put("activity_type_id", actividadTipoId)
                put("small_group_id", grupoId)
                put("value", value)
                put("status", status)
                if (status == "approved" || status == "pending_board") {
                    put("approved_by", actorId)
                    put("approved_at", Instant.now().toString())
                }
            }
        ) { select(Columns.raw("id")) }.data
        val entryId = Json.parseToJsonElement(inserted).jsonArray.firstOrNull()
            ?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull ?: return@runCatching
        logEvent(entryId, action = "created", oldValue = null, newValue = value, actorRole = actorRole, actorId = actorId ?: miembroId)
        // Si el líder lo agrega ya aprobado, dejar constancia de la aprobación.
        if (status != "draft") {
            logEvent(entryId, action = "approved", oldValue = null, newValue = null, actorRole = actorRole, actorId = actorId)
        }
    }

    override suspend fun editEntry(
        entryId: String,
        newValue: Double,
        actorRole: String,
        actorId: String?,
    ): Result<Unit> = runCatching {
        val old = currentValue(entryId)
        supabase.from("member_entry").update(
            buildJsonObject {
                put("value", newValue)
                put("updated_at", Instant.now().toString())
            }
        ) { filter { eq("id", entryId) } }
        logEvent(entryId, action = "edited", oldValue = old, newValue = newValue, actorRole = actorRole, actorId = actorId)
    }

    override suspend fun deleteEntry(entryId: String, actorRole: String, actorId: String?): Result<Unit> = runCatching {
        val old = currentValue(entryId)
        // Soft-delete: conservamos la fila y su auditoría (cualquier acción queda reportada).
        supabase.from("member_entry").update(
            buildJsonObject { put("is_deleted", true); put("updated_at", Instant.now().toString()) }
        ) { filter { eq("id", entryId) } }
        logEvent(entryId, action = "deleted", oldValue = old, newValue = null, actorRole = actorRole, actorId = actorId)
    }

    override suspend fun approveMemberSum(
        miembroId: String,
        actividadTipoId: String,
        actorId: String?,
    ): Result<Unit> = runCatching {
        // Ids de los aportes draft del miembro para esta actividad.
        val data = supabase.from("member_entry").select(Columns.raw("id")) {
            filter {
                eq("member_id", miembroId)
                eq("activity_type_id", actividadTipoId)
                eq("status", "draft")
                eq("is_deleted", false)
            }
        }.data
        val ids = Json.parseToJsonElement(data).jsonArray.mapNotNull {
            it.jsonObject["id"]?.jsonPrimitive?.contentOrNull
        }
        if (ids.isEmpty()) return@runCatching

        supabase.from("member_entry").update(
            buildJsonObject {
                put("status", "approved")
                put("approved_by", actorId)
                put("approved_at", Instant.now().toString())
                put("updated_at", Instant.now().toString())
            }
        ) {
            filter { isIn("id", ids) }
        }
        ids.forEach { logEvent(it, action = "approved", oldValue = null, newValue = null, actorRole = "leader", actorId = actorId) }
    }

    override suspend fun setMemberDraftTotal(
        miembroId: String,
        actividadTipoId: String,
        grupoId: String,
        newTotal: Double,
        actorId: String?,
    ): Result<Unit> = runCatching {
        // Todos los aportes draft (no borrados) del miembro para esta actividad.
        val data = supabase.from("member_entry").select(Columns.raw("id, value, is_adjustment")) {
            filter {
                eq("member_id", miembroId)
                eq("activity_type_id", actividadTipoId)
                eq("status", "draft")
                eq("is_deleted", false)
            }
        }.data
        val rows = Json.parseToJsonElement(data).jsonArray.map { it.jsonObject }
        val ajuste = rows.firstOrNull { it["is_adjustment"]?.jsonPrimitive?.booleanOrNull == true }
        val ajusteId = ajuste?.get("id")?.jsonPrimitive?.contentOrNull
        // Suma solo de los aportes reales del miembro (excluye la línea de ajuste).
        val sumaMiembro = rows.filter { it["is_adjustment"]?.jsonPrimitive?.booleanOrNull != true }
            .sumOf { it["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0 }
        val totalAnterior = sumaMiembro + (ajuste?.get("value")?.jsonPrimitive?.doubleOrNull ?: 0.0)
        if (newTotal == totalAnterior) return@runCatching   // sin cambios

        val delta = newTotal - sumaMiembro
        val now = Instant.now().toString()
        // El evento describe el cambio del TOTAL (no del delta) → bitácora legible.
        when {
            // El nuevo total coincide con la suma real → sobra el ajuste; se soft-borra.
            delta == 0.0 && ajusteId != null -> {
                supabase.from("member_entry").update(
                    buildJsonObject { put("is_deleted", true); put("updated_at", now) }
                ) { filter { eq("id", ajusteId) } }
                logEvent(ajusteId, action = "edited", oldValue = totalAnterior, newValue = newTotal, actorRole = "leader", actorId = actorId)
            }
            delta == 0.0 -> Unit
            // Ya existe una línea de ajuste → actualiza su valor al nuevo delta.
            ajusteId != null -> {
                supabase.from("member_entry").update(
                    buildJsonObject { put("value", delta); put("is_deleted", false); put("updated_at", now) }
                ) { filter { eq("id", ajusteId) } }
                logEvent(ajusteId, action = "edited", oldValue = totalAnterior, newValue = newTotal, actorRole = "leader", actorId = actorId)
            }
            // No existe → inserta la línea "Ajuste del líder".
            else -> {
                val inserted = supabase.from("member_entry").insert(
                    buildJsonObject {
                        put("member_id", miembroId)
                        put("activity_type_id", actividadTipoId)
                        put("small_group_id", grupoId)
                        put("value", delta)
                        put("status", "draft")
                        put("is_adjustment", true)
                    }
                ) { select(Columns.raw("id")) }.data
                val newId = Json.parseToJsonElement(inserted).jsonArray.firstOrNull()
                    ?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
                if (newId != null) {
                    logEvent(newId, action = "edited", oldValue = totalAnterior, newValue = newTotal, actorRole = "leader", actorId = actorId)
                }
            }
        }
    }

    // ── Líder ────────────────────────────────────────────────────────────────

    override suspend fun getActivityMemberSummary(grupoId: String, actividadTipoId: String): Result<ActivityEntrySummary> = offlineSafe(ActivityEntrySummary(0.0, 0.0, 0, emptyList())) {
        val data = supabase.from("member_entry").select(
            Columns.raw("id, member_id, value, status, member!inner(first_name, last_name)")
        ) {
            filter {
                eq("small_group_id", grupoId)
                eq("activity_type_id", actividadTipoId)
                eq("is_deleted", false)
            }
        }.data

        var approvedTotal = 0.0
        var pendingTotal  = 0.0
        var pendingCount  = 0
        // miembroId → (nombre, totalNoRechazado, count)
        val agg = LinkedHashMap<String, Triple<String, Double, Int>>()

        Json.parseToJsonElement(data).jsonArray.forEach { elem ->
            val obj    = elem.jsonObject
            val mid    = obj["member_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
            val value  = obj["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            val status = obj["status"]?.jsonPrimitive?.contentOrNull ?: "draft"
            val member = obj["member"]?.takeIf { it !is JsonNull }?.jsonObject
            val nombre = "${member?.get("first_name")?.jsonPrimitive?.contentOrNull ?: ""} ${member?.get("last_name")?.jsonPrimitive?.contentOrNull ?: ""}".trim()

            when (status) {
                "approved", "pending_board" -> approvedTotal += value
                "draft"                     -> { pendingTotal += value; pendingCount++ }
            }
            if (status != "rejected") {
                val prev = agg[mid] ?: Triple(nombre, 0.0, 0)
                agg[mid] = Triple(nombre.ifBlank { prev.first }, prev.second + value, prev.third + 1)
            }
        }

        ActivityEntrySummary(
            approvedTotal = approvedTotal,
            pendingTotal  = pendingTotal,
            pendingCount  = pendingCount,
            perMember     = agg.map { (mid, t) ->
                MemberEntryAggregate(miembroId = mid, miembroNombre = t.first, total = t.second, count = t.third)
            }.sortedByDescending { it.total },
        )
    }

    override suspend fun getPendingEntriesForActivity(grupoId: String, actividadTipoId: String): Result<List<MemberPendingEntry>> = runCatching {
        cachedRead(
            offline = { pendingDesdeRoom(grupoId, actividadTipoId) },
            online = {
                val data = supabase.from("member_entry").select(
                    Columns.raw("id, member_id, activity_type_id, value, entered_at, is_adjustment, member!inner(first_name, last_name), activity_type!inner(name, marker_type, unit_label)")
                ) {
                    filter {
                        eq("status", "draft")
                        eq("small_group_id", grupoId)
                        eq("activity_type_id", actividadTipoId)
                        eq("is_deleted", false)
                    }
                }.data
                cachePendingEntries(grupoId, data)
                Json.parseToJsonElement(data).jsonArray.mapNotNull { it.jsonObject.toPendingEntry() }
                    .sortedByDescending { it.enteredAt ?: Instant.EPOCH }
            },
        )
    }

    override suspend fun getPendingEntriesForGroup(grupoId: String): Result<List<MemberPendingEntry>> = runCatching {
        cachedRead(
            offline = { pendingDesdeRoom(grupoId, null) },
            online = {
                val data = supabase.from("member_entry").select(
                    Columns.raw("id, member_id, activity_type_id, value, entered_at, is_adjustment, member!inner(first_name, last_name), activity_type!inner(name, marker_type, unit_label)")
                ) {
                    filter {
                        eq("status", "draft")
                        eq("small_group_id", grupoId)
                        eq("is_deleted", false)
                    }
                }.data
                cachePendingEntries(grupoId, data)
                Json.parseToJsonElement(data).jsonArray.mapNotNull { it.jsonObject.toPendingEntry() }
                    .sortedByDescending { it.enteredAt ?: Instant.EPOCH }
            },
        )
    }

    override suspend fun getPendingEntriesCount(grupoId: String): Result<Int> = runCatching {
        cachedRead(
            offline = { pendingDesdeRoom(grupoId, null).size },
            online = {
                val data = supabase.from("member_entry").select(Columns.raw("id")) {
                    filter {
                        eq("status", "draft")
                        eq("small_group_id", grupoId)
                        eq("is_deleted", false)
                    }
                }.data
                Json.parseToJsonElement(data).jsonArray.size
            },
        )
    }

    // ── Caché offline (Room): aportes pendientes ─────────────────────────────────

    /** Guarda los drafts del grupo para verlos offline. Snapshot best-effort. */
    private suspend fun cachePendingEntries(grupoId: String, data: String) {
        val rows = Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj = elem.jsonObject
            val id  = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val enteredAt = obj["entered_at"]?.jsonPrimitive?.contentOrNull ?: ""
            MemberEntryEntity(
                id             = id,
                memberId       = obj["member_id"]?.jsonPrimitive?.contentOrNull ?: "",
                activityTypeId = obj["activity_type_id"]?.jsonPrimitive?.contentOrNull ?: "",
                smallGroupId   = grupoId,
                value          = obj["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                enteredAt      = enteredAt,
                status         = "draft",
                approvedBy     = null,
                approvedAt     = null,
                updatedAt      = enteredAt,
                isDeleted      = false,
                isAdjustment   = obj["is_adjustment"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.booleanOrNull ?: false,
            )
        }
        if (rows.isNotEmpty()) memberEntryDao.upsert(rows)
    }

    private suspend fun pendingDesdeRoom(grupoId: String, actividadTipoId: String?): List<MemberPendingEntry> {
        val entries = if (actividadTipoId == null) memberEntryDao.getByGroup(grupoId)
                      else memberEntryDao.getByGroupActivity(grupoId, actividadTipoId)
        return entries.filter { it.status == "draft" }.map { e ->
            val member = memberDao.getById(e.memberId)
            val at     = activityTypeDao.getById(e.activityTypeId)
            MemberPendingEntry(
                entryId         = e.id,
                miembroId       = e.memberId,
                miembroNombre   = member?.let { "${it.firstName} ${it.lastName}".trim() } ?: "",
                activityTypeId  = e.activityTypeId,
                actividadNombre = at?.name ?: "",
                markerType      = at?.markerType ?: "counter",
                unitLabel       = at?.unitLabel ?: "",
                value           = e.value,
                enteredAt       = e.enteredAt.takeIf { it.isNotBlank() }?.let(::parseInstant),
                grupoNombre     = "",
                isAdjustment    = e.isAdjustment,
            )
        }.sortedByDescending { it.enteredAt ?: Instant.EPOCH }
    }

    override suspend fun approveEntry(
        entryId: String,
        correctedValue: Double?,
        isMonetary: Boolean,
        actorId: String?,
    ): Result<Unit> = runCatching {
        if (correctedValue != null) {
            val old = currentValue(entryId)
            if (old == null || old != correctedValue) {
                supabase.from("member_entry").update(
                    buildJsonObject { put("value", correctedValue); put("updated_at", Instant.now().toString()) }
                ) { filter { eq("id", entryId) } }
                logEvent(entryId, action = "edited", oldValue = old, newValue = correctedValue, actorRole = "leader", actorId = actorId)
            }
        }
        val nuevoStatus = if (isMonetary) "pending_board" else "approved"
        supabase.from("member_entry").update(
            buildJsonObject {
                put("status", nuevoStatus)
                put("approved_by", actorId)
                put("approved_at", Instant.now().toString())
                put("updated_at", Instant.now().toString())
            }
        ) { filter { eq("id", entryId) } }
        logEvent(entryId, action = "approved", oldValue = null, newValue = null, actorRole = "leader", actorId = actorId)
    }

    override suspend fun rejectEntry(entryId: String, actorId: String?, note: String?): Result<Unit> = runCatching {
        supabase.from("member_entry").update(
            buildJsonObject { put("status", "rejected"); put("updated_at", Instant.now().toString()) }
        ) { filter { eq("id", entryId) } }
        logEvent(entryId, action = "rejected", oldValue = null, newValue = null, actorRole = "leader", actorId = actorId, note = note)
    }

    // ── Iglesia ──────────────────────────────────────────────────────────────

    override suspend fun getPendingBoardEntries(iglesiaId: String): Result<List<MemberPendingEntry>> = offlineSafe(emptyList()) {
        val data = supabase.from("member_entry").select(
            Columns.raw("id, member_id, activity_type_id, value, entered_at, member!inner(first_name, last_name, small_group!inner(name, church_id)), activity_type!inner(name, marker_type, unit_label)")
        ) {
            filter {
                eq("status", "pending_board")
                eq("member.small_group.church_id", iglesiaId)
                eq("is_deleted", false)
            }
        }.data
        Json.parseToJsonElement(data).jsonArray.mapNotNull { it.jsonObject.toPendingEntry(includeGroup = true) }
            .sortedByDescending { it.enteredAt ?: Instant.EPOCH }
    }

    override suspend fun getPendingBoardCount(iglesiaId: String): Result<Int> = offlineSafe(0) {
        getPendingBoardEntries(iglesiaId).getOrDefault(emptyList()).size
    }

    override suspend fun approveBoardEntry(entryId: String, actorId: String?): Result<Unit> = runCatching {
        supabase.from("member_entry").update(
            buildJsonObject {
                put("status", "approved")
                put("approved_by", actorId)
                put("approved_at", Instant.now().toString())
                put("updated_at", Instant.now().toString())
            }
        ) { filter { eq("id", entryId) } }
        logEvent(entryId, action = "board_approved", oldValue = null, newValue = null, actorRole = "church", actorId = actorId)
    }

    override suspend fun rejectBoardEntry(entryId: String, actorId: String?, note: String?): Result<Unit> = runCatching {
        supabase.from("member_entry").update(
            buildJsonObject { put("status", "rejected"); put("updated_at", Instant.now().toString()) }
        ) { filter { eq("id", entryId) } }
        logEvent(entryId, action = "rejected", oldValue = null, newValue = null, actorRole = "church", actorId = actorId, note = note)
    }

    // ── Historial de aportes aprobados ─────────────────────────────────────────

    override suspend fun getHistorialActividades(
        scopeLevel: String,
        scopeId: String,
        filtro: HistFiltroTrimestre,
    ): Result<List<HistActividad>> = runCatching {
        cachedRead(
            offline = { histActividadesDesdeRoom(scopeLevel, scopeId, filtro) },
            online  = { histActividadesOnline(scopeLevel, scopeId, filtro) },
        )
    }

    private suspend fun histActividadesOnline(
        scopeLevel: String,
        scopeId: String,
        filtro: HistFiltroTrimestre,
    ): List<HistActividad> {
        val gpIds = scopeGpIds(scopeLevel, scopeId)
        if (gpIds.isEmpty()) return emptyList()
        val bounds = if (filtro == HistFiltroTrimestre.TODOS) null else currentQuarterRange()

        val data = supabase.from("member_entry").select(
            Columns.raw("*, activity_type!inner(name, marker_type, unit_label)")
        ) {
            filter {
                isIn("small_group_id", gpIds)
                eq("status", "approved")
                eq("is_deleted", false)
                if (bounds != null) when (filtro) {
                    HistFiltroTrimestre.ACTUAL     -> { gte("approved_at", bounds.first); lte("approved_at", bounds.second) }
                    HistFiltroTrimestre.ANTERIORES -> lt("approved_at", bounds.first)
                    HistFiltroTrimestre.TODOS      -> {}
                }
            }
        }.data

        cacheHistorialEntries(data)

        data class Agg(var total: Double, val nombre: String, val marker: String, val unit: String)
        val agg = LinkedHashMap<String, Agg>()
        Json.parseToJsonElement(data).jsonArray.forEach { elem ->
            val obj = elem.jsonObject
            val aid = obj["activity_type_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
            val v   = obj["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            val at  = obj["activity_type"]?.takeIf { it !is JsonNull }?.jsonObject
            val cur = agg.getOrPut(aid) {
                Agg(0.0,
                    at?.get("name")?.jsonPrimitive?.contentOrNull ?: "",
                    at?.get("marker_type")?.jsonPrimitive?.contentOrNull ?: "monetary",
                    at?.get("unit_label")?.jsonPrimitive?.contentOrNull ?: "")
            }
            cur.total += v
        }
        return agg.map { (aid, a) -> HistActividad(aid, a.nombre, a.marker, a.unit, a.total) }
            .sortedByDescending { it.totalAprobado }
    }

    override suspend fun getHistorialMiembros(
        scopeLevel: String,
        scopeId: String,
        activityId: String,
        filtro: HistFiltroTrimestre,
    ): Result<List<HistMiembro>> = runCatching {
        cachedRead(
            offline = { histMiembrosDesdeRoom(scopeLevel, scopeId, activityId, filtro) },
            online  = { histMiembrosOnline(scopeLevel, scopeId, activityId, filtro) },
        )
    }

    private suspend fun histMiembrosOnline(
        scopeLevel: String,
        scopeId: String,
        activityId: String,
        filtro: HistFiltroTrimestre,
    ): List<HistMiembro> {
        val gpIds = scopeGpIds(scopeLevel, scopeId)
        if (gpIds.isEmpty()) return emptyList()
        val bounds = if (filtro == HistFiltroTrimestre.TODOS) null else currentQuarterRange()

        val data = supabase.from("member_entry").select(
            Columns.raw("*, member!inner(first_name, last_name)")
        ) {
            filter {
                isIn("small_group_id", gpIds)
                eq("activity_type_id", activityId)
                eq("status", "approved")
                eq("is_deleted", false)
                if (bounds != null) when (filtro) {
                    HistFiltroTrimestre.ACTUAL     -> { gte("approved_at", bounds.first); lte("approved_at", bounds.second) }
                    HistFiltroTrimestre.ANTERIORES -> lt("approved_at", bounds.first)
                    HistFiltroTrimestre.TODOS      -> {}
                }
            }
        }.data

        cacheHistorialEntries(data)

        data class Agg(var total: Double, var count: Int, val nombre: String)
        val agg = LinkedHashMap<String, Agg>()
        Json.parseToJsonElement(data).jsonArray.forEach { elem ->
            val obj = elem.jsonObject
            val mid = obj["member_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
            val v   = obj["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            val m   = obj["member"]?.takeIf { it !is JsonNull }?.jsonObject
            val nombre = "${m?.get("first_name")?.jsonPrimitive?.contentOrNull ?: ""} ${m?.get("last_name")?.jsonPrimitive?.contentOrNull ?: ""}".trim()
            val cur = agg.getOrPut(mid) { Agg(0.0, 0, nombre) }
            cur.total += v; cur.count++
        }
        return agg.map { (mid, a) -> HistMiembro(mid, a.nombre, a.total, a.count) }
            .sortedByDescending { it.totalAprobado }
    }

    override suspend fun getHistorialMiembroAprobaciones(
        miembroId: String,
        activityId: String,
        filtro: HistFiltroTrimestre,
    ): Result<List<HistAprobacion>> = runCatching {
        cachedRead(
            offline = { histAprobacionesDesdeRoom(miembroId, activityId, filtro) },
            online  = { histAprobacionesOnline(miembroId, activityId, filtro) },
        )
    }

    private suspend fun histAprobacionesOnline(
        miembroId: String,
        activityId: String,
        filtro: HistFiltroTrimestre,
    ): List<HistAprobacion> {
        val bounds = if (filtro == HistFiltroTrimestre.TODOS) null else currentQuarterRange()
        val data = supabase.from("member_entry").select(Columns.raw("*")) {
            filter {
                eq("member_id", miembroId)
                eq("activity_type_id", activityId)
                eq("status", "approved")
                eq("is_deleted", false)
                if (bounds != null) when (filtro) {
                    HistFiltroTrimestre.ACTUAL     -> { gte("approved_at", bounds.first); lte("approved_at", bounds.second) }
                    HistFiltroTrimestre.ANTERIORES -> lt("approved_at", bounds.first)
                    HistFiltroTrimestre.TODOS      -> {}
                }
            }
        }.data

        cacheHistorialEntries(data)

        val entries = Json.parseToJsonElement(data).jsonArray.mapNotNull { it.jsonObject.toMemberEntry() }
        return agruparAprobaciones(entries)
    }

    /** Agrupa aportes en lotes por fecha de aprobación. Compartido por la ruta online y la de Room. */
    private fun agruparAprobaciones(entries: List<MemberEntry>): List<HistAprobacion> =
        entries.groupBy { it.approvedAt?.toString() ?: it.enteredAt?.toString() ?: "sin_fecha" }
            .map { (key, list) ->
                HistAprobacion(
                    approvedAtKey = key,
                    fecha         = list.first().approvedAt ?: list.first().enteredAt,
                    total         = list.sumOf { it.value },
                    count         = list.size,
                    aportes       = list.sortedByDescending { it.enteredAt ?: Instant.EPOCH },
                )
            }
            .sortedByDescending { it.fecha ?: Instant.EPOCH }

    // ── Historial: caché y reconstrucción desde Room ───────────────────────────

    /**
     * Cachea filas completas de `member_entry`. Las tres consultas de historial piden `*`
     * justamente para poder guardar la fila entera (los nombres de miembro/actividad se
     * resuelven offline desde member/activity_type, ya cacheadas por el preloader).
     */
    private suspend fun cacheHistorialEntries(data: String) {
        val rows = Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj = elem.jsonObject
            val id  = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val enteredAt = obj["entered_at"]?.jsonPrimitive?.contentOrNull ?: ""
            MemberEntryEntity(
                id             = id,
                memberId       = obj["member_id"]?.jsonPrimitive?.contentOrNull ?: "",
                activityTypeId = obj["activity_type_id"]?.jsonPrimitive?.contentOrNull ?: "",
                smallGroupId   = obj["small_group_id"]?.jsonPrimitive?.contentOrNull ?: "",
                value          = obj["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                enteredAt      = enteredAt,
                status         = obj["status"]?.jsonPrimitive?.contentOrNull ?: "approved",
                approvedBy     = obj["approved_by"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
                approvedAt     = obj["approved_at"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
                updatedAt      = obj["updated_at"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull ?: enteredAt,
                isDeleted      = obj["is_deleted"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.booleanOrNull ?: false,
                isAdjustment   = obj["is_adjustment"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.booleanOrNull ?: false,
            )
        }
        if (rows.isNotEmpty()) memberEntryDao.upsert(rows)
    }

    /** Espejo offline de currentQuarterRange(), leyendo el trimestre cacheado en Room. */
    private suspend fun quarterRangeDesdeRoom(): Pair<String, String>? {
        val q = quarterDao.getCubriendo(java.time.LocalDate.now().toString()) ?: return null
        return "${q.startDate}T00:00:00Z" to "${q.endDate}T23:59:59Z"
    }

    /**
     * Aportes aprobados del scope, con el mismo filtro de trimestre que aplica PostgREST.
     * Las marcas ISO-8601 UTC se comparan como texto: mismo formato ⇒ el orden lexicográfico
     * coincide con el cronológico.
     */
    private suspend fun entriesHistorialDesdeRoom(
        scopeLevel: String,
        scopeId: String,
        filtro: HistFiltroTrimestre,
    ): List<MemberEntryEntity> {
        val gpIds = scopeGpIdsDesdeRoom(scopeLevel, scopeId)
        if (gpIds.isEmpty()) return emptyList()
        val bounds = if (filtro == HistFiltroTrimestre.TODOS) null else quarterRangeDesdeRoom()
        return memberEntryDao.getByGroups(gpIds)
            .filter { it.status == "approved" }
            .filter { e -> dentroDelFiltro(e.approvedAt, filtro, bounds) }
    }

    private fun dentroDelFiltro(
        approvedAt: String?,
        filtro: HistFiltroTrimestre,
        bounds: Pair<String, String>?,
    ): Boolean {
        if (bounds == null || filtro == HistFiltroTrimestre.TODOS) return true
        // Sin approved_at no se puede ubicar en un trimestre; PostgREST también lo excluiría.
        val at = approvedAt ?: return false
        return when (filtro) {
            HistFiltroTrimestre.ACTUAL     -> at >= bounds.first && at <= bounds.second
            HistFiltroTrimestre.ANTERIORES -> at < bounds.first
            HistFiltroTrimestre.TODOS      -> true
        }
    }

    private suspend fun histActividadesDesdeRoom(
        scopeLevel: String,
        scopeId: String,
        filtro: HistFiltroTrimestre,
    ): List<HistActividad> =
        entriesHistorialDesdeRoom(scopeLevel, scopeId, filtro)
            .groupBy { it.activityTypeId }
            .map { (aid, list) ->
                val at = activityTypeDao.getById(aid)
                HistActividad(
                    activityId    = aid,
                    nombre        = at?.name ?: "",
                    markerType    = at?.markerType ?: "monetary",
                    unitLabel     = at?.unitLabel ?: "",
                    totalAprobado = list.sumOf { it.value },
                )
            }
            .sortedByDescending { it.totalAprobado }

    private suspend fun histMiembrosDesdeRoom(
        scopeLevel: String,
        scopeId: String,
        activityId: String,
        filtro: HistFiltroTrimestre,
    ): List<HistMiembro> =
        entriesHistorialDesdeRoom(scopeLevel, scopeId, filtro)
            .filter { it.activityTypeId == activityId }
            .groupBy { it.memberId }
            .map { (mid, list) ->
                val m = memberDao.getById(mid)
                HistMiembro(
                    miembroId     = mid,
                    nombre        = m?.let { "${it.firstName} ${it.lastName}".trim() } ?: "",
                    totalAprobado = list.sumOf { it.value },
                    count         = list.size,
                )
            }
            .sortedByDescending { it.totalAprobado }

    private suspend fun histAprobacionesDesdeRoom(
        miembroId: String,
        activityId: String,
        filtro: HistFiltroTrimestre,
    ): List<HistAprobacion> {
        val bounds = if (filtro == HistFiltroTrimestre.TODOS) null else quarterRangeDesdeRoom()
        val entries = memberEntryDao.getByMemberActivity(miembroId, activityId)
            .filter { it.status == "approved" }
            .filter { e -> dentroDelFiltro(e.approvedAt, filtro, bounds) }
            .map { e ->
                MemberEntry(
                    id           = e.id,
                    value        = e.value,
                    enteredAt    = e.enteredAt.takeIf { it.isNotBlank() }?.let(::parseInstant),
                    status       = e.status,
                    approvedAt   = e.approvedAt?.let(::parseInstant),
                    isAdjustment = e.isAdjustment,
                )
            }
        return agruparAprobaciones(entries)
    }

    /** Espejo offline de scopeGpIds(): los GPs salen de small_group cacheada. */
    private suspend fun scopeGpIdsDesdeRoom(scopeLevel: String, scopeId: String): List<String> {
        if (scopeLevel != "church") return listOf(scopeId)
        return smallGroupDao.getByChurch(scopeId).map { it.id }
    }

    /** GPs del scope: "gp" → [scopeId]; "church" → GPs de la iglesia (sin grupo general). */
    private suspend fun scopeGpIds(scopeLevel: String, scopeId: String): List<String> {
        if (scopeLevel != "church") return listOf(scopeId)
        val data = supabase.from("small_group").select(Columns.raw("id, is_general_group")) {
            filter { eq("church_id", scopeId) }
        }.data
        return Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj = elem.jsonObject
            if (obj["is_general_group"]?.jsonPrimitive?.contentOrNull?.toBoolean() == true) return@mapNotNull null
            obj["id"]?.jsonPrimitive?.contentOrNull
        }
    }

    /** Rango del trimestre actual (el que contiene hoy) como instantes ISO. */
    private suspend fun currentQuarterRange(): Pair<String, String>? {
        val today = java.time.LocalDate.now().toString()
        val data = supabase.from("quarter").select(Columns.raw("id, start_date, end_date")) {
            filter { lte("start_date", today); gte("end_date", today) }
            limit(1)
        }.data
        val obj = Json.parseToJsonElement(data).jsonArray.firstOrNull()?.jsonObject ?: return null
        val start = obj["start_date"]?.jsonPrimitive?.contentOrNull ?: return null
        val end   = obj["end_date"]?.jsonPrimitive?.contentOrNull ?: return null
        // Se cachea para que el filtro por trimestre siga funcionando sin conexión.
        val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: "$start/$end"
        runCatching { quarterDao.upsert(listOf(QuarterEntity(id, start, end))) }
        return "${start}T00:00:00Z" to "${end}T23:59:59Z"
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private suspend fun currentValue(entryId: String): Double? {
        val data = supabase.from("member_entry").select(Columns.raw("value")) {
            filter { eq("id", entryId) }
            limit(1)
        }.data
        return Json.parseToJsonElement(data).jsonArray.firstOrNull()
            ?.jsonObject?.get("value")?.jsonPrimitive?.doubleOrNull
    }

    private suspend fun logEvent(
        entryId: String,
        action: String,
        oldValue: Double?,
        newValue: Double?,
        actorRole: String,
        actorId: String?,
        note: String? = null,
    ) {
        runCatching {
            supabase.from("member_entry_event").insert(
                buildJsonObject {
                    put("entry_id", entryId)
                    put("action", action)
                    if (oldValue != null) put("old_value", oldValue)
                    if (newValue != null) put("new_value", newValue)
                    put("actor_role", actorRole)
                    if (!actorId.isNullOrBlank()) put("actor_id", actorId)
                    if (!note.isNullOrBlank()) put("note", note)
                }
            )
        }
    }

    private fun kotlinx.serialization.json.JsonObject.toMemberEntry(): MemberEntry? {
        val id = this["id"]?.jsonPrimitive?.contentOrNull ?: return null
        return MemberEntry(
            id           = id,
            value        = this["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
            enteredAt    = this["entered_at"]?.jsonPrimitive?.contentOrNull?.let(::parseInstant),
            status       = this["status"]?.jsonPrimitive?.contentOrNull ?: "draft",
            approvedAt   = this["approved_at"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull?.let(::parseInstant),
            isAdjustment = this["is_adjustment"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.booleanOrNull ?: false,
        )
    }

    private fun kotlinx.serialization.json.JsonObject.toPendingEntry(includeGroup: Boolean = false): MemberPendingEntry? {
        val id     = this["id"]?.jsonPrimitive?.contentOrNull ?: return null
        val member = this["member"]?.takeIf { it !is JsonNull }?.jsonObject ?: return null
        val nombre = "${member["first_name"]?.jsonPrimitive?.contentOrNull ?: ""} ${member["last_name"]?.jsonPrimitive?.contentOrNull ?: ""}".trim()
        val act    = this["activity_type"]?.takeIf { it !is JsonNull }?.jsonObject ?: return null
        val grupo  = if (includeGroup) {
            member["small_group"]?.takeIf { it !is JsonNull }?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull ?: ""
        } else ""
        return MemberPendingEntry(
            entryId         = id,
            miembroId       = this["member_id"]?.jsonPrimitive?.contentOrNull ?: "",
            miembroNombre   = nombre,
            activityTypeId  = this["activity_type_id"]?.jsonPrimitive?.contentOrNull ?: "",
            actividadNombre = act["name"]?.jsonPrimitive?.contentOrNull ?: "",
            markerType      = act["marker_type"]?.jsonPrimitive?.contentOrNull ?: "counter",
            unitLabel       = act["unit_label"]?.jsonPrimitive?.contentOrNull ?: "",
            value           = this["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
            enteredAt       = this["entered_at"]?.jsonPrimitive?.contentOrNull?.let(::parseInstant),
            grupoNombre     = grupo,
            isAdjustment    = this["is_adjustment"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.booleanOrNull ?: false,
        )
    }

    private fun parseInstant(s: String): Instant? = runCatching {
        Instant.parse(s)
    }.recoverCatching {
        OffsetDateTime.parse(s.replace(" ", "T")).toInstant()
    }.getOrNull()
}
