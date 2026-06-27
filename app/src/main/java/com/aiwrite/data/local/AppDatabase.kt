package com.aiwrite.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aiwrite.data.local.dao.ChapterDao
import com.aiwrite.data.local.dao.NovelDao
import com.aiwrite.data.local.dao.VolumeDao
import com.aiwrite.data.local.entity.ChapterEntity
import com.aiwrite.data.local.entity.NovelEntity
import com.aiwrite.data.local.entity.VolumeEntity

@Database(
    entities = [NovelEntity::class, VolumeEntity::class, ChapterEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun novelDao(): NovelDao
    abstract fun volumeDao(): VolumeDao
    abstract fun chapterDao(): ChapterDao
}
