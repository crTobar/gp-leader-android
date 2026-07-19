package com.gpleader.app.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.local.room.dao.CacheMetaDao
import com.gpleader.app.core.data.network.NetworkMonitor
import com.gpleader.app.core.data.sync.OfflinePreloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Estado de conexión + fecha de última sincronización para el indicador offline de la UI. */
@HiltViewModel
class OfflineStatusViewModel @Inject constructor(
    network: NetworkMonitor,
    cacheMetaDao: CacheMetaDao,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = network.isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /**
     * Reactivo a propósito: el ViewModel del Home se crea antes de que el preloader termine, así
     * que una lectura única se quedaría en null y el banner nunca mostraría la fecha.
     */
    val lastSync: StateFlow<Long?> = cacheMetaDao.observeUpdatedAt(OfflinePreloader.LAST_SYNC_KEY)
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/**
 * Estado de conexión para la UI. El modo offline es SOLO LECTURA: úsalo para deshabilitar los
 * controles que escriben (registrar reunión, editar actividades, aprobar aportes), no para ocultar
 * contenido — los datos cacheados sí se muestran sin conexión.
 */
@Composable
fun rememberIsOnline(viewModel: OfflineStatusViewModel = hiltViewModel()): Boolean {
    val isOnline by viewModel.isOnline.collectAsState()
    return isOnline
}
