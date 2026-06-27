package com.aiwrite.data.repository

import com.aiwrite.data.local.dao.StyleProfileDao
import com.aiwrite.data.local.entity.StyleProfileEntity
import com.aiwrite.domain.model.StyleFeature
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleRepository @Inject constructor(
    private val dao: StyleProfileDao
) {
    fun getProfilesByNovel(novelId: String): Flow<List<StyleProfileEntity>> =
        dao.getProfilesByNovel(novelId)

    suspend fun getById(id: String): StyleProfileEntity? = dao.getById(id)

    suspend fun getActiveProfile(novelId: String): StyleProfileEntity? = dao.getActiveProfile(novelId)

    suspend fun create(novelId: String, name: String, description: String = ""): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        dao.insert(
            StyleProfileEntity(
                id = id, novelId = novelId, name = name,
                description = description, createdAt = now, updatedAt = now
            )
        )
        return id
    }

    suspend fun update(entity: StyleProfileEntity) {
        dao.update(entity.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(entity: StyleProfileEntity) {
        dao.delete(entity)
    }

    suspend fun setActive(profile: StyleProfileEntity) {
        dao.deactivateAll(profile.novelId)
        dao.update(profile.copy(isActive = true, updatedAt = System.currentTimeMillis()))
    }

    suspend fun addFeature(profile: StyleProfileEntity, feature: StyleFeature) {
        val features = parseFeatures(profile.features).toMutableList()
        features.add(feature)
        val newFeaturesJson = featuresToJson(features)
        val newRules = compileRules(features)
        dao.update(profile.copy(
            features = newFeaturesJson,
            compiledRules = newRules,
            updatedAt = System.currentTimeMillis()
        ))
    }

    suspend fun toggleFeature(profile: StyleProfileEntity, featureId: String, enabled: Boolean) {
        val features = parseFeatures(profile.features).map {
            if (it.id == featureId) it.copy(enabled = enabled) else it
        }
        dao.update(profile.copy(
            features = featuresToJson(features),
            compiledRules = compileRules(features),
            updatedAt = System.currentTimeMillis()
        ))
    }

    companion object {
        fun parseFeatures(featuresJson: String): List<StyleFeature> {
            if (featuresJson.isBlank()) return emptyList()
            return try {
                val arr = JSONArray(featuresJson)
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    StyleFeature(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.getString("name"),
                        description = obj.optString("description", ""),
                        enabled = obj.optBoolean("enabled", true),
                        weight = obj.optDouble("weight", 1.0).toFloat()
                    )
                }
            } catch (_: Exception) { emptyList() }
        }

        fun featuresToJson(features: List<StyleFeature>): String {
            val arr = JSONArray()
            features.forEach { f ->
                arr.put(JSONObject().apply {
                    put("id", f.id)
                    put("name", f.name)
                    put("description", f.description)
                    put("enabled", f.enabled)
                    put("weight", f.weight.toDouble())
                })
            }
            return arr.toString()
        }

        fun compileRules(features: List<StyleFeature>): String {
            val enabled = features.filter { it.enabled }
            if (enabled.isEmpty()) return ""

            val rules = buildList {
                add("写作风格规则：")
                enabled.forEach { f ->
                    val weightNote = if (f.weight < 1.0f) "（权重：${String.format("%.1f", f.weight)}）" else ""
                    add("- ${f.name}$weightNote")
                    if (f.description.isNotBlank()) {
                        add("  ${f.description}")
                    }
                }
                add("")
                add("反AI规则：")
                add("- 避免模板化开头（如'在...中'、'随着...'、'众所周知'）")
                add("- 避免过度使用形容词和副词堆砌")
                add("- 对话应自然，避免所有角色使用相似的语气")
                add("- 场景过渡避免使用'与此同时'、'另一方面'等机械连接词")
                add("- 描写应通过角色的感知和动作来呈现，而非直接说明")
            }
            return rules.joinToString("\n")
        }

        // Pre-built feature templates for quick add
        val FEATURE_TEMPLATES = listOf(
            StyleFeature(name = "简练白描", description = "用最少的词语表达最多的意思，避免冗余修饰"),
            StyleFeature(name = "多感官描写", description = "调动视觉、听觉、嗅觉、触觉、味觉营造沉浸感"),
            StyleFeature(name = "短句节奏", description = "使用短句和碎片化句式营造紧张感或快节奏"),
            StyleFeature(name = "长句铺陈", description = "使用复合句和修饰从句营造舒缓、沉思的氛围"),
            StyleFeature(name = "口语化对话", description = "角色对话贴近真实口语，包含停顿、重复和破碎句式"),
            StyleFeature(name = "内心独白", description = "深入角色内心，用第一人称或自由间接引语呈现思考"),
            StyleFeature(name = "环境隐喻", description = "通过环境描写暗示角色情绪和故事氛围"),
            StyleFeature(name = "冷峻叙述", description = "叙述者保持距离，不直接评判角色行为，让读者自行判断"),
            StyleFeature(name = "多视角切换", description = "在不同角色视角间切换，呈现事件的多个侧面"),
            StyleFeature(name = "伏笔密集", description = "在细节中埋设伏笔，后续章节回收，增强阅读参与感"),
            StyleFeature(name = "文化厚度", description = "融入典故、习俗、语言特色等文化元素增加世界真实感"),
            StyleFeature(name = "悬念驱动", description = "每章结尾设置悬念或未解问题，驱动读者继续阅读"),
        )
    }
}
