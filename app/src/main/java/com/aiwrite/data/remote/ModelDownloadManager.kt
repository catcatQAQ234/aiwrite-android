package com.aiwrite.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class DownloadProgress(
    val modelName: String = "",
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val isDownloading: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
) {
    val progress: Float
        get() = if (totalBytes == 0L) 0f else downloadedBytes.toFloat() / totalBytes
}

@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(600, TimeUnit.SECONDS)
        .build()

    private val _downloadProgress = MutableStateFlow(DownloadProgress())
    val downloadProgress: StateFlow<DownloadProgress> = _downloadProgress

    val modelsDir: File
        get() = File(context.filesDir, "models/llm").also { it.mkdirs() }

    suspend fun downloadModel(
        name: String,
        url: String,
        forceRedownload: Boolean = false
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val targetFile = File(modelsDir, name)
            if (targetFile.exists() && !forceRedownload) {
                return@withContext Result.success(targetFile)
            }

            _downloadProgress.value = DownloadProgress(
                modelName = name, isDownloading = true
            )

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                _downloadProgress.value = DownloadProgress(error = "HTTP ${response.code}")
                return@withContext Result.failure(Exception("下载失败: HTTP ${response.code}"))
            }

            val body = response.body ?: run {
                _downloadProgress.value = DownloadProgress(error = "Empty response")
                return@withContext Result.failure(Exception("Empty response"))
            }

            val total = body.contentLength()
            _downloadProgress.value = _downloadProgress.value.copy(totalBytes = total)

            // Download to temp file first, then rename
            val tempFile = File(modelsDir, "$name.tmp")
            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    var downloaded = 0L
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        _downloadProgress.value = _downloadProgress.value.copy(
                            downloadedBytes = downloaded
                        )
                    }
                }
            }

            tempFile.renameTo(targetFile)
            _downloadProgress.value = DownloadProgress(
                modelName = name, isComplete = true, totalBytes = targetFile.length(), downloadedBytes = targetFile.length()
            )
            Result.success(targetFile)
        } catch (e: Exception) {
            _downloadProgress.value = DownloadProgress(error = e.message)
            Result.failure(e)
        }
    }

    fun getDownloadedModels(): List<File> {
        return modelsDir.listFiles()
            ?.filter { it.isFile && it.extension == "gguf" }
            ?: emptyList()
    }
}
