package com.mxwis.aitranslate.data.translation

import com.mxwis.aitranslate.domain.Languages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MlKitTranslationEngineTest {
    @Test
    fun `语言选项会映射为 ML Kit 语言标签`() {
        assertEquals("en", MlKitTranslationEngine.toMlKitLanguageTag(Languages.byCode("en")))
        assertEquals("ja", MlKitTranslationEngine.toMlKitLanguageTag(Languages.byCode("ja")))
        assertEquals("ko", MlKitTranslationEngine.toMlKitLanguageTag(Languages.byCode("ko")))
        assertEquals("zh", MlKitTranslationEngine.toMlKitLanguageTag(Languages.byCode("zh-CN")))
        assertEquals("zh", MlKitTranslationEngine.toMlKitLanguageTag(Languages.byCode("zh-TW")))
    }

    @Test
    fun `自动语言不会直接作为 ML Kit 翻译语言`() {
        assertNull(MlKitTranslationEngine.toMlKitLanguageTag(Languages.auto))
    }
}
