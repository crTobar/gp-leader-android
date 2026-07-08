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

class MoneyApprovalRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : MoneyApprovalRepository {

    // childId -> (activityId -> monto)
    private data class Matrix(
        val children: List<Pair<String, String>>,          // (id, nombre)
        val inflow:   Map<String, Map<String, Double>>,    // lo que llega del nivel de abajo
        val approved: Map<String, Map<String, Double>>,    // lo ya aprobado por este nivel (approvedUp)
    )

    // ── API ──────────────────────────────────────────────────────────────────

    override suspend fun getPendingChildren(
        approver: ApprovalLevel,
        nodeId: String,
        activityTypeId: String,
    ): Result<List<NivelHijoPendiente>> = runCatching {
        val m = buildMatrix(approver, nodeId, activityTypeId)
        m.children.mapNotNull { (id, name) ->
            val inf = m.inflow[id]?.get(activityTypeId) ?: 0.0
            val apr = m.approved[id]?.get(activityTypeId) ?: 0.0
            val pend = (inf - apr).coerceAtLeast(0.0)
            if (pend <= 0.0 && apr <= 0.0) null
            else NivelHijoPendiente(childId = id, childNombre = name, pendiente = pend, aprobado = apr)
        }.sortedByDescending { it.pendiente }
    }

    override suspend fun getMonetaryActivitiesWithPending(
        approver: ApprovalLevel,
        nodeId: String,
    ): Result<List<NivelActividadPendiente>> = runCatching {
        val m = buildMatrix(approver, nodeId, null)
        val actIds = buildSet {
            m.inflow.values.forEach { addAll(it.keys) }
            m.approved.values.forEach { addAll(it.keys) }
        }
        if (actIds.isEmpty()) return@runCatching emptyList()

        val meta = fetchActivityMeta(actIds)
        actIds.mapNotNull { a ->
            var pend = 0.0; var apr = 0.0
            m.children.forEach { (id, _) ->
                val inf = m.inflow[id]?.get(a) ?: 0.0
                val ap  = m.approved[id]?.get(a) ?: 0.0
                pend += (inf - ap).coerceAtLeast(0.0)
                apr  += ap
            }
            if (pend <= 0.0 && apr <= 0.0) return@mapNotNull null
            val md = meta[a] ?: return@mapNotNull null
            if (md.marker != "monetary") return@mapNotNull null
            NivelActividadPendiente(
                activityTypeId  = a,
                actividadNombre = md.nombre,
                markerType      = md.marker,
                unitLabel       = md.unit,
                pendienteTotal  = pend,
                aprobadoTotal   = apr,
            )
        }.sortedByDescending { it.pendienteTotal }
    }

    override suspend fun getNivelDetalle(
        approver: ApprovalLevel,
        nodeId: String,
    ): Result<List<NivelActividadDetalle>> = runCatching {
        val m = buildMatrix(approver, nodeId, null)
        val actIds = buildSet {
            m.inflow.values.forEach { addAll(it.keys) }
            m.approved.values.forEach { addAll(it.keys) }
        }
        if (actIds.isEmpty()) return@runCatching emptyList()

        val meta = fetchActivityMeta(actIds)
        actIds.mapNotNull { a ->
            val hijos = m.children.mapNotNull { (id, name) ->
                val inf = m.inflow[id]?.get(a) ?: 0.0
                val apr = m.approved[id]?.get(a) ?: 0.0
                val pend = (inf - apr).coerceAtLeast(0.0)
                if (pend <= 0.0 && apr <= 0.0) null
                else NivelHijoPendiente(childId = id, childNombre = name, pendiente = pend, aprobado = apr)
            }
            if (hijos.isEmpty()) return@mapNotNull null
            val md = meta[a] ?: return@mapNotNull null
            if (md.marker != "monetary") return@mapNotNull null
            NivelActividadDetalle(
                actividad = NivelActividadPendiente(
                    activityTypeId  = a,
                    actividadNombre = md.nombre,
                    markerType      = md.marker,
                    unitLabel       = md.unit,
                    pendienteTotal  = hijos.sumOf { it.pendiente },
                    aprobadoTotal   = hijos.sumOf { it.aprobado },
                ),
                hijos = hijos.sortedByDescending { it.pendiente },
            )
        }.sortedByDescending { it.actividad.pendienteTotal }
    }

