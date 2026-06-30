package com.gpleader.app.core.data.repository

import java.time.LocalDate

data class ChurchHit(
    val id:           String,
    val churchName:   String,
    val districtName: String,
    val campoName:    String,
)

data class GrupoResumen(
    val id:               String,
    val nombre:           String,
    val totalMiembros:    Int,
    val pendingBoardCount: Int,
)

data class PendingBoardItem(
    val recordId:        String,
    val miembroNombre:   String,
    val grupoNombre:     String,
    val actividadNombre: String,
    val monto:           Double,
    val recordDate:      LocalDate,
)

interface IglesiaRepository {
    suspend fun searchChurches(query: String, maxResults: Int = 5): List<ChurchHit>
    suspend fun getChurchById(churchId: String): ChurchHit?

    // ── Church-level leader ───────────────────────────────────────────────────
    suspend fun getGruposByIglesia(iglesiaId: String): List<GrupoResumen>
    suspend fun getPendingBoardActivities(iglesiaId: String): List<PendingBoardItem>
    suspend fun approveMonetaryActivity(recordId: String): Result<Unit>
    suspend fun rejectMonetaryActivity(recordId: String): Result<Unit>
}
