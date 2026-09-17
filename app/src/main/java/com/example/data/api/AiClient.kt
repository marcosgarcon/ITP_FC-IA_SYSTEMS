package com.example.data.api

import android.util.Log
import com.example.data.model.AiModality
import com.example.data.model.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class AiQuotaExceededException(val provider: ProviderType, message: String) : Exception(message)
class AiApiException(val provider: ProviderType, val statusCode: Int, message: String) : Exception(message)

class AiClient {
    private val tag = "AiClient"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateText(
        provider: ProviderType,
        apiKey: String,
        prompt: String,
        modality: AiModality,
        conversationHistory: List<Pair<String, String>> = emptyList() // List of role to content
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = getSystemPromptForModality(modality)

        when (provider) {
            ProviderType.GEMINI -> callGemini(apiKey, prompt, systemPrompt, conversationHistory)
            ProviderType.GEMMA_3 -> callGemma3(apiKey, prompt, systemPrompt, conversationHistory)
            ProviderType.GROQ -> callGroq(apiKey, prompt, systemPrompt, conversationHistory)
            ProviderType.OPEN_ROUTER -> callOpenRouter(apiKey, prompt, systemPrompt, conversationHistory)
            ProviderType.POLLINATIONS -> callPollinationsText(prompt, systemPrompt, modality)
        }
    }

    fun buildImageUrl(prompt: String, style: String? = null): String {
        val enhancedPrompt = if (style.isNullOrBlank() || style == "Padrão") {
            prompt
        } else {
            "$prompt, in $style style, high quality, 4k, detailed"
        }
        val encodedPrompt = URLEncoder.encode(enhancedPrompt, "UTF-8")
        val seed = (100000..999999).random()
        return "https://image.pollinations.ai/prompt/$encodedPrompt?width=1024&height=1024&nologo=true&seed=$seed"
    }

