package com.aiwrite.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "knowledge_items",
    foreignKeys = [
        ForeignKey(
            entity = NovelEntity::class,
            parentColumns = ["id"],
            childColumns = ["novelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("novelId")]
)
data class KnowledgeItemEntity(
    @PrimaryKey
    val id: String,
    val novelId: String,
    val title: String,
    val content: String,
    val source: String = "",        // "imported", "analysis", "manual"
    val tags: String = "",           // JSON array of tags
    val createdAt: Long = System.currentTimeMillis()
)
