package com.mxwis.aitranslate.data.translation

import org.junit.Assert.assertEquals
import org.junit.Test

class CloudTranslationEngineTest {
    @Test
    fun `根地址会补全 chat completions 路径`() {
        assertEquals(
            "https://api.example.com/v1/chat/completions",
            CloudTranslationEngine.resolveEndpoint("https://api.example.com"),
        )
    }

    @Test
    fun `v1 地址只补全末尾路径`() {
        assertEquals(
            "https://api.example.com/v1/chat/completions",
            CloudTranslationEngine.resolveEndpoint("https://api.example.com/v1/"),
        )
    }

    @Test
    fun `完整地址保持不变`() {
        assertEquals(
            "https://api.example.com/v1/chat/completions",
            CloudTranslationEngine.resolveEndpoint("https://api.example.com/v1/chat/completions"),
        )
    }

    @Test
    fun `根地址会补全 models 路径`() {
        assertEquals(
            "https://api.example.com/v1/models",
            CloudTranslationEngine.resolveModelsEndpoint("https://api.example.com"),
        )
    }

    @Test
    fun `chat completions 地址会替换为 models 路径`() {
        assertEquals(
            "https://api.example.com/v1/models",
            CloudTranslationEngine.resolveModelsEndpoint("https://api.example.com/v1/chat/completions"),
        )
    }

    @Test
    fun `模型列表会解析 id 并去重`() {
        val response = """
            {
              "object": "list",
              "data": [
                { "id": "gpt-4o-mini" },
                { "id": "gpt-4.1-mini" },
                { "id": "gpt-4o-mini" },
                { "name": "invalid" }
              ]
            }
        """.trimIndent()

        assertEquals(
            listOf("gpt-4o-mini", "gpt-4.1-mini"),
            CloudTranslationEngine.parseModelIds(response),
        )
    }
}
