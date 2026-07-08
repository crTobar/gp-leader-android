package com.gpleader.app.core.data.session

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences("gp_session", Context.MODE_PRIVATE)

    var grupoId: String
        get() = prefs.getString("grupoId", "") ?: ""
        set(value) { prefs.edit { putString("grupoId", value) } }

    var grupoNombre: String
        get() = prefs.getString("grupoNombre", "") ?: ""
        set(value) { prefs.edit { putString("grupoNombre", value) } }

    var miembroId: String
        get() = prefs.getString("miembroId", "") ?: ""
        set(value) { prefs.edit { putString("miembroId", value) } }

    var miembroNombre: String
        get() = prefs.getString("miembroNombre", "") ?: ""
        set(value) { prefs.edit { putString("miembroNombre", value) } }

    var grupoUsername: String
        get() = prefs.getString("grupoUsername", "") ?: ""
        set(value) { prefs.edit { putString("grupoUsername", value) } }

    var grupoPasswordSet: Boolean
        get() = prefs.getBoolean("grupoPasswordSet", true)
        set(value) { prefs.edit { putBoolean("grupoPasswordSet", value) } }

    var iglesiaId: String
        get() = prefs.getString("iglesiaId", "") ?: ""
        set(value) { prefs.edit { putString("iglesiaId", value) } }

    var iglesiaNombre: String
        get() = prefs.getString("iglesiaNombre", "") ?: ""
        set(value) { prefs.edit { putString("iglesiaNombre", value) } }

    var districtId: String
        get() = prefs.getString("districtId", "") ?: ""
        set(value) { prefs.edit { putString("districtId", value) } }

    var districtNombre: String
        get() = prefs.getString("districtNombre", "") ?: ""
        set(value) { prefs.edit { putString("districtNombre", value) } }

    var campoId: String
        get() = prefs.getString("campoId", "") ?: ""
        set(value) { prefs.edit { putString("campoId", value) } }

    var campoNombre: String
        get() = prefs.getString("campoNombre", "") ?: ""
        set(value) { prefs.edit { putString("campoNombre", value) } }

    var unionId: String
        get() = prefs.getString("unionId", "") ?: ""
        set(value) { prefs.edit { putString("unionId", value) } }

    var unionNombre: String
        get() = prefs.getString("unionNombre", "") ?: ""
        set(value) { prefs.edit { putString("unionNombre", value) } }

    var gpCode: String
        get() = prefs.getString("gpCode", "") ?: ""
        set(value) { prefs.edit { putString("gpCode", value) } }

    var sessionToken: String
        get() = prefs.getString("sessionToken", "") ?: ""
        set(value) { prefs.edit { putString("sessionToken", value) } }

    var isMiembroGuardado: Boolean
        get() = prefs.getBoolean("isMiembroGuardado", false)
        set(value) { prefs.edit { putBoolean("isMiembroGuardado", value) } }

    var isIglesiaLeader: Boolean
        get() = prefs.getBoolean("isIglesiaLeader", false)
        set(value) { prefs.edit { putBoolean("isIglesiaLeader", value) } }

    val isLoggedIn: Boolean
        get() = grupoId.isNotEmpty() && miembroId.isNotEmpty()

    fun guardarPerfilMiembro(
        miembroId: String,
        nombre: String,
        grupoId: String,
        grupoNombre: String,
        iglesiaId: String,
        iglesiaNombre: String,
    ) {
        prefs.edit {
            putString("miembroId", miembroId)
            putString("miembroNombre", nombre)
            putString("grupoId", grupoId)
            putString("grupoNombre", grupoNombre)
            putString("iglesiaId", iglesiaId)
            putString("iglesiaNombre", iglesiaNombre)
            putBoolean("isMiembroGuardado", true)
        }
    }

    fun cerrarSesionMiembro() {
        prefs.edit {
            remove("miembroId")
            remove("miembroNombre")
            remove("grupoId")
            remove("grupoNombre")
            remove("iglesiaId")
            remove("iglesiaNombre")
            putBoolean("isMiembroGuardado", false)
        }
    }

    fun clear() {
        prefs.edit { clear() }
    }
}
