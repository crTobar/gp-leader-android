package com.gpleader.app.core.data.repository

import com.gpleader.app.core.data.network.NetworkMonitor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject

class SolicitudRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val network:  NetworkMonitor,
) : SolicitudRepository {

    // Sin caché offline: degrada a vacío sin error cuando no hay red.
    private suspend fun <T> offlineSafe(fallback: T, block: suspend () -> T): T {
        if (!network.isOnline()) return fallback
        return try { block() }
        catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) { fallback }
    }

    override suspend fun getSolicitudesCreadas(grupoId: String): List<Solicitud> = offlineSafe(emptyList()) {
        val data = supabase.from("solicitude").select(
            columns = Columns.raw("*, member!solicitude_assigned_to_fkey(first_name, last_name)")
        ) {
            filter {
                eq("small_group_id", grupoId)
                isIn("status", listOf("pending", "active"))
            }
        }.data
        parseSolicitudes(data)
    }

    override suspend fun getSolicitudesAsignadas(miembroId: String): List<Solicitud> = offlineSafe(emptyList()) {
        val data = supabase.from("solicitude").select(
            columns = Columns.raw("*")
        ) {
            filter {
                eq("assigned_to", miembroId)
                eq("status", "pending")
            }
        }.data
        parseSolicitudes(data)
    }

    override suspend fun createSolicitud(
        assignedToId: String,
        grupoId:      String,
        nota:         String?,
    ): Solicitud {
        val resp = supabase.postgrest.rpc("create_solicitude", buildJsonObject {
            put("p_assigned_to",    assignedToId)
            put("p_small_group_id", grupoId)
            if (nota != null) put("p_note", nota) else put("p_note", "")
        })
        val arr = Json.parseToJsonElement(resp.data).jsonArray
        return parseSolicitud(arr.first().jsonObject)
    }

    override suspend fun cancelSolicitud(solicitudId: String): Solicitud {
        val resp = supabase.postgrest.rpc("cancel_solicitude", buildJsonObject {
            put("p_solicitude_id", solicitudId)
        })
        val arr = Json.parseToJsonElement(resp.data).jsonArray
        return parseSolicitud(arr.first().jsonObject)
    }

    override suspend fun activateSolicitud(solicitudId: String, miembroId: String): Solicitud {
        val resp = supabase.postgrest.rpc("activate_solicitude", buildJsonObject {
            put("p_solicitude_id", solicitudId)
            put("p_member_id",     miembroId)
        })
        val arr = Json.parseToJsonElement(resp.data).jsonArray
        return parseSolicitud(arr.first().jsonObject)
    }

    override suspend fun finishSolicitud(solicitudId: String, meetingId: String, miembroId: String): Solicitud {
        val resp = supabase.postgrest.rpc("finish_solicitude", buildJsonObject {
            put("p_solicitude_id", solicitudId)
            put("p_meeting_id",    meetingId)
            put("p_member_id",     miembroId)
        })
        val arr = Json.parseToJsonElement(resp.data).jsonArray
        return parseSolicitud(arr.first().jsonObject)
    }

    override suspend fun cancelSolicitudesByAssignee(assignedToId: String, grupoId: String) {
        val data = supabase.from("solicitude").select(columns = Columns.raw("id")) {
            filter {
                eq("assigned_to", assignedToId)
                eq("small_group_id", grupoId)
                isIn("status", listOf("pending", "active"))
            }
        }.data
        val ids = Json.parseToJsonElement(data).jsonArray
            .mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.contentOrNull }
        ids.forEach { id -> cancelSolicitud(id) }
    }

    override suspend fun revokeDeputyCode(grupoId: String) {
        supabase.postgrest.rpc("revoke_deputy_code", buildJsonObject {
            put("p_small_group_id", grupoId)
        })
    }

    override suspend fun createDeputyCodeForMember(grupoId: String, memberId: String): DeputyCodeResult {
        val resp = supabase.postgrest.rpc("create_deputy_code", buildJsonObject {
            put("p_small_group_id", grupoId)
        })
        val row = Json.parseToJsonElement(resp.data).jsonArray.first().jsonObject
        val codeId = row["deputy_code_id"]?.jsonPrimitive?.contentOrNull
            ?: error("Respuesta sin deputy_code_id")
        val code = row["code"]?.jsonPrimitive?.contentOrNull
            ?: error("Respuesta sin código")

        supabase.from("deputy_code").update(buildJsonObject {
            put("assigned_member_id", memberId)
        }) {
            filter { eq("id", codeId) }
        }

        return DeputyCodeResult(codeId = codeId, code = code)
    }

    override suspend fun getAsignadosPotenciales(grupoId: String): List<AsignadoPotencial> = offlineSafe(emptyList()) {
        val data = supabase.from("member").select {
            filter {
                eq("small_group_id", grupoId)
                eq("is_visitor", false)
                eq("is_active", true)
            }
        }.data
        parseAsignados(data)
    }

    // ── Parsers ───────────────────────────────────────────────────────────────

    private fun parseSolicitudes(data: String): List<Solicitud> =
        Json.parseToJsonElement(data).jsonArray.map { parseSolicitud(it.jsonObject) }

    private fun parseSolicitud(obj: kotlinx.serialization.json.JsonObject): Solicitud {
        val profileCreado   = obj["profile!solicitude_created_by_fkey"]
            ?.takeIf { it !is JsonNull }?.jsonObject
        val profileAsignado = (obj["member!solicitude_assigned_to_fkey"]
            ?: obj["profile!solicitude_assigned_to_fkey"])
            ?.takeIf { it !is JsonNull }?.jsonObject

        val profileRef = profileAsignado ?: profileCreado
        val nombre = buildString {
            profileRef?.get("first_name")?.jsonPrimitive?.contentOrNull?.let { append(it) }
            profileRef?.get("last_name")?.jsonPrimitive?.contentOrNull?.let {
                if (isNotEmpty()) append(" ")
                append(it)
            }
        }

        return Solicitud(
            id            = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
            createdBy     = obj["created_by"]?.jsonPrimitive?.contentOrNull ?: "",
            assignedTo    = obj["assigned_to"]?.jsonPrimitive?.contentOrNull ?: "",
            smallGroupId  = obj["small_group_id"]?.jsonPrimitive?.contentOrNull ?: "",
            meetingId     = obj["meeting_id"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            status        = obj["status"]?.jsonPrimitive?.contentOrNull ?: "",
            createdAt     = obj["created_at"]?.jsonPrimitive?.contentOrNull ?: "",
            activatedAt   = obj["activated_at"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            expiresAt     = obj["expires_at"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            finishedAt    = obj["finished_at"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            cancelledAt   = obj["cancelled_at"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            note          = obj["note"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            assignedToNombre = nombre,
        )
    }

    private fun parseAsignados(data: String): List<AsignadoPotencial> =
        Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj         = elem.jsonObject
            val id          = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val firstName   = obj["first_name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val midName     = obj["middle_name"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull
            val lastName    = obj["last_name"]?.jsonPrimitive?.contentOrNull ?: ""
            val secondLast  = obj["second_last_name"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull
            val isLeader    = obj["is_leader"]?.jsonPrimitive?.booleanOrNull ?: false
            val nombre      = listOfNotNull(firstName, midName, lastName.ifBlank { null }, secondLast).joinToString(" ")
            AsignadoPotencial(
                profileId = id,
                nombre    = nombre,
                rol       = if (isLeader) "leader" else "member",
            )
        }
}
