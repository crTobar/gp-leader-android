package com.gpleader.app.core.data.repository

import java.time.LocalDate

data class DuoMisioneroData(
    val id: String,
    val grupoId: String,
    val member1: MiembroData,
    val member2: MiembroData,
    val isActive: Boolean,
)

data class DuoActividadTipo(
    val id: String,
    val duoId: String,
    val nombre: String,
    val markerType: String,  // "counter" | "checkbox" | "monetary" | "daily_checker"
    val unitLabel: String,
    val startDate: java.time.LocalDate? = null,
    val endDate:   java.time.LocalDate? = null,
)

data class DuoActividadRecord(
    val id: String,
    val duoId: String,
    val actividadTipoId: String,
    val recordDate: LocalDate,
    val count: Int?,
    val isDone: Boolean,
    val updatedBy: String?,
    val updatedAt: java.time.Instant?,
)

data class DuoActividadConTotal(
    val tipo:          DuoActividadTipo,
    val duo:           DuoMisioneroData,
    val totalCantidad: Int,
    val montoTotal:    Double,
    val diasMarcados:  Int,
)

data class DuoBibleStudy(
    val id: String,
    val duoId: String,
    val studentName: String,
    val completedLessons: List<Int>,
) {
    val totalCompleted: Int get() = completedLessons.size
    val currentLesson: Int get() = ((completedLessons.maxOrNull() ?: 0) + 1).coerceAtMost(20)
}

interface DuoRepository {
    suspend fun getDuosByGrupo(grupoId: String): Result<List<DuoMisioneroData>>
    suspend fun getDuoPorMiembro(miembroId: String): Result<DuoMisioneroData?>
    suspend fun crearDuo(grupoId: String, member1Id: String, member2Id: String): Result<String>
    suspend fun desactivarDuo(duoId: String): Result<Unit>

    suspend fun getActividadesDuo(duoId: String): Result<List<DuoActividadTipo>>
    suspend fun crearActividadDuo(duoId: String, nombre: String, markerType: String, unitLabel: String, startDate: java.time.LocalDate? = null, endDate: java.time.LocalDate? = null): Result<String>
    suspend fun getRegistrosDuo(duoId: String, actividadTipoId: String, desde: LocalDate): Result<List<DuoActividadRecord>>
    suspend fun upsertRegistroDuo(duoId: String, actividadTipoId: String, fecha: LocalDate, count: Int?, isDone: Boolean, updatedBy: String): Result<Unit>
    suspend fun getActividadesConTotalesPorGrupo(grupoId: String): Result<List<DuoActividadConTotal>>
    suspend fun getRegistrosPorTipoActividad(actividadTipoId: String, duoId: String, desde: LocalDate): Result<List<DuoActividadRecord>>

    suspend fun getEstudiosDuo(duoId: String): Result<List<DuoBibleStudy>>
    suspend fun getEstudioDuoById(estudioId: String): Result<DuoBibleStudy?>
    suspend fun crearEstudioDuo(duoId: String, studentName: String): Result<String>
    suspend fun toggleLeccionDuo(estudioId: String, leccion: Int, completado: Boolean): Result<Unit>
}
