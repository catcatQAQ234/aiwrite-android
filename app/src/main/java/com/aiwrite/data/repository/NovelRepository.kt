package com.aiwrite.data.repository

import com.aiwrite.data.local.dao.ChapterDao
import com.aiwrite.data.local.dao.NovelDao
import com.aiwrite.data.local.dao.VolumeDao
import com.aiwrite.data.local.entity.ChapterEntity
import com.aiwrite.data.local.entity.NovelEntity
import com.aiwrite.data.local.entity.VolumeEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NovelRepository @Inject constructor(
    private val novelDao: NovelDao,
    private val volumeDao: VolumeDao,
    private val chapterDao: ChapterDao
) {
    // Novel
    fun getAllNovels(): Flow<List<NovelEntity>> = novelDao.getAllNovels()

    suspend fun getNovelById(id: String): NovelEntity? = novelDao.getNovelById(id)

    suspend fun createNovel(title: String, synopsis: String = ""): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        novelDao.insertNovel(
            NovelEntity(id = id, title = title, synopsis = synopsis, createdAt = now, updatedAt = now)
        )
        return id
    }

    suspend fun updateNovel(novel: NovelEntity) {
        novelDao.updateNovel(novel.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNovel(novel: NovelEntity) {
        novelDao.deleteNovel(novel)
    }

    // Volume
    fun getVolumesByNovel(novelId: String): Flow<List<VolumeEntity>> =
        volumeDao.getVolumesByNovel(novelId)

    suspend fun createVolume(novelId: String, title: String): String {
        val id = UUID.randomUUID().toString()
        val count = volumeDao.getVolumeCount(novelId)
        volumeDao.insertVolume(
            VolumeEntity(id = id, novelId = novelId, title = title, orderIndex = count)
        )
        return id
    }

    suspend fun updateVolume(volume: VolumeEntity) {
        volumeDao.updateVolume(volume)
    }

    suspend fun deleteVolume(volume: VolumeEntity) {
        volumeDao.deleteVolume(volume)
    }

    // Chapter
    fun getChaptersByVolume(volumeId: String): Flow<List<ChapterEntity>> =
        chapterDao.getChaptersByVolume(volumeId)

    suspend fun getChapterById(id: String): ChapterEntity? = chapterDao.getChapterById(id)

    suspend fun createChapter(volumeId: String, title: String): String {
        val id = UUID.randomUUID().toString()
        val count = chapterDao.getChapterCount(volumeId)
        val now = System.currentTimeMillis()
        chapterDao.insertChapter(
            ChapterEntity(
                id = id, volumeId = volumeId, title = title, orderIndex = count,
                createdAt = now, updatedAt = now
            )
        )
        return id
    }

    suspend fun updateChapter(chapter: ChapterEntity) {
        chapterDao.updateChapter(chapter.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteChapter(chapter: ChapterEntity) {
        chapterDao.deleteChapter(chapter)
    }
}
