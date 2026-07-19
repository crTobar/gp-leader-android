package com.gpleader.app.core.data.sync

import com.gpleader.app.core.data.local.room.dao.CacheMetaDao
import com.gpleader.app.core.data.local.room.entity.CacheMetaEntity
import com.gpleader.app.core.data.network.NetworkMonitor
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.GrupoRepository
import com.gpleader.app.core.data.repository.MemberEntryRepository
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.ReunionRepository
import com.gpleader.app.core.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Precarga el caché offline del grupo del líder en segundo plano cuando hay internet.
 * Reutiliza los `get*` de los repos (que ya cachean en Room al leer online). Best-effort:
 * cada paso se aísla para que un fallo no aborte el resto.
 */
@Singleton
class OfflinePreloader @Inject constructor(
    private val session:         SessionManager,
    private val network:         NetworkMonitor,
    private val grupoRepo:       GrupoRepository,
    private val miembroRepo:     MiembroRepository,
    private val actividadRepo:   ActividadRepository,
    private val reunionRepo:     ReunionRepository,
    private val memberEntryRepo: MemberEntryRepository,
    private val cacheMetaDao:    CacheMetaDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Dispara la precarga si hay sesión de líder y conexión. No bloquea. */
    fun start() {
        val grupoId = session.grupoId
        if (grupoId.isEmpty() || !session.isLoggedIn) return
        if (!network.isOnline()) return
        scope.launch { preload(grupoId) }
    }

    private suspend fun preload(grupoId: String) {
        // 1) Referencia primero (nombres) → luego el resto puede resolver offline.
        step { grupoRepo.getGrupoDetalle(grupoId) }
        step { actividadRepo.getTodasActividadesTipo(session.iglesiaId, session.districtId, session.campoId, grupoId) }
        step { miembroRepo.getMiembros(grupoId).first() }
        step { miembroRepo.getVisitasAnteriores(grupoId).first() }

        // 2) Reuniones (lista) + detalle de las más recientes (para ver actividades offline).
        val reuniones = runCatching { reunionRepo.getReuniones(grupoId).first() }.getOrDefault(emptyList())
        step { reunionRepo.getReunionesSabado(grupoId).first() }
        reuniones.take(DETALLE_PRELOAD).forEach { r ->
            step { reunionRepo.getDetalleReunion(r.id) }
        }

        // 3) Aportes: pendientes + bitácora de movimientos.
        step { memberEntryRepo.getPendingEntriesForGroup(grupoId) }
        step { memberEntryRepo.getMovimientosGrupo(grupoId) }

        // Marca de última sincronización (para el indicador de la UI).
        step { cacheMetaDao.upsert(CacheMetaEntity(LAST_SYNC_KEY, System.currentTimeMillis())) }
    }

    private suspend fun step(block: suspend () -> Unit) { runCatching { block() } }

    companion object {
        private const val DETALLE_PRELOAD = 10   // nº de reuniones recientes cuyo detalle se cachea
        const val LAST_SYNC_KEY = "last_sync"
    }
}
