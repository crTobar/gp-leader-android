package com.gpleader.app.core.data.repository

import java.time.Instant

data class GroupLogEntry(
    val id:          String,
    val actionType:  String,
    val description: String,
    val createdAt:   Instant,
)

interface GroupLogRepository {
    suspend fun getEntradas(grupoId: String, limit: Int = 60): Result<List<GroupLogEntry>>
    suspend fun logAccion(grupoId: String, actionType: String, description: String): Result<Unit>
}
