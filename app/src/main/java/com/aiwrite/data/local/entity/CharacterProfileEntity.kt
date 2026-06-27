package com.aiwrite.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "character_profiles",
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
data class CharacterProfileEntity(
    @PrimaryKey
    val id: String,
    val novelId: String,
    val name: String,
    val role: String = "",         // protagonist/antagonist/supporting/minor
    val factionId: String = "",    // reference to WorldSettingEntity (faction)
    val traits: String = "",       // JSON: {"age":..., "gender":..., "personality":[...], "appearance":"..."}
    val background: String = "",
    val motivation: String = "",
    val arc: String = "",          // Character growth arc
    val relations: String = "",    // JSON: [{"characterId":"...","type":"ally/rival/lover/...","desc":"..."}]
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
