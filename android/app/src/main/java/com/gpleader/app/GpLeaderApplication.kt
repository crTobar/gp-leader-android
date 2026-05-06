package com.gpleader.app

import android.app.Application
import com.gpleader.app.core.data.sync.PowerSyncConnector
import com.powersync.PowerSyncDatabase
import dagger.hilt.android.HiltAndroidApp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GpLeaderApplication : Application() {

    @Inject lateinit var database: PowerSyncDatabase
    @Inject lateinit var connector: PowerSyncConnector
    @Inject lateinit var supabase: SupabaseClient

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            database.connect(connector)
        }
    }
}
