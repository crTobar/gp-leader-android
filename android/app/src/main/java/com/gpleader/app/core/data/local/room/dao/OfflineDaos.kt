package com.gpleader.app.core.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.gpleader.app.core.data.local.room.entity.ActivityRecordEntity
import com.gpleader.app.core.data.local.room.entity.ActivityTypeEntity
import com.gpleader.app.core.data.local.room.entity.AttendanceEntity
import com.gpleader.app.core.data.local.room.entity.MeetingEntity
import com.gpleader.app.core.data.local.room.entity.MemberActivityRecordEntity
import com.gpleader.app.core.data.local.room.entity.MemberEntity
import com.gpleader.app.core.data.local.room.entity.MemberEntryEntity
import com.gpleader.app.core.data.local.room.entity.MemberEntryEventEntity
import com.gpleader.app.core.data.local.room.entity.QuarterEntity
import com.gpleader.app.core.data.local.room.entity.SmallGroupEntity

@Dao
interface SmallGroupDao {
    @Upsert suspend fun upsert(rows: List<SmallGroupEntity>)
    @Query("SELECT * FROM small_group WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SmallGroupEntity?

    /** GPs de una iglesia, sin el grupo general — espejo de scopeGpIds(scopeLevel = "church"). */
    @Query("SELECT * FROM small_group WHERE churchId = :churchId AND isGeneralGroup = 0")
    suspend fun getByChurch(churchId: String): List<SmallGroupEntity>
}

@Dao
interface MemberDao {
    @Upsert suspend fun upsert(rows: List<MemberEntity>)
    @Query("SELECT * FROM member WHERE smallGroupId = :grupoId")
    suspend fun getByGroup(grupoId: String): List<MemberEntity>
    @Query("SELECT * FROM member WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MemberEntity?

    /** Reconciliación: borra miembros locales del grupo que ya no existen en el servidor. */
    @Query("DELETE FROM member WHERE smallGroupId = :gid AND id NOT IN (:validIds)")
    suspend fun deleteMissing(gid: String, validIds: List<String>)
}

@Dao
interface MeetingDao {
    @Upsert suspend fun upsert(rows: List<MeetingEntity>)
    @Query("SELECT * FROM meeting WHERE smallGroupId = :grupoId AND registryKind = :kind ORDER BY meetingDate DESC")
    suspend fun getByGroupKind(grupoId: String, kind: String): List<MeetingEntity>
    @Query("SELECT * FROM meeting WHERE smallGroupId = :grupoId AND registryKind = :kind AND meetingDate = :fecha LIMIT 1")
    suspend fun getByGroupKindDate(grupoId: String, kind: String, fecha: String): MeetingEntity?
    @Query("SELECT * FROM meeting WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MeetingEntity?

    /** Reconciliación: borra reuniones locales (en la ventana) que ya no existen en el servidor. */
    @Query("DELETE FROM meeting WHERE smallGroupId = :gid AND meetingDate >= :cutoff AND id NOT IN (:validIds)")
    suspend fun deleteMissing(gid: String, cutoff: String, validIds: List<String>)
}

@Dao
interface AttendanceDao {
    @Upsert suspend fun upsert(rows: List<AttendanceEntity>)
    @Query("SELECT * FROM attendance WHERE meetingId = :meetingId")
    suspend fun getByMeeting(meetingId: String): List<AttendanceEntity>
    @Query("SELECT * FROM attendance WHERE meetingId IN (:meetingIds)")
    suspend fun getByMeetings(meetingIds: List<String>): List<AttendanceEntity>

    @Query("DELETE FROM attendance WHERE meetingId = :meetingId")
    suspend fun deleteByMeeting(meetingId: String)

    /** Cascada: borra asistencia huérfana (de reuniones que ya no existen en el servidor). */
    @Query("DELETE FROM attendance WHERE meetingId NOT IN (:validMeetingIds)")
    suspend fun deleteOrphans(validMeetingIds: List<String>)