    override suspend fun approve(
        approver: ApprovalLevel,
        childId: String,
        activityTypeId: String,
        requested: Double,
        approved: Double,
        note: String?,
        approverProfileId: String?,
    ): Result<Unit> = runCatching {
        val quarterId = currentQuarterId()
        supabase.from("money_approval").insert(
            buildJsonObject {
                put("activity_type_id", activityTypeId)
                put("source_level", approver.source)
                put("source_id", childId)
                put("approver_level", approver.approver)
                if (!approverProfileId.isNullOrBlank()) put("approver_profile_id", approverProfileId)
                put("requested", requested)
                put("approved", approved)
                if (!note.isNullOrBlank()) put("note", note)
                if (quarterId != null) put("quarter_id", quarterId)
            }
        )
    }

    override suspend fun getTotalPendiente(approver: ApprovalLevel, nodeId: String): Result<Double> = runCatching {
        getMonetaryActivitiesWithPending(approver, nodeId).getOrDefault(emptyList())
            .sumOf { it.pendienteTotal }
    }

    override suspend fun getHistory(
        sourceLevel: String,
        sourceId: String,
        activityTypeId: String?,
    ): Result<List<MoneyMovimiento>> = runCatching {
        val data = supabase.from("money_approval").select(
            Columns.raw("id, source_level, source_id, approver_level, requested, approved, note, created_at")
        ) {
            filter {
                eq("source_level", sourceLevel)
                eq("source_id", sourceId)
                if (activityTypeId != null) eq("activity_type_id", activityTypeId)
            }
        }.data
        Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj = elem.jsonObject
            MoneyMovimiento(
                id            = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                sourceLevel   = obj["source_level"]?.jsonPrimitive?.contentOrNull ?: "",
                sourceId      = obj["source_id"]?.jsonPrimitive?.contentOrNull ?: "",
                approverLevel = obj["approver_level"]?.jsonPrimitive?.contentOrNull ?: "",
                requested     = obj["requested"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                approved      = obj["approved"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                note          = obj["note"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
                createdAt     = obj["created_at"]?.jsonPrimitive?.contentOrNull?.let(::parseInstant),
            )
        }.sortedByDescending { it.createdAt ?: Instant.EPOCH }
    }

    // ── Núcleo: matriz hijo × actividad ────────────────────────────────────────

    private suspend fun buildMatrix(approver: ApprovalLevel, nodeId: String, act: String?): Matrix {
        val children = childrenOf(approver, nodeId)
        val childIds = children.map { it.first }
        if (childIds.isEmpty()) return Matrix(emptyList(), emptyMap(), emptyMap())

        val approved = moneyApprovedMatrix(approver.source, childIds, act)

        val inflow: Map<String, Map<String, Double>> = when (approver) {
            ApprovalLevel.CHURCH -> memberEntryMatrix(childIds, act)
            else -> {
                val (grandTable, grandParent, grandSource) = grandMeta(approver)
                val grandToChild = childParentMap(grandTable, grandParent, childIds)
                val grandIds = grandToChild.keys.toList()
                if (grandIds.isEmpty()) emptyMap()
                else {
                    val grandApproved = moneyApprovedMatrix(grandSource, grandIds, act)
                    val res = HashMap<String, HashMap<String, Double>>()
                    grandApproved.forEach { (grandId, actMap) ->
                        val child = grandToChild[grandId] ?: return@forEach
                        val target = res.getOrPut(child) { HashMap() }
                        actMap.forEach { (a, v) -> target[a] = (target[a] ?: 0.0) + v }
                    }
                    res
                }
            }
        }
        return Matrix(children, inflow, approved)
    }

    /** (grandTable, grandParentCol, grandSourceLevel) para calcular el inflow de niveles > church. */
    private fun grandMeta(approver: ApprovalLevel): Triple<String, String, String> = when (approver) {
        ApprovalLevel.DISTRICT -> Triple("small_group", "church_id",   "gp")
        ApprovalLevel.CAMPO    -> Triple("church",      "district_id", "church")
        ApprovalLevel.UNION    -> Triple("district",    "campo_id",    "district")
        ApprovalLevel.CHURCH   -> Triple("member",      "small_group_id", "member") // no usado
    }

    // ── Consultas base ─────────────────────────────────────────────────────────

    private suspend fun childrenOf(approver: ApprovalLevel, nodeId: String): List<Pair<String, String>> {
        val cols = if (approver == ApprovalLevel.CHURCH) "id, name, is_general_group" else "id, name"
        val data = supabase.from(approver.childTable).select(Columns.raw(cols)) {
            filter { eq(approver.childParent, nodeId) }
        }.data
        return Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj = elem.jsonObject
            if (approver == ApprovalLevel.CHURCH &&
                (obj["is_general_group"]?.jsonPrimitive?.contentOrNull?.toBoolean() == true)
            ) return@mapNotNull null
            val id   = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: ""
            id to name
        }
    }

    /** table.id -> table.parentCol, para nodos cuyo parentCol ∈ parentIds (excluye grupo general). */
    private suspend fun childParentMap(table: String, parentCol: String, parentIds: List<String>): Map<String, String> {
        if (parentIds.isEmpty()) return emptyMap()
        val cols = if (table == "small_group") "id, $parentCol, is_general_group" else "id, $parentCol"
        val data = supabase.from(table).select(Columns.raw(cols)) {
            filter { isIn(parentCol, parentIds) }
        }.data
        return Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj = elem.jsonObject
            if (table == "small_group" &&
                (obj["is_general_group"]?.jsonPrimitive?.contentOrNull?.toBoolean() == true)
            ) return@mapNotNull null
            val id     = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val parent = obj[parentCol]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            id to parent
        }.toMap()
    }

