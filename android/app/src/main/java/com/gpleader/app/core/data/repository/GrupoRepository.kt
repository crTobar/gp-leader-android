package com.gpleader.app.core.data.repository

// ── Tipos de display ──────────────────────────────────────────────────────────

data class CampoItem(val id: String, val nombre: String)
data class DistritoItem(val id: String, val campoId: String, val nombre: String)
data class IglesiaItem(
    val id:             String,
    val districtId:     String,
    val nombre:         String,
    val districtNombre: String = "",
    val campoNombre:    String = "",
)
data class GrupoItem(
    val id:            String,
    val iglesiaId:     String,
    val nombre:        String,
    val username:      String? = null,
    val passwordSet:   Boolean = false,
    val gpCode:        String? = null,
    val iglesiaNombre:  String = "",
    val districtNombre: String = "",
    val campoNombre:    String = "",
)

data class GrupoDetalle(
    val meetingDay:  String,  // "Sábado"
    val meetingTime: String,  // "7:30 PM"
)

// ── Interfaz ─────────────────────────────────────────────────────────────────

// Jerarquía: Campo → Distrito → Iglesia → GP
interface GrupoRepository {
    suspend fun getCampos(): List<CampoItem>
    suspend fun getDistritos(campoId: String? = null): List<DistritoItem>  // null = todos
    suspend fun getIglesias(distritoId: String? = null): List<IglesiaItem> // null = todas
    suspend fun getGrupos(iglesiaId: String? = null): List<GrupoItem>  // null = todos
    suspend fun getGrupoDetalle(grupoId: String): GrupoDetalle?
    /** Unión (id + nombre) a la que pertenece un campo, para setear la sesión de nivel Unión. */
    suspend fun getUnionByCampo(campoId: String): CampoItem?
}
