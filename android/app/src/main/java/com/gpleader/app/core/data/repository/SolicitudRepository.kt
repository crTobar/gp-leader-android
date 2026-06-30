package com.gpleader.app.core.data.repository

// ── Modelos ───────────────────────────────────────────────────────────────────

data class Solicitud(
    val id: String,
    val createdBy: String,
    val assignedTo: String,
    val smallGroupId: String,
    val meetingId: String?,
    val status: String,       // pending | active | expired | finished | cancelled
    val createdAt: String,
    val activatedAt: String?,
    val expiresAt: String?,
    val finishedAt: String?,
    val cancelledAt: String?,
    val note: String?,
    val assignedToNombre: String = "",
)

data class AsignadoPotencial(
    val profileId: String,
    val nombre: String,
    val rol: String,
)

data class DeputyCodeResult(
    val codeId: String,
    val code: String,
)

// ── Interfaz ──────────────────────────────────────────────────────────────────

interface SolicitudRepository {
    /** Solicitudes creadas por el líder para su grupo (pending + active). */
    suspend fun getSolicitudesCreadas(grupoId: String): List<Solicitud>

    /** Solicitudes asignadas a [miembroId] con status pending. */
    suspend fun getSolicitudesAsignadas(miembroId: String): List<Solicitud>

    /** Crea una nueva solicitud delegando a [assignedToId]. */
    suspend fun createSolicitud(
        assignedToId: String,
        grupoId:      String,
        nota:         String?,
    ): Solicitud

    /** Cancela una solicitud propia (pending o active). */
    suspend fun cancelSolicitud(solicitudId: String): Solicitud

    /** Activa una solicitud asignada a [miembroId]. */
    suspend fun activateSolicitud(solicitudId: String, miembroId: String): Solicitud

    /** Finaliza una solicitud activa vinculando la reunión registrada. */
    suspend fun finishSolicitud(solicitudId: String, meetingId: String, miembroId: String): Solicitud

    /** Cancela todas las solicitudes pending/active del miembro en el grupo (para reasignación). */
    suspend fun cancelSolicitudesByAssignee(assignedToId: String, grupoId: String)

    /** Invalida todos los códigos de suplente activos del grupo. */
    suspend fun revokeDeputyCode(grupoId: String)

    /** Genera un código de 6 dígitos y lo asocia al miembro suplente. */
    suspend fun createDeputyCodeForMember(grupoId: String, memberId: String): DeputyCodeResult

    /** Devuelve miembros del grupo con rol assignable (co_leader, anciano, leader). */
    suspend fun getAsignadosPotenciales(grupoId: String): List<AsignadoPotencial>
}
