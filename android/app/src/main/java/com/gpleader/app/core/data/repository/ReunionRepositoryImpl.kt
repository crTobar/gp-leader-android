package com.gpleader.app.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.LocalDate
import javax.inject.Inject

class ReunionRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : ReunionRepository {

    override fun getReuniones(grupoId: String, limit: Int): Flow<List<ReunionConStats>> = flow {
        val data = supabase.from("meeting").select(
            columns = Columns.raw("*, attendance(*)")
        ) {
            filter { eq("small_group_id", grupoId) }
        }.data

        val all = parseReuniones(data)
            .sortedByDescending { it.fecha }
        val result = if (limit == Int.MAX_VALUE) all else all.take(limit)
        emit(result)
    }

    override suspend fun saveReunion(
        grupoId:       String,
        fecha:         LocalDate,
        noHuboReunion: Boolean,
        asistencias:   List<AsistenciaParaGuardar>,
    ): Result<String> = runCatching {
        // 1 — Insertar la reunión y recuperar el ID generado
        val meetingPayload = buildJsonObject {
            put("small_group_id", grupoId)
            put("meeting_date",   fecha.toString())
            put("status",         "submitted")
        }
        val meetingResponse = supabase.from("meeting")
            .insert(meetingPayload) { select() }
            .data

        val meetingId = Json.parseToJsonElement(meetingResponse)
            .jsonArray[0].jsonObject["id"]?.jsonPrimitive?.contentOrNull
            ?: error("No se recibió el ID de la reunión creada")

        // 2 — Insertar visitas nuevas en member (is_visitor=true) y obtener sus IDs
        val visitasNuevas = asistencias.filter { it.esVisita && it.miembroId == null && it.nombreVisita != null }
        val idsVisitasNuevas = mutableListOf<String>()

        if (visitasNuevas.isNotEmpty()) {
            val memberPayload = buildJsonArray {
                visitasNuevas.forEach { v ->
                    val partes = v.nombreVisita!!.trim().split(" ", limit = 2)
                    addJsonObject {
                        put("small_group_id", grupoId)
                        put("first_name",     partes[0])
                        put("last_name",      partes.getOrElse(1) { "-" })
                        put("is_visitor",     true)
                        put("is_active",      true)
                    }
                }
            }
            val memberResponse = supabase.from("member").insert(memberPayload) { select() }.data
            Json.parseToJsonElement(memberResponse).jsonArray.forEach { elem ->
                elem.jsonObject["id"]?.jsonPrimitive?.contentOrNull?.let { idsVisitasNuevas.add(it) }
            }
        }

        // 3 — Insertar asistencia: miembros reales + visitas anteriores + visitas nuevas
        val todasConId: List<Pair<String, String>> = buildList {
            asistencias.filter { !it.esVisita && it.miembroId != null }
                .forEach { add(it.miembroId!! to it.estado) }
            asistencias.filter { it.esVisita && it.miembroId != null }
                .forEach { add(it.miembroId!! to it.estado) }
            visitasNuevas.forEachIndexed { i, v ->
                idsVisitasNuevas.getOrNull(i)?.let { add(it to v.estado) }
            }
        }

        if (todasConId.isNotEmpty()) {
            val attendancePayload = buildJsonArray {
                todasConId.forEach { (memberId, estado) ->
                    addJsonObject {
                        put("meeting_id", meetingId)
                        put("member_id",  memberId)
                        put("status",     mapEstadoAsistencia(estado))
                    }
                }
            }
            supabase.from("attendance").insert(attendancePayload)
        }

        meetingId
    }

    private fun mapEstadoAsistencia(estado: String): String = when (estado.uppercase()) {
        "PRESENTE", "PRESENT"                 -> "present"
        "AUSENTE",  "ABSENT"                  -> "absent"
        "JUSTIFICADO", "JUSTIFIED", "EXCUSED" -> "justified"
        else                                  -> "present"
    }

    private fun parseReuniones(data: String): List<ReunionConStats> =
        Json.parseToJsonElement(data).jsonArray.map { elem ->
            val obj        = elem.jsonObject
            val attendance = obj["attendance"]?.jsonArray ?: JsonArray(emptyList())

            var presentes    = 0
            var ausentes     = 0
            var justificados = 0

            attendance.forEach { a ->
                when (a.jsonObject["status"]?.jsonPrimitive?.contentOrNull?.uppercase()) {
                    "PRESENTE", "PRESENT"                    -> presentes++
                    "AUSENTE",  "ABSENT"                     -> ausentes++
                    "JUSTIFICADO", "EXCUSED", "JUSTIFIED"    -> justificados++
                }
            }

            ReunionConStats(
                id            = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                grupoId       = obj["small_group_id"]?.jsonPrimitive?.contentOrNull ?: "",
                fecha         = runCatching {
                    LocalDate.parse(obj["meeting_date"]?.jsonPrimitive?.contentOrNull ?: "")
                }.getOrElse { LocalDate.now() },
                estado        = obj["status"]?.jsonPrimitive?.contentOrNull ?: "BORRADOR",
                noHuboReunion = false,
                presentes     = presentes,
                ausentes      = ausentes,
                justificados  = justificados,
            )
        }
}
