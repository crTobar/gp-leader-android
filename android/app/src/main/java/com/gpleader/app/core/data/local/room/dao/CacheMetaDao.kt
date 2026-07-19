package com.gpleader.app.core.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gpleader.app.core.data.local.room.entity.CacheMetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheMetaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: CacheMetaEntity)

    @Query("SELECT updatedAt FROM cache_meta WHERE `key` = :key LIMIT 1")
    suspend fun getUpdatedAt(key: String): Long?

    /**
     * Versión reactiva: emite de nuevo cuando el preloader termina. La UI debe usar esta —
     * con la suspend, un ViewModel creado antes del primer sync se queda con null para siempre.
     */
    @Query("SELECT updatedAt FROM cache_meta WHERE `key` = :key LIMIT 1")
    fun observeUpdatedAt(key: String): Flow<Long?>
}
