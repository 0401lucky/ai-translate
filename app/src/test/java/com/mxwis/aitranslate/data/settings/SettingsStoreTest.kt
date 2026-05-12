package com.mxwis.aitranslate.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsStoreTest {
    @Test
    fun `自定义模型编码会清理空值并按大小写去重`() {
        assertEquals(
            "gpt-4o-mini\ndeepseek-chat",
            SettingsStore.encodeCustomModelNames(
                listOf(" gpt-4o-mini ", "", "GPT-4O-MINI", "deepseek-chat"),
            ),
        )
    }

    @Test
    fun `自定义模型解码会清理空行并按大小写去重`() {
        assertEquals(
            listOf("gpt-4o-mini", "deepseek-chat"),
            SettingsStore.decodeCustomModelNames(
                "gpt-4o-mini\n\n GPT-4O-MINI \ndeepseek-chat",
            ),
        )
    }

    @Test
    fun `供应商配置编码和解码会保留模型与接口信息`() {
        val providers = listOf(
            CloudProviderSettings(
                id = "openai",
                name = "OpenAI",
                baseUrl = "https://api.openai.com",
                apiKey = "sk-test",
                modelName = "gpt-4o-mini",
                customModelNames = listOf("gpt-4.1-mini"),
            ),
            CloudProviderSettings(
                id = "deepseek",
                name = "DeepSeek",
                baseUrl = "https://api.deepseek.com",
                modelName = "deepseek-chat",
            ),
        )

        assertEquals(
            providers,
            SettingsStore.decodeCloudProviders(SettingsStore.encodeCloudProviders(providers)),
        )
    }

    @Test
    fun `损坏的供应商配置会安全解码为空列表`() {
        assertEquals(emptyList<CloudProviderSettings>(), SettingsStore.decodeCloudProviders("not-json"))
    }
}
