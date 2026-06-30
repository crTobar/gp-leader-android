package com.gpleader.app.core.data.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject

private data class GpCredentials(
    val tempPassword: String?,
    val passwordHash: String?,
    val passwordSet: Boolean,
)

class GpAuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : GpAuthRepository {

    override suspend fun validateGroupPassword(
        gpCode: String,
        username: String,
        password: String,
    ): Result<GpAuthResult> {
        if (gpCode.isBlank()) {
            return Result.failure(IllegalStateException("Este grupo no tiene código de acceso configurado"))
        }

        tryGpLogin(gpCode, password)?.let { return Result.success(it) }

        val creds = fetchCredentials(gpCode)
            ?: return Result.failure(IllegalArgumentException("Grupo no encontrado"))

        if (!matchesPassword(password, creds)) {
            return Result.failure(IllegalArgumentException("Contraseña incorrecta"))
        }

        signInBestEffort(username, password)

        return Result.success(
            GpAuthResult(
                sessionToken = null,
                passwordSet = creds.passwordSet,
            )
        )
    }

    private suspend fun tryGpLogin(gpCode: String, password: String): GpAuthResult? = runCatching {
        val resp = supabase.postgrest.rpc("gp_login", buildJsonObject {
            put("p_gp_code", gpCode)
            put("p_password", password)
            put("p_device_info", "Android")
        })
        val row = Json.parseToJsonElement(resp.data).jsonArray.firstOrNull()?.jsonObject
            ?: return@runCatching null
        val token = row["session_token"]?.jsonPrimitive?.contentOrNull
        val passwordSet = row["gp_password_set"]?.jsonPrimitive?.booleanOrNull ?: true
        GpAuthResult(sessionToken = token, passwordSet = passwordSet)
    }.getOrNull()

    private suspend fun fetchCredentials(gpCode: String): GpCredentials? {
        val data = supabase.from("small_group").select {
            filter { eq("gp_code", gpCode) }
            limit(1)
        }.data
        val obj = Json.parseToJsonElement(data).jsonArray.firstOrNull()?.jsonObject ?: return null
        return GpCredentials(
            tempPassword = obj["gp_temp_password"]?.jsonPrimitive?.contentOrNull,
            passwordHash = obj["gp_password"]?.jsonPrimitive?.contentOrNull,
            passwordSet  = obj["gp_password_set"]?.jsonPrimitive?.booleanOrNull ?: false,
        )
    }

    private fun matchesPassword(password: String, creds: GpCredentials): Boolean {
        creds.tempPassword?.let { if (password == it) return true }
        creds.passwordHash?.let { hash ->
            if (hash.startsWith("\$2")) {
                return BCrypt.verifyer().verify(password.toCharArray(), hash.toCharArray()).verified
            }
        }
        return false
    }

    private suspend fun signInBestEffort(username: String, password: String) {
        withContext(NonCancellable) {
            runCatching {
                supabase.auth.signInWith(Email) {
                    email = "$username@login.presencia.app"
                    this.password = password
                }
            }
        }
    }
}
