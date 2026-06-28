package com.aiwrite.ml

import android.content.Context
import com.aiwrite.domain.model.LocalLlmConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MnnLlmEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var nativeLoaded = false
    private var modelPtr: Long = 0

    /**
     * Initialize the LLM engine with the given model.
     */
    suspend fun initialize(config: LocalLlmConfig): Boolean = withContext(Dispatchers.IO) {
        if (!config.isEnabled || config.modelPath.isBlank()) return@withContext false

        try {
            val modelFile = File(config.modelPath)
            if (!modelFile.exists()) return@withContext false

            // Try to load native library
            try {
                System.loadLibrary("stable_diffusion_core")
                nativeLoaded = true
            } catch (_: UnsatisfiedLinkError) {
                nativeLoaded = false
            }

            // If native is available, initialize the model
            if (nativeLoaded) {
                modelPtr = nativeInit(
                    config.modelPath,
                    config.contextSize,
                    config.threads
                )
                modelPtr > 0
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generate text with streaming output.
     */
    fun generateStream(
        config: LocalLlmConfig,
        prompt: String
    ): Flow<String> = flow {
        if (!nativeLoaded || modelPtr == 0L) {
            emit("[本地模型未加载，请在设置中配置模型]")
            return@flow
        }

        try {
            withContext(Dispatchers.IO) {
                nativeGenerate(
                    modelPtr,
                    prompt,
                    config.temperature,
                    config.topP,
                    config.repeatPenalty
                ) { token ->
                    // Callback for each token - emit to flow
                    kotlinx.coroutines.runBlocking { emit(token) }
                }
            }
        } catch (e: Exception) {
            emit("\n[生成错误: ${e.message}]")
        }
    }

    /**
     * Generate text synchronously.
     */
    suspend fun generate(config: LocalLlmConfig, prompt: String): String = withContext(Dispatchers.IO) {
        if (!nativeLoaded || modelPtr == 0L) {
            return@withContext "[本地模型未加载]"
        }

        try {
            val result = StringBuilder()
            nativeGenerate(
                modelPtr,
                prompt,
                config.temperature,
                config.topP,
                config.repeatPenalty
            ) { token ->
                result.append(token)
            }
            result.toString()
        } catch (e: Exception) {
            "生成错误: ${e.message}"
        }
    }

    fun isReady(): Boolean = nativeLoaded && modelPtr > 0L

    fun release() {
        if (modelPtr > 0) {
            nativeRelease(modelPtr)
            modelPtr = 0
        }
    }

    // Native methods
    private external fun nativeInit(modelPath: String, contextSize: Int, threads: Int): Long
    private external fun nativeGenerate(
        modelPtr: Long,
        prompt: String,
        temperature: Float,
        topP: Float,
        repeatPenalty: Float,
        callback: (String) -> Unit
    ): Int
    private external fun nativeRelease(modelPtr: Long): Int
}
