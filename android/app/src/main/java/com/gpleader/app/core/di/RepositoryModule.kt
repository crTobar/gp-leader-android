package com.gpleader.app.core.di

import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.ActividadRepositoryImpl
import com.gpleader.app.core.data.repository.GrupoRepository
import com.gpleader.app.core.data.repository.GrupoRepositoryImpl
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.MiembroRepositoryImpl
import com.gpleader.app.core.data.repository.ReunionRepository
import com.gpleader.app.core.data.repository.ReunionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindActividadRepository(impl: ActividadRepositoryImpl): ActividadRepository

    @Binds
    @Singleton
    abstract fun bindGrupoRepository(impl: GrupoRepositoryImpl): GrupoRepository

    @Binds
    @Singleton
    abstract fun bindMiembroRepository(impl: MiembroRepositoryImpl): MiembroRepository

    @Binds
    @Singleton
    abstract fun bindReunionRepository(impl: ReunionRepositoryImpl): ReunionRepository
}
