package com.aiwrite.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aiwrite.data.local.entity.CharacterProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterProfileDao {
    @Query("SELECT * FROM character_profiles WHERE novelId = :novelId ORDER BY role, name ASC")
    fun getCharactersByNovel(novelId: String): Flow<List<CharacterProfileEntity>>

    @Query("SELECT * FROM character_profiles WHERE novelId = :novelId AND role = :role ORDER BY name ASC")
    fun getCharactersByRole(novelId: String, role: String): Flow<List<CharacterProfileEntity>>

    @Query("SELECT * FROM character_profiles WHERE id = :id")
    suspend fun getById(id: String): CharacterProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(character: CharacterProfileEntity)

    @Update
    suspend fun update(character: CharacterProfileEntity)

    @Delete
    suspend fun delete(character: CharacterProfileEntity)
}
