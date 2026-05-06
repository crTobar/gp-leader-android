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

    val isLoggedIn: Boolean
        get() = grupoId.isNotEmpty() && miembroId.isNotEmpty()

    fun clear() {
        prefs.edit { clear() }
    }
}
