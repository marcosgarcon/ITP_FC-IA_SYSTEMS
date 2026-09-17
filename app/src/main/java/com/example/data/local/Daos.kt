package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversationsFlow(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversationFlow(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(conversationId: String): MessageEntity?
}
