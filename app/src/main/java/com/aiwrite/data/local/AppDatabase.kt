package com.aiwrite.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aiwrite.data.local.dao.ChapterDao
import com.aiwrite.data.local.dao.CharacterProfileDao
import com.aiwrite.data.local.dao.GenerationHistoryDao
import com.aiwrite.data.local.dao.NovelDao
import com.aiwrite.data.local.dao.StyleProfileDao
import com.aiwrite.data.local.dao.VolumeDao
import com.aiwrite.data.local.dao.WorldSettingDao
import com.aiwrite.data.local.entity.ChapterEntity
import com.aiwrite.data.local.entity.CharacterProfileEntity
import com.aiwrite.data.local.entity.GenerationHistoryEntity
import com.aiwrite.data.local.entity.NovelEntity
import com.aiwrite.data.local.entity.StyleProfileEntity
import com.aiwrite.data.local.entity.VolumeEntity
import com.aiwrite.data.local.entity.WorldSettingEntity

@Database(
    entities = [
        NovelEntity::class,
        VolumeEntity::class,
        ChapterEntity::class,
        WorldSettingEntity::class,
        CharacterProfileEntity::class,
        StyleProfileEntity::class,
        GenerationHistoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun novelDao(): NovelDao
    abstract fun volumeDao(): VolumeDao
    abstract fun chapterDao(): ChapterDao
    abstract fun worldSettingDao(): WorldSettingDao
    abstract fun characterProfileDao(): CharacterProfileDao
    abstract fun styleProfileDao(): StyleProfileDao
    abstract fun generationHistoryDao(): GenerationHistoryDao
}
