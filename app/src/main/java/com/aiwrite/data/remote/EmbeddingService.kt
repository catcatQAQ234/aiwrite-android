package com.aiwrite.data.remote

import com.aiwrite.data.local.SettingsDataStore
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmbeddingService @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun embed(text: String): FloatArray? {
        val config = settingsDataStore.apiConfig.first()
        if (config.apiKey.isBlank()) return null

        return try {
            val json = JSONObject().apply {
                put("model", "text-embedding-3-small")
                put("input", text)
            }

            val request = Request.Builder()
                .url("${config.baseUrl.trimEnd('/')}/v1/embeddings")
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return null

            if (response.isSuccessful) {
                val embedding = JSONObject(body)
                    .getJSONArray("data")
                    .getJSONObject(0)
                    .getJSONArray("embedding")
                FloatArray(embedding.length()) { embedding.getDouble(it).toFloat() }
            } else null
        } catch (_: Exception) { null }
    }

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB)
        return if (denom == 0f) 0f else dot / denom
    }
}
