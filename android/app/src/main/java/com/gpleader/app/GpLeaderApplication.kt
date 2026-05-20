package com.gpleader.app

import android.app.Application
import com.gpleader.app.core.data.sync.PowerSyncConnector
import com.powersync.PowerSyncDatabase
import dagger.hilt.android.HiltAndroidApp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.sentry.android.core.SentryAndroid
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
        SentryAndroid.init(this) { options ->
            options.dsn = "http://e526dea95274df347b61f6e0d77a668f@10.0.2.2:9000/2"
            options.isAttachScreenshot = true
            options.isAttachViewHierarchy = true
            options.tracesSampleRate = 1.0
            options.logs.isEnabled = true
            options.isSendDefaultPii = true
        }
        appScope.launch {
            database.connect(connector)
        }
    }
}
