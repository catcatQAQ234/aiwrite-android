package com.aiwrite.data.repository

import com.aiwrite.data.local.dao.ChapterDao
import com.aiwrite.data.local.dao.NovelDao
import com.aiwrite.data.local.dao.VolumeDao
import com.aiwrite.data.local.entity.ChapterEntity
import com.aiwrite.data.local.entity.VolumeEntity
import com.aiwrite.data.remote.ModelRouter
import com.aiwrite.data.remote.TaskType
import com.aiwrite.domain.model.ChatMessage
import com.aiwrite.domain.model.ChapterJob
import com.aiwrite.domain.model.JobStatus
import com.aiwrite.domain.model.PipelineProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PipelineRepository @Inject constructor(
    private val novelDao: NovelDao,
    private val volumeDao: VolumeDao,
    private val chapterDao: ChapterDao,
    private val modelRouter: ModelRouter
) {
    private val _progress = MutableStateFlow(PipelineProgress())
    val progress: StateFlow<PipelineProgress> = _progress

    suspend fun prepareJobs(novelId: String): PipelineProgress = withContext(Dispatchers.IO) {
        val novel = novelDao.getNovelById(novelId) ?: return@withContext PipelineProgress()

        val jobs = mutableListOf<ChapterJob>()
        val volumes = volumeDao.getVolumesByNovel(novelId).first()

        for (volume in volumes) {
            val chapters = chapterDao.getChaptersByVolume(volume.id).first()
            for (chapter in chapters) {
                jobs.add(
                    ChapterJob(
                        chapterId = chapter.id,
                        chapterTitle = chapter.title,
                        volumeTitle = volume.title
                    )
                )
            }
        }

        PipelineProgress(
            novelId = novelId,
            novelTitle = novel.title,
            jobs = jobs,
            totalChapters = jobs.size
        )
    }

    suspend fun runPipeline(initialProgress: PipelineProgress) {
        _progress.value = initialProgress.copy(isRunning = true)

        for ((index, job) in initialProgress.jobs.withIndex()) {
            if (job.status == JobStatus.DONE) continue

            _progress.value = _progress.value.copy(
                currentJobIndex = index,
                jobs = _progress.value.jobs.toMutableList().also {
                    it[index] = job.copy(status = JobStatus.GENERATING)
                }
            )

            try {
                val chapter = chapterDao.getChapterById(job.chapterId)
                if (chapter != null) {
                    val content = generateChapterContent(chapter)
                    chapterDao.updateChapter(chapter.copy(
                        content = content,
                        status = "done",
                        updatedAt = System.currentTimeMillis()
                    ))
                }

                _progress.value = _progress.value.copy(
                    completedChapters = _progress.value.completedChapters + 1,
                    jobs = _progress.value.jobs.toMutableList().also {
                        it[index] = it[index].copy(status = JobStatus.DONE)
                    }
                )
            } catch (e: Exception) {
                _progress.value = _progress.value.copy(
                    jobs = _progress.value.jobs.toMutableList().also {
                        it[index] = it[index].copy(
                            status = JobStatus.FAILED,
                            error = e.message ?: "Unknown error"
                        )
                    }
                )
            }
        }

        _progress.value = _progress.value.copy(isRunning = false, currentJobIndex = -1)
    }

    fun retryFailed() {
        val current = _progress.value
        val retryJobs = current.jobs.map { job ->
            if (job.status == JobStatus.FAILED) job.copy(status = JobStatus.PENDING, error = null) else job
        }
        _progress.value = current.copy(jobs = retryJobs, completedChapters = retryJobs.count { it.status == JobStatus.DONE })
    }

    private suspend fun generateChapterContent(chapter: ChapterEntity): String {
        val messages = listOf(
            ChatMessage("system", """
你是一位专业的小说作家。请根据章节标题和大纲，生成一章完整的正文。

写作要求：
- 语言流畅自然，避免AI模板化表达
- 每段聚焦一个场景或情绪
- 对话自然真实
- 在关键节点埋设情感起伏
- 章节结尾留有余韵
            """.trimIndent()),
            ChatMessage("user", """
章节标题：${chapter.title}
章节大纲：${chapter.outline.ifBlank { "自由发挥" }}

请生成本章完整正文：
            """.trimIndent())
        )
        return modelRouter.chat(TaskType.WRITING, messages).getOrDefault("生成失败：${chapter.title}")
    }

    fun stop() {
        _progress.value = _progress.value.copy(isRunning = false)
    }
}
