package com.gpleader.app.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class ActividadRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : ActividadRepository {

    private fun parseActividadTipo(obj: kotlinx.serialization.json.JsonObject): ActividadTipoData =
        ActividadTipoData(
            id                 = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
            nombre             = obj["name"]?.jsonPrimitive?.contentOrNull ?: "",
            level              = obj["level"]?.jsonPrimitive?.contentOrNull ?: "my_group",
            markerType         = obj["marker_type"]?.jsonPrimitive?.contentOrNull ?: "counter",
            unitLabel          = obj["unit_label"]?.jsonPrimitive?.contentOrNull ?: "",
            sortOrder          = obj["sort_order"]?.jsonPrimitive?.intOrNull ?: 0,
            scope              = obj["scope"]?.jsonPrimitive?.contentOrNull ?: "global",
            churchId           = obj["church_id"]?.jsonPrimitive?.contentOrNull,
            startDate          = obj["start_date"]?.jsonPrimitive?.contentOrNull?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            },
            endDate            = obj["end_date"]?.jsonPrimitive?.contentOrNull?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            },
            frecuencia         = obj["frecuencia"]?.jsonPrimitive?.contentOrNull ?: "semanal",
            isMemberAccessible = obj["is_member_accessible"]?.jsonPrimitive?.booleanOrNull ?: false,
            districtId         = obj["district_id"]?.jsonPrimitive?.contentOrNull,
            campoId            = obj["campo_id"]?.jsonPrimitive?.contentOrNull,
            grupoId            = obj["small_group_id"]?.jsonPrimitive?.contentOrNull,
        )

    private val ACTIVIDAD_COLUMNS = "id, name, level, marker_type, unit_label, sort_order, scope, church_id, district_id, campo_id, small_group_id, start_date, end_date, frecuencia, is_member_accessible"

    private fun scopeOk(
        tipo: ActividadTipoData,
        iglesiaId: String,
        districtId: String,
        campoId: String,
        grupoId: String = "",
    ): Boolean = when (tipo.scope) {
        "global"   -> true
        "church"   -> tipo.churchId   == iglesiaId
        "district" -> tipo.districtId != null && tipo.districtId == districtId
        "campo"    -> tipo.campoId    != null && tipo.campoId    == campoId
        "group"    -> tipo.grupoId    != null && tipo.grupoId    == grupoId
        else       -> false
    }

    override suspend fun getActividadesTipo(iglesiaId: String, districtId: String, campoId: String, grupoId: String): Result<List<ActividadTipoData>> = runCatching {
        val data = supabase.from("activity_type").select(
            columns = Columns.raw(ACTIVIDAD_COLUMNS)
        ) {
            filter { eq("is_active", true) }
        }.data

        val hoy = LocalDate.now()
        Json.parseToJsonElement(data).jsonArray
            .map { parseActividadTipo(it.jsonObject) }
            .filter { tipo ->
                val periodoOk = (tipo.startDate == null || !hoy.isBefore(tipo.startDate)) &&
                                (tipo.endDate   == null || !hoy.isAfter(tipo.endDate))
                scopeOk(tipo, iglesiaId, districtId, campoId, grupoId) && periodoOk
            }
            .sortedBy { it.sortOrder }
    }

    override suspend fun getTodasActividadesTipo(iglesiaId: String, districtId: String, campoId: String, grupoId: String): Result<List<ActividadTipoData>> = runCatching {
        val data = supabase.from("activity_type").select(
            columns = Columns.raw(ACTIVIDAD_COLUMNS)
        ) {
            filter { eq("is_active", true) }
        }.data

        Json.parseToJsonElement(data).jsonArray
            .map { parseActividadTipo(it.jsonObject) }
            .filter { tipo -> scopeOk(tipo, iglesiaId, districtId, campoId, grupoId) }
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

    override suspend fun getActividadesConTotales(grupoId: String): Result<Map<String, ActividadTotalData>> = runCatching {
        val totals = mutableMapOf<String, Pair<Int, Double>>()

        // Registros del líder por reunión
        val gpData = supabase.from("activity_record").select(
            Columns.raw("activity_type_id, count, monto, meeting!inner(small_group_id)")
        ) {
            filter { eq("meeting.small_group_id", grupoId) }
        }.data
        Json.parseToJsonElement(gpData).jsonArray.forEach { elem ->
            val obj    = elem.jsonObject
            val tipoId = obj["activity_type_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
            val count  = obj["count"]?.jsonPrimitive?.intOrNull ?: 0
            val monto  = obj["monto"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            val prev   = totals[tipoId] ?: (0 to 0.0)
            totals[tipoId] = (prev.first + count) to (prev.second + monto)
        }

        // Registros individuales de miembros — two-step to avoid fragile embedded join filter
        val memberIds = runCatching {
            val mData = supabase.from("member").select(Columns.raw("id")) {
                filter {
                    eq("small_group_id", grupoId)
                    eq("is_visitor", false)
                }
            }.data
            Json.parseToJsonElement(mData).jsonArray.mapNotNull {
                it.jsonObject["id"]?.jsonPrimitive?.contentOrNull
            }
        }.getOrElse { emptyList() }

        if (memberIds.isNotEmpty()) {
            val memberData = supabase.from("member_activity_record").select(
                Columns.raw("activity_type_id, count, is_done, status")
            ) {
                filter {
                    isIn("member_id", memberIds)
                    isIn("status", listOf("approved", "pending_board"))
                }
            }.data
            Json.parseToJsonElement(memberData).jsonArray.forEach { elem ->
                val obj    = elem.jsonObject
                val tipoId = obj["activity_type_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                val count  = obj["count"]?.jsonPrimitive?.intOrNull
                val isDone = obj["is_done"]?.jsonPrimitive?.booleanOrNull ?: false
                val aporte = count ?: if (isDone) 1 else 0
                val prev   = totals[tipoId] ?: (0 to 0.0)
                totals[tipoId] = (prev.first + aporte) to prev.second
            }
        }

        totals.mapValues { (_, v) -> ActividadTotalData(v.first, v.second) }
    }

    override suspend fun getRegistrosSemanal(grupoId: String, actividadTipoId: String): Result<List<RegistroSemanalData>> = runCatching {
        // Registros del líder por reunión
        val gpData = supabase.from("activity_record").select(
            Columns.raw("id, count, monto, notes, meeting_id, meeting!inner(meeting_date, small_group_id)")
        ) {
            filter {
                eq("activity_type_id", actividadTipoId)
                eq("meeting.small_group_id", grupoId)
            }
        }.data

        val gpRecords = Json.parseToJsonElement(gpData).jsonArray
            .mapNotNull { elem ->
                val obj        = elem.jsonObject
                val meetingObj = obj["meeting"]?.jsonObject ?: return@mapNotNull null
                val dateStr    = meetingObj["meeting_date"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val meetingDate = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: return@mapNotNull null
                RegistroSemanalData(
                    recordId    = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                    meetingId   = obj["meeting_id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                    meetingDate = meetingDate,
                    cantidad    = obj["count"]?.jsonPrimitive?.intOrNull,
                    monto       = obj["monto"]?.jsonPrimitive?.doubleOrNull,
                    notas       = obj["notes"]?.jsonPrimitive?.contentOrNull,
                )
            }
            .sortedByDescending { it.meetingDate }

        // Aportes individuales de miembros, agrupados por semana — two-step to avoid embedded join filter
        val memberIds2 = runCatching {
            val mData = supabase.from("member").select(Columns.raw("id")) {
                filter {
                    eq("small_group_id", grupoId)
                    eq("is_visitor", false)
                }
            }.data
            Json.parseToJsonElement(mData).jsonArray.mapNotNull {
                it.jsonObject["id"]?.jsonPrimitive?.contentOrNull
            }
        }.getOrElse { emptyList() }

        val aportesPorSemana = mutableMapOf<LocalDate, Int>()
        if (memberIds2.isNotEmpty()) {
            val memberData = supabase.from("member_activity_record").select(
                Columns.raw("count, is_done, record_date")
            ) {
                filter {
                    eq("activity_type_id", actividadTipoId)
                    isIn("member_id", memberIds2)
                }
            }.data
            Json.parseToJsonElement(memberData).jsonArray.forEach { elem ->
                val obj     = elem.jsonObject
                val dateStr = obj["record_date"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                val recDate = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: return@forEach
                val lunes   = inicioSemana(recDate)
                val count   = obj["count"]?.jsonPrimitive?.intOrNull
                val isDone  = obj["is_done"]?.jsonPrimitive?.booleanOrNull ?: false
                val aporte  = count ?: if (isDone) 1 else 0
                aportesPorSemana[lunes] = (aportesPorSemana[lunes] ?: 0) + aporte
            }
        }

        gpRecords.map { registro ->
            val lunes = inicioSemana(registro.meetingDate)
            registro.copy(aportesMiembros = aportesPorSemana[lunes] ?: 0)
        }
    }

    private fun inicioSemana(date: LocalDate): LocalDate {
        var d = date
        while (d.dayOfWeek != DayOfWeek.MONDAY) d = d.minusDays(1)
        return d
    }

    override suspend fun updateRegistro(recordId: String, cantidad: Int?, monto: Double?): Result<Unit> = runCatching {
        supabase.from("activity_record").update(
            buildJsonObject {
                if (cantidad != null) put("count", cantidad)
                if (monto != null)    put("monto", monto)
            }
        ) {
            filter { eq("id", recordId) }
        }
    }

    override suspend fun getActividadesMiembro(iglesiaId: String, districtId: String, campoId: String, grupoId: String): Result<List<ActividadTipoData>> = runCatching {
        val data = supabase.from("activity_type").select(
            columns = Columns.raw(ACTIVIDAD_COLUMNS)
        ) {
            filter {
                eq("is_active", true)
                eq("is_member_accessible", true)
            }
        }.data

        val hoy = LocalDate.now()
        Json.parseToJsonElement(data).jsonArray
            .map { parseActividadTipo(it.jsonObject) }
            .filter { tipo ->
                val periodoOk = (tipo.startDate == null || !hoy.isBefore(tipo.startDate)) &&
                                (tipo.endDate   == null || !hoy.isAfter(tipo.endDate))
                scopeOk(tipo, iglesiaId, districtId, campoId, grupoId) && periodoOk
            }
            .sortedBy { it.sortOrder }
    }

    override suspend fun getRegistrosMiembro(
        miembroId: String,
        actividadTipoId: String,
        desde: LocalDate,
    ): Result<List<LocalDate>> = runCatching {
        val data = supabase.from("member_activity_record").select(
            Columns.raw("record_date")
        ) {
            filter {
                eq("member_id", miembroId)
                eq("activity_type_id", actividadTipoId)
                eq("is_done", true)
                gte("record_date", desde.toString())
            }
        }.data

        Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            elem.jsonObject["record_date"]?.jsonPrimitive?.contentOrNull?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            }
        }
    }

    override suspend fun toggleMiembroActividad(
        miembroId: String,
        actividadTipoId: String,
        fecha: LocalDate,
        isDone: Boolean,
    ): Result<Unit> = runCatching {
        if (isDone) {
            supabase.from("member_activity_record").upsert(
                buildJsonObject {
                    put("member_id", miembroId)
                    put("activity_type_id", actividadTipoId)
                    put("record_date", fecha.toString())
                    put("is_done", true)
                    put("status", "draft")
                }
            ) {
                onConflict = "member_id,activity_type_id,record_date"
            }
        } else {
            supabase.from("member_activity_record").delete {
                filter {
                    eq("member_id", miembroId)
                    eq("activity_type_id", actividadTipoId)
                    eq("record_date", fecha.toString())
                }
            }
        }
    }

    override suspend fun getContadorSemanalMiembro(
        miembroId: String,
        actividadTipoId: String,
        semanaStart: LocalDate,
    ): Result<Int> = runCatching {
        val data = supabase.from("member_activity_record").select(
            Columns.raw("count")
        ) {
            filter {
                eq("member_id", miembroId)
                eq("activity_type_id", actividadTipoId)
                eq("record_date", semanaStart.toString())
            }
        }.data

        Json.parseToJsonElement(data).jsonArray
            .firstOrNull()
            ?.jsonObject
            ?.get("count")
            ?.jsonPrimitive
            ?.intOrNull
            ?: 0
    }

    override suspend fun upsertContadorSemanalMiembro(
        miembroId: String,
        actividadTipoId: String,
        semanaStart: LocalDate,
        count: Int,
    ): Result<Unit> = runCatching {
        supabase.from("member_activity_record").upsert(
            buildJsonObject {
                put("member_id", miembroId)
                put("activity_type_id", actividadTipoId)
                put("record_date", semanaStart.toString())
                put("count", count)
                put("is_done", count > 0)
                put("status", "draft")
            }
        ) {
            onConflict = "member_id,activity_type_id,record_date"
        }
    }

    override suspend fun getPendingMemberActivities(grupoId: String, actividadTipoId: String): Result<List<MemberActivitySubmission>> = runCatching {
        val mData = supabase.from("member").select(Columns.raw("id, first_name, last_name")) {
            filter {
                eq("small_group_id", grupoId)
                eq("is_visitor", false)
            }
        }.data
        val members = Json.parseToJsonElement(mData).jsonArray.associate { elem ->
            val obj  = elem.jsonObject
            val id   = obj["id"]?.jsonPrimitive?.contentOrNull ?: ""
            val name = "${obj["first_name"]?.jsonPrimitive?.contentOrNull ?: ""} ${obj["last_name"]?.jsonPrimitive?.contentOrNull ?: ""}".trim()
            id to name
        }
        if (members.isEmpty()) return@runCatching emptyList()

        val data = supabase.from("member_activity_record").select(
            Columns.raw("id, member_id, activity_type_id, record_date, count, monto, is_done, status")
        ) {
            filter {
                eq("activity_type_id", actividadTipoId)
                isIn("member_id", members.keys.toList())
                eq("status", "draft")
            }
        }.data

        Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            val obj       = elem.jsonObject
            val miembroId = obj["member_id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val dateStr   = obj["record_date"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val recordDate = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: return@mapNotNull null
            MemberActivitySubmission(
                recordId      = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                miembroId     = miembroId,
                miembroNombre = members[miembroId] ?: "Miembro",
                recordDate    = recordDate,
                count         = obj["count"]?.jsonPrimitive?.intOrNull,
                monto         = obj["monto"]?.jsonPrimitive?.doubleOrNull,
                isDone        = obj["is_done"]?.jsonPrimitive?.booleanOrNull ?: false,
                status        = obj["status"]?.jsonPrimitive?.contentOrNull ?: "draft",
            )
        }.sortedByDescending { it.recordDate }
    }

    override suspend fun getPendingCountPerTipo(grupoId: String): Result<Map<String, Int>> = runCatching {
        val memberIds = runCatching {
            val mData = supabase.from("member").select(Columns.raw("id")) {
                filter {
                    eq("small_group_id", grupoId)
                    eq("is_visitor", false)
                }
            }.data
            Json.parseToJsonElement(mData).jsonArray.mapNotNull {
                it.jsonObject["id"]?.jsonPrimitive?.contentOrNull
            }
        }.getOrElse { emptyList() }
        if (memberIds.isEmpty()) return@runCatching emptyMap()

        val data = supabase.from("member_activity_record").select(
            Columns.raw("activity_type_id")
        ) {
            filter {
                isIn("member_id", memberIds)
                eq("status", "draft")
            }
        }.data
        val counts = mutableMapOf<String, Int>()
        Json.parseToJsonElement(data).jsonArray.forEach { elem ->
            val tipoId = elem.jsonObject["activity_type_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
            counts[tipoId] = (counts[tipoId] ?: 0) + 1
        }
        counts
    }

    override suspend fun approveMemberActivity(recordId: String, correctedCount: Int?, correctedMonto: Double?, isMonetary: Boolean): Result<Unit> = runCatching {
        supabase.from("member_activity_record").update(
            buildJsonObject {
                put("status", if (isMonetary) "pending_board" else "approved")
                if (correctedCount != null) put("count", correctedCount)
                if (correctedMonto != null) put("monto", correctedMonto)
            }
        ) {
            filter { eq("id", recordId) }
        }
    }

    override suspend fun rejectMemberActivity(recordId: String): Result<Unit> = runCatching {
        supabase.from("member_activity_record").update(
            buildJsonObject { put("status", "rejected") }
        ) {
            filter { eq("id", recordId) }
        }
    }

    override suspend fun saveActividadTipo(
        nombre: String,
        level: String,
        markerType: String,
        frecuencia: String,
        unitLabel: String,
        isMemberAccessible: Boolean,
        iglesiaId: String,
        grupoId: String?,
        scope: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Result<Unit> = runCatching {
        val valueType = when (markerType) {
            "monetary" -> "monetary"
            "checkbox" -> "boolean"
            else       -> "discrete"
        }
        supabase.from("activity_type").insert(
            buildJsonObject {
                put("name", nombre)
                put("level", level)
                put("marker_type", markerType)
                put("value_type", valueType)
                put("frecuencia", frecuencia)
                put("unit_label", unitLabel)
                put("is_member_accessible", isMemberAccessible)
                put("scope", scope)
                if (scope == "church") put("church_id", iglesiaId)
                if (scope == "group" && grupoId != null) put("small_group_id", grupoId)
                if (startDate != null) put("start_date", startDate.toString())
                if (endDate != null)   put("end_date", endDate.toString())
                put("is_active", true)
            }
        )
    }

}
