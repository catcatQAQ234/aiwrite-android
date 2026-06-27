package com.aiwrite.domain.model

data class ApiConfig(
    val baseUrl: String = "https://api.openai.com",
    val apiKey: String = "",
    val planningModel: String = "gpt-4o",
    val writingModel: String = "gpt-4o",
    val reviewModel: String = "gpt-4o-mini",
    val maxTokens: Int = 4096,
    val temperature: Float = 0.8f
)
