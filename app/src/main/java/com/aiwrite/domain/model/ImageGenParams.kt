package com.aiwrite.domain.model

data class ImageGenParams(
    val prompt: String = "",
    val negativePrompt: String = "",
    val width: Int = 512,
    val height: Int = 512,
    val steps: Int = 20,
    val cfgScale: Float = 7.5f,
    val seed: Long = -1L,
    val scheduler: String = "euler",
    val denoiseStrength: Float = 1.0f,
    val useCpu: Boolean = false
)

val SCHEDULERS = listOf("ddim", "pndm", "lms", "euler", "euler_ancestral", "dpm_2", "dpm_2_ancestral")
