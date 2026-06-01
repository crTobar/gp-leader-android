package com.gpleader.app.core.data.repository

import java.time.LocalDate

data class ActividadTipoData(
    val id:                 String,
    val nombre:             String,
    val level:              String,       // "union", "pastor", "my_group"
    val markerType:         String,       // "counter", "monetary", "checkbox", "participants"
    val unitLabel:          String,
    val sortOrder:          Int,
    val scope:              String,       // "global", "church", "district", "campo", "group"
    val churchId:           String?,
    val startDate:          LocalDate?,
    val endDate:            LocalDate?,
    val frecuencia:         String  = "semanal",   // "diaria" | "semanal"
    val isMemberAccessible: Boolean = false,
    val districtId:         String? = null,
    val campoId:            String? = null,
    val grupoId:            String? = null,
)

data class RegistroActividadData(
    val actividadTipoId: String,
    val cantidad: Int?,
    val monto:    Double?,
    val notas:    String?,
)

data class RegistroSemanalData(
    val recordId:        String,
    val meetingId:       String,
    val meetingDate:     LocalDate,
    val cantidad:        Int?,
    val monto:           Double?,
    val notas:           String?,
    val aportesMiembros: Int = 0,
)

data class ActividadTotalData(
    val totalCantidad: Int,
    val montoTotal:    Double,
)

data class MemberActivitySubmission(
    val recordId:      String,
    val miembroId:     String,
    val miembroNombre: String,
    val recordDate:    LocalDate,
    val count:         Int?,
    val monto:         Double?,
    val isDone:        Boolean,
    val status:        String,
)

interface ActividadRepository {
    suspend fun getActividadesTipo(iglesiaId: String, districtId: String = "", campoId: String = "", grupoId: String = ""): Result<List<ActividadTipoData>>
    suspend fun getTodasActividadesTipo(iglesiaId: String, districtId: String = "", campoId: String = "", grupoId: String = ""): Result<List<ActividadTipoData>>
    suspend fun saveRegistros(meetingId: String, registros: List<RegistroActividadData>): Result<Unit>
    suspend fun getActividadesConTotales(grupoId: String): Result<Map<String, ActividadTotalData>>
    suspend fun getRegistrosSemanal(grupoId: String, actividadTipoId: String): Result<List<RegistroSemanalData>>
    suspend fun updateRegistro(recordId: String, cantidad: Int?, monto: Double?): Result<Unit>

    // ── Actividades para miembros ─────────────────────────────────────────────
    suspend fun getActividadesMiembro(iglesiaId: String, districtId: String = "", campoId: String = "", grupoId: String = ""): Result<List<ActividadTipoData>>
    suspend fun getRegistrosMiembro(miembroId: String, actividadTipoId: String, desde: LocalDate): Result<List<LocalDate>>
    suspend fun toggleMiembroActividad(miembroId: String, actividadTipoId: String, fecha: LocalDate, isDone: Boolean): Result<Unit>
    suspend fun getContadorSemanalMiembro(miembroId: String, actividadTipoId: String, semanaStart: LocalDate): Result<Int>
    suspend fun upsertContadorSemanalMiembro(miembroId: String, actividadTipoId: String, semanaStart: LocalDate, count: Int): Result<Unit>

    // ── Aprobación de actividades de miembros ─────────────────────────────────
    suspend fun getPendingMemberActivities(grupoId: String, actividadTipoId: String): Result<List<MemberActivitySubmission>>
    suspend fun getPendingCountPerTipo(grupoId: String): Result<Map<String, Int>>
    suspend fun approveMemberActivity(recordId: String, correctedCount: Int?, correctedMonto: Double?, isMonetary: Boolean): Result<Unit>
    suspend fun rejectMemberActivity(recordId: String): Result<Unit>

    // ── Gestión de tipos (líder) ──────────────────────────────────────────────
    suspend fun saveActividadTipo(
        nombre: String,
        level: String,
        markerType: String,
        frecuencia: String,
        unitLabel: String,
        isMemberAccessible: Boolean,
        iglesiaId: String,
        grupoId: String? = null,
        scope: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Result<Unit>
}
