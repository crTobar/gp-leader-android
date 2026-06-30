package com.gpleader.app.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import javax.inject.Inject

class IglesiaRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : IglesiaRepository {

    override suspend fun searchChurches(query: String, maxResults: Int): List<ChurchHit> {
        val trimmed = query.trim()
        val isEmptyQuery = trimmed.isBlank()

        val data = if (isEmptyQuery) {
            supabase.from("church").select { limit(50) }.data
        } else {
            val longestToken = trimmed.split(" ").maxByOrNull { it.length } ?: return emptyList()
            supabase.from("church").select {
                filter { ilike("name", "%$longestToken%") }
                limit(50)
            }.data
        }

        val allTokens = trimmed.lowercase().split(" ").filter { it.isNotBlank() }

        val results = Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj = elem.jsonObject
            val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val churchName = obj["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val districtId = obj["district_id"]?.jsonPrimitive?.contentOrNull ?: ""
            ChurchHit(
                id           = id,
                churchName   = churchName,
                districtName = "",
                campoName    = "",
            ) to districtId
        }

        if (results.isEmpty()) return emptyList()

        val districtIds = results.map { it.second }.distinct().filter { it.isNotBlank() }
        val districtMap = mutableMapOf<String, Pair<String, String>>() // districtId -> (name, campoId)
        if (districtIds.isNotEmpty()) {
            val distData = supabase.from("district").select {
                filter { isIn("id", districtIds) }
            }.data
            Json.parseToJsonElement(distData).jsonArray.forEach { elem ->
                val obj = elem.jsonObject
                val did = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                val dname = obj["name"]?.jsonPrimitive?.contentOrNull ?: ""
                val campoId = obj["campo_id"]?.jsonPrimitive?.contentOrNull ?: ""
                districtMap[did] = dname to campoId
            }
        }

        val campoIds = districtMap.values.map { it.second }.distinct().filter { it.isNotBlank() }
        val campoMap = mutableMapOf<String, String>() // campoId -> name
        if (campoIds.isNotEmpty()) {
            val campoData = supabase.from("campo").select {
                filter { isIn("id", campoIds) }
            }.data
            Json.parseToJsonElement(campoData).jsonArray.forEach { elem ->
                val obj = elem.jsonObject
                val cid = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                val cname = obj["name"]?.jsonPrimitive?.contentOrNull ?: ""
                campoMap[cid] = cname
            }
        }

        val enriched = results.map { (hit, districtId) ->
            val (dname, campoId) = districtMap[districtId] ?: ("" to "")
            hit.copy(districtName = dname, campoName = campoMap[campoId] ?: "")
        }

        return if (allTokens.isEmpty()) enriched.take(maxResults) else fuzzyRank(enriched, allTokens).take(maxResults)
    }

    override suspend fun getChurchById(churchId: String): ChurchHit? {
        val data = supabase.from("church").select {
            filter { eq("id", churchId) }
            limit(1)
        }.data
        val arr = Json.parseToJsonElement(data).jsonArray
        if (arr.isEmpty()) return null
        val obj = arr[0].jsonObject
        val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return null
        val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: return null
        val districtId = obj["district_id"]?.jsonPrimitive?.contentOrNull ?: ""

        var districtName = ""
        var campoName    = ""
        if (districtId.isNotBlank()) {
            val dData = supabase.from("district").select {
                filter { eq("id", districtId) }
                limit(1)
            }.data
            val dArr = Json.parseToJsonElement(dData).jsonArray
            if (dArr.isNotEmpty()) {
                val dObj = dArr[0].jsonObject
                districtName = dObj["name"]?.jsonPrimitive?.contentOrNull ?: ""
                val campoId = dObj["campo_id"]?.jsonPrimitive?.contentOrNull ?: ""
                if (campoId.isNotBlank()) {
                    val cData = supabase.from("campo").select {
                        filter { eq("id", campoId) }
                        limit(1)
                    }.data
                    val cArr = Json.parseToJsonElement(cData).jsonArray
                    if (cArr.isNotEmpty()) {
                        campoName = cArr[0].jsonObject["name"]?.jsonPrimitive?.contentOrNull ?: ""
                    }
                }
            }
        }
        return ChurchHit(id = id, churchName = name, districtName = districtName, campoName = campoName)
    }

