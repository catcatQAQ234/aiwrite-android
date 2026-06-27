package com.aiwrite.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = VolumeEntity::class,
            parentColumns = ["id"],
            childColumns = ["volumeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("volumeId")]
)
data class ChapterEntity(
    @PrimaryKey
    val id: String,
    val volumeId: String,
    val title: String,
    val content: String = "",
    val outline: String = "",
    val status: String = "draft",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
