package com.gpleader.app.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

// Jerarquía real en Supabase: campo → district → church → small_group
class GrupoRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
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
            )
        }
    }
}
