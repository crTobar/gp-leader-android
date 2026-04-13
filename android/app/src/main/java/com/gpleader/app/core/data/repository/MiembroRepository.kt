package com.gpleader.app.core.data.repository

import kotlinx.coroutines.flow.Flow

data class MiembroData(
    val id: String,
    val grupoId: String,
    val primerNombre: String,
    val segundoNombre: String? = null,
    val primerApellido: String,
    val segundoApellido: String? = null,
    val telefono: String? = null,
    val correo: String? = null,
    val estado: String = "ACTIVO",
    val createdAt: String? = null,
)

val MiembroData.nombreCompleto: String
    get() = buildString {
        append(primerNombre)
        if (!segundoNombre.isNullOrBlank()) append(" $segundoNombre")
        append(" $primerApellido")
        if (!segundoApellido.isNullOrBlank()) append(" $segundoApellido")
    }

val MiembroData.iniciales: String
    get() = "${primerNombre.firstOrNull() ?: ""}${primerApellido.firstOrNull() ?: ""}".uppercase()

interface MiembroRepository {
    fun getMiembros(grupoId: String): Flow<List<MiembroData>>
    fun getMiembrosActivos(grupoId: String): Flow<List<MiembroData>>
    fun getVisitasAnteriores(grupoId: String): Flow<List<MiembroData>>
}
