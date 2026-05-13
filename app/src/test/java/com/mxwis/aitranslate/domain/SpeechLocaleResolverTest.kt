package com.mxwis.aitranslate.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class SpeechLocaleResolverTest {
    @Test
    fun `具体语言会直接映射到朗读 Locale`() {
        assertEquals(
            Locale.JAPANESE,
            SpeechLocaleResolver.resolve(
                language = Languages.byCode("ja"),
                text = "こんにちは",
                defaultLocale = Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `自动检测会优先识别日文假名`() {
        assertEquals(
            Locale.JAPANESE,
            SpeechLocaleResolver.resolve(
                language = Languages.auto,
                text = "今日はいい天気です",
                defaultLocale = Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `自动检测会识别中文字符`() {
        assertEquals(
            Locale.SIMPLIFIED_CHINESE,
            SpeechLocaleResolver.resolve(
                language = Languages.auto,
                text = "你好，世界",
                defaultLocale = Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `无法识别时回退设备默认语言`() {
        assertEquals(
            Locale.GERMAN,
            SpeechLocaleResolver.resolve(
                language = Languages.auto,
                text = "12345",
                defaultLocale = Locale.GERMAN,
            ),
        )
    }

    @Test
    fun `候选 Locale 会追加默认语言英文和简体中文`() {
        assertEquals(
            listOf(Locale.JAPANESE, Locale.GERMAN, Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE),
            SpeechLocaleResolver.resolveCandidates(
                language = Languages.byCode("ja"),
                text = "こんにちは",
                defaultLocale = Locale.GERMAN,
            ),
        )
    }

    @Test
    fun `候选 Locale 会去重`() {
        assertEquals(
            listOf(Locale.SIMPLIFIED_CHINESE, Locale.ENGLISH),
            SpeechLocaleResolver.resolveCandidates(
                language = Languages.auto,
                text = "你好",
                defaultLocale = Locale.SIMPLIFIED_CHINESE,
            ),
        )
    }
}
