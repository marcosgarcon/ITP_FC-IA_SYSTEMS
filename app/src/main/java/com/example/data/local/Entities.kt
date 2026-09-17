package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val mode: String, // CHAT, CODE, SHEET, IMAGE
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val role: String, // "user", "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val providerUsed: String? = null,
    val modelUsed: String? = null,
    val fallbackOccurred: Boolean = false,
    val fallbackDetails: String? = null,
    val modality: String = "CHAT",
    val imageUrl: String? = null
)
