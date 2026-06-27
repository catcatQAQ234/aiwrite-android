package com.aiwrite.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aiwrite.data.local.dao.NovelDao
import com.aiwrite.data.local.entity.NovelEntity

@Database(
    entities = [NovelEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun novelDao(): NovelDao
}
