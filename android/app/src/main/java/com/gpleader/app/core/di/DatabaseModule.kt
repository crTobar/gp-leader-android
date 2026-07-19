package com.gpleader.app.core.di

import android.content.Context
import androidx.room.Room
import com.gpleader.app.core.data.local.room.AppDatabase
import com.gpleader.app.core.data.local.room.dao.ActivityRecordDao
import com.gpleader.app.core.data.local.room.dao.ActivityTypeDao
import com.gpleader.app.core.data.local.room.dao.AttendanceDao
import com.gpleader.app.core.data.local.room.dao.CacheMetaDao
import com.gpleader.app.core.data.local.room.dao.MeetingDao
import com.gpleader.app.core.data.local.room.dao.MemberActivityRecordDao
import com.gpleader.app.core.data.local.room.dao.MemberDao
import com.gpleader.app.core.data.local.room.dao.MemberEntryDao
import com.gpleader.app.core.data.local.room.dao.MemberEntryEventDao
import com.gpleader.app.core.data.local.room.dao.SmallGroupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provee la base de datos Room (caché offline) y sus DAOs. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "gpleader_cache.db",
    )
        // Solo lectura/caché: si el schema cambia, descartar y recachear.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides fun provideCacheMetaDao(db: AppDatabase): CacheMetaDao = db.cacheMetaDao()
    @Provides fun provideSmallGroupDao(db: AppDatabase): SmallGroupDao = db.smallGroupDao()
    @Provides fun provideMemberDao(db: AppDatabase): MemberDao = db.memberDao()
    @Provides fun provideMeetingDao(db: AppDatabase): MeetingDao = db.meetingDao()
    @Provides fun provideAttendanceDao(db: AppDatabase): AttendanceDao = db.attendanceDao()
    @Provides fun provideActivityTypeDao(db: AppDatabase): ActivityTypeDao = db.activityTypeDao()
    @Provides fun provideActivityRecordDao(db: AppDatabase): ActivityRecordDao = db.activityRecordDao()
    @Provides fun provideMemberActivityRecordDao(db: AppDatabase): MemberActivityRecordDao = db.memberActivityRecordDao()
    @Provides fun provideMemberEntryDao(db: AppDatabase): MemberEntryDao = db.memberEntryDao()
    @Provides fun provideMemberEntryEventDao(db: AppDatabase): MemberEntryEventDao = db.memberEntryEventDao()
}
