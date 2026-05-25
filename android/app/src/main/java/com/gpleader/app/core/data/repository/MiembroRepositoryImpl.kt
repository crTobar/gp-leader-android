package com.gpleader.app.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

class MiembroRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : MiembroRepository {

    override fun getMiembros(grupoId: String): Flow<List<MiembroData>> = flow {
        val data = supabase.from("member").select {
            filter {
                eq("small_group_id", grupoId)
                eq("is_visitor", false)
            }
        }.data
        emit(parseMiembros(data))
    }

    override fun getMiembrosActivos(grupoId: String): Flow<List<MiembroData>> = flow {
        val data = supabase.from("member").select {
            filter {
                eq("small_group_id", grupoId)
                eq("is_visitor", false)
                eq("is_active", true)
            }
        }.data
        emit(parseMiembros(data))
    }

    override suspend fun getMiembroById(miembroId: String): MiembroData? {
        val data = supabase.from("member").select {
            filter { eq("id", miembroId) }
        }.data
        return runCatching { parseMiembros(data).firstOrNull() }.getOrNull()
    }

    override fun getVisitasAnteriores(grupoId: String): Flow<List<MiembroData>> = flow {
        val data = supabase.from("member").select {
            filter {
                eq("small_group_id", grupoId)
                eq("is_visitor", true)
            }
        }.data
        emit(parseMiembros(data))
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
}
