package com.example.data.router

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.api.AiApiException
import com.example.data.api.AiClient
import com.example.data.api.AiQuotaExceededException
import com.example.data.model.AiGenerationResult
import com.example.data.model.AiModality
import com.example.data.model.ExecutionMode
import com.example.data.model.FallbackAttempt
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderHealthStatus
import com.example.data.model.ProviderType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.system.measureTimeMillis

class AiRouter(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_router_preferences", Context.MODE_PRIVATE)

    private val aiClient = AiClient()

    private val _providersState = MutableStateFlow<List<ProviderConfig>>(emptyList())
    val providersState: StateFlow<List<ProviderConfig>> = _providersState.asStateFlow()

    private val _executionMode = MutableStateFlow(
        try {
            val saved = prefs.getString("execution_mode", ExecutionMode.AUTO_ROTATION.name)
            ExecutionMode.valueOf(saved ?: ExecutionMode.AUTO_ROTATION.name)
        } catch (_: Exception) {
            ExecutionMode.AUTO_ROTATION
        }
    )
    val executionMode: StateFlow<ExecutionMode> = _executionMode.asStateFlow()

    init {
        loadProviderConfigs()
    }

    fun setExecutionMode(mode: ExecutionMode) {
        _executionMode.value = mode
        prefs.edit().putString("execution_mode", mode.name).apply()
    }

    private fun loadProviderConfigs() {
        val geminiKey = prefs.getString("key_gemini", "") ?: ""
        val effectiveGeminiKey = if (geminiKey.isNotBlank()) geminiKey else BuildConfig.GEMINI_API_KEY
        val gemmaKey = prefs.getString("key_gemma3", "") ?: ""
        val effectiveGemmaKey = if (gemmaKey.isNotBlank()) gemmaKey else effectiveGeminiKey
        val groqKey = prefs.getString("key_groq", "") ?: ""
        val openRouterKey = prefs.getString("key_openrouter", "") ?: ""

        val initialList = mutableListOf(
            ProviderConfig(
                type = ProviderType.GEMINI,
                apiKey = effectiveGeminiKey,
                isEnabled = prefs.getBoolean("enabled_gemini", true),
                priorityOrder = prefs.getInt("order_gemini", 0),
                currentStatus = if (effectiveGeminiKey.isNotBlank()) ProviderHealthStatus.HEALTHY else ProviderHealthStatus.KEY_MISSING,
                statusMessage = if (effectiveGeminiKey.isNotBlank()) "Pronto (Tier Gratuito)" else "Chave necessária"
            ),
            ProviderConfig(
                type = ProviderType.GEMMA_3,
                apiKey = effectiveGemmaKey,
                isEnabled = prefs.getBoolean("enabled_gemma3", true),
                priorityOrder = prefs.getInt("order_gemma3", 1),
                currentStatus = if (effectiveGemmaKey.isNotBlank()) ProviderHealthStatus.HEALTHY else ProviderHealthStatus.KEY_MISSING,
                statusMessage = if (effectiveGemmaKey.isNotBlank()) "Pronto (Gemma 3 27B)" else "Chave Google necessária"
            ),
            ProviderConfig(
                type = ProviderType.GROQ,
                apiKey = groqKey,
                isEnabled = prefs.getBoolean("enabled_groq", true),
                priorityOrder = prefs.getInt("order_groq", 2),
                currentStatus = if (groqKey.isNotBlank()) ProviderHealthStatus.HEALTHY else ProviderHealthStatus.KEY_MISSING,
                statusMessage = if (groqKey.isNotBlank()) "Pronto (Ultra Rápido)" else "Sem chave (opcional)"
            ),
            ProviderConfig(
                type = ProviderType.OPEN_ROUTER,
                apiKey = openRouterKey,
                isEnabled = prefs.getBoolean("enabled_openrouter", true),
                priorityOrder = prefs.getInt("order_openrouter", 3),
                currentStatus = if (openRouterKey.isNotBlank()) ProviderHealthStatus.HEALTHY else ProviderHealthStatus.KEY_MISSING,
                statusMessage = if (openRouterKey.isNotBlank()) "Pronto (Modelos Free)" else "Sem chave (opcional)"
            ),
            ProviderConfig(
                type = ProviderType.POLLINATIONS,
                apiKey = "",
                isEnabled = prefs.getBoolean("enabled_pollinations", true),
                priorityOrder = prefs.getInt("order_pollinations", 4),
                currentStatus = ProviderHealthStatus.HEALTHY,
                statusMessage = "100% Grátis & Ilimitado (Sem Chave)"
            )
        )

        initialList.sortBy { it.priorityOrder }
        _providersState.value = initialList
    }

    fun saveProviderKey(type: ProviderType, key: String) {
        val keyName = when (type) {
            ProviderType.GEMINI -> "key_gemini"
            ProviderType.GEMMA_3 -> "key_gemma3"
            ProviderType.GROQ -> "key_groq"
            ProviderType.OPEN_ROUTER -> "key_openrouter"
            ProviderType.POLLINATIONS -> "key_pollinations"
        }
        prefs.edit().putString(keyName, key.trim()).apply()
        loadProviderConfigs()
    }

    fun toggleProviderEnabled(type: ProviderType, isEnabled: Boolean) {
        val prefName = when (type) {
            ProviderType.GEMINI -> "enabled_gemini"
            ProviderType.GEMMA_3 -> "enabled_gemma3"
            ProviderType.GROQ -> "enabled_groq"
            ProviderType.OPEN_ROUTER -> "enabled_openrouter"
            ProviderType.POLLINATIONS -> "enabled_pollinations"
        }
        prefs.edit().putBoolean(prefName, isEnabled).apply()
        loadProviderConfigs()
    }

    fun reorderPriority(fromIndex: Int, toIndex: Int) {
        val currentList = _providersState.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            currentList.forEachIndexed { index, config ->
                config.priorityOrder = index
                val prefOrder = when (config.type) {
                    ProviderType.GEMINI -> "order_gemini"
                    ProviderType.GEMMA_3 -> "order_gemma3"
                    ProviderType.GROQ -> "order_groq"
                    ProviderType.OPEN_ROUTER -> "order_openrouter"
                    ProviderType.POLLINATIONS -> "order_pollinations"
                }
                prefs.edit().putInt(prefOrder, index).apply()
            }
            _providersState.value = currentList
        }
    }

    fun resetAllQuotaStatuses() {
        val updated = _providersState.value.map { config ->
            val hasKey = config.apiKey.isNotBlank() || !config.type.requiresKey
            config.copy(
                currentStatus = if (hasKey) ProviderHealthStatus.HEALTHY else ProviderHealthStatus.KEY_MISSING,
                statusMessage = if (hasKey) "Pronto (Cota redefinida)" else "Chave ausente"
            )
        }
        _providersState.value = updated
    }

    /**
     * Executes automatic fallback router.
     * Tries Provider 1 -> If 429/Quota Limit or Error -> Tries Provider 2 -> Tries Provider 3...
     */
    suspend fun executeWithFallback(
        prompt: String,
        modality: AiModality,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        imageStyle: String? = null
    ): AiGenerationResult {
        // Handle image generation modality
        if (modality == AiModality.IMAGE) {
            val imageUrl = aiClient.buildImageUrl(prompt, imageStyle)
            return AiGenerationResult(
                textContent = "Imagem gerada com sucesso para o prompt: \"$prompt\"${if (!imageStyle.isNullOrBlank()) " no estilo $imageStyle" else ""}.",
                imageUrl = imageUrl,
                providerUsed = ProviderType.POLLINATIONS,
                modelUsed = "Flux / Turbo AI",
                fallbackOccurred = false,
                fallbackHistory = emptyList(),
                latencyMs = 120
            )
        }

        val currentMode = _executionMode.value
        val baseProviders = _providersState.value.filter { it.isEnabled }
        if (baseProviders.isEmpty()) {
            throw Exception("Nenhum provedor de IA está habilitado. Ative ao menos um provedor nas configurações.")
        }

        // If a specific provider is pinned, prioritize it first; if it runs out of quota,
        // it gracefully falls back through the remaining providers.
        val providers = when (currentMode) {
            ExecutionMode.AUTO_ROTATION -> baseProviders
            ExecutionMode.PINNED_GEMINI -> baseProviders.sortedBy { if (it.type == ProviderType.GEMINI) 0 else 1 }
            ExecutionMode.PINNED_GEMMA_3 -> baseProviders.sortedBy { if (it.type == ProviderType.GEMMA_3) 0 else 1 }
            ExecutionMode.PINNED_GROQ -> baseProviders.sortedBy { if (it.type == ProviderType.GROQ) 0 else 1 }
            ExecutionMode.PINNED_OPEN_ROUTER -> baseProviders.sortedBy { if (it.type == ProviderType.OPEN_ROUTER) 0 else 1 }
            ExecutionMode.PINNED_POLLINATIONS -> baseProviders.sortedBy { if (it.type == ProviderType.POLLINATIONS) 0 else 1 }
        }

        val fallbackHistory = mutableListOf<FallbackAttempt>()
        var lastException: Throwable? = null

        for (providerConfig in providers) {
            val providerType = providerConfig.type
            val key = providerConfig.apiKey

            // Skip if key is strictly required but absent
            if (providerType.requiresKey && key.isBlank()) {
                updateProviderStatus(providerType, ProviderHealthStatus.KEY_MISSING, "Chave de API não configurada")
                fallbackHistory.add(
                    FallbackAttempt(
                        provider = providerType,
                        errorReason = "Chave de API não fornecida (pulando para próxima IA)"
                    )
                )
                continue
            }

            var resultText = ""
            var executionTime = 0L

            try {
                executionTime = measureTimeMillis {
                    resultText = aiClient.generateText(
                        provider = providerType,
                        apiKey = key,
                        prompt = prompt,
                        modality = modality,
                        conversationHistory = conversationHistory
                    )
                }

                // Success!
                updateProviderStatus(
                    providerType = providerType,
                    status = ProviderHealthStatus.HEALTHY,
                    message = "Operacional (${executionTime}ms)"
                )
                incrementProviderMetrics(providerType, isSuccess = true)

                return AiGenerationResult(
                    textContent = resultText,
                    imageUrl = null,
                    providerUsed = providerType,
                    modelUsed = providerType.defaultModel,
                    fallbackOccurred = fallbackHistory.isNotEmpty(),
                    fallbackHistory = fallbackHistory,
                    latencyMs = executionTime
                )

            } catch (quotaEx: AiQuotaExceededException) {
                // Rate limit / Quota exceeded (429)!
                updateProviderStatus(
                    providerType = providerType,
                    status = ProviderHealthStatus.QUOTA_EXHAUSTED,
                    message = "Limite de cota excedido (HTTP 429) - Alternando..."
                )
                incrementProviderMetrics(providerType, isSuccess = false)
                fallbackHistory.add(
                    FallbackAttempt(
                        provider = providerType,
                        errorReason = "Cota gratuita esgotada ou limite de taxa atingido (429)"
                    )
                )
                lastException = quotaEx
                // Loop continues seamlessly to NEXT provider!

            } catch (apiEx: AiApiException) {
                updateProviderStatus(
                    providerType = providerType,
                    status = ProviderHealthStatus.ERROR,
                    message = "Erro ${apiEx.statusCode}: ${apiEx.message?.take(40)}"
                )
                incrementProviderMetrics(providerType, isSuccess = false)
                fallbackHistory.add(
                    FallbackAttempt(
                        provider = providerType,
                        errorReason = apiEx.message ?: "Erro na API ${providerType.displayName}"
                    )
                )
                lastException = apiEx
                // Loop continues to NEXT provider!

            } catch (e: Exception) {
                updateProviderStatus(
                    providerType = providerType,
                    status = ProviderHealthStatus.ERROR,
                    message = "Falha: ${e.localizedMessage?.take(40)}"
                )
                incrementProviderMetrics(providerType, isSuccess = false)
                fallbackHistory.add(
                    FallbackAttempt(
                        provider = providerType,
                        errorReason = e.localizedMessage ?: "Erro de conexão"
                    )
                )
                lastException = e
                // Loop continues to NEXT provider!
            }
        }

        // If all providers failed
        val summary = fallbackHistory.joinToString("\n• ") { "${it.provider.displayName}: ${it.errorReason}" }
        throw Exception(
            "Todas as IAs gratuitas configuradas falharam ou atingiram limite de cota:\n• $summary\n\nDetalhes: ${lastException?.message}"
        )
    }

    private fun updateProviderStatus(providerType: ProviderType, status: ProviderHealthStatus, message: String) {
        val currentList = _providersState.value.toMutableList()
        val index = currentList.indexOfFirst { it.type == providerType }
        if (index != -1) {
            val old = currentList[index]
            currentList[index] = old.copy(currentStatus = status, statusMessage = message)
            _providersState.value = currentList
        }
    }

    private fun incrementProviderMetrics(providerType: ProviderType, isSuccess: Boolean) {
        val currentList = _providersState.value.toMutableList()
        val index = currentList.indexOfFirst { it.type == providerType }
        if (index != -1) {
            val old = currentList[index]
            currentList[index] = old.copy(
                successfulRequests = if (isSuccess) old.successfulRequests + 1 else old.successfulRequests,
                failedRequests = if (!isSuccess) old.failedRequests + 1 else old.failedRequests
            )
            _providersState.value = currentList
        }
    }
}
