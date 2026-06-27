package com.aiwrite.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aiwrite.data.local.entity.WorldSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldSettingDao {
    @Query("SELECT * FROM world_settings WHERE novelId = :novelId ORDER BY category, name ASC")
    fun getSettingsByNovel(novelId: String): Flow<List<WorldSettingEntity>>

    @Query("SELECT * FROM world_settings WHERE novelId = :novelId AND category = :category ORDER BY name ASC")
    fun getSettingsByCategory(novelId: String, category: String): Flow<List<WorldSettingEntity>>

    @Query("SELECT * FROM world_settings WHERE id = :id")
    suspend fun getById(id: String): WorldSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: WorldSettingEntity)

    @Update
    suspend fun update(setting: WorldSettingEntity)

    @Delete
    suspend fun delete(setting: WorldSettingEntity)
}
