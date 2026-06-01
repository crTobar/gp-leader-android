package com.gpleader.app.core.di

import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.ActividadRepositoryImpl
import com.gpleader.app.core.data.repository.GroupLogRepository
import com.gpleader.app.core.data.repository.GroupLogRepositoryImpl
import com.gpleader.app.core.data.repository.GrupoRepository
import com.gpleader.app.core.data.repository.GrupoRepositoryImpl
import com.gpleader.app.core.data.repository.IglesiaRepository
import com.gpleader.app.core.data.repository.IglesiaRepositoryImpl
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.MiembroRepositoryImpl
import com.gpleader.app.core.data.repository.ReunionRepository
import com.gpleader.app.core.data.repository.ReunionRepositoryImpl
import com.gpleader.app.core.data.repository.SolicitudRepository
import com.gpleader.app.core.data.repository.SolicitudRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindSolicitudRepository(impl: SolicitudRepositoryImpl): SolicitudRepository

    @Binds
    @Singleton
    abstract fun bindGroupLogRepository(impl: GroupLogRepositoryImpl): GroupLogRepository

    @Binds
    @Singleton
    abstract fun bindIglesiaRepository(impl: IglesiaRepositoryImpl): IglesiaRepository
}
