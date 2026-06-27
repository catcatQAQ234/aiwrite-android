package com.aiwrite.data.repository

import com.aiwrite.data.local.dao.CharacterProfileDao
import com.aiwrite.data.local.entity.CharacterProfileEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepository @Inject constructor(
    private val dao: CharacterProfileDao
) {
    fun getCharactersByNovel(novelId: String): Flow<List<CharacterProfileEntity>> =
        dao.getCharactersByNovel(novelId)

    fun getCharactersByRole(novelId: String, role: String): Flow<List<CharacterProfileEntity>> =
        dao.getCharactersByRole(novelId, role)

    suspend fun getById(id: String): CharacterProfileEntity? = dao.getById(id)

    suspend fun create(
        novelId: String,
        name: String,
        role: String = ""
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insert(CharacterProfileEntity(id = id, novelId = novelId, name = name, role = role))
        return id
    }

    suspend fun update(entity: CharacterProfileEntity) {
        dao.update(entity.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(entity: CharacterProfileEntity) {
        dao.delete(entity)
    }

    companion object {
        val ROLES = listOf(
            "protagonist" to "主角",
            "antagonist" to "反派/对抗",
            "supporting" to "主要配角",
            "minor" to "次要角色"
        )
        val ROLE_LABELS = ROLES.toMap()
    }
}
