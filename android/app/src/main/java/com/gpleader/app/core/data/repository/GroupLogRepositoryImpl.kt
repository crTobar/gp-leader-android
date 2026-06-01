package com.gpleader.app.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.OffsetDateTime
import javax.inject.Inject

class GroupLogRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : GroupLogRepository {

    override suspend fun getEntradas(grupoId: String, limit: Int): Result<List<GroupLogEntry>> = runCatching {
        val data = supabase.from("group_log").select(
            Columns.raw("id, action_type, description, created_at")
        ) {
            filter { eq("small_group_id", grupoId) }
            order("created_at", order = Order.DESCENDING)
            limit(limit.toLong())
        }.data

        Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj          = elem.jsonObject
            val id           = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val actionType   = obj["action_type"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val description  = obj["description"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val createdAtStr = obj["created_at"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val createdAt    = parseTimestamp(createdAtStr) ?: return@mapNotNull null
            GroupLogEntry(id, actionType, description, createdAt)
        }
    }

    override suspend fun logAccion(grupoId: String, actionType: String, description: String): Result<Unit> = runCatching {
        val profileId = supabase.auth.currentSessionOrNull()?.user?.id
        supabase.from("group_log").insert(buildJsonObject {
            put("small_group_id", grupoId)
            put("action_type",    actionType)
            put("description",    description)
            if (profileId != null) put("profile_id", profileId)
        })
    }

    // PostgREST puede devolver "2026-05-26 17:45:31.178+00" (con espacio y offset corto)
    private fun parseTimestamp(s: String): Instant? = runCatching {
        val normalized = s.replace(" ", "T").let { t ->
            if (Regex("[+-]\\d{2}$").containsMatchIn(t)) "${t}:00" else t
        }
        OffsetDateTime.parse(normalized).toInstant()
    }.getOrNull()
}
