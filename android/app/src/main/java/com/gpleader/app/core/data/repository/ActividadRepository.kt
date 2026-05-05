package com.gpleader.app.core.data.repository

data class ActividadTipoData(
    val id: String,
    val nombre: String,
    val level: String,       // "union", "pastor", "my_group"
    val markerType: String,  // "counter", "monetary", "checkbox"
    val unitLabel: String,
    val sortOrder: Int,
    val scope: String,       // "global", "church", "campo"
    val churchId: String?,
)

data class RegistroActividadData(
    val actividadTipoId: String,
    val cantidad: Int?,
    val monto: Double?,
    val notas: String?,
)

interface ActividadRepository {
    suspend fun getActividadesTipo(iglesiaId: String): Result<List<ActividadTipoData>>
    suspend fun saveRegistros(meetingId: String, registros: List<RegistroActividadData>): Result<Unit>
}
