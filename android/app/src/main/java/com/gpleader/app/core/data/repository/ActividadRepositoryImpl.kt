package com.gpleader.app.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject

class ActividadRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : ActividadRepository {

    override suspend fun getActividadesTipo(iglesiaId: String): Result<List<ActividadTipoData>> = runCatching {
        val data = supabase.from("activity_type").select(
            columns = Columns.raw("id, name, level, marker_type, unit_label, sort_order, scope, church_id")
        ) {
            filter { eq("is_active", true) }
        }.data

        Json.parseToJsonElement(data).jsonArray
            .map { elem ->
                val obj = elem.jsonObject
                ActividadTipoData(
                    id         = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                    nombre     = obj["name"]?.jsonPrimitive?.contentOrNull ?: "",
                    level      = obj["level"]?.jsonPrimitive?.contentOrNull ?: "my_group",
                    markerType = obj["marker_type"]?.jsonPrimitive?.contentOrNull ?: "counter",
                    unitLabel  = obj["unit_label"]?.jsonPrimitive?.contentOrNull ?: "",
                    sortOrder  = obj["sort_order"]?.jsonPrimitive?.intOrNull ?: 0,
                    scope      = obj["scope"]?.jsonPrimitive?.contentOrNull ?: "global",
                    churchId   = obj["church_id"]?.jsonPrimitive?.contentOrNull,
                )
            }
            .filter { tipo ->
                tipo.scope == "global" ||
                (tipo.scope == "church" && tipo.churchId == iglesiaId)
            }
            .sortedBy { it.sortOrder }
    }

    override suspend fun saveRegistros(meetingId: String, registros: List<RegistroActividadData>): Result<Unit> = runCatching {
        if (registros.isEmpty()) return@runCatching
        val payload = buildJsonArray {
            registros.forEach { r ->
                addJsonObject {
                    put("meeting_id", meetingId)
                    put("activity_type_id", r.actividadTipoId)
                    if (r.cantidad != null) put("count", r.cantidad)
                    if (r.monto != null)    put("monto", r.monto)
                    if (r.notas != null)    put("notes", r.notas)
                }
            }
        }
        supabase.from("activity_record").insert(payload)
    }
}
