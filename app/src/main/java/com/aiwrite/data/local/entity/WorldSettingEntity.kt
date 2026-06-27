package com.aiwrite.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "world_settings",
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
data class WorldSettingEntity(
    @PrimaryKey
    val id: String,
    val novelId: String,
    val name: String,
    val category: String, // "rule", "faction", "location", "relation", "conflict", "other"
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
