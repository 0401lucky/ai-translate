package com.mxwis.aitranslate.data.translation

import com.mxwis.aitranslate.data.settings.AppSettings
import com.mxwis.aitranslate.domain.TranslateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class CloudTranslationEngine(
    private val client: OkHttpClient,
) {
    suspend fun fetchModels(settings: AppSettings): List<String> = withContext(Dispatchers.IO) {
        require(settings.apiKey.isNotBlank()) { "请先填写 API Key" }
        require(settings.baseUrl.isNotBlank()) { "请先填写接口地址" }

        val httpRequest = Request.Builder()
            .url(resolveModelsEndpoint(settings.baseUrl))
            .header("Authorization", "Bearer ${settings.apiKey}")
            .get()
            .build()

        client.newCall(httpRequest).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("获取模型失败：HTTP ${response.code} ${responseText.take(160)}")
            }

            val models = parseModelIds(responseText)
            require(models.isNotEmpty()) { "接口返回的模型列表为空" }
            models
        }
    }

    suspend fun translate(request: TranslateRequest, settings: AppSettings): String = withContext(Dispatchers.IO) {
        require(settings.apiKey.isNotBlank()) { "请先在设置中填写 API Key" }
        require(settings.baseUrl.isNotBlank()) { "请先在设置中填写接口地址" }
        require(settings.modelName.isNotBlank()) { "请先在设置中填写模型名称" }

        val endpoint = resolveEndpoint(settings.baseUrl)
        val bodyJson = JSONObject()
            .put("model", settings.modelName)
            .put("temperature", 0.1)
            .put(
                "messages",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put("content", "你是专业翻译引擎。只输出译文，不解释，不添加注释。"),
                    )
                    .put(
                        JSONObject()
                            .put("role", "user")
                            .put("content", buildPrompt(request)),
                    ),
            )

        val httpRequest = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer ${settings.apiKey}")
            .header("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(httpRequest).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("云端翻译失败：HTTP ${response.code} ${responseText.take(160)}")
            }
            val content = JSONObject(responseText)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
            require(content.isNotBlank()) { "云端翻译结果为空" }
            content
        }
    }

    private fun buildPrompt(request: TranslateRequest): String {
        val source = if (request.sourceLanguage.code == "auto") {
            "auto-detected language"
        } else {
            request.sourceLanguage.promptName
        }
        return """
            Translate the following segment from $source into ${request.targetLanguage.promptName}.
            Output only the translation.

            ${request.sourceText}
        """.trimIndent()
    }

    companion object {
        internal fun resolveEndpoint(baseUrl: String): String {
            val trimmed = baseUrl.trim().trimEnd('/')
            return when {
                trimmed.endsWith("/chat/completions") -> trimmed
                trimmed.endsWith("/v1") -> "$trimmed/chat/completions"
                else -> "$trimmed/v1/chat/completions"
            }
        }

        internal fun resolveModelsEndpoint(baseUrl: String): String {
            val trimmed = baseUrl.trim().trimEnd('/')
            return when {
                trimmed.endsWith("/models") -> trimmed
                trimmed.endsWith("/chat/completions") -> "${trimmed.removeSuffix("/chat/completions")}/models"
                trimmed.endsWith("/v1") -> "$trimmed/models"
                else -> "$trimmed/v1/models"
            }
        }

        internal fun parseModelIds(responseText: String): List<String> {
            val data = JSONObject(responseText).optJSONArray("data") ?: JSONArray()
            return buildList {
                for (index in 0 until data.length()) {
                    val id = data.optJSONObject(index)
                        ?.optString("id")
                        .orEmpty()
                        .trim()
                    if (id.isNotBlank()) add(id)
                }
            }.distinct()
        }
    }
}
