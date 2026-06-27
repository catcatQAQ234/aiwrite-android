package com.aiwrite.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aiwrite.data.local.entity.StyleProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StyleProfileDao {
    @Query("SELECT * FROM style_profiles WHERE novelId = :novelId ORDER BY updatedAt DESC")
    fun getProfilesByNovel(novelId: String): Flow<List<StyleProfileEntity>>

    @Query("SELECT * FROM style_profiles WHERE id = :id")
    suspend fun getById(id: String): StyleProfileEntity?

    @Query("SELECT * FROM style_profiles WHERE novelId = :novelId AND isActive = 1 LIMIT 1")
    suspend fun getActiveProfile(novelId: String): StyleProfileEntity?

    @Query("UPDATE style_profiles SET isActive = 0 WHERE novelId = :novelId")
    suspend fun deactivateAll(novelId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: StyleProfileEntity)

    @Update
    suspend fun update(profile: StyleProfileEntity)

    @Delete
    suspend fun delete(profile: StyleProfileEntity)
}
