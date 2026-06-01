package com.gpleader.app.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
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
            filter {
                eq("small_group_id", grupoId)
                eq("registry_kind", "gp_meeting")
            }
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
        tipoReunion:   String,
        status:        String,
    ): Result<String> = runCatching {
        // 1 — Insertar la reunión y recuperar el ID generado
        val meetingPayload = buildJsonObject {
            put("small_group_id", grupoId)
            put("meeting_date",   fecha.toString())
            put("status",         status)
            put("registry_kind",  tipoReunion)
            put("no_meeting",     noHuboReunion)
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
            val asistenciasMap = (
                asistencias.filter { !it.esVisita && it.miembroId != null } +
                asistencias.filter { it.esVisita && it.miembroId != null }
            ).associateBy { it.miembroId!! }

            val attendancePayload = buildJsonArray {
                todasConId.forEach { (memberId, estado) ->
                    addJsonObject {
                        put("meeting_id", meetingId)
                        put("member_id",  memberId)
                        put("status",     mapEstadoAsistencia(estado))
                        val iglesiaId = asistenciasMap[memberId]?.iglesiaVisitadaId
                        if (iglesiaId != null) put("visited_church_id", iglesiaId)
                    }
                }
            }
            supabase.from("attendance").insert(attendancePayload)
        }

        meetingId
    }

    override suspend fun getDetalleReunion(reunionId: String): Result<DetalleReunionData> = runCatching {
        val data = supabase.from("meeting").select(
            columns = Columns.raw(
                "id, meeting_date, status, registry_kind, no_meeting, " +
                "attendance(member_id, status, visited_church_id, church(nombre:name), " +
                "member(first_name, middle_name, last_name, second_last_name, is_visitor)), " +
                "activity_record(count, monto, notes, activity_type(name, level, unit_label))"
            )
        ) {
            filter { eq("id", reunionId) }
        }.data

        val arr = Json.parseToJsonElement(data).jsonArray
        val obj = arr.firstOrNull()?.jsonObject
            ?: error("Reunión no encontrada")

        val attendance = obj["attendance"]?.jsonArray ?: JsonArray(emptyList())

        var presentes    = 0
        var ausentes     = 0
        var justificados = 0
        val asistencias  = mutableListOf<AsistenciaConNombre>()

        attendance.forEach { elem ->
            val a        = elem.jsonObject
            val memberId = a["member_id"]?.jsonPrimitive?.contentOrNull
            val status   = a["status"]?.jsonPrimitive?.contentOrNull ?: ""
            val member   = a["member"]?.takeIf { it !is JsonNull }?.jsonObject
            val church   = a["church"]?.takeIf { it !is JsonNull }?.jsonObject

            val estadoDisplay = when (status.uppercase()) {
                "PRESENT"   -> { presentes++;    "P" }
                "ABSENT"    -> { ausentes++;     "A" }
                "JUSTIFIED", "EXCUSED" -> { justificados++; "J" }
                else        -> { ausentes++;     "A" }
            }

            if (member != null) {
                val firstName      = member["first_name"]?.jsonPrimitive?.contentOrNull ?: ""
                val middleName     = member["middle_name"]?.jsonPrimitive?.contentOrNull
                val lastName       = member["last_name"]?.jsonPrimitive?.contentOrNull ?: ""
                val secondLastName = member["second_last_name"]?.jsonPrimitive?.contentOrNull
                val isVisitor      = member["is_visitor"]?.jsonPrimitive?.booleanOrNull ?: false
                val iglesiaVisitada = church?.get("nombre")?.jsonPrimitive?.contentOrNull

                val nombre = buildString {
                    append(firstName)
                    if (!middleName.isNullOrBlank()) { append(" "); append(middleName) }
                    append(" "); append(lastName)
                    if (!secondLastName.isNullOrBlank()) { append(" "); append(secondLastName) }
                }.trim()

                asistencias.add(AsistenciaConNombre(
                    memberId              = memberId,
                    nombre                = nombre,
                    estado                = estadoDisplay,
                    esVisita              = isVisitor,
                    iglesiaVisitadaNombre = iglesiaVisitada,
                ))
            }
        }

        val actRecords = obj["activity_record"]?.jsonArray ?: JsonArray(emptyList())
        val actividades = actRecords.mapNotNull { elem ->
            val r    = elem.jsonObject
            val tipo = r["activity_type"]?.takeIf { it !is JsonNull }?.jsonObject ?: return@mapNotNull null
            ActividadConDetalle(
                nombre   = tipo["name"]?.jsonPrimitive?.contentOrNull ?: "",
                nivel    = tipo["level"]?.jsonPrimitive?.contentOrNull ?: "my_group",
                cantidad = r["count"]?.jsonPrimitive?.intOrNull,
                monto    = r["monto"]?.jsonPrimitive?.doubleOrNull,
                unidad   = tipo["unit_label"]?.jsonPrimitive?.contentOrNull ?: "",
            )
        }

        DetalleReunionData(
            id           = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
            fecha        = runCatching {
                LocalDate.parse(obj["meeting_date"]?.jsonPrimitive?.contentOrNull ?: "")
            }.getOrElse { LocalDate.now() },
            estado       = obj["status"]?.jsonPrimitive?.contentOrNull ?: "draft",
            presentes    = presentes,
            ausentes     = ausentes,
            justificados = justificados,
            asistencias  = asistencias,
            actividades  = actividades,
            tipoReunion  = obj["registry_kind"]?.jsonPrimitive?.contentOrNull ?: "gp_meeting",
        )
    }

    override suspend fun getSabbathMeeting(grupoId: String, fecha: LocalDate): Result<SabbathMeetingResumen?> = runCatching {
        val data = supabase.from("meeting").select(
            columns = Columns.raw("id, meeting_date, status, attendance(status)")
        ) {
            filter {
                eq("small_group_id", grupoId)
                eq("registry_kind", "saturday_worship")
                eq("meeting_date",  fecha.toString())
            }
            limit(1)
        }.data

        val arr = Json.parseToJsonElement(data).jsonArray
        val obj = arr.firstOrNull()?.jsonObject ?: return@runCatching null

        val attendance = obj["attendance"]?.jsonArray ?: JsonArray(emptyList())
        val presentes  = attendance.count {
            it.jsonObject["status"]?.jsonPrimitive?.contentOrNull?.uppercase() == "PRESENT"
        }

        // Total real de miembros activos del grupo (no visitors)
        val totalMiembros = runCatching {
            supabase.from("member").select {
                count(Count.EXACT)
                filter {
                    eq("small_group_id", grupoId)
                    eq("is_visitor",     false)
                    eq("is_active",      true)
                }
            }.countOrNull()?.toInt() ?: attendance.size
        }.getOrElse { attendance.size }

        SabbathMeetingResumen(
            id            = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
            fecha         = runCatching {
                LocalDate.parse(obj["meeting_date"]?.jsonPrimitive?.contentOrNull ?: "")
            }.getOrElse { LocalDate.now() },
            status        = obj["status"]?.jsonPrimitive?.contentOrNull ?: "draft",
            presentes     = presentes,
            totalMiembros = totalMiembros,
        )
    }

    override suspend fun saveDraftAttendance(
        meetingId: String,
        memberId:  String,
        presente:  Boolean,
        iglesiaId: String?,
    ): Result<Unit> = runCatching {
        val payload = buildJsonObject {
            put("meeting_id", meetingId)
            put("member_id",  memberId)
            put("status",     if (presente) "present" else "absent")
            if (iglesiaId != null) put("visited_church_id", iglesiaId)
        }
        supabase.from("attendance").upsert(payload) {
            onConflict = "meeting_id,member_id"
        }
    }

    override suspend fun submitSabbathMeeting(
        meetingId:  String,
        asistencias: List<AsistenciaParaGuardar>,
    ): Result<Unit> = runCatching {
        // Actualizar estado del meeting
        val updatePayload = buildJsonObject { put("status", "submitted") }
        supabase.from("meeting").update(updatePayload) {
            filter { eq("id", meetingId) }
        }

        // Upsert asistencias
        if (asistencias.isNotEmpty()) {
            val attendancePayload = buildJsonArray {
                asistencias.forEach { a ->
                    if (a.miembroId != null) {
                        addJsonObject {
                            put("meeting_id", meetingId)
                            put("member_id",  a.miembroId)
                            put("status",     mapEstadoAsistencia(a.estado))
                            if (a.iglesiaVisitadaId != null) put("visited_church_id", a.iglesiaVisitadaId)
                        }
                    }
                }
            }
            supabase.from("attendance").upsert(attendancePayload) {
                onConflict = "meeting_id,member_id"
            }
        }
    }

    private fun mapEstadoAsistencia(estado: String): String = when (estado.uppercase()) {
        "PRESENTE", "PRESENT"                 -> "present"
        "AUSENTE",  "ABSENT"                  -> "absent"
        "JUSTIFICADO", "JUSTIFIED", "EXCUSED" -> "justified"
        else                                  -> "present"
    }

    private fun parseReuniones(data: String): List<ReunionConStats> {
        return Json.parseToJsonElement(data).jsonArray.map { elem ->
            val obj        = elem.jsonObject
            val attendance = obj["attendance"]?.jsonArray ?: JsonArray(emptyList())

            var presentes    = 0
            var ausentes     = 0
            var justificados = 0

            attendance.forEach { a ->
                val status = a.jsonObject["status"]?.jsonPrimitive?.contentOrNull
                when (status?.uppercase()) {
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
                noHuboReunion = obj["no_meeting"]?.jsonPrimitive?.booleanOrNull ?: false,
                presentes     = presentes,
                ausentes      = ausentes,
                justificados  = justificados,
            )
        }
    }
}
