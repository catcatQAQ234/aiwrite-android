package com.aiwrite.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aiwrite.domain.model.ApiConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val BASE_URL = stringPreferencesKey("base_url")
        val API_KEY = stringPreferencesKey("api_key")
        val PLANNING_MODEL = stringPreferencesKey("planning_model")
        val WRITING_MODEL = stringPreferencesKey("writing_model")
        val REVIEW_MODEL = stringPreferencesKey("review_model")
        val MAX_TOKENS = intPreferencesKey("max_tokens")
        val TEMPERATURE = floatPreferencesKey("temperature")
    }

    val apiConfig: Flow<ApiConfig> = context.dataStore.data.map { prefs ->
        ApiConfig(
            baseUrl = prefs[Keys.BASE_URL] ?: "https://api.openai.com",
            apiKey = prefs[Keys.API_KEY] ?: "",
            planningModel = prefs[Keys.PLANNING_MODEL] ?: "gpt-4o",
            writingModel = prefs[Keys.WRITING_MODEL] ?: "gpt-4o",
            reviewModel = prefs[Keys.REVIEW_MODEL] ?: "gpt-4o-mini",
            maxTokens = prefs[Keys.MAX_TOKENS] ?: 4096,
            temperature = prefs[Keys.TEMPERATURE] ?: 0.8f
        )
    }

    suspend fun updateBaseUrl(url: String) {
        context.dataStore.edit { it[Keys.BASE_URL] = url }
    }

    suspend fun updateApiKey(key: String) {
        context.dataStore.edit { it[Keys.API_KEY] = key }
    }

    suspend fun updatePlanningModel(model: String) {
        context.dataStore.edit { it[Keys.PLANNING_MODEL] = model }
    }

    suspend fun updateWritingModel(model: String) {
        context.dataStore.edit { it[Keys.WRITING_MODEL] = model }
    }

    suspend fun updateReviewModel(model: String) {
        context.dataStore.edit { it[Keys.REVIEW_MODEL] = model }
    }

    suspend fun updateMaxTokens(tokens: Int) {
        context.dataStore.edit { it[Keys.MAX_TOKENS] = tokens }
    }

    suspend fun updateTemperature(temp: Float) {
        context.dataStore.edit { it[Keys.TEMPERATURE] = temp }
    }
}
