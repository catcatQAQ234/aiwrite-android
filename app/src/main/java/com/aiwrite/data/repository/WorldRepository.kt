package com.aiwrite.data.repository

import com.aiwrite.data.local.dao.WorldSettingDao
import com.aiwrite.data.local.entity.WorldSettingEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorldRepository @Inject constructor(
    private val dao: WorldSettingDao
) {
    fun getSettingsByNovel(novelId: String): Flow<List<WorldSettingEntity>> =
        dao.getSettingsByNovel(novelId)

    fun getSettingsByCategory(novelId: String, category: String): Flow<List<WorldSettingEntity>> =
        dao.getSettingsByCategory(novelId, category)

    suspend fun getById(id: String): WorldSettingEntity? = dao.getById(id)

    suspend fun create(
        novelId: String,
        name: String,
        category: String,
        content: String = ""
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insert(WorldSettingEntity(id = id, novelId = novelId, name = name, category = category, content = content))
        return id
    }

    suspend fun update(entity: WorldSettingEntity) {
        dao.update(entity)
    }

    suspend fun delete(entity: WorldSettingEntity) {
        dao.delete(entity)
    }

    companion object {
        val CATEGORIES = listOf(
            "rule" to "世界规则",
            "faction" to "势力阵营",
            "location" to "地点",
            "relation" to "关系网",
            "conflict" to "冲突入口",
            "other" to "其他"
        )
        val CATEGORY_LABELS = CATEGORIES.toMap()
    }
}
