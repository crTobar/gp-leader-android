package com.gpleader.app.core.data.repository

import com.gpleader.app.core.data.network.NetworkMonitor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import javax.inject.Inject

class BibleStudyRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val network:  NetworkMonitor,
) : BibleStudyRepository {

    // Sin caché offline: degrada a vacío/null sin error cuando no hay red.
    private suspend fun <T> offlineSafe(fallback: T, block: suspend () -> T): Result<T> =
        runCatching { block() }.recoverCatching { if (!network.isOnline()) fallback else throw it }

    override suspend fun getEstudios(miembroId: String): Result<List<EstudioBiblico>> = offlineSafe(emptyList()) {
        val data = supabase.from("bible_study").select(
            Columns.raw("id, member_id, student_name, completed_lessons")
        ) {
            filter { eq("member_id", miembroId) }
        }.data

        Json.parseToJsonElement(data).jsonArray.mapNotNull { elem ->
            parseEstudio(elem.jsonObject)
        }.sortedBy { it.studentName }
    }

    override suspend fun getEstudioById(estudioId: String): Result<EstudioBiblico?> = offlineSafe(null) {
        val data = supabase.from("bible_study").select(
            Columns.raw("id, member_id, student_name, completed_lessons")
        ) {
            filter { eq("id", estudioId) }
        }.data
        Json.parseToJsonElement(data).jsonArray.firstOrNull()?.jsonObject?.let { parseEstudio(it) }
    }

    override suspend fun createEstudio(miembroId: String, studentName: String): Result<EstudioBiblico> = runCatching {
        val payload = buildJsonObject {
            put("member_id", miembroId)
            put("student_name", studentName.trim())
            putJsonArray("completed_lessons") { }
        }
        val data = supabase.from("bible_study").insert(payload) {
            select(Columns.raw("id, member_id, student_name, completed_lessons"))
        }.data

        Json.parseToJsonElement(data).jsonArray.first().jsonObject.let { parseEstudio(it)!! }
    }

    override suspend fun toggleLesson(estudioId: String, lessonNumber: Int, completed: Boolean): Result<Unit> = runCatching {
        // Fetch current state, update client-side, then save
        val data = supabase.from("bible_study").select(
            Columns.raw("completed_lessons")
        ) {
            filter { eq("id", estudioId) }
        }.data

        val current = Json.parseToJsonElement(data).jsonArray
            .firstOrNull()
            ?.jsonObject
            ?.get("completed_lessons")
            ?.jsonArray
            ?.mapNotNull { it.jsonPrimitive.int }
            ?.toMutableList() ?: mutableListOf()

        val updated = if (completed) {
            (current + lessonNumber).distinct().sorted()
        } else {
            current.filter { it != lessonNumber }
        }

        supabase.from("bible_study").update(
            buildJsonObject {
                putJsonArray("completed_lessons") { updated.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }
            }
        ) {
            filter { eq("id", estudioId) }
        }
    }

    private fun parseEstudio(obj: kotlinx.serialization.json.JsonObject): EstudioBiblico? {
        val id   = obj["id"]?.jsonPrimitive?.contentOrNull ?: return null
        val mId  = obj["member_id"]?.jsonPrimitive?.contentOrNull ?: return null
        val name = obj["student_name"]?.jsonPrimitive?.contentOrNull ?: return null
        val lessons = obj["completed_lessons"]?.jsonArray
            ?.mapNotNull { runCatching { it.jsonPrimitive.int }.getOrNull() }
            ?: emptyList()
        return EstudioBiblico(id = id, memberId = mId, studentName = name, completedLessons = lessons)
    }
}
