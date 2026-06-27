package com.aiwrite.data.remote

import com.aiwrite.data.local.SettingsDataStore
import com.aiwrite.domain.model.ApiConfig
import com.aiwrite.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

enum class TaskType {
    PLANNING,   // High-level planning, creative direction
    WRITING,    // Chapter content generation
    REVIEW      // Review and editing
}

@Singleton
class ModelRouter @Inject constructor(
    private val apiClient: OpenAiClient,
    private val settingsDataStore: SettingsDataStore
) {
    private suspend fun getConfig(): ApiConfig = settingsDataStore.apiConfig.first()

    private fun selectModel(config: ApiConfig, taskType: TaskType): String = when (taskType) {
        TaskType.PLANNING -> config.planningModel
        TaskType.WRITING -> config.writingModel
        TaskType.REVIEW -> config.reviewModel
    }

    suspend fun chat(
        taskType: TaskType,
        messages: List<ChatMessage>
    ): Result<String> {
        val config = getConfig()
        val model = selectModel(config, taskType)
        return apiClient.chat(config, model, messages, config.maxTokens, config.temperature)
    }

    suspend fun chatStream(
        taskType: TaskType,
        messages: List<ChatMessage>
    ): Flow<String> {
        val config = getConfig()
        val model = selectModel(config, taskType)
        return apiClient.chatStream(config, model, messages, config.maxTokens, config.temperature)
    }
}
