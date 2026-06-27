package com.aiwrite.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aiwrite.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE volumeId = :volumeId ORDER BY orderIndex ASC")
    fun getChaptersByVolume(volumeId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: String): ChapterEntity?

    @Query("SELECT COUNT(*) FROM chapters WHERE volumeId = :volumeId")
    suspend fun getChapterCount(volumeId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)
}
