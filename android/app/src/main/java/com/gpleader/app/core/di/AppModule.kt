package com.gpleader.app.core.di

import android.content.Context
import com.gpleader.app.BuildConfig
import com.gpleader.app.core.data.local.createGpDatabase
import com.gpleader.app.core.data.remote.createGpSupabaseClient
import com.powersync.PowerSyncDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createGpSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY,
    )

    @Provides
    @Singleton
    fun providePowerSyncDatabase(
        @ApplicationContext context: Context,
    ): PowerSyncDatabase = createGpDatabase(context)
}
