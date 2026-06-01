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

// ── Interfaz ──────────────────────────────────────────────────────────────────

interface SolicitudRepository {
    /** Solicitudes creadas por el líder para su grupo (pending + active). */
    suspend fun getSolicitudesCreadas(grupoId: String): List<Solicitud>

    /** Solicitudes donde el usuario autenticado es el asignado (pending). */
    suspend fun getSolicitudesAsignadas(): List<Solicitud>

    /** Crea una nueva solicitud delegando a [assignedToId]. */
    suspend fun createSolicitud(
        assignedToId: String,
        grupoId:      String,
        nota:         String?,
    ): Solicitud

    /** Cancela una solicitud propia (pending o active). */
    suspend fun cancelSolicitud(solicitudId: String): Solicitud

    /** Activa una solicitud asignada al usuario autenticado. */
    suspend fun activateSolicitud(solicitudId: String): Solicitud

    /** Finaliza una solicitud activa vinculando la reunión registrada. */
    suspend fun finishSolicitud(solicitudId: String, meetingId: String): Solicitud

    /** Devuelve miembros del grupo con rol assignable (co_leader, anciano, leader). */
    suspend fun getAsignadosPotenciales(grupoId: String): List<AsignadoPotencial>
}
