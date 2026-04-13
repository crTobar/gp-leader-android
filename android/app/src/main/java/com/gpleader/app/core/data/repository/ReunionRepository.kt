package com.gpleader.app.core.data.repository

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class ReunionConStats(
    val id: String,
    val grupoId: String,
    val fecha: LocalDate,
    val estado: String,
    val noHuboReunion: Boolean,
    val presentes: Int,
    val ausentes: Int,
    val justificados: Int,
)

data class AsistenciaParaGuardar(
    val miembroId:    String?,  // null si es visita
    val nombreVisita: String?,  // solo si es visita
    val esVisita:     Boolean,
    val estado:       String,   // "PRESENTE", "AUSENTE", "JUSTIFICADO"
)

interface ReunionRepository {
    fun getReuniones(grupoId: String, limit: Int = Int.MAX_VALUE): Flow<List<ReunionConStats>>
    suspend fun saveReunion(
        grupoId:       String,
        fecha:         LocalDate,
        noHuboReunion: Boolean,
        asistencias:   List<AsistenciaParaGuardar>,
    ): Result<String>
}
