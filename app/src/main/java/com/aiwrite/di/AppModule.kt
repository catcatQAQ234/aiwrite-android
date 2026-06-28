package com.aiwrite.di

import android.content.Context
import androidx.room.Room
import com.aiwrite.data.local.AppDatabase
import com.aiwrite.data.local.dao.ChapterDao
import com.aiwrite.data.local.dao.CharacterProfileDao
import com.aiwrite.data.local.dao.GenerationHistoryDao
import com.aiwrite.data.local.dao.KnowledgeItemDao
import com.aiwrite.data.local.dao.NovelDao
import com.aiwrite.data.local.dao.StyleProfileDao
import com.aiwrite.data.local.dao.VolumeDao
import com.aiwrite.data.local.dao.WorldSettingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aiwrite.db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideNovelDao(db: AppDatabase): NovelDao = db.novelDao()

    @Provides
    fun provideVolumeDao(db: AppDatabase): VolumeDao = db.volumeDao()

    @Provides
    fun provideChapterDao(db: AppDatabase): ChapterDao = db.chapterDao()

    @Provides
    fun provideWorldSettingDao(db: AppDatabase): WorldSettingDao = db.worldSettingDao()

    @Provides
    fun provideCharacterProfileDao(db: AppDatabase): CharacterProfileDao = db.characterProfileDao()

    @Provides
    fun provideStyleProfileDao(db: AppDatabase): StyleProfileDao = db.styleProfileDao()

    @Provides
    fun provideGenerationHistoryDao(db: AppDatabase): GenerationHistoryDao = db.generationHistoryDao()

    @Provides
    fun provideKnowledgeItemDao(db: AppDatabase): KnowledgeItemDao = db.knowledgeItemDao()
}
