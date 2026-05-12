package com.mxwis.aitranslate.data.settings

import com.mxwis.aitranslate.domain.TranslationMode

const val DEFAULT_PROVIDER_ID = "openai"
const val DEFAULT_BASE_URL = "https://api.openai.com"
const val DEFAULT_MODEL_NAME = "gpt-4o-mini"

data class CloudProviderSettings(
    val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String = "",
    val modelName: String = DEFAULT_MODEL_NAME,
    val customModelNames: List<String> = emptyList(),
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && apiKey.isNotBlank()
}

data class AppSettings(
    val baseUrl: String = DEFAULT_BASE_URL,
    val apiKey: String = "",
    val modelName: String = DEFAULT_MODEL_NAME,
    val customModelNames: List<String> = emptyList(),
    val cloudProviders: List<CloudProviderSettings> = defaultCloudProviders(),
    val selectedProviderId: String = DEFAULT_PROVIDER_ID,
    val defaultMode: TranslationMode = TranslationMode.CLOUD,
) {
    val selectedProvider: CloudProviderSettings
        get() = cloudProviders.firstOrNull { it.id == selectedProviderId }
            ?: cloudProviders.firstOrNull()
            ?: defaultCloudProviders().first()

    val configuredProviderCount: Int
        get() = cloudProviders.count { it.isConfigured }

    fun withSelectedProvider(provider: CloudProviderSettings): AppSettings {
        val nextProviders = cloudProviders
            .filterNot { it.id == provider.id }
            .plus(provider)
            .sortedWith(compareBy<CloudProviderSettings> { providerOrder(it.id) }.thenBy { it.name })

        return copy(
            baseUrl = provider.baseUrl,
            apiKey = provider.apiKey,
            modelName = provider.modelName,
            customModelNames = provider.customModelNames,
            cloudProviders = nextProviders,
            selectedProviderId = provider.id,
        )
    }
}

fun defaultCloudProviders(
    openAiBaseUrl: String = DEFAULT_BASE_URL,
    openAiApiKey: String = "",
    openAiModelName: String = DEFAULT_MODEL_NAME,
    openAiCustomModelNames: List<String> = emptyList(),
): List<CloudProviderSettings> {
    return listOf(
        CloudProviderSettings(
            id = DEFAULT_PROVIDER_ID,
            name = "OpenAI",
            baseUrl = openAiBaseUrl,
            apiKey = openAiApiKey,
            modelName = openAiModelName,
            customModelNames = openAiCustomModelNames,
        ),
        CloudProviderSettings(
            id = "deepseek",
            name = "DeepSeek",
            baseUrl = "https://api.deepseek.com",
            modelName = "deepseek-chat",
        ),
        CloudProviderSettings(
            id = "openrouter",
            name = "OpenRouter",
            baseUrl = "https://openrouter.ai/api/v1",
            modelName = "openai/gpt-4o-mini",
        ),
        CloudProviderSettings(
            id = "custom-compatible",
            name = "自定义兼容接口",
            baseUrl = "",
            modelName = DEFAULT_MODEL_NAME,
        ),
    )
}

private fun providerOrder(id: String): Int {
    return when (id) {
        DEFAULT_PROVIDER_ID -> 0
        "deepseek" -> 1
        "openrouter" -> 2
        "custom-compatible" -> 3
        else -> 10
    }
}
