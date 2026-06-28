package com.aiwrite.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aiwrite.data.local.entity.GenerationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GenerationHistoryDao {
    @Query("SELECT * FROM generation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<GenerationHistoryEntity>>

    @Query("SELECT * FROM generation_history WHERE favorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<GenerationHistoryEntity>>

    @Query("SELECT * FROM generation_history WHERE id = :id")
    suspend fun getById(id: Long): GenerationHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GenerationHistoryEntity): Long

    @Update
    suspend fun update(item: GenerationHistoryEntity)

    @Delete
    suspend fun delete(item: GenerationHistoryEntity)
}
