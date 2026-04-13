package com.gpleader.app

import android.app.Application
import com.gpleader.app.core.data.sync.PowerSyncConnector
import com.powersync.PowerSyncDatabase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GpLeaderApplication : Application() {

    @Inject lateinit var database: PowerSyncDatabase
    @Inject lateinit var connector: PowerSyncConnector

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            // TODO: reemplazar loginAnonymously() con auth real cuando esté definido
            runCatching { connector.loginAnonymously() }
            database.connect(connector)
        }
    }
}
