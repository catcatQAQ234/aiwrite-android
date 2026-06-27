package com.aiwrite.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aiwrite.data.local.entity.VolumeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VolumeDao {
    @Query("SELECT * FROM volumes WHERE novelId = :novelId ORDER BY orderIndex ASC")
    fun getVolumesByNovel(novelId: String): Flow<List<VolumeEntity>>

    @Query("SELECT * FROM volumes WHERE id = :id")
    suspend fun getVolumeById(id: String): VolumeEntity?

    @Query("SELECT COUNT(*) FROM volumes WHERE novelId = :novelId")
    suspend fun getVolumeCount(novelId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVolume(volume: VolumeEntity)

    @Update
    suspend fun updateVolume(volume: VolumeEntity)

    @Delete
    suspend fun deleteVolume(volume: VolumeEntity)
}
