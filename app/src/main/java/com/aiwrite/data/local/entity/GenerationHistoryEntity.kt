package com.aiwrite.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generation_history")
data class GenerationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val modelId: String = "stable-diffusion-v1.5",
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String = "",
    val width: Int = 512,
    val height: Int = 512,
    val steps: Int = 20,
    val cfgScale: Float = 7.5f,
    val seed: Long = -1L,
    val prompt: String = "",
    val negativePrompt: String = "",
    val generationTime: String = "",
    val scheduler: String = "euler",
    val denoiseStrength: Float = 1.0f,
    val useCpu: Boolean = false,
    val favorite: Boolean = false
)
