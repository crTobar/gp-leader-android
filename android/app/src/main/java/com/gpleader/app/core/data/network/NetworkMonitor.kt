package com.gpleader.app.core.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper de conectividad. Único punto para saber si hay internet.
 * Reemplaza los chequeos inline de ConnectivityManager (ej. RegistroViewModel).
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val cm: ConnectivityManager?
        get() = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    /**
     * true si hay una red con capacidad de internet. NO se exige VALIDATED porque al arrancar en frío
     * suele ser false aunque haya internet (falso-offline). Las lecturas caen a Room si la red falla.
     */
    fun isOnline(): Boolean {
        val manager = cm ?: return false
        val network = manager.activeNetwork ?: return false
        val caps = manager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Emite el estado de conexión de forma reactiva (para la UI). */
    val isOnlineFlow: Flow<Boolean> = callbackFlow {
        trySend(isOnline())
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(isOnline()) }
            override fun onLost(network: Network) { trySend(isOnline()) }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) { trySend(isOnline()) }
        }
        val manager = cm
        manager?.registerDefaultNetworkCallback(callback)
        awaitClose { manager?.unregisterNetworkCallback(callback) }
    }.conflate().distinctUntilChanged()
}
