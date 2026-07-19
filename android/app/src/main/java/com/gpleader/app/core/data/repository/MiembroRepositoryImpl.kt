package com.gpleader.app.core.data.repository

import com.gpleader.app.core.data.local.room.dao.MemberDao
import com.gpleader.app.core.data.local.room.entity.MemberEntity
import com.gpleader.app.core.data.network.NetworkMonitor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject

class MiembroRepositoryImpl @Inject constructor(
    private val supabase:  SupabaseClient,
    private val network:   NetworkMonitor,
    private val memberDao: MemberDao,
) : MiembroRepository {

    /** Lee online (y cachea); si no hay red o la llamada falla, cae al caché de Room. */
    private suspend fun <T> cachedRead(offline: suspend () -> T, online: suspend () -> T): T {
        if (!network.isOnline()) return offline()
        return try { online() }
        catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) { offline() }
    }

    override fun getMiembros(grupoId: String): Flow<List<MiembroData>> = flow {
        emit(cachedRead(
            offline = { memberDao.getByGroup(grupoId).filter { !it.isVisitor }.map { it.toMiembroData() } },
            online = {
                val data = supabase.from("member").select {
                    filter { eq("small_group_id", grupoId); eq("is_visitor", false) }
                }.data
                cacheMembers(data)
                parseMiembros(data)
            },
        ))
    }

    override fun getMiembrosActivos(grupoId: String): Flow<List<MiembroData>> = flow {
        emit(cachedRead(
            offline = { memberDao.getByGroup(grupoId).filter { !it.isVisitor && it.isActive }.map { it.toMiembroData() } },
            online = {
                val data = supabase.from("member").select {
                    filter { eq("small_group_id", grupoId); eq("is_visitor", false); eq("is_active", true) }
                }.data
                cacheMembers(data)
                parseMiembros(data)
            },
        ))
    }

    override suspend fun getMiembroById(miembroId: String): MiembroData? = cachedRead(
        offline = { memberDao.getById(miembroId)?.toMiembroData() },
        online = {
            val data = supabase.from("member").select { filter { eq("id", miembroId) } }.data
            cacheMembers(data)
            runCatching { parseMiembros(data).firstOrNull() }.getOrNull()
        },
    )

    override fun getVisitasAnteriores(grupoId: String): Flow<List<MiembroData>> = flow {
        emit(cachedRead(
            offline = { memberDao.getByGroup(grupoId).filter { it.isVisitor }.map { it.toMiembroData() } },
            online = {
                val data = supabase.from("member").select {
                    filter { eq("small_group_id", grupoId); eq("is_visitor", true) }
                }.data
                cacheMembers(data)
                parseMiembros(data)
            },
        ))
    }

    override suspend fun agregarMiembro(
        grupoId: String,
        primerNombre: String,
        segundoNombre: String?,
        primerApellido: String,
        segundoApellido: String?,
        telefono: String?,
        correo: String?,
    ): MiembroData {
        val data = supabase.from("member").insert(buildJsonObject {
            put("small_group_id", grupoId)
            put("first_name",     primerNombre)
            if (segundoNombre   != null) put("middle_name",       segundoNombre)
            put("last_name",      primerApellido)
            if (segundoApellido != null) put("second_last_name",  segundoApellido)
            if (telefono        != null) put("phone",             telefono)
            if (correo          != null) put("email",             correo)
            put("is_visitor", false)
            put("is_active",  true)
        }) { select() }.data
        return parseMiembros(data).first()
    }

    override suspend fun actualizarMiembro(
        miembroId: String,
        primerNombre: String,
        segundoNombre: String?,
        primerApellido: String,
        segundoApellido: String?,
        telefono: String?,
        correo: String?,
        isActive: Boolean,
    ): MiembroData {
        val data = supabase.from("member").update(buildJsonObject {
            put("first_name",  primerNombre)
            put("last_name",   primerApellido)
            put("is_active",   isActive)
            if (segundoNombre   != null) put("middle_name",      segundoNombre)   else put("middle_name",      JsonNull)
            if (segundoApellido != null) put("second_last_name", segundoApellido) else put("second_last_name", JsonNull)
            if (telefono        != null) put("phone",            telefono)        else put("phone",            JsonNull)
            if (correo          != null) put("email",            correo)          else put("email",            JsonNull)
        }) {
            filter { eq("id", miembroId) }
            select()
        }.data
        return parseMiembros(data).first()
    }

    override suspend fun toggleActivoMiembro(miembroId: String, isActive: Boolean) {
        supabase.from("member").update(buildJsonObject { put("is_active", isActive) }) {
            filter { eq("id", miembroId) }
        }
    }

    private fun parseMiembros(data: String): List<MiembroData> =
        Json.parseToJsonElement(data).jsonArray.map { elem ->
            val obj       = elem.jsonObject
            val isVisitor = obj["is_visitor"]?.jsonPrimitive?.booleanOrNull ?: false
            val isActive  = obj["is_active"]?.jsonPrimitive?.booleanOrNull ?: true
            val estado = when {
                isVisitor -> "VISITA"
                isActive  -> "ACTIVO"
                else      -> "ARCHIVADO"
            }
            MiembroData(
                id              = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                grupoId         = obj["small_group_id"]?.jsonPrimitive?.contentOrNull ?: "",
                primerNombre    = obj["first_name"]?.jsonPrimitive?.contentOrNull ?: "",
                segundoNombre   = obj["middle_name"]?.jsonPrimitive?.contentOrNull,
                primerApellido  = obj["last_name"]?.jsonPrimitive?.contentOrNull ?: "",
                segundoApellido = obj["second_last_name"]?.jsonPrimitive?.contentOrNull,
                telefono        = obj["phone"]?.jsonPrimitive?.contentOrNull,
                correo          = obj["email"]?.jsonPrimitive?.contentOrNull,
                estado          = estado,
                createdAt       = obj["created_at"]?.jsonPrimitive?.contentOrNull,
                isLider         = obj["is_leader"]?.jsonPrimitive?.booleanOrNull ?: false,
            )
        }

    // ── Caché offline (Room) ─────────────────────────────────────────────────────

    private suspend fun cacheMembers(data: String) {
        val rows = Json.parseToJsonElement(data).jsonArray.mapNotNull { it.jsonObject.toMemberEntity() }
        if (rows.isNotEmpty()) memberDao.upsert(rows)
    }

    private fun JsonObject.toMemberEntity(): MemberEntity? {
        val id = this["id"]?.jsonPrimitive?.contentOrNull ?: return null
        return MemberEntity(
            id             = id,
            smallGroupId   = this["small_group_id"]?.jsonPrimitive?.contentOrNull ?: "",
            firstName      = this["first_name"]?.jsonPrimitive?.contentOrNull ?: "",
            middleName     = this["middle_name"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            lastName       = this["last_name"]?.jsonPrimitive?.contentOrNull ?: "",
            secondLastName = this["second_last_name"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            phone          = this["phone"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            email          = this["email"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
            isVisitor      = this["is_visitor"]?.jsonPrimitive?.booleanOrNull ?: false,
            isActive       = this["is_active"]?.jsonPrimitive?.booleanOrNull ?: true,
            isLeader       = this["is_leader"]?.jsonPrimitive?.booleanOrNull ?: false,
            status         = this["status"]?.jsonPrimitive?.contentOrNull ?: "active",
            createdAt      = this["created_at"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.contentOrNull,
        )
    }

    private fun MemberEntity.toMiembroData(): MiembroData {
        val estado = when {
            isVisitor -> "VISITA"
            isActive  -> "ACTIVO"
            else      -> "ARCHIVADO"
        }
        return MiembroData(
            id              = id,
            grupoId         = smallGroupId,
            primerNombre    = firstName,
            segundoNombre   = middleName,
            primerApellido  = lastName,
            segundoApellido = secondLastName,
            telefono        = phone,
            correo          = email,
            estado          = estado,
            createdAt       = createdAt,
            isLider         = isLeader,
        )
    }
}
