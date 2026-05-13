package com.mxwis.aitranslate.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardQuickTranslatePolicyTest {
    @Test
    fun `剪贴板文本会先清理首尾空白`() {
        assertTrue(ClipboardQuickTranslatePolicy.normalize("  hello  ") == "hello")
    }

    @Test
    fun `新剪贴板文本且当前无弹窗时会提示`() {
        assertTrue(
            ClipboardQuickTranslatePolicy.shouldOffer(
                normalizedText = "hello",
                lastPromptText = null,
                currentSourceText = "",
                isMiniTranslatorOpen = false,
                isClipboardSuggestionOpen = false,
            ),
        )
    }

    @Test
    fun `重复剪贴板文本不会重复打扰`() {
        assertFalse(
            ClipboardQuickTranslatePolicy.shouldOffer(
                normalizedText = "hello",
                lastPromptText = "hello",
                currentSourceText = "",
                isMiniTranslatorOpen = false,
                isClipboardSuggestionOpen = false,
            ),
        )
    }

    @Test
    fun `已在主输入框中的文本不会弹出提示`() {
        assertFalse(
            ClipboardQuickTranslatePolicy.shouldOffer(
                normalizedText = "hello",
                lastPromptText = null,
                currentSourceText = " hello ",
                isMiniTranslatorOpen = false,
                isClipboardSuggestionOpen = false,
            ),
        )
    }
}
