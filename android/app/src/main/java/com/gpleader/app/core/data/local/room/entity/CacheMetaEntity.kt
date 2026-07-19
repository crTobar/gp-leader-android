package com.gpleader.app.core.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadatos de caché: registra cuándo se actualizó por última vez cada conjunto de datos
 * (por `key`, ej. "reuniones:$grupoId"). Alimenta el indicador de "última actualización" offline.
 */
@Entity(tableName = "cache_meta")
data class CacheMetaEntity(
    @PrimaryKey val key: String,
    val updatedAt: Long,   // epoch millis
)
