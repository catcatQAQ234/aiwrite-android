package com.aiwrite.data.remote

import com.aiwrite.domain.model.ApiConfig
import com.aiwrite.domain.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAiClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun chat(
        config: ApiConfig,
        model: String,
        messages: List<ChatMessage>,
        maxTokens: Int = 4096,
        temperature: Float = 0.8f
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("model", model)
                put("messages", JSONArray().apply {
                    messages.forEach { msg ->
                        put(JSONObject().apply {
                            put("role", msg.role)
                            put("content", msg.content)
                        })
                    }
                })
                put("max_tokens", maxTokens)
                put("temperature", temperature.toDouble())
            }

            val request = Request.Builder()
                .url("${config.baseUrl.trimEnd('/')}/v1/chat/completions")
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val content = JSONObject(body)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                Result.success(content)
            } else {
                val error = try {
                    JSONObject(body).getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun chatStream(
        config: ApiConfig,
        model: String,
        messages: List<ChatMessage>,
        maxTokens: Int = 4096,
        temperature: Float = 0.8f
    ): Flow<String> = flow {
        try {
            val json = JSONObject().apply {
                put("model", model)
                put("messages", JSONArray().apply {
                    messages.forEach { msg ->
                        put(JSONObject().apply {
                            put("role", msg.role)
                            put("content", msg.content)
                        })
                    }
                })
                put("max_tokens", maxTokens)
                put("temperature", temperature.toDouble())
                put("stream", true)
            }

            val request = Request.Builder()
                .url("${config.baseUrl.trimEnd('/')}/v1/chat/completions")
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val error = try {
                    JSONObject(body).getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    "HTTP ${response.code}"
                }
                throw Exception(error)
            }

            val reader = BufferedReader(InputStreamReader(response.body?.byteStream()!!))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val current = line ?: continue
                if (current.startsWith("data: ")) {
                    val data = current.removePrefix("data: ").trim()
                    if (data == "[DONE]") break

                    try {
                        val delta = JSONObject(data)
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .optJSONObject("delta")
                            ?.optString("content", "") ?: ""
                        if (delta.isNotEmpty()) {
                            emit(delta)
                        }
                    } catch (_: Exception) { }
                }
            }
            reader.close()
        } catch (e: Exception) {
            throw e
        }
    }
}