    private fun callGemini(
        apiKey: String,
        prompt: String,
        systemPrompt: String,
        history: List<Pair<String, String>>
    ): String {
        if (apiKey.isBlank()) {
            throw AiApiException(ProviderType.GEMINI, 401, "Chave de API Gemini não configurada.")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        val contentsArray = JSONArray()

        // Append past history turns (up to last 6 for context)
        val recentHistory = history.takeLast(6)
        for ((role, text) in recentHistory) {
            val contentObj = JSONObject()
            contentObj.put("role", if (role == "assistant") "model" else "user")
            val partsArr = JSONArray()
            partsArr.put(JSONObject().put("text", text))
            contentObj.put("parts", partsArr)
            contentsArray.put(contentObj)
        }

        // Current prompt
        val currentContent = JSONObject()
        currentContent.put("role", "user")
        val curParts = JSONArray()
        curParts.put(JSONObject().put("text", prompt))
        currentContent.put("parts", curParts)
        contentsArray.put(currentContent)

        rootJson.put("contents", contentsArray)

        // System Instruction
        val sysInstructionObj = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", systemPrompt))
        sysInstructionObj.put("parts", sysParts)
        rootJson.put("systemInstruction", sysInstructionObj)

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val code = response.code
            val responseBody = response.body?.string().orEmpty()

            if (code == 429) {
                throw AiQuotaExceededException(ProviderType.GEMINI, "Gemini atingiu limite de requisições / cota excedida (429).")
            }

            if (!response.isSuccessful) {
                if (responseBody.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                    responseBody.contains("quota", ignoreCase = true)
                ) {
                    throw AiQuotaExceededException(ProviderType.GEMINI, "Gemini cota esgotada.")
                }
                throw AiApiException(ProviderType.GEMINI, code, "Erro Gemini ($code): $responseBody")
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "Sem texto gerado.")
                }
            }
            return "Resposta vazia retornada pelo Gemini."
        }
    }

    private fun callGemma3(
        apiKey: String,
        prompt: String,
        systemPrompt: String,
        history: List<Pair<String, String>>
    ): String {
        // Gemma 3 is accessible via Google Generative Language API (gemma-3-27b-it)
        // Can use either the user's Gemma/Gemini key or fallback to default
        if (apiKey.isBlank()) {
            throw AiApiException(ProviderType.GEMMA_3, 401, "Chave de API do Google necessária para acessar o Gemma 3.")
        }

        // Try primary Gemma 3 27B model; if unavailable, model ID gemma-3-12b-it or gemma-3-4b-it
        val modelName = "gemma-3-27b-it"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        val contentsArray = JSONArray()

        // Append recent history turns
        val recentHistory = history.takeLast(6)
        for ((role, text) in recentHistory) {
            val contentObj = JSONObject()
            contentObj.put("role", if (role == "assistant") "model" else "user")
            val partsArr = JSONArray()
            partsArr.put(JSONObject().put("text", text))
            contentObj.put("parts", partsArr)
            contentsArray.put(contentObj)
        }

        // Current prompt with system instructions prepended if needed or structured
        val currentContent = JSONObject()
        currentContent.put("role", "user")
        val curParts = JSONArray()
        curParts.put(JSONObject().put("text", prompt))
        currentContent.put("parts", curParts)
        contentsArray.put(currentContent)

        rootJson.put("contents", contentsArray)

        // System Instruction for Gemma
        val sysInstructionObj = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", systemPrompt))
        sysInstructionObj.put("parts", sysParts)
        rootJson.put("systemInstruction", sysInstructionObj)

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val code = response.code
            val responseBody = response.body?.string().orEmpty()

            if (code == 429) {
                throw AiQuotaExceededException(ProviderType.GEMMA_3, "Gemma 3 atingiu limite de requisições / cota excedida (429).")
            }

            if (!response.isSuccessful) {
                if (responseBody.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                    responseBody.contains("quota", ignoreCase = true)
                ) {
                    throw AiQuotaExceededException(ProviderType.GEMMA_3, "Gemma 3 cota gratuita esgotada.")
                }
                throw AiApiException(ProviderType.GEMMA_3, code, "Erro Google Gemma 3 ($code): $responseBody")
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "Sem texto gerado.")
                }
            }
            return "Resposta vazia retornada pelo Google Gemma 3."
        }
    }

    private fun callGroq(
        apiKey: String,
        prompt: String,
        systemPrompt: String,
        history: List<Pair<String, String>>
    ): String {
        if (apiKey.isBlank()) {
            throw AiApiException(ProviderType.GROQ, 401, "Chave da Groq não configurada.")
        }

        val url = "https://api.groq.com/openai/v1/chat/completions"

        val rootJson = JSONObject()
        rootJson.put("model", "llama-3.3-70b-versatile")

        val messagesArr = JSONArray()
        messagesArr.put(JSONObject().put("role", "system").put("content", systemPrompt))

        val recentHistory = history.takeLast(6)
        for ((role, text) in recentHistory) {
            messagesArr.put(JSONObject().put("role", if (role == "assistant") "assistant" else "user").put("content", text))
        }
        messagesArr.put(JSONObject().put("role", "user").put("content", prompt))

        rootJson.put("messages", messagesArr)
        rootJson.put("temperature", 0.7)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val code = response.code
            val responseBody = response.body?.string().orEmpty()

            if (code == 429) {
                throw AiQuotaExceededException(ProviderType.GROQ, "Groq limite de taxa atingido (429).")
            }

            if (!response.isSuccessful) {
                if (code == 402 || responseBody.contains("quota", ignoreCase = true) || responseBody.contains("rate_limit", ignoreCase = true)) {
                    throw AiQuotaExceededException(ProviderType.GROQ, "Groq cota excedida.")
                }
                throw AiApiException(ProviderType.GROQ, code, "Erro Groq ($code): $responseBody")
            }

            val json = JSONObject(responseBody)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val choice = choices.getJSONObject(0)
                val msg = choice.optJSONObject("message")
                return msg?.optString("content", "") ?: "Sem conteúdo na resposta."
            }
            return "Resposta vazia da Groq."
        }
    }

    private fun callOpenRouter(
        apiKey: String,
        prompt: String,
        systemPrompt: String,
        history: List<Pair<String, String>>
    ): String {
        if (apiKey.isBlank()) {
            throw AiApiException(ProviderType.OPEN_ROUTER, 401, "Chave da OpenRouter não configurada.")
        }

        val url = "https://openrouter.ai/api/v1/chat/completions"

        val rootJson = JSONObject()
        rootJson.put("model", "meta-llama/llama-3.3-70b-instruct:free")

        val messagesArr = JSONArray()
        messagesArr.put(JSONObject().put("role", "system").put("content", systemPrompt))

        val recentHistory = history.takeLast(6)
        for ((role, text) in recentHistory) {
            messagesArr.put(JSONObject().put("role", if (role == "assistant") "assistant" else "user").put("content", text))
        }
        messagesArr.put(JSONObject().put("role", "user").put("content", prompt))

        rootJson.put("messages", messagesArr)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("HTTP-Referer", "https://ai.studio")
            .addHeader("X-Title", "AI Multi-Hub")
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val code = response.code
            val responseBody = response.body?.string().orEmpty()

            if (code == 429) {
                throw AiQuotaExceededException(ProviderType.OPEN_ROUTER, "OpenRouter limite de taxa gratuito atingido (429).")
            }

            if (!response.isSuccessful) {
                if (code == 402 || responseBody.contains("credits", ignoreCase = true) || responseBody.contains("quota", ignoreCase = true)) {
                    throw AiQuotaExceededException(ProviderType.OPEN_ROUTER, "OpenRouter sem créditos disponíveis.")
                }
                throw AiApiException(ProviderType.OPEN_ROUTER, code, "Erro OpenRouter ($code): $responseBody")
            }

            val json = JSONObject(responseBody)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val choice = choices.getJSONObject(0)
                val msg = choice.optJSONObject("message")
                return msg?.optString("content", "") ?: "Sem conteúdo."
            }
            return "Resposta vazia da OpenRouter."
        }
    }

    private fun callPollinationsText(
        prompt: String,
        systemPrompt: String,
        modality: AiModality
    ): String {
        // Pollinations.ai provides 100% free, unlimited AI text generation without any key
        val url = "https://text.pollinations.ai/"

        val rootJson = JSONObject()
        val messagesArr = JSONArray()
        messagesArr.put(JSONObject().put("role", "system").put("content", systemPrompt))
        messagesArr.put(JSONObject().put("role", "user").put("content", prompt))

        rootJson.put("messages", messagesArr)
        rootJson.put("model", if (modality == AiModality.CODE) "qwen-coder" else "openai")
        rootJson.put("seed", (1000..9999).random())

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val code = response.code
            val bodyStr = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                if (code == 429) {
                    throw AiQuotaExceededException(ProviderType.POLLINATIONS, "Pollinations temporariamente sobrecarregado (429).")
                }
                throw AiApiException(ProviderType.POLLINATIONS, code, "Erro Pollinations ($code): $bodyStr")
            }

            // Pollinations returns pure text body directly
            if (bodyStr.isNotBlank()) {
                return bodyStr
            }
            return "Resposta gerada com sucesso via Pollinations AI."
        }
    }

    private fun getSystemPromptForModality(modality: AiModality): String {
        return when (modality) {
            AiModality.CHAT ->
                "Você é um assistente de inteligência artificial de elite, prestativo, rápido e informativo. Responda em português com clareza, formatação Markdown e tom amigável."

            AiModality.CODE ->
                "Você é um engenheiro de software sênior. Forneça soluções de código de alta qualidade, limpas, completas e funcionais. SEMPRE envolva blocos de código com a sintaxe correta (ex: ```kotlin, ```python, ```javascript, ```sql) e inclua comentários explicativos onde relevante."

            AiModality.SHEET ->
                "Você é um especialista sênior em planilhas, análise de dados e relatórios do Excel e Google Sheets. Estruture dados primariamente como tabelas Markdown legíveis com cabeçalhos e linhas bem formatadas (| Coluna 1 | Coluna 2 |). Sempre que solicitado fórmulas ou cálculos, forneça as fórmulas exatas (ex: SOMA, PROCV, ÍNDICE/CORRESP, SOMASE) e explique seu uso."

            AiModality.IMAGE ->
                "Você é um diretor de arte e engenheiro de prompts para inteligência artificial generativa. Descreva cenas visuais em alta resolução, iluminação cinematográfica, atmosfera e detalhes visuais envolventes."
        }
    }
}
