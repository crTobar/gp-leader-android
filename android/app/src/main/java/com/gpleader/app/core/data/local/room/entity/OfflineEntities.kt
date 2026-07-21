package com.gpleader.app.core.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidades espejo de las tablas de Supabase para el caché offline (solo lectura).
 * Fechas/timestamps se guardan como String ISO (parity con Supabase). Los `id` son UUID en String.
 * La conversión JSON(Supabase) → Entity ocurre en los repos; Entity → DTO también (mappers por repo).
 */

@Entity(tableName = "small_group")
data class SmallGroupEntity(
    @PrimaryKey val id: String,
    val churchId: String,
    val name: String,
    val meetingDay: String?,
    val meetingTime: String?,
    val hymn: String?,
    val favoriteVerse: String?,
    val bibleChapter: String?,
    val meetingPlace: String?,
    val isGeneralGroup: Boolean,
)

@Entity(tableName = "member")
data class MemberEntity(
    @PrimaryKey val id: String,
    val smallGroupId: String,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val secondLastName: String?,
    val phone: String?,
    val email: String?,
    val isVisitor: Boolean,
    val isActive: Boolean,
    val isLeader: Boolean,
    val status: String,
    val createdAt: String?,
)

@Entity(tableName = "meeting")
data class MeetingEntity(
    @PrimaryKey val id: String,
    val smallGroupId: String,
    val meetingDate: String,          // date ISO
    val status: String,
    val registryKind: String,         // "gp_meeting" | "saturday_worship"
    val noMeeting: Boolean,
    val submittedAt: String?,
    val submittedActorDisplayName: String?,
    val notes: String?,
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val meetingId: String,
    val memberId: String,
    val status: String,               // present | absent | justified
    val note: String?,
)

@Entity(tableName = "activity_type")
data class ActivityTypeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val level: String,                // union | pastor | my_group
    val markerType: String,           // counter | monetary | checkbox | participants
    val unitLabel: String,
    val scope: String,                // global | church | district | campo | group
    val churchId: String?,
    val districtId: String?,
    val campoId: String?,
    val unionId: String?,
    val smallGroupId: String?,
    val startDate: String?,           // date ISO
    val endDate: String?,             // date ISO
    val isActive: Boolean,
    val isMemberAccessible: Boolean,
    val frecuencia: String,           // diaria | semanal
    val sortOrder: Int,
)

@Entity(tableName = "activity_record")
data class ActivityRecordEntity(
    @PrimaryKey val id: String,
    val meetingId: String,
    val activityTypeId: String,
    val count: Int?,
    val monto: Double?,
    val notes: String?,
    val submissionStatus: String,
)

@Entity(tableName = "member_activity_record")
data class MemberActivityRecordEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val activityTypeId: String,
    val smallGroupId: String?,
    val recordDate: String,           // date ISO
    val weekStart: String?,           // date ISO
    val isDone: Boolean,
    val count: Int?,
    val monto: Double?,
    val markedAt: String?,
    val status: String,
    val submissionStatus: String,
)

@Entity(tableName = "member_entry")
data class MemberEntryEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val activityTypeId: String,
    val smallGroupId: String,
    val value: Double,
    val enteredAt: String,            // timestamptz ISO
    val status: String,               // draft | approved | pending_board | rejected
    val approvedBy: String?,
    val approvedAt: String?,
    val updatedAt: String,
    val isDeleted: Boolean,
    val isAdjustment: Boolean,
)

@Entity(tableName = "member_entry_event")
data class MemberEntryEventEntity(
    @PrimaryKey val id: String,
    val entryId: String,
    val smallGroupId: String,         // desnormalizado (del member_entry) para consultar por grupo
    val action: String,               // created | edited | approved | rejected | board_approved | deleted
    val oldValue: Double?,
    val newValue: Double?,
    val entryValue: Double?,          // valor del aporte (monto aprobado/rechazado)
    val actorRole: String,            // member | leader | church
    val actorId: String?,
    val actorName: String?,           // desnormalizado (resuelto online) para mostrar offline
    val note: String?,
    val createdAt: String,            // timestamptz ISO
    // Desnormalizados del member_entry + joins (para la bitácora offline autónoma):
    val miembroNombre: String,
    val actividadNombre: String,
    val markerType: String,
    val unitLabel: String,
)

/** Trimestre eclesiástico. Necesario offline para el filtro del historial de aportes. */
@Entity(tableName = "quarter")
data class QuarterEntity(
    @PrimaryKey val id: String,
    val startDate: String,            // date ISO (yyyy-MM-dd)
    val endDate: String,              // date ISO (yyyy-MM-dd)
)
