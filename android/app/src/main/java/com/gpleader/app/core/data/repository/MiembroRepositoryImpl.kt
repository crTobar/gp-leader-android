package com.gpleader.app.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class MiembroRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : MiembroRepository {

    override fun getMiembros(grupoId: String): Flow<List<MiembroData>> = flow {
        val data = supabase.from("member").select {
            filter { eq("small_group_id", grupoId) }
        }.data
        emit(parseMiembros(data).filter { it.estado != "VISITA" })
    }

    override fun getMiembrosActivos(grupoId: String): Flow<List<MiembroData>> = flow {
        val data = supabase.from("member").select {
            filter { eq("small_group_id", grupoId) }
        }.data
        emit(parseMiembros(data).filter { it.estado == "ACTIVO" })
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
