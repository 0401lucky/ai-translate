package com.mxwis.aitranslate.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExternalTextInputTest {
    @Test
    fun `系统划词会读取并清理选中文本`() {
        assertEquals(
            "hello world",
            ExternalTextInput.extractText(
                action = ExternalTextInput.ACTION_PROCESS_TEXT,
                mimeType = "text/plain",
                processText = "  hello world  ",
                sharedText = null,
            ),
        )
    }

    @Test
    fun `文本分享会读取 shared text`() {
        assertEquals(
            "share me",
            ExternalTextInput.extractText(
                action = ExternalTextInput.ACTION_SEND,
                mimeType = "text/plain",
                processText = null,
                sharedText = " share me ",
            ),
        )
    }

    @Test
    fun `非文本分享不会被接收`() {
        assertNull(
            ExternalTextInput.extractText(
                action = ExternalTextInput.ACTION_SEND,
                mimeType = "image/png",
                processText = null,
                sharedText = "not text",
            ),
        )
    }

    @Test
    fun `空白文本不会被接收`() {
        assertNull(
            ExternalTextInput.extractText(
                action = ExternalTextInput.ACTION_PROCESS_TEXT,
                mimeType = "text/plain",
                processText = "   ",
                sharedText = null,
            ),
        )
    }

    @Test
    fun `剪贴板文本会被清理空白`() {
        assertEquals(
            "复制的文本",
            ExternalTextInput.extractClipboardText("  复制的文本  "),
        )
    }

    @Test
    fun `空白剪贴板文本不会被接收`() {
        assertNull(ExternalTextInput.extractClipboardText("   "))
    }
}
