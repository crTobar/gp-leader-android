package com.gpleader.app.core.data.repository

import com.gpleader.app.core.data.local.room.dao.SmallGroupDao
import com.gpleader.app.core.data.local.room.entity.SmallGroupEntity
import com.gpleader.app.core.data.network.NetworkMonitor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

// Jerarquía real en Supabase: campo → district → church → small_group
class GrupoRepositoryImpl @Inject constructor(
    private val supabase:      SupabaseClient,
    private val network:       NetworkMonitor,
    private val smallGroupDao: SmallGroupDao,
) : GrupoRepository {

    override suspend fun getCampos(): List<CampoItem> {
        val data = supabase.from("campo").select().data
        return Json.parseToJsonElement(data).jsonArray.map { elem ->
            val obj = elem.jsonObject
            CampoItem(
                id     = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                nombre = obj["name"]?.jsonPrimitive?.contentOrNull ?: "",
            )
        }
    }

    override suspend fun getDistritos(campoId: String?): List<DistritoItem> {
        val data = if (campoId != null) {
            supabase.from("district").select { filter { eq("campo_id", campoId) } }.data
        } else {
            supabase.from("district").select().data
        }
        return Json.parseToJsonElement(data).jsonArray.map { elem ->
            val obj = elem.jsonObject
            DistritoItem(
                id      = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                campoId = obj["campo_id"]?.jsonPrimitive?.contentOrNull ?: "",
                nombre  = obj["name"]?.jsonPrimitive?.contentOrNull ?: "",
            )
        }
    }

    override suspend fun getIglesias(distritoId: String?): List<IglesiaItem> {
        val data = if (distritoId != null) {
            supabase.from("church").select { filter { eq("district_id", distritoId) } }.data
        } else {
            supabase.from("church").select().data
        }
        return Json.parseToJsonElement(data).jsonArray.map { elem ->
            val obj = elem.jsonObject
            IglesiaItem(
                id         = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                districtId = obj["district_id"]?.jsonPrimitive?.contentOrNull ?: "",
                nombre     = obj["name"]?.jsonPrimitive?.contentOrNull ?: "",
            )
        }
    }

    override suspend fun getGrupoDetalle(grupoId: String): GrupoDetalle? {
        val offline: suspend () -> GrupoDetalle? = {
            smallGroupDao.getById(grupoId)?.let { g ->
                GrupoDetalle(meetingDay = g.meetingDay ?: "", meetingTime = formatMeetingTime(g.meetingTime ?: ""))
            }
        }
        if (!network.isOnline()) return offline()
        return try {
            val data = supabase.from("small_group").select {
                filter { eq("id", grupoId) }
                limit(1)
            }.data
            val arr = Json.parseToJsonElement(data).jsonArray
            if (arr.isEmpty()) return null
            val obj = arr[0].jsonObject
            obj.toSmallGroupEntity()?.let { smallGroupDao.upsert(listOf(it)) }
            GrupoDetalle(
                meetingDay  = obj["meeting_day"]?.jsonPrimitive?.contentOrNull ?: "",
                meetingTime = formatMeetingTime(obj["meeting_time"]?.jsonPrimitive?.contentOrNull ?: ""),
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            offline()
        }
    }

    private fun kotlinx.serialization.json.JsonObject.toSmallGroupEntity(): SmallGroupEntity? {
        val id = this["id"]?.jsonPrimitive?.contentOrNull ?: return null
        return SmallGroupEntity(
            id             = id,
            churchId       = this["church_id"]?.jsonPrimitive?.contentOrNull ?: "",
            name           = this["name"]?.jsonPrimitive?.contentOrNull ?: "",
            meetingDay     = this["meeting_day"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            meetingTime    = this["meeting_time"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            hymn           = this["hymn"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            favoriteVerse  = this["favorite_verse"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            bibleChapter   = this["bible_chapter"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            meetingPlace   = this["meeting_place"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            isGeneralGroup = this["is_general_group"]?.jsonPrimitive?.booleanOrNull ?: false,
        )
    }

    override suspend fun getUnionByCampo(campoId: String): CampoItem? {
        val cData = supabase.from("campo").select { filter { eq("id", campoId) }; limit(1) }.data
        val unionId = Json.parseToJsonElement(cData).jsonArray.firstOrNull()
            ?.jsonObject?.get("union_id")?.jsonPrimitive?.contentOrNull ?: return null
        val uData = supabase.from("union_org").select { filter { eq("id", unionId) }; limit(1) }.data
        val uObj = Json.parseToJsonElement(uData).jsonArray.firstOrNull()?.jsonObject ?: return null
        return CampoItem(
            id     = uObj["id"]?.jsonPrimitive?.contentOrNull ?: unionId,
            nombre = uObj["name"]?.jsonPrimitive?.contentOrNull ?: "",
        )
    }

    private fun formatMeetingTime(timeStr: String): String = try {
        val time      = java.time.LocalTime.parse(timeStr)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH)
        time.format(formatter)
    } catch (_: Exception) { timeStr }

    override suspend fun getGrupos(iglesiaId: String?): List<GrupoItem> {
        val data = if (iglesiaId != null) {
            supabase.from("small_group").select { filter { eq("church_id", iglesiaId) } }.data
        } else {
            supabase.from("small_group").select().data
        }
        return Json.parseToJsonElement(data).jsonArray.map { elem ->
            val obj = elem.jsonObject
            GrupoItem(
                id          = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                iglesiaId   = obj["church_id"]?.jsonPrimitive?.contentOrNull ?: "",
                nombre      = obj["name"]?.jsonPrimitive?.contentOrNull ?: "",
                username    = obj["gp_username"]?.jsonPrimitive?.contentOrNull,
                passwordSet = obj["gp_password_set"]?.jsonPrimitive?.booleanOrNull ?: false,
                gpCode      = obj["gp_code"]?.jsonPrimitive?.contentOrNull,
            )
        }
    }
}
