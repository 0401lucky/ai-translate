package com.mxwis.aitranslate.data.translation

import com.mxwis.aitranslate.domain.Languages
import com.mxwis.aitranslate.domain.TranslateRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineTranslationEngineTest {
    @Test
    fun `离线 prompt 使用 HY-MT 推荐的只输出译文格式`() {
        val prompt = OfflineTranslationEngine.buildHyMtPrompt(
            TranslateRequest(
                sourceText = "Good morning!",
                sourceLanguage = Languages.byCode("en"),
                targetLanguage = Languages.byCode("zh-CN"),
            ),
        )

        assertTrue(prompt.contains("without additional explanation"))
        assertTrue(prompt.contains("Simplified Chinese"))
        assertTrue(prompt.contains("Good morning!"))
    }

    @Test
    fun `清理输出会去掉常见模板标记`() {
        val prompt = "Translate the following segment into Chinese"
        val raw = "$prompt<start_of_turn>assistant\n早上好！<end_of_turn>"

        assertEquals(
            "早上好！",
            OfflineTranslationEngine.cleanOutput(raw, prompt),
        )
    }
}
