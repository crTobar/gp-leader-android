package com.gpleader.app.core.data.sync

import com.gpleader.app.core.data.local.room.dao.ActivityRecordDao
import com.gpleader.app.core.data.local.room.dao.AttendanceDao
import com.gpleader.app.core.data.local.room.dao.MeetingDao
import com.gpleader.app.core.data.local.room.dao.MemberDao
import com.gpleader.app.core.data.network.NetworkMonitor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reconciliación de borrados: elimina de Room lo que ya no existe en el servidor (hard deletes),
 * para que la caché offline sea un espejo exacto. Los DAOs solo hacen upsert, así que sin esto
 * las filas borradas en Supabase quedarían para siempre.
 *
 * REGLA FAIL-SAFE: nunca borra sin un fetch de ids EXITOSO. Si la red falla, la consulta lanza y
 * el bloque de borrado no se ejecuta → la caché queda intacta. Cada tabla va aislada.
 */
@Singleton
class OfflineReconciler @Inject constructor(
    private val supabase:          SupabaseClient,
    private val network:           NetworkMonitor,
    private val meetingDao:        MeetingDao,
    private val attendanceDao:     AttendanceDao,
    private val activityRecordDao: ActivityRecordDao,
    private val memberDao:         MemberDao,
) {
    private fun cacheCutoff(): String = LocalDate.now().minusYears(1).toString()

    suspend fun reconcile(grupoId: String) {
        if (grupoId.isBlank() || !network.isOnline()) return

        // ── Reuniones (dentro de la ventana) + cascada a sus hijas ────────────────
        reconcileTabla {
            val cutoff = cacheCutoff()
            val ids = fetchMeetingIds(grupoId, cutoff)   // lanza si falla → no se borra nada
            meetingDao.deleteMissing(grupoId, cutoff, ids)
            // Cascada: asistencia y registros de actividad de reuniones que desaparecieron.
            attendanceDao.deleteOrphans(ids)
            activityRecordDao.deleteOrphans(ids)
        }

        // ── Miembros del grupo (incluye visitas: sin filtro is_visitor) ───────────
        reconcileTabla {
            val ids = fetchMemberIds(grupoId)
            memberDao.deleteMissing(grupoId, ids)
        }
    }

    /** Aísla la reconciliación de una tabla: un fallo NO aborta las demás ni borra nada. */
    private suspend inline fun reconcileTabla(block: () -> Unit) {
        runCatching { block() }
    }

    private suspend fun fetchMeetingIds(grupoId: String, cutoff: String): List<String> {
        val data = supabase.from("meeting").select(Columns.raw("id")) {
            filter {
                eq("small_group_id", grupoId)
                gte("meeting_date", cutoff)
            }
        }.data
        return parseIds(data)
    }

    private suspend fun fetchMemberIds(grupoId: String): List<String> {
        val data = supabase.from("member").select(Columns.raw("id")) {
            filter { eq("small_group_id", grupoId) }
        }.data
        return parseIds(data)
    }

    private fun parseIds(data: String): List<String> =
        Json.parseToJsonElement(data).jsonArray.mapNotNull {
            it.jsonObject["id"]?.jsonPrimitive?.contentOrNull
        }
}
