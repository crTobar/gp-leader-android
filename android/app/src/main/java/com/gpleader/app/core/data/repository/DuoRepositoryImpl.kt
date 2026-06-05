package com.gpleader.app.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.LocalDate
import javax.inject.Inject

class DuoRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : DuoRepository {

    override suspend fun getDuosByGrupo(grupoId: String): Result<List<DuoMisioneroData>> = runCatching {
        val rows = supabase.from("missionary_duo").select(
            columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                "id, small_group_id, is_active, " +
                "member1:member1_id(id, first_name, middle_name, last_name, second_last_name, is_active), " +
                "member2:member2_id(id, first_name, middle_name, last_name, second_last_name, is_active)"
            )
        ) {
            filter {
                eq("small_group_id", grupoId)
                eq("is_active", true)
            }
        }.data

        kotlinx.serialization.json.Json.parseToJsonElement(rows).jsonArray.map { el ->
            val obj = el.jsonObject
            DuoMisioneroData(
                id      = obj["id"]!!.jsonPrimitive.content,
                grupoId = obj["small_group_id"]!!.jsonPrimitive.content,
                isActive = obj["is_active"]?.jsonPrimitive?.content?.toBoolean() ?: true,
                member1  = parseMiembroEmbed(obj["member1"]),
                member2  = parseMiembroEmbed(obj["member2"]),
            )
        }
    }

    override suspend fun getDuoPorMiembro(miembroId: String): Result<DuoMisioneroData?> = runCatching {
        // Busca en member1_id O member2_id
        val rows = supabase.from("missionary_duo").select(
            columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                "id, small_group_id, is_active, " +
                "member1:member1_id(id, first_name, middle_name, last_name, second_last_name, is_active), " +
                "member2:member2_id(id, first_name, middle_name, last_name, second_last_name, is_active)"
            )
        ) {
            filter {
                or {
                    eq("member1_id", miembroId)
                    eq("member2_id", miembroId)
                }
                eq("is_active", true)
            }
        }.data

        val arr = kotlinx.serialization.json.Json.parseToJsonElement(rows).jsonArray
        if (arr.isEmpty()) return@runCatching null
        val obj = arr[0].jsonObject
        DuoMisioneroData(
            id       = obj["id"]!!.jsonPrimitive.content,
            grupoId  = obj["small_group_id"]!!.jsonPrimitive.content,
            isActive = obj["is_active"]?.jsonPrimitive?.content?.toBoolean() ?: true,
            member1  = parseMiembroEmbed(obj["member1"]),
            member2  = parseMiembroEmbed(obj["member2"]),
        )
    }

    override suspend fun crearDuo(grupoId: String, member1Id: String, member2Id: String): Result<String> = runCatching {
        val rows = supabase.from("missionary_duo").insert(
            buildJsonObject {
                put("small_group_id", grupoId)
                put("member1_id", member1Id)
                put("member2_id", member2Id)
                put("is_active", true)
            }
        ) { select() }.data
        kotlinx.serialization.json.Json.parseToJsonElement(rows).jsonArray[0].jsonObject["id"]!!.jsonPrimitive.content
    }

    override suspend fun desactivarDuo(duoId: String): Result<Unit> = runCatching {
        supabase.from("missionary_duo").update(
            buildJsonObject { put("is_active", false) }
        ) {
            filter { eq("id", duoId) }
        }
    }

    // ── Actividades del dúo ────────────────────────────────────────────────────

    override suspend fun getActividadesDuo(duoId: String): Result<List<DuoActividadTipo>> = runCatching {
        val rows = supabase.from("duo_activity_type").select {
            filter { eq("duo_id", duoId) }
        }.data
        kotlinx.serialization.json.Json.parseToJsonElement(rows).jsonArray.map { el ->
            val obj = el.jsonObject
            DuoActividadTipo(
                id         = obj["id"]!!.jsonPrimitive.content,
                duoId      = obj["duo_id"]!!.jsonPrimitive.content,
                nombre     = obj["nombre"]!!.jsonPrimitive.content,
                markerType = obj["marker_type"]?.jsonPrimitive?.contentOrNull ?: "counter",
                unitLabel  = obj["unit_label"]?.jsonPrimitive?.contentOrNull ?: "veces",
                startDate  = obj["start_date"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
                endDate    = obj["end_date"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            )
        }
    }

    override suspend fun crearActividadDuo(duoId: String, nombre: String, markerType: String, unitLabel: String, startDate: LocalDate?, endDate: LocalDate?): Result<String> = runCatching {
        val rows = supabase.from("duo_activity_type").insert(
            buildJsonObject {
                put("duo_id", duoId)
                put("nombre", nombre)
                put("marker_type", markerType)
                put("unit_label", unitLabel)
                if (startDate != null) put("start_date", startDate.toString()) else put("start_date", JsonNull)
                if (endDate != null)   put("end_date",   endDate.toString())   else put("end_date",   JsonNull)
            }
        ) { select() }.data
        kotlinx.serialization.json.Json.parseToJsonElement(rows).jsonArray[0].jsonObject["id"]!!.jsonPrimitive.content
    }

    override suspend fun getRegistrosDuo(duoId: String, actividadTipoId: String, desde: LocalDate): Result<List<DuoActividadRecord>> = runCatching {
        val rows = supabase.from("duo_activity_record").select {
            filter {
                eq("duo_id", duoId)
                eq("activity_type_id", actividadTipoId)
                gte("record_date", desde.toString())
            }
        }.data
        parseRegistrosDuo(rows, duoId)
    }

    override suspend fun getRegistrosPorTipoActividad(actividadTipoId: String, duoId: String, desde: LocalDate): Result<List<DuoActividadRecord>> = runCatching {
        val rows = supabase.from("duo_activity_record").select {
            filter {
                eq("activity_type_id", actividadTipoId)
                eq("duo_id", duoId)
                gte("record_date", desde.toString())
            }
        }.data
        parseRegistrosDuo(rows, duoId)
    }

    override suspend fun getActividadesConTotalesPorGrupo(grupoId: String): Result<List<DuoActividadConTotal>> = runCatching {
        val duos = getDuosByGrupo(grupoId).getOrThrow().filter { it.isActive }
        val desde = LocalDate.now().minusDays(90)
        val result = mutableListOf<DuoActividadConTotal>()
        for (duo in duos) {
            val tipos = getActividadesDuo(duo.id).getOrElse { emptyList() }
            for (tipo in tipos) {
                val registros = getRegistrosDuo(duo.id, tipo.id, desde).getOrElse { emptyList() }
                val totalCantidad = registros.sumOf { it.count ?: 0 }
                val diasMarcados  = registros.count { it.isDone }
                result.add(DuoActividadConTotal(
                    tipo          = tipo,
                    duo           = duo,
                    totalCantidad = totalCantidad,
                    montoTotal    = totalCantidad.toDouble(),
                    diasMarcados  = diasMarcados,
                ))
            }
        }
        result
    }

    private fun parseRegistrosDuo(rows: String, duoIdFallback: String): List<DuoActividadRecord> =
        kotlinx.serialization.json.Json.parseToJsonElement(rows).jsonArray.map { el ->
            val obj = el.jsonObject
            DuoActividadRecord(
                id              = obj["id"]!!.jsonPrimitive.content,
                duoId           = obj["duo_id"]?.jsonPrimitive?.contentOrNull ?: duoIdFallback,
                actividadTipoId = obj["activity_type_id"]!!.jsonPrimitive.content,
                recordDate      = LocalDate.parse(obj["record_date"]!!.jsonPrimitive.content),
                count           = obj["count"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.intOrNull,
                isDone          = obj["is_done"]?.jsonPrimitive?.content?.toBoolean() ?: false,
                updatedBy       = obj["updated_by"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
                updatedAt       = obj["updated_at"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull
                                    ?.let { runCatching { java.time.Instant.parse(it) }.getOrNull() },
            )
        }

    override suspend fun upsertRegistroDuo(duoId: String, actividadTipoId: String, fecha: LocalDate, count: Int?, isDone: Boolean, updatedBy: String): Result<Unit> = runCatching {
        supabase.from("duo_activity_record").upsert(
            buildJsonObject {
                put("duo_id", duoId)
                put("activity_type_id", actividadTipoId)
                put("record_date", fecha.toString())
                if (count != null) put("count", count)
                put("is_done", isDone)
                put("updated_by", updatedBy)
                put("updated_at", java.time.Instant.now().toString())
            }
        ) {
            onConflict = "duo_id,activity_type_id,record_date"
        }
    }

    // ── Estudios bíblicos del dúo ─────────────────────────────────────────────

    override suspend fun getEstudiosDuo(duoId: String): Result<List<DuoBibleStudy>> = runCatching {
        val rows = supabase.from("duo_bible_study").select {
            filter { eq("duo_id", duoId) }
        }.data
        kotlinx.serialization.json.Json.parseToJsonElement(rows).jsonArray.map { el ->
            val obj = el.jsonObject
            val lessonsArr = obj["completed_lessons"]?.takeIf { it !is JsonNull }
                ?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()
            DuoBibleStudy(
                id               = obj["id"]!!.jsonPrimitive.content,
                duoId            = obj["duo_id"]!!.jsonPrimitive.content,
                studentName      = obj["student_name"]!!.jsonPrimitive.content,
                completedLessons = lessonsArr,
            )
        }
    }

    override suspend fun getEstudioDuoById(estudioId: String): Result<DuoBibleStudy?> = runCatching {
        val rows = supabase.from("duo_bible_study").select {
            filter { eq("id", estudioId) }
            limit(1)
        }.data
        val arr = kotlinx.serialization.json.Json.parseToJsonElement(rows).jsonArray
        if (arr.isEmpty()) return@runCatching null
        val obj = arr[0].jsonObject
        val lessonsArr = obj["completed_lessons"]?.takeIf { it !is JsonNull }
            ?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()
        DuoBibleStudy(
            id               = obj["id"]!!.jsonPrimitive.content,
            duoId            = obj["duo_id"]!!.jsonPrimitive.content,
            studentName      = obj["student_name"]!!.jsonPrimitive.content,
            completedLessons = lessonsArr,
        )
    }

    override suspend fun crearEstudioDuo(duoId: String, studentName: String): Result<String> = runCatching {
        val rows = supabase.from("duo_bible_study").insert(
            buildJsonObject {
                put("duo_id", duoId)
                put("student_name", studentName)
            }
        ) { select() }.data
        kotlinx.serialization.json.Json.parseToJsonElement(rows).jsonArray[0].jsonObject["id"]!!.jsonPrimitive.content
    }

    override suspend fun toggleLeccionDuo(estudioId: String, leccion: Int, completado: Boolean): Result<Unit> = runCatching {
        val current = supabase.from("duo_bible_study").select {
            filter { eq("id", estudioId) }
        }.data
        val obj = kotlinx.serialization.json.Json.parseToJsonElement(current).jsonArray[0].jsonObject
        val lessons = obj["completed_lessons"]?.takeIf { it !is JsonNull }
            ?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull }?.toMutableList() ?: mutableListOf()
        if (completado) { if (!lessons.contains(leccion)) lessons.add(leccion) }
        else lessons.remove(leccion)

        supabase.from("duo_bible_study").update(
            buildJsonObject {
                put("completed_lessons", kotlinx.serialization.json.buildJsonArray {
                    lessons.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) }
                })
            }
        ) {
            filter { eq("id", estudioId) }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun parseMiembroEmbed(el: kotlinx.serialization.json.JsonElement?): MiembroData {
        val obj = el?.takeIf { it !is JsonNull }?.jsonObject ?: return MiembroData(
            id = "", grupoId = "", primerNombre = "?", primerApellido = ""
        )
        return MiembroData(
            id            = obj["id"]!!.jsonPrimitive.content,
            grupoId       = "",
            primerNombre  = obj["first_name"]?.jsonPrimitive?.contentOrNull ?: "",
            segundoNombre = obj["middle_name"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            primerApellido  = obj["last_name"]?.jsonPrimitive?.contentOrNull ?: "",
            segundoApellido = obj["second_last_name"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
        )
    }
}
