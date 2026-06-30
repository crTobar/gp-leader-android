package com.gpleader.app.core.data.repository

data class GpAuthResult(
    val sessionToken: String? = null,
    val passwordSet: Boolean = true,
)

interface GpAuthRepository {
    suspend fun validateGroupPassword(
        gpCode: String,
        username: String,
        password: String,
    ): Result<GpAuthResult>
}
