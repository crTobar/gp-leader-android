package com.gpleader.app.core.data.sync

import com.gpleader.app.BuildConfig
import com.powersync.connector.supabase.SupabaseConnector
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Conector PowerSync específico de la app.
 * Extiende SupabaseConnector para heredar fetchCredentials y uploadData.
 *
 * Auth: actualmente usa loginAnonymously() como placeholder.
 * Cuando se decida el mecanismo de login real, reemplazar en GpLeaderApplication.
 */
@Singleton
class PowerSyncConnector @Inject constructor(supabase: SupabaseClient) :
    SupabaseConnector(
        supabaseClient    = supabase,
        powerSyncEndpoint = BuildConfig.POWERSYNC_URL,
    )
