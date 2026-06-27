package com.aiwrite.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "style_profiles",
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
data class StyleProfileEntity(
    @PrimaryKey
    val id: String,
    val novelId: String,
    val name: String,
    val description: String = "",
    val features: String = "[]",        // JSON array of StyleFeature
    val compiledRules: String = "",      // Auto-compiled style prompt
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