    /** Reemplaza la asistencia de una reunión (borra + inserta) para no dejar filas obsoletas. */
    @Transaction
    suspend fun replaceForMeeting(meetingId: String, rows: List<AttendanceEntity>) {
        deleteByMeeting(meetingId)
        upsert(rows)
    }
}

@Dao
interface ActivityTypeDao {
    @Upsert suspend fun upsert(rows: List<ActivityTypeEntity>)
    @Query("SELECT * FROM activity_type")
    suspend fun getAll(): List<ActivityTypeEntity>
    @Query("SELECT * FROM activity_type WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ActivityTypeEntity?
}

@Dao
interface ActivityRecordDao {
    @Upsert suspend fun upsert(rows: List<ActivityRecordEntity>)
    @Query("SELECT * FROM activity_record WHERE meetingId = :meetingId")
    suspend fun getByMeeting(meetingId: String): List<ActivityRecordEntity>
    @Query(
        "SELECT ar.* FROM activity_record ar " +
            "JOIN meeting m ON ar.meetingId = m.id " +
            "WHERE m.smallGroupId = :grupoId",
    )
    suspend fun getByGroup(grupoId: String): List<ActivityRecordEntity>

    @Query("DELETE FROM activity_record WHERE meetingId = :meetingId")
    suspend fun deleteByMeeting(meetingId: String)

    /** Cascada: borra registros de actividad huérfanos (de reuniones que ya no existen). */
    @Query("DELETE FROM activity_record WHERE meetingId NOT IN (:validMeetingIds)")
    suspend fun deleteOrphans(validMeetingIds: List<String>)

    @Transaction
    suspend fun replaceForMeeting(meetingId: String, rows: List<ActivityRecordEntity>) {
        deleteByMeeting(meetingId)
        upsert(rows)
    }
}

@Dao
interface MemberActivityRecordDao {
    @Upsert suspend fun upsert(rows: List<MemberActivityRecordEntity>)
    @Query("SELECT * FROM member_activity_record WHERE smallGroupId = :grupoId")
    suspend fun getByGroup(grupoId: String): List<MemberActivityRecordEntity>
    @Query("SELECT * FROM member_activity_record WHERE memberId = :miembroId AND activityTypeId = :actividadTipoId")
    suspend fun getByMemberActivity(miembroId: String, actividadTipoId: String): List<MemberActivityRecordEntity>
    @Query("SELECT * FROM member_activity_record WHERE activityTypeId = :actividadTipoId AND recordDate >= :desde AND recordDate <= :hasta")
    suspend fun getByActivityRange(actividadTipoId: String, desde: String, hasta: String): List<MemberActivityRecordEntity>
}

@Dao
interface MemberEntryDao {
    @Upsert suspend fun upsert(rows: List<MemberEntryEntity>)
    @Query("SELECT * FROM member_entry WHERE smallGroupId = :grupoId AND isDeleted = 0")
    suspend fun getByGroup(grupoId: String): List<MemberEntryEntity>
    @Query("SELECT * FROM member_entry WHERE memberId = :miembroId AND activityTypeId = :actividadTipoId AND isDeleted = 0")
    suspend fun getByMemberActivity(miembroId: String, actividadTipoId: String): List<MemberEntryEntity>
    @Query("SELECT * FROM member_entry WHERE smallGroupId = :grupoId AND activityTypeId = :actividadTipoId AND isDeleted = 0")
    suspend fun getByGroupActivity(grupoId: String, actividadTipoId: String): List<MemberEntryEntity>

    /** Varios grupos a la vez: el historial con scope "church" agrega todos los GPs de la iglesia. */
    @Query("SELECT * FROM member_entry WHERE smallGroupId IN (:grupoIds) AND isDeleted = 0")
    suspend fun getByGroups(grupoIds: List<String>): List<MemberEntryEntity>
}

@Dao
interface MemberEntryEventDao {
    @Upsert suspend fun upsert(rows: List<MemberEntryEventEntity>)
    @Query("SELECT * FROM member_entry_event WHERE smallGroupId = :grupoId ORDER BY createdAt DESC")
    suspend fun getByGroup(grupoId: String): List<MemberEntryEventEntity>
}

@Dao
interface QuarterDao {
    @Upsert suspend fun upsert(rows: List<QuarterEntity>)

    /** Trimestre que cubre :hoy (yyyy-MM-dd). Espejo del filtro online sobre la tabla quarter. */
    @Query("SELECT * FROM quarter WHERE startDate <= :hoy AND endDate >= :hoy LIMIT 1")
    suspend fun getCubriendo(hoy: String): QuarterEntity?
}
