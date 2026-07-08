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

    /**
     * Valida la contraseña de un miembro normal del GP.
     * Por ahora compara contra una contraseña DEV (la tabla `member` aún no tiene
     * columna de contraseña). Migrar a RPC `member_login` cuando exista — ver docs.
     */
    suspend fun validateMemberPassword(
        memberId: String,
        password: String,
    ): Result<Unit>
}
