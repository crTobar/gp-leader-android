package com.gpleader.app.core.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
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
import com.gpleader.app.core.data.local.room.entity.ActivityRecordEntity
import com.gpleader.app.core.data.local.room.entity.ActivityTypeEntity
import com.gpleader.app.core.data.local.room.entity.AttendanceEntity
import com.gpleader.app.core.data.local.room.entity.CacheMetaEntity
import com.gpleader.app.core.data.local.room.entity.MeetingEntity
import com.gpleader.app.core.data.local.room.entity.MemberActivityRecordEntity
import com.gpleader.app.core.data.local.room.entity.MemberEntity
import com.gpleader.app.core.data.local.room.entity.MemberEntryEntity
import com.gpleader.app.core.data.local.room.entity.MemberEntryEventEntity
import com.gpleader.app.core.data.local.room.entity.SmallGroupEntity

/**
 * Base de datos local (Room) para el modo offline de solo lectura.
 * Espejo de las tablas de Supabase relevantes para las pantallas del líder.
 */
@Database(
    entities = [
        CacheMetaEntity::class,
        SmallGroupEntity::class,
        MemberEntity::class,
        MeetingEntity::class,
        AttendanceEntity::class,
        ActivityTypeEntity::class,
        ActivityRecordEntity::class,
        MemberActivityRecordEntity::class,
        MemberEntryEntity::class,
        MemberEntryEventEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheMetaDao(): CacheMetaDao
    abstract fun smallGroupDao(): SmallGroupDao
    abstract fun memberDao(): MemberDao
    abstract fun meetingDao(): MeetingDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun activityTypeDao(): ActivityTypeDao
    abstract fun activityRecordDao(): ActivityRecordDao
    abstract fun memberActivityRecordDao(): MemberActivityRecordDao
    abstract fun memberEntryDao(): MemberEntryDao
    abstract fun memberEntryEventDao(): MemberEntryEventDao
}
