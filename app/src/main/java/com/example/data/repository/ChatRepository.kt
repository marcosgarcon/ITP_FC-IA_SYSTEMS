package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.model.AiGenerationResult
import com.example.data.model.AiModality
import com.example.data.router.AiRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class ChatRepository(
    private val database: AppDatabase,
    val aiRouter: AiRouter
) {
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()

    val conversationsFlow: Flow<List<ConversationEntity>> =
        conversationDao.getAllConversationsFlow()

    fun getMessagesFlow(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversationFlow(conversationId)

    suspend fun createConversation(mode: AiModality, customTitle: String? = null): String {
        val id = UUID.randomUUID().toString()
        val defaultTitle = customTitle ?: when (mode) {
            AiModality.CHAT -> "Conversa Geral"
            AiModality.CODE -> "Assistente de Código"
            AiModality.SHEET -> "Planilha & Dados"
            AiModality.IMAGE -> "Estúdio de Imagens"
        }
        val entity = ConversationEntity(
            id = id,
            title = defaultTitle,
            mode = mode.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        conversationDao.insertConversation(entity)
        return id
    }

    suspend fun getConversation(id: String): ConversationEntity? =
        conversationDao.getConversationById(id)

    suspend fun deleteConversation(id: String) {
        conversationDao.deleteConversationById(id)
    }

    suspend fun sendMessage(
        conversationId: String,
        prompt: String,
        modality: AiModality,
        imageStyle: String? = null
    ): AiGenerationResult {
        // 1. Insert user message
        val userMsg = MessageEntity(
            conversationId = conversationId,
            role = "user",
            content = prompt,
            timestamp = System.currentTimeMillis(),
            modality = modality.name
        )
        messageDao.insertMessage(userMsg)

        // 2. Fetch recent conversation messages for contextual chat history
        val pastMessages = messageDao.getMessagesForConversationFlow(conversationId)
            .firstOrNull().orEmpty()
        val historyPairs = pastMessages.dropLast(1).map { it.role to it.content }

        // 3. Execute AI Router with automatic fallback
        val result = aiRouter.executeWithFallback(
            prompt = prompt,
            modality = modality,
            conversationHistory = historyPairs,
            imageStyle = imageStyle
        )

        // 4. Summarize fallback details if rotation occurred
        val fallbackSummary = if (result.fallbackOccurred) {
            result.fallbackHistory.joinToString(" ➔ ") {
                "${it.provider.displayName} (${it.errorReason})"
            } + " ➔ Concluído via ${result.providerUsed.displayName}"
        } else {
            null
        }

        // 5. Insert assistant response
        val assistantMsg = MessageEntity(
            conversationId = conversationId,
            role = "assistant",
            content = result.textContent,
            timestamp = System.currentTimeMillis(),
            providerUsed = result.providerUsed.displayName,
            modelUsed = result.modelUsed,
            fallbackOccurred = result.fallbackOccurred,
            fallbackDetails = fallbackSummary,
            modality = modality.name,
            imageUrl = result.imageUrl
        )
        messageDao.insertMessage(assistantMsg)

        // 6. Update conversation timestamp and title if first message
        val currentConv = conversationDao.getConversationById(conversationId)
        if (currentConv != null) {
            val isFirstExchange = pastMessages.size <= 1
            val updatedTitle = if (isFirstExchange && (currentConv.title.startsWith("Conversa") || currentConv.title.startsWith("Assistente") || currentConv.title.startsWith("Planilha") || currentConv.title.startsWith("Estúdio"))) {
                prompt.take(32).replace("\n", " ") + (if (prompt.length > 32) "..." else "")
            } else {
                currentConv.title
            }

            conversationDao.updateConversation(
                currentConv.copy(
                    title = updatedTitle,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        return result
    }
}
