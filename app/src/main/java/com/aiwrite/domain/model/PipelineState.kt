package com.aiwrite.domain.model

data class ChapterJob(
    val chapterId: String,
    val chapterTitle: String,
    val volumeTitle: String,
    val status: JobStatus = JobStatus.PENDING,
    val error: String? = null
)

enum class JobStatus { PENDING, GENERATING, DONE, FAILED }

data class PipelineProgress(
    val novelId: String = "",
    val novelTitle: String = "",
    val jobs: List<ChapterJob> = emptyList(),
    val currentJobIndex: Int = -1,
    val isRunning: Boolean = false,
    val totalChapters: Int = 0,
    val completedChapters: Int = 0
) {
    val progress: Float
        get() = if (totalChapters == 0) 0f else completedChapters.toFloat() / totalChapters
}
