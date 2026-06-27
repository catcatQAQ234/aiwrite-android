package com.aiwrite.domain.model

data class StyleFeature(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val enabled: Boolean = true,
    val weight: Float = 1.0f
)
