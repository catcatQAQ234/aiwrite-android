package com.aiwrite.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.aiwrite.data.local.dao.GenerationHistoryDao
import com.aiwrite.data.local.entity.GenerationHistoryEntity
import com.aiwrite.domain.model.ImageGenParams
import com.aiwrite.ml.MnnImageGenEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val historyDao: GenerationHistoryDao,
    private val engine: MnnImageGenEngine
) {
    fun getAllHistory(): Flow<List<GenerationHistoryEntity>> = historyDao.getAllHistory()

    fun getFavorites(): Flow<List<GenerationHistoryEntity>> = historyDao.getFavorites()

    suspend fun toggleFavorite(id: Long) {
        val item = historyDao.getById(id) ?: return
        historyDao.update(item.copy(favorite = !item.favorite))
    }

    suspend fun deleteHistory(item: GenerationHistoryEntity) {
        // Delete the image file
        if (item.imagePath.isNotBlank()) {
            File(item.imagePath).delete()
        }
        historyDao.delete(item)
    }

    suspend fun generate(params: ImageGenParams): Result<GenerationHistoryEntity> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()

            val result = engine.generate(params)

            result.fold(
                onSuccess = { imagePath ->
                    val elapsed = (System.currentTimeMillis() - startTime) / 1000f
                    val entity = GenerationHistoryEntity(
                        timestamp = System.currentTimeMillis(),
                        imagePath = imagePath,
                        width = params.width,
                        height = params.height,
                        steps = params.steps,
                        cfgScale = params.cfgScale,
                        seed = params.seed,
                        prompt = params.prompt,
                        negativePrompt = params.negativePrompt,
                        generationTime = "${String.format("%.1f", elapsed)}s",
                        scheduler = params.scheduler,
                        denoiseStrength = params.denoiseStrength,
                        useCpu = params.useCpu
                    )
                    historyDao.insert(entity)
                    Result.success(entity)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveToGallery(imagePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (!file.exists()) return@withContext false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AIWrite")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                ) ?: return@withContext false

                context.contentResolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { it.copyTo(out) }
                }
            } else {
                val destDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "AIWrite"
                )
                destDir.mkdirs()
                file.copyTo(File(destDir, file.name), overwrite = true)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun initializeEngine(): Boolean = engine.initialize()
}
