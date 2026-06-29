package com.aiwrite.data.remote

import com.aiwrite.domain.model.ApiConfig
import com.aiwrite.domain.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.CertificatePinner
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
        .certificatePinner(
            CertificatePinner.Builder()
                .add("api.openai.com", "sha256/6D9jvY4Xx8J5K3mP2nQ7rL1wV8tB5cF3gH9kN6sR2pM=")
                .add("api.deepseek.com", "sha256/6D9jvY4Xx8J5K3mP2nQ7rL1wV8tB5cF3gH9kN6sR2pM=")
                .add("api.siliconflow.cn", "sha256/6D9jvY4Xx8J5K3mP2nQ7rL1wV8tB5cF3gH9kN6sR2pM=")
                .build()
        )
        .build()

    private val JSON_MEDIA = "application/json".toMediaType()

    suspend fun chat(
        config: ApiConfig,
        model: String,
        messages: List<ChatMessage>,
        maxTokens: Int = 4096,
        temperature: Float = 0.8f
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = buildRequestBody(model, messages, maxTokens, temperature)
            val request = buildRequest(config, json, stream = false)
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
                Result.failure(Exception(sanitizeError(response.code, body)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(sanitizeException(e)))
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
            val json = buildRequestBody(model, messages, maxTokens, temperature, stream = true)
            val request = buildRequest(config, json, stream = true)
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val body = response.body?.string() ?: ""
                throw Exception(sanitizeError(response.code, body))
            }

            val body = response.body
                ?: throw Exception("Empty response from server")

            body.byteStream().bufferedReader().use { reader ->
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
                            if (delta.isNotEmpty()) emit(delta)
                        } catch (_: Exception) { }
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception(sanitizeException(e))
        }
    }

    private fun buildRequestBody(
        model: String, messages: List<ChatMessage>,
        maxTokens: Int, temperature: Float, stream: Boolean = false
    ): String = JSONObject().apply {
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
        if (stream) put("stream", true)
    }.toString()

    private fun buildRequest(config: ApiConfig, json: String, stream: Boolean): Request {
        return Request.Builder()
            .url("${config.baseUrl.trimEnd('/')}/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(json.toRequestBody(JSON_MEDIA))
            .build()
    }

    /**
     * Sanitize API error responses - don't leak raw server messages to UI.
     */
    private fun sanitizeError(code: Int, body: String): String {
        return when (code) {
            401, 403 -> "认证失败，请检查 API Key 是否正确"
            429 -> "请求频率过高，请稍后重试"
            500, 502, 503 -> "服务器暂时不可用 (HTTP $code)"
            else -> "请求失败 (HTTP $code)"
        }
    }

    private fun sanitizeException(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("Unable to resolve host") -> "网络连接失败，请检查网络"
            msg.contains("timeout") || msg.contains("Timeout") -> "请求超时，请稍后重试"
            msg.contains("Certificate") || msg.contains("certificate") -> "安全连接失败"
            msg.contains("401") || msg.contains("403") -> "认证失败，请检查 API Key"
            msg.contains("429") -> "请求频率过高，请稍后重试"
            else -> "请求失败，请稍后重试"
        }
    }
}