    /** money_approval.approved sumado por (source_id, activity_type_id). */
    private suspend fun moneyApprovedMatrix(
        sourceLevel: String,
        sourceIds: List<String>,
        act: String?,
    ): Map<String, Map<String, Double>> {
        if (sourceIds.isEmpty()) return emptyMap()
        val data = supabase.from("money_approval").select(
            Columns.raw("source_id, activity_type_id, approved")
        ) {
            filter {
                eq("source_level", sourceLevel)
                isIn("source_id", sourceIds)
                if (act != null) eq("activity_type_id", act)
            }
        }.data
        val res = HashMap<String, HashMap<String, Double>>()
        Json.parseToJsonElement(data).jsonArray.forEach { elem ->
            val obj = elem.jsonObject
            val sid = obj["source_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
            val aid = obj["activity_type_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
            val v   = obj["approved"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            val map = res.getOrPut(sid) { HashMap() }
            map[aid] = (map[aid] ?: 0.0) + v
        }
        return res
    }

    /** member_entry.value (status=approved, solo actividades monetarias) sumado por (gp, actividad). */
    private suspend fun memberEntryMatrix(gpIds: List<String>, act: String?): Map<String, Map<String, Double>> {
        if (gpIds.isEmpty()) return emptyMap()
        val data = supabase.from("member_entry").select(
            Columns.raw("small_group_id, activity_type_id, value, activity_type!inner(marker_type)")
        ) {
            filter {
                isIn("small_group_id", gpIds)
                eq("status", "approved")
                eq("is_deleted", false)
                eq("activity_type.marker_type", "monetary")
                if (act != null) eq("activity_type_id", act)
            }
        }.data
        val res = HashMap<String, HashMap<String, Double>>()
        Json.parseToJsonElement(data).jsonArray.forEach { elem ->
            val obj = elem.jsonObject
            val gid = obj["small_group_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
            val aid = obj["activity_type_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
            val v   = obj["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            val map = res.getOrPut(gid) { HashMap() }
            map[aid] = (map[aid] ?: 0.0) + v
        }
        return res
    }

    private data class ActMeta(val nombre: String, val marker: String, val unit: String)

    private suspend fun fetchActivityMeta(actIds: Set<String>): Map<String, ActMeta> {
        if (actIds.isEmpty()) return emptyMap()
        val data = supabase.from("activity_type").select(
            Columns.raw("id, name, marker_type, unit_label")
        ) {
            filter { isIn("id", actIds.toList()) }
        }.data
        return Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj = elem.jsonObject
            val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            id to ActMeta(
                nombre = obj["name"]?.jsonPrimitive?.contentOrNull ?: "",
                marker = obj["marker_type"]?.jsonPrimitive?.contentOrNull ?: "monetary",
                unit   = obj["unit_label"]?.jsonPrimitive?.contentOrNull ?: "",
            )
        }.toMap()
    }

    private suspend fun currentQuarterId(): String? = runCatching {
        val data = supabase.from("quarter").select(Columns.raw("id")) {
            filter { eq("is_current", true) }
            limit(1)
        }.data
        Json.parseToJsonElement(data).jsonArray.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
    }.getOrNull()

    private fun parseInstant(s: String): Instant? = runCatching {
        Instant.parse(s)
    }.recoverCatching {
        OffsetDateTime.parse(s.replace(" ", "T")).toInstant()
    }.getOrNull()
}
