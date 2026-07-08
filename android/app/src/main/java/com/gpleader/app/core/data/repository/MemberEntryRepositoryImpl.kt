package com.gpleader.app.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
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
    private val supabase: SupabaseClient,
) : MemberEntryRepository {

    // ── Miembro ────────────────────────────────────────────────────────────────

    override suspend fun getEntries(miembroId: String, actividadTipoId: String): Result<List<MemberEntry>> = runCatching {
        val data = supabase.from("member_entry").select(
            Columns.raw("id, value, entered_at, status")
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

    override suspend fun getEntry(entryId: String): Result<MemberEntry?> = runCatching {
        val data = supabase.from("member_entry").select(Columns.raw("id, value, entered_at, status, approved_at")) {
            filter { eq("id", entryId) }
            limit(1)
        }.data
        Json.parseToJsonElement(data).jsonArray.firstOrNull()?.jsonObject?.toMemberEntry()
    }

    override suspend fun getEntryTotal(miembroId: String, actividadTipoId: String): Result<Double> = runCatching {
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

    override suspend fun getEntryEvents(entryId: String): Result<List<MemberEntryEvent>> = runCatching {
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

    // ── Líder ────────────────────────────────────────────────────────────────

    override suspend fun getActivityMemberSummary(grupoId: String, actividadTipoId: String): Result<ActivityEntrySummary> = runCatching {
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
        val data = supabase.from("member_entry").select(
            Columns.raw("id, member_id, activity_type_id, value, entered_at, member!inner(first_name, last_name), activity_type!inner(name, marker_type, unit_label)")
        ) {
            filter {
                eq("status", "draft")
                eq("small_group_id", grupoId)
                eq("activity_type_id", actividadTipoId)
                eq("is_deleted", false)
            }
        }.data
        Json.parseToJsonElement(data).jsonArray.mapNotNull { it.jsonObject.toPendingEntry() }
            .sortedByDescending { it.enteredAt ?: Instant.EPOCH }
    }

    override suspend fun getPendingEntriesForGroup(grupoId: String): Result<List<MemberPendingEntry>> = runCatching {
        val data = supabase.from("member_entry").select(
            Columns.raw("id, member_id, activity_type_id, value, entered_at, member!inner(first_name, last_name), activity_type!inner(name, marker_type, unit_label)")
        ) {
            filter {
                eq("status", "draft")
                eq("small_group_id", grupoId)
                eq("is_deleted", false)
            }
        }.data
        Json.parseToJsonElement(data).jsonArray.mapNotNull { it.jsonObject.toPendingEntry() }
            .sortedByDescending { it.enteredAt ?: Instant.EPOCH }
    }

    override suspend fun getPendingEntriesCount(grupoId: String): Result<Int> = runCatching {
        val data = supabase.from("member_entry").select(Columns.raw("id")) {
            filter {
                eq("status", "draft")
                eq("small_group_id", grupoId)
                eq("is_deleted", false)
            }
        }.data
        Json.parseToJsonElement(data).jsonArray.size
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

    override suspend fun getPendingBoardEntries(iglesiaId: String): Result<List<MemberPendingEntry>> = runCatching {
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

    override suspend fun getPendingBoardCount(iglesiaId: String): Result<Int> = runCatching {
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
        val gpIds = scopeGpIds(scopeLevel, scopeId)
        if (gpIds.isEmpty()) return@runCatching emptyList()
        val bounds = if (filtro == HistFiltroTrimestre.TODOS) null else currentQuarterRange()

        val data = supabase.from("member_entry").select(
            Columns.raw("activity_type_id, value, activity_type!inner(name, marker_type, unit_label)")
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
        agg.map { (aid, a) -> HistActividad(aid, a.nombre, a.marker, a.unit, a.total) }
            .sortedByDescending { it.totalAprobado }
    }

    override suspend fun getHistorialMiembros(
        scopeLevel: String,
        scopeId: String,
        activityId: String,
        filtro: HistFiltroTrimestre,
    ): Result<List<HistMiembro>> = runCatching {
        val gpIds = scopeGpIds(scopeLevel, scopeId)
        if (gpIds.isEmpty()) return@runCatching emptyList()
        val bounds = if (filtro == HistFiltroTrimestre.TODOS) null else currentQuarterRange()

        val data = supabase.from("member_entry").select(
            Columns.raw("member_id, value, member!inner(first_name, last_name)")
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
        agg.map { (mid, a) -> HistMiembro(mid, a.nombre, a.total, a.count) }
            .sortedByDescending { it.totalAprobado }
    }

    override suspend fun getHistorialMiembroAprobaciones(
        miembroId: String,
        activityId: String,
        filtro: HistFiltroTrimestre,
    ): Result<List<HistAprobacion>> = runCatching {
        val bounds = if (filtro == HistFiltroTrimestre.TODOS) null else currentQuarterRange()
        val data = supabase.from("member_entry").select(
            Columns.raw("id, value, entered_at, status, approved_at")
        ) {
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

        val entries = Json.parseToJsonElement(data).jsonArray.mapNotNull { it.jsonObject.toMemberEntry() }
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
        val data = supabase.from("quarter").select(Columns.raw("start_date, end_date")) {
            filter { lte("start_date", today); gte("end_date", today) }
            limit(1)
        }.data
        val obj = Json.parseToJsonElement(data).jsonArray.firstOrNull()?.jsonObject ?: return null
        val start = obj["start_date"]?.jsonPrimitive?.contentOrNull ?: return null
        val end   = obj["end_date"]?.jsonPrimitive?.contentOrNull ?: return null
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
            id         = id,
            value      = this["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
            enteredAt  = this["entered_at"]?.jsonPrimitive?.contentOrNull?.let(::parseInstant),
            status     = this["status"]?.jsonPrimitive?.contentOrNull ?: "draft",
            approvedAt = this["approved_at"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull?.let(::parseInstant),
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
        )
    }

    private fun parseInstant(s: String): Instant? = runCatching {
        Instant.parse(s)
    }.recoverCatching {
        OffsetDateTime.parse(s.replace(" ", "T")).toInstant()
    }.getOrNull()
}
