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
    val miembroId:         String?,  // null si es visita
    val nombreVisita:      String?,  // solo si es visita
    val esVisita:          Boolean,
    val estado:            String,   // "PRESENTE", "AUSENTE", "JUSTIFICADO"
    val iglesiaVisitadaId: String? = null,
)

data class AsistenciaConNombre(
    val nombre:              String,
    val estado:              String,   // "P", "A", "J"
    val esVisita:            Boolean,
    val iglesiaVisitadaNombre: String? = null,
)

data class DetalleReunionData(
    val id:           String,
    val fecha:        LocalDate,
    val estado:       String,
    val presentes:    Int,
    val ausentes:     Int,
    val justificados: Int,
    val asistencias:  List<AsistenciaConNombre>,
    val tipoReunion:  String = "gp_meeting",
)

data class SabbathMeetingResumen(
    val id:           String,
    val fecha:        LocalDate,
    val status:       String,
    val presentes:    Int,
    val totalMiembros: Int,
)

interface ReunionRepository {
    fun getReuniones(grupoId: String, limit: Int = Int.MAX_VALUE): Flow<List<ReunionConStats>>
    suspend fun saveReunion(
        grupoId:       String,
        fecha:         LocalDate,
        noHuboReunion: Boolean,
        asistencias:   List<AsistenciaParaGuardar>,
        tipoReunion:   String = "gp_meeting",
    ): Result<String>
    suspend fun getDetalleReunion(reunionId: String): Result<DetalleReunionData>
    suspend fun getSabbathMeeting(grupoId: String, fecha: LocalDate): Result<SabbathMeetingResumen?>
    suspend fun saveDraftAttendance(
        meetingId:  String,
        memberId:   String,
        presente:   Boolean,
        iglesiaId:  String?,
    ): Result<Unit>
    suspend fun submitSabbathMeeting(meetingId: String, asistencias: List<AsistenciaParaGuardar>): Result<Unit>
}
