package com.aiwrite.domain.model

data class LocalLlmConfig(
    val modelPath: String = "",
    val modelName: String = "",
    val isEnabled: Boolean = false,
    val contextSize: Int = 2048,
    val threads: Int = 4,
    val temperature: Float = 0.8f,
    val topP: Float = 0.9f,
    val repeatPenalty: Float = 1.1f
)

data class ModelInfo(
    val name: String,
    val displayName: String,
    val size: String,
    val downloadUrl: String,
    val description: String
)

val AVAILABLE_MODELS = listOf(
    ModelInfo(
        name = "qwen2.5-1.5b-instruct-q4_k_m",
        displayName = "Qwen 2.5 1.5B",
        size = "~1GB",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
        description = "轻量级中文模型，适合手机运行"
    ),
    ModelInfo(
        name = "gemma-3-1b-it-q4_k_m",
        displayName = "Gemma 3 1B",
        size = "~700MB",
        downloadUrl = "https://huggingface.co/unsloth/gemma-3-1b-it-GGUF/resolve/main/gemma-3-1b-it-q4_k_m.gguf",
        description = "Google 轻量模型，英文能力强"
    ),
    ModelInfo(
        name = "deepseek-r1-distill-qwen-1.5b-q4_k_m",
        displayName = "DeepSeek R1 1.5B",
        size = "~1GB",
        downloadUrl = "https://huggingface.co/unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF/resolve/main/DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf",
        description = "推理增强模型，适合复杂规划"
    )
)
