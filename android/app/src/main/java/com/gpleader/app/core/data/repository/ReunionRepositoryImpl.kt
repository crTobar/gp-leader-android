package com.gpleader.app.core.data.repository

import com.gpleader.app.core.data.local.room.dao.ActivityRecordDao
import com.gpleader.app.core.data.local.room.dao.ActivityTypeDao
import com.gpleader.app.core.data.local.room.dao.AttendanceDao
import com.gpleader.app.core.data.local.room.dao.MeetingDao
import com.gpleader.app.core.data.local.room.dao.MemberDao
import com.gpleader.app.core.data.local.room.entity.ActivityRecordEntity
import com.gpleader.app.core.data.local.room.entity.AttendanceEntity
import com.gpleader.app.core.data.local.room.entity.MeetingEntity
import com.gpleader.app.core.data.network.NetworkMonitor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
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
    private val supabase:      SupabaseClient,
    private val network:       NetworkMonitor,
    private val meetingDao:    MeetingDao,
    private val attendanceDao: AttendanceDao,
    private val activityRecordDao: ActivityRecordDao,
    private val activityTypeDao:   ActivityTypeDao,
    private val memberDao:     MemberDao,
) : ReunionRepository {

    // Horizonte de caché: solo se persiste el último año.
    private fun cacheCutoff(): LocalDate = LocalDate.now().minusYears(1)

    /** Lee online (y cachea); si no hay red o la llamada falla, cae al caché de Room. */
    private suspend fun <T> cachedRead(offline: suspend () -> T, online: suspend () -> T): T {
        if (!network.isOnline()) return offline()
        return try { online() }
        catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) { offline() }
    }

    override fun getReuniones(grupoId: String, limit: Int): Flow<List<ReunionConStats>> =
        reunionesFlow(grupoId, "gp_meeting", limit)

    override fun getReunionesSabado(grupoId: String, limit: Int): Flow<List<ReunionConStats>> =
        reunionesFlow(grupoId, "saturday_worship", limit)

    private fun reunionesFlow(grupoId: String, kind: String, limit: Int): Flow<List<ReunionConStats>> = flow {
        val all = cachedRead(
            offline = { reunionesDesdeRoom(grupoId, kind) },
            online = {
                val data = supabase.from("meeting").select(columns = Columns.raw("*, attendance(*)")) {
                    filter {
                        eq("small_group_id", grupoId)
                        eq("registry_kind", kind)
                    }
                }.data
                cacheMeetings(data)   // upsert del último año
                parseReuniones(data).sortedByDescending { it.fecha }
            },
        )
        emit(if (limit == Int.MAX_VALUE) all else all.take(limit))
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
        cachedRead(offline = { detalleDesdeRoom(reunionId) }, online = { detalleOnline(reunionId) })
    }

    private suspend fun detalleOnline(reunionId: String): DetalleReunionData {
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

        // Cachear las filas de esta reunión (meeting + attendance + activity_record) para offline.
        runCatching { cacheMeetingDetalle(reunionId) }

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

        return DetalleReunionData(
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
        if (!network.isOnline()) return@runCatching sabbathDesdeRoom(grupoId, fecha)
        try {
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
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            sabbathDesdeRoom(grupoId, fecha)
        }
    }

    override suspend fun getReunionesRecientesSabado(grupoId: String, limit: Int): Result<List<SabbathMeetingResumen>> = runCatching {
        val offline: suspend () -> List<SabbathMeetingResumen> = {
            reunionesDesdeRoom(grupoId, "saturday_worship")
                .take(limit)
                .map { SabbathMeetingResumen(it.id, it.fecha, it.estado, it.presentes, totalMiembros = 0) }
        }
        if (!network.isOnline()) return@runCatching offline()
        try {
        val data = supabase.from("meeting").select(
            columns = Columns.raw("id, meeting_date, status, attendance(status)")
        ) {
            filter {
                eq("small_group_id", grupoId)
                eq("registry_kind",  "saturday_worship")
            }
            order("meeting_date", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            limit(limit.toLong())
        }.data

        val arr = Json.parseToJsonElement(data).jsonArray
        arr.map { element ->
            val obj        = element.jsonObject
            val attendance = obj["attendance"]?.takeIf { it !is JsonNull }?.jsonArray ?: JsonArray(emptyList())
            val presentes  = attendance.count {
                it.jsonObject["status"]?.jsonPrimitive?.contentOrNull?.uppercase() == "PRESENT"
            }
            SabbathMeetingResumen(
                id            = obj["id"]!!.jsonPrimitive.content,
                fecha         = LocalDate.parse(obj["meeting_date"]!!.jsonPrimitive.content),
                status        = obj["status"]?.jsonPrimitive?.contentOrNull ?: "draft",
                presentes     = presentes,
                totalMiembros = 0,
            )
        }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            offline()
        }
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

    // ── Caché offline (Room) ─────────────────────────────────────────────────────

    /** Upsert de meeting + attendance del JSON de lista ("*, attendance(*)"); solo el último año. */
    private suspend fun cacheMeetings(data: String) {
        val cutoff = cacheCutoff()
        val meetings    = mutableListOf<MeetingEntity>()
        val attendances = mutableListOf<AttendanceEntity>()
        Json.parseToJsonElement(data).jsonArray.forEach { elem ->
            val obj   = elem.jsonObject
            val fecha = obj["meeting_date"]?.jsonPrimitive?.contentOrNull
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@forEach
            if (fecha.isBefore(cutoff)) return@forEach
            obj.toMeetingEntity()?.let { meetings += it }
            obj["attendance"]?.takeIf { it !is JsonNull }?.jsonArray?.forEach { a ->
                a.jsonObject.toAttendanceEntity()?.let { attendances += it }
            }
        }
        if (meetings.isNotEmpty())    meetingDao.upsert(meetings)
        if (attendances.isNotEmpty()) attendanceDao.upsert(attendances)
    }

    /** Cachea una reunión completa (meeting + attendance + activity_record) para verla offline. */
    private suspend fun cacheMeetingDetalle(meetingId: String) {
        val raw = supabase.from("meeting").select(Columns.raw("*, attendance(*), activity_record(*)")) {
            filter { eq("id", meetingId) }
        }.data
        val obj = Json.parseToJsonElement(raw).jsonArray.firstOrNull()?.jsonObject ?: return
        obj.toMeetingEntity()?.let { meetingDao.upsert(listOf(it)) }
        // Reemplaza (borra + inserta) las hijas: si una asistencia/registro se quitó en el
        // servidor, el upsert-solo la dejaría obsoleta. replaceForMeeting la elimina.
        val attendance = obj["attendance"]?.takeIf { it !is JsonNull }?.jsonArray
            ?.mapNotNull { it.jsonObject.toAttendanceEntity() } ?: emptyList()
        attendanceDao.replaceForMeeting(meetingId, attendance)
        val records = obj["activity_record"]?.takeIf { it !is JsonNull }?.jsonArray
            ?.mapNotNull { it.jsonObject.toActivityRecordEntity() } ?: emptyList()
        activityRecordDao.replaceForMeeting(meetingId, records)
    }

    private suspend fun reunionesDesdeRoom(grupoId: String, kind: String): List<ReunionConStats> {
        val meetings = meetingDao.getByGroupKind(grupoId, kind)
        if (meetings.isEmpty()) return emptyList()
        val porMeeting = attendanceDao.getByMeetings(meetings.map { it.id }).groupBy { it.meetingId }
        return meetings.map { m ->
            var p = 0; var a = 0; var j = 0
            porMeeting[m.id].orEmpty().forEach {
                when (it.status.uppercase()) {
                    "PRESENT", "PRESENTE"                 -> p++
                    "JUSTIFIED", "EXCUSED", "JUSTIFICADO" -> j++
                    else                                  -> a++
                }
            }
            ReunionConStats(
                id = m.id, grupoId = m.smallGroupId,
                fecha = runCatching { LocalDate.parse(m.meetingDate) }.getOrElse { LocalDate.now() },
                estado = m.status, noHuboReunion = m.noMeeting,
                presentes = p, ausentes = a, justificados = j,
            )
        }.sortedByDescending { it.fecha }
    }

    private suspend fun detalleDesdeRoom(reunionId: String): DetalleReunionData {
        val m = meetingDao.getById(reunionId) ?: error("Reunión no disponible sin conexión")
        var presentes = 0; var ausentes = 0; var justificados = 0
        val asistencias = mutableListOf<AsistenciaConNombre>()
        attendanceDao.getByMeeting(reunionId).forEach { a ->
            val estado = when (a.status.uppercase()) {
                "PRESENT", "PRESENTE"                 -> { presentes++;    "P" }
                "JUSTIFIED", "EXCUSED", "JUSTIFICADO" -> { justificados++; "J" }
                else                                  -> { ausentes++;     "A" }
            }
            val member = memberDao.getById(a.memberId)
            if (member != null) {
                val nombre = buildString {
                    append(member.firstName)
                    if (!member.middleName.isNullOrBlank())     { append(" "); append(member.middleName) }
                    append(" "); append(member.lastName)
                    if (!member.secondLastName.isNullOrBlank()) { append(" "); append(member.secondLastName) }
                }.trim()
                asistencias += AsistenciaConNombre(
                    memberId              = a.memberId,
                    nombre                = nombre,
                    estado                = estado,
                    esVisita              = member.isVisitor,
                    iglesiaVisitadaNombre = null,   // church no se cachea offline
                )
            }
        }
        val actividades = activityRecordDao.getByMeeting(reunionId).mapNotNull { r ->
            val t = activityTypeDao.getById(r.activityTypeId) ?: return@mapNotNull null
            ActividadConDetalle(nombre = t.name, nivel = t.level, cantidad = r.count, monto = r.monto, unidad = t.unitLabel)
        }
        return DetalleReunionData(
            id           = m.id,
            fecha        = runCatching { LocalDate.parse(m.meetingDate) }.getOrElse { LocalDate.now() },
            estado       = m.status,
            presentes    = presentes,
            ausentes     = ausentes,
            justificados = justificados,
            asistencias  = asistencias,
            actividades  = actividades,
            tipoReunion  = m.registryKind,
        )
    }

    private suspend fun sabbathDesdeRoom(grupoId: String, fecha: LocalDate): SabbathMeetingResumen? {
        val m = meetingDao.getByGroupKindDate(grupoId, "saturday_worship", fecha.toString()) ?: return null
        val att = attendanceDao.getByMeeting(m.id)
        val presentes = att.count { it.status.uppercase() == "PRESENT" || it.status.uppercase() == "PRESENTE" }
        val totalMiembros = memberDao.getByGroup(grupoId).count { !it.isVisitor && it.isActive }
            .takeIf { it > 0 } ?: att.size
        return SabbathMeetingResumen(
            id            = m.id,
            fecha         = runCatching { LocalDate.parse(m.meetingDate) }.getOrElse { LocalDate.now() },
            status        = m.status,
            presentes     = presentes,
            totalMiembros = totalMiembros,
        )
    }

    // ── Mappers JSON(Supabase) → Entity ──────────────────────────────────────────

    private fun JsonObject.toMeetingEntity(): MeetingEntity? {
        val id = this["id"]?.jsonPrimitive?.contentOrNull ?: return null
        return MeetingEntity(
            id                        = id,
            smallGroupId              = this["small_group_id"]?.jsonPrimitive?.contentOrNull ?: "",
            meetingDate               = this["meeting_date"]?.jsonPrimitive?.contentOrNull ?: "",
            status                    = this["status"]?.jsonPrimitive?.contentOrNull ?: "draft",
            registryKind              = this["registry_kind"]?.jsonPrimitive?.contentOrNull ?: "gp_meeting",
            noMeeting                 = this["no_meeting"]?.jsonPrimitive?.booleanOrNull ?: false,
            submittedAt               = this["submitted_at"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            submittedActorDisplayName = this["submitted_actor_display_name"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            notes                     = this["notes"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
        )
    }

    private fun JsonObject.toAttendanceEntity(): AttendanceEntity? {
        val id = this["id"]?.jsonPrimitive?.contentOrNull ?: return null
        return AttendanceEntity(
            id        = id,
            meetingId = this["meeting_id"]?.jsonPrimitive?.contentOrNull ?: "",
            memberId  = this["member_id"]?.jsonPrimitive?.contentOrNull ?: "",
            status    = this["status"]?.jsonPrimitive?.contentOrNull ?: "",
            note      = this["note"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
        )
    }

    private fun JsonObject.toActivityRecordEntity(): ActivityRecordEntity? {
        val id = this["id"]?.jsonPrimitive?.contentOrNull ?: return null
        return ActivityRecordEntity(
            id               = id,
            meetingId        = this["meeting_id"]?.jsonPrimitive?.contentOrNull ?: "",
            activityTypeId   = this["activity_type_id"]?.jsonPrimitive?.contentOrNull ?: "",
            count            = this["count"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.intOrNull,
            monto            = this["monto"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.doubleOrNull,
            notes            = this["notes"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            submissionStatus = this["submission_status"]?.jsonPrimitive?.contentOrNull ?: "approved",
        )
    }
}
