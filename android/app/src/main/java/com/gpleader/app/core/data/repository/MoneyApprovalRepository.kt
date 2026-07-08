package com.gpleader.app.core.data.repository

import java.time.Instant

/**
 * Nivel del aprobador en la cadena de montos. Cada nivel aprueba el total agregado
 * de sus hijos directos (source):  church←gp, district←church, campo←district, union←campo.
 */
enum class ApprovalLevel(
    val source:      String,   // nivel de los hijos que este nivel aprueba
    val approver:    String,   // nivel que aprueba (este)
    val childTable:  String,   // tabla de los hijos
    val childParent: String,   // columna FK del hijo hacia este nodo
) {
    CHURCH  ("gp",       "church",   "small_group", "church_id"),
    DISTRICT("church",   "district", "church",      "district_id"),
    CAMPO   ("district", "campo",    "district",    "campo_id"),
    UNION   ("campo",    "union",    "campo",       "union_id");
}

/** Una actividad monetaria con su pendiente/aprobado agregado para un aprobador. */
data class NivelActividadPendiente(
    val activityTypeId:  String,
    val actividadNombre: String,
    val markerType:      String,
    val unitLabel:       String,
    val pendienteTotal:  Double,
    val aprobadoTotal:   Double,
)

/** Un hijo (GP/iglesia/distrito/campo) con su pendiente y aprobado acumulado para una actividad. */
data class NivelHijoPendiente(
    val childId:     String,
    val childNombre: String,
    val pendiente:   Double,
    val aprobado:    Double,
)

/** Una actividad con el desglose de sus hijos, para la pantalla de aprobación por nivel. */
data class NivelActividadDetalle(
    val actividad: NivelActividadPendiente,
    val hijos:     List<NivelHijoPendiente>,
)

/** Un movimiento del ledger de aprobación (historial de cada aprobación). */
data class MoneyMovimiento(
    val id:            String,
    val sourceLevel:   String,
    val sourceId:      String,
    val approverLevel: String,
    val requested:     Double,
    val approved:      Double,
    val note:          String?,
    val createdAt:     Instant?,
)

interface MoneyApprovalRepository {
    /** Actividades monetarias con pendiente/aprobado agregado para el nodo del aprobador. */
    suspend fun getMonetaryActivitiesWithPending(
        approver: ApprovalLevel,
        nodeId:   String,
    ): Result<List<NivelActividadPendiente>>

    /** Actividades monetarias con el desglose de hijos, en una sola pasada. */
    suspend fun getNivelDetalle(
        approver: ApprovalLevel,
        nodeId:   String,
    ): Result<List<NivelActividadDetalle>>

    /** Hijos del nodo con su pendiente/aprobado para una actividad concreta. */
    suspend fun getPendingChildren(
        approver:        ApprovalLevel,
        nodeId:          String,
        activityTypeId:  String,
    ): Result<List<NivelHijoPendiente>>

    /** Registra una aprobación (approved ≤ requested; note obligatorio si approved < requested). */
    suspend fun approve(
        approver:          ApprovalLevel,
        childId:           String,
        activityTypeId:    String,
        requested:         Double,
        approved:          Double,
        note:              String?,
        approverProfileId: String?,
    ): Result<Unit>

    /** Pendiente total (todas las actividades monetarias) para el badge del home. */
    suspend fun getTotalPendiente(approver: ApprovalLevel, nodeId: String): Result<Double>

    /** Historial de movimientos de un hijo (opcionalmente filtrado por actividad). */
    suspend fun getHistory(
        sourceLevel:    String,
        sourceId:       String,
        activityTypeId: String? = null,
    ): Result<List<MoneyMovimiento>>
}
