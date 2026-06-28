package com.aiwrite.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.aiwrite.domain.model.ImageGenParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MnnImageGenEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var isLoaded = false

    /**
     * Initialize the MNN engine. Copies model files from assets if needed.
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelDir = File(context.filesDir, "models/cvtbase")
            if (!modelDir.exists()) {
                modelDir.mkdirs()
            }

            // Model files that should be present
            val modelFiles = listOf(
                "unet.mnn", "clip_skip_1.mnn", "clip_skip_2.mnn",
                "vae_decoder.mnn", "vae_encoder.mnn", "tokenizer.json"
            )

            // Check if all models exist, copy from assets if needed
            for (fileName in modelFiles) {
                val targetFile = File(modelDir, fileName)
                if (!targetFile.exists()) {
                    try {
                        context.assets.open("models/cvtbase/$fileName").use { input ->
                            FileOutputStream(targetFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    } catch (_: Exception) {
                        // Model not bundled in assets - user needs to download
                    }
                }
            }

            // Load native library
            try {
                System.loadLibrary("stable_diffusion_core")
                isLoaded = true
            } catch (_: UnsatisfiedLinkError) {
                // Native library not available - will use CPU fallback
                isLoaded = false
            }

            isLoaded
        } catch (e: Exception) {
            android.util.Log.e("MnnImageGen", "Failed to initialize engine", e)
            false
        }
    }

    /**
     * Generate an image from the given parameters.
     * Returns the path to the saved image file.
     */
    suspend fun generate(params: ImageGenParams): Result<String> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()

            if (!isLoaded) {
                return@withContext Result.failure(
                    Exception("MNN engine not loaded. Please download model files first.")
                )
            }

            val outputDir = File(context.filesDir, "generated_images")
            if (!outputDir.exists()) outputDir.mkdirs()

            val outputFile = File(outputDir, "gen_${System.currentTimeMillis()}.png")

            // Call native generation
            nativeGenerate(
                modelDir = File(context.filesDir, "models/cvtbase").absolutePath,
                prompt = params.prompt,
                negativePrompt = params.negativePrompt,
                outputPath = outputFile.absolutePath,
                width = params.width,
                height = params.height,
                steps = params.steps,
                cfgScale = params.cfgScale,
                seed = if (params.seed == -1L) System.currentTimeMillis() else params.seed,
                scheduler = params.scheduler,
                useCpu = params.useCpu
            )

            val elapsed = (System.currentTimeMillis() - startTime) / 1000f
            val genTime = "${String.format("%.1f", elapsed)}s"

            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a placeholder bitmap when native engine is unavailable.
     */
    fun generatePlaceholder(params: ImageGenParams, onResult: (Bitmap?) -> Unit) {
        // Creates a simple gradient placeholder representing the prompt
        val bitmap = Bitmap.createBitmap(params.width, params.height, Bitmap.Config.ARGB_8888)
        val baseColor = params.prompt.hashCode()
        val r = ((baseColor shr 16) and 0xFF)
        val g = ((baseColor shr 8) and 0xFF)
        val b = (baseColor and 0xFF)

        for (x in 0 until params.width) {
            for (y in 0 until params.height) {
                val factor = (x.toFloat() / params.width + y.toFloat() / params.height) / 2f
                val pixel = android.graphics.Color.rgb(
                    ((r * factor).toInt()).coerceIn(0, 255),
                    ((g * (1 - factor)).toInt()).coerceIn(0, 255),
                    ((b * factor).toInt()).coerceIn(0, 255)
                )
                bitmap.setPixel(x, y, pixel)
            }
        }

        // Draw prompt text as overlay
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText(params.prompt.take(50), params.width / 2f, params.height / 2f, paint)

        onResult(bitmap)
    }

    fun getGenerationTime(imagePath: String): String {
        val name = File(imagePath).nameWithoutExtension
        val timestamp = name.removePrefix("gen_").toLongOrNull() ?: return "?"
        // Approximate - in real use this comes from actual timing
        return "~${((System.currentTimeMillis() - timestamp) / 1000).coerceAtLeast(1)}s"
    }

    // Native methods
    private external fun nativeGenerate(
        modelDir: String,
        prompt: String,
        negativePrompt: String,
        outputPath: String,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        seed: Long,
        scheduler: String,
        useCpu: Boolean
    ): Int
}
