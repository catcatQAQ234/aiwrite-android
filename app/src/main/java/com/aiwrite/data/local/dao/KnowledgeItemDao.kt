package com.aiwrite.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aiwrite.data.local.entity.KnowledgeItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeItemDao {
    @Query("SELECT * FROM knowledge_items WHERE novelId = :novelId ORDER BY createdAt DESC")
    fun getByNovel(novelId: String): Flow<List<KnowledgeItemEntity>>

    @Query("SELECT * FROM knowledge_items WHERE novelId = :novelId AND source = :source")
    fun getBySource(novelId: String, source: String): Flow<List<KnowledgeItemEntity>>

    @Query("SELECT * FROM knowledge_items WHERE id = :id")
    suspend fun getById(id: String): KnowledgeItemEntity?

    @Query("SELECT * FROM knowledge_items WHERE novelId = :novelId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')")
    fun search(novelId: String, query: String): Flow<List<KnowledgeItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: KnowledgeItemEntity)

    @Update
    suspend fun update(item: KnowledgeItemEntity)

    @Delete
    suspend fun delete(item: KnowledgeItemEntity)

    @Query("DELETE FROM knowledge_items WHERE novelId = :novelId")
    suspend fun deleteByNovel(novelId: String)
}
