package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.model.AiModality
import com.example.data.model.ExecutionMode
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderType
import com.example.data.model.TaskCategory
import com.example.data.repository.ChatRepository
import com.example.data.router.AiRouter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val aiRouter = AiRouter(application)
    private val repository = ChatRepository(database, aiRouter)

    val providersState: StateFlow<List<ProviderConfig>> = aiRouter.providersState
    val executionMode: StateFlow<ExecutionMode> = aiRouter.executionMode

    val conversationsState: StateFlow<List<ConversationEntity>> = repository.conversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    val currentMessagesState: StateFlow<List<MessageEntity>> = _currentConversationId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getMessagesFlow(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedModality = MutableStateFlow(AiModality.CHAT)
    val selectedModality: StateFlow<AiModality> = _selectedModality.asStateFlow()

    private val _selectedTaskCategory = MutableStateFlow<TaskCategory?>(null)
    val selectedTaskCategory: StateFlow<TaskCategory?> = _selectedTaskCategory.asStateFlow()

    private val _selectedImageStyle = MutableStateFlow("Realista")
    val selectedImageStyle: StateFlow<String> = _selectedImageStyle.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _fallbackAlert = MutableStateFlow<String?>(null)
    val fallbackAlert: StateFlow<String?> = _fallbackAlert.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            // Auto-select or create default conversation on start
            val existing = repository.conversationsFlow.firstOrNull()
            if (!existing.isNullOrEmpty()) {
                _currentConversationId.value = existing.first().id
            } else {
                startNewConversation(AiModality.CHAT)
            }
        }
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun setExecutionMode(mode: ExecutionMode) {
        aiRouter.setExecutionMode(mode)
    }

    fun setTaskCategory(category: TaskCategory?) {
        _selectedTaskCategory.value = category
        if (category != null) {
            _selectedModality.value = category.associatedModality
            // If the user's input is currently blank or only had another prefix, populate with first sample or prefix
            if (_inputText.value.isBlank()) {
                val sample = if (category.samplePrompts.isNotEmpty()) category.samplePrompts[0] else ""
                _inputText.value = sample
            }
        }
    }

    fun applyPromptSuggestion(prompt: String) {
        _inputText.value = prompt
    }

    fun setModality(modality: AiModality) {
        _selectedModality.value = modality
        if (_selectedTaskCategory.value?.associatedModality != modality) {
            _selectedTaskCategory.value = null
        }
    }

    fun setImageStyle(style: String) {
        _selectedImageStyle.value = style
    }

    fun selectConversation(id: String) {
        _currentConversationId.value = id
        viewModelScope.launch {
            val conv = repository.getConversation(id)
            if (conv != null) {
                try {
                    _selectedModality.value = AiModality.valueOf(conv.mode)
                } catch (_: Exception) {
                    _selectedModality.value = AiModality.CHAT
                }
            }
        }
    }

    fun startNewConversation(mode: AiModality = _selectedModality.value) {
        viewModelScope.launch {
            _selectedModality.value = mode
            val newId = repository.createConversation(mode)
            _currentConversationId.value = newId
            _fallbackAlert.value = null
            _errorMessage.value = null
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                val remaining = repository.conversationsFlow.first()
                if (remaining.isNotEmpty()) {
                    _currentConversationId.value = remaining.first().id
                } else {
                    startNewConversation(AiModality.CHAT)
                }
            }
        }
    }

    fun sendMessage() {
        val rawText = _inputText.value.trim()
        if (rawText.isBlank() || _isGenerating.value) return

        var convId = _currentConversationId.value
        val modality = _selectedModality.value
        val style = _selectedImageStyle.value
        val category = _selectedTaskCategory.value

        val promptToSend = if (category != null && category.defaultPromptPrefix.isNotBlank() && !rawText.startsWith("[MODO:")) {
            "${category.defaultPromptPrefix}\n\nTarefa Solicitada:\n$rawText"
        } else {
            rawText
        }

        _inputText.value = ""
        _isGenerating.value = true
        _fallbackAlert.value = null
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                if (convId == null) {
                    val initialTitle = category?.title ?: modality.label
                    convId = repository.createConversation(modality, initialTitle)
                    _currentConversationId.value = convId
                }

                val result = repository.sendMessage(
                    conversationId = convId!!,
                    prompt = promptToSend,
                    modality = modality,
                    imageStyle = if (modality == AiModality.IMAGE) style else null
                )

                if (result.fallbackOccurred) {
                    val fallbackFrom = result.fallbackHistory.firstOrNull()?.provider?.displayName ?: "IA anterior"
                    _fallbackAlert.value = "🔄 Limite de cota atingido em $fallbackFrom. Rotação automática concluída com sucesso via ${result.providerUsed.displayName}!"
                }

            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Ocorreu um erro ao processar requisição."
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun dismissFallbackAlert() {
        _fallbackAlert.value = null
    }

    fun dismissErrorMessage() {
        _errorMessage.value = null
    }

    fun saveProviderKey(type: ProviderType, key: String) {
        aiRouter.saveProviderKey(type, key)
    }

    fun toggleProvider(type: ProviderType, isEnabled: Boolean) {
        aiRouter.toggleProviderEnabled(type, isEnabled)
    }

    fun reorderPriority(from: Int, to: Int) {
        aiRouter.reorderPriority(from, to)
    }

    fun resetQuotas() {
        aiRouter.resetAllQuotaStatuses()
    }
}
