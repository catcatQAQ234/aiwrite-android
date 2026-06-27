package com.aiwrite.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "volumes",
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
data class VolumeEntity(
    @PrimaryKey
    val id: String,
    val novelId: String,
    val title: String,
    val orderIndex: Int = 0,
    val strategy: String = ""
)
