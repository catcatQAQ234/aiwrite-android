package com.aiwrite.domain.model

data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)