    override suspend fun getGruposByIglesia(iglesiaId: String): List<GrupoResumen> {
        val data = supabase.from("small_group").select {
            filter { eq("church_id", iglesiaId) }
        }.data

        return Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj = elem.jsonObject
            // Excluir el grupo general de la iglesia — no es un GP regular
            val isGeneral = obj["is_general_group"]?.jsonPrimitive?.contentOrNull?.toBoolean() ?: false
            if (isGeneral) return@mapNotNull null
            val id    = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val name  = obj["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            GrupoResumen(id = id, nombre = name, totalMiembros = 0, pendingBoardCount = 0)
        }.let { grupos ->
            if (grupos.isEmpty()) return emptyList()
            val grupoIds = grupos.map { it.id }

            // Contar miembros activos por grupo
            val memberCounts = mutableMapOf<String, Int>()
            val memberData = supabase.from("member").select {
                filter {
                    isIn("small_group_id", grupoIds)
                    eq("is_active", true)
                    eq("is_visitor", false)
                }
            }.data
            Json.parseToJsonElement(memberData).jsonArray.forEach { elem ->
                val groupId = elem.jsonObject["small_group_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                memberCounts[groupId] = (memberCounts[groupId] ?: 0) + 1
            }

            // Contar pending_board por grupo
            val pendingCounts = mutableMapOf<String, Int>()
            val pendingData = supabase.from("member_activity_record").select(
                Columns.raw("id, member_id, member!inner(small_group_id)")
            ) {
                filter { eq("status", "pending_board") }
            }.data
            Json.parseToJsonElement(pendingData).jsonArray.forEach { elem ->
                val obj2    = elem.jsonObject
                val member  = obj2["member"]?.takeIf { it !is JsonNull }?.jsonObject ?: return@forEach
                val groupId = member["small_group_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                if (groupId in grupoIds) pendingCounts[groupId] = (pendingCounts[groupId] ?: 0) + 1
            }

            grupos.map { g ->
                g.copy(totalMiembros = memberCounts[g.id] ?: 0, pendingBoardCount = pendingCounts[g.id] ?: 0)
            }
        }
    }

    override suspend fun getPendingBoardActivities(iglesiaId: String): List<PendingBoardItem> {
        val data = supabase.from("member_activity_record").select(
            Columns.raw("id, record_date, count, member!inner(first_name, last_name, small_group_id, small_group!inner(name, church_id)), activity_type!inner(name)")
        ) {
            filter {
                eq("status", "pending_board")
                eq("member.small_group.church_id", iglesiaId)
            }
        }.data

        return Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj        = elem.jsonObject
            val recordId   = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val recordDate = obj["record_date"]?.jsonPrimitive?.contentOrNull?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            } ?: LocalDate.now()
            val monto      = obj["count"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 0.0

            val member        = obj["member"]?.takeIf { it !is JsonNull }?.jsonObject ?: return@mapNotNull null
            val firstName     = member["first_name"]?.jsonPrimitive?.contentOrNull ?: ""
            val lastName      = member["last_name"]?.jsonPrimitive?.contentOrNull ?: ""
            val smallGroup    = member["small_group"]?.takeIf { it !is JsonNull }?.jsonObject ?: return@mapNotNull null
            val grupoNombre   = smallGroup["name"]?.jsonPrimitive?.contentOrNull ?: ""

            val actividadObj  = obj["activity_type"]?.takeIf { it !is JsonNull }?.jsonObject ?: return@mapNotNull null
            val actNombre     = actividadObj["name"]?.jsonPrimitive?.contentOrNull ?: ""

            PendingBoardItem(
                recordId        = recordId,
                miembroNombre   = "$firstName $lastName".trim(),
                grupoNombre     = grupoNombre,
                actividadNombre = actNombre,
                monto           = monto,
                recordDate      = recordDate,
            )
        }
    }

    override suspend fun approveMonetaryActivity(recordId: String): Result<Unit> = runCatching {
        supabase.from("member_activity_record").update(
            mapOf("status" to "approved")
        ) {
            filter { eq("id", recordId) }
        }
    }

    override suspend fun rejectMonetaryActivity(recordId: String): Result<Unit> = runCatching {
        supabase.from("member_activity_record").update(
            mapOf("status" to "rejected")
        ) {
            filter { eq("id", recordId) }
        }
    }

    private fun fuzzyRank(hits: List<ChurchHit>, tokens: List<String>): List<ChurchHit> {
        return hits.sortedByDescending { hit ->
            val churchLower = hit.churchName.lowercase()
            var score = 0
            for (token in tokens) {
                when {
                    churchLower.startsWith(token) -> score += 5
                    churchLower.contains(token)   -> score += 3
                    "${hit.churchName} ${hit.districtName} ${hit.campoName}"
                        .lowercase().contains(token) -> score += 1
                }
            }
            score
        }
    }
}
