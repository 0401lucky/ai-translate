package com.mxwis.aitranslate.domain

import java.util.Locale

object SpeechLocaleResolver {
    fun resolve(
        language: LanguageOption,
        text: String,
        defaultLocale: Locale = Locale.getDefault(),
    ): Locale {
        return resolveCandidates(language, text, defaultLocale).first()
    }

    fun resolveCandidates(
        language: LanguageOption,
        text: String,
        defaultLocale: Locale = Locale.getDefault(),
    ): List<Locale> {
        val preferred = if (language.code != Languages.auto.code) {
            resolveKnownLanguage(language.code)
        } else {
            inferFromText(text)
        }
        return listOfNotNull(
            preferred,
            defaultLocale,
            Locale.ENGLISH,
            Locale.SIMPLIFIED_CHINESE,
        ).distinctBy { it.toLanguageTag() }
    }

    private fun resolveKnownLanguage(code: String): Locale? {
        return when (code) {
            "zh-CN" -> Locale.SIMPLIFIED_CHINESE
            "zh-TW" -> Locale.TRADITIONAL_CHINESE
            "en" -> Locale.ENGLISH
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "es" -> Locale.forLanguageTag("es-ES")
            "ru" -> Locale.forLanguageTag("ru-RU")
            "ar" -> Locale.forLanguageTag("ar")
            "it" -> Locale.ITALIAN
            "pt" -> Locale.forLanguageTag("pt-PT")
            else -> null
        }
    }

    private fun inferFromText(text: String): Locale? {
        return when {
            text.any { it.isJapaneseKana() } -> Locale.JAPANESE
            text.any { it.isHangul() } -> Locale.KOREAN
            text.any { it.isArabic() } -> Locale.forLanguageTag("ar")
            text.any { it.isCyrillic() } -> Locale.forLanguageTag("ru-RU")
            text.any { it.isCjkUnifiedIdeograph() } -> Locale.SIMPLIFIED_CHINESE
            text.any { it.isBasicLatinLetter() } -> Locale.ENGLISH
            else -> null
        }
    }

    private fun Char.isJapaneseKana(): Boolean {
        return code in 0x3040..0x30FF || code in 0x31F0..0x31FF
    }

    private fun Char.isHangul(): Boolean {
        return code in 0xAC00..0xD7AF || code in 0x1100..0x11FF
    }

    private fun Char.isArabic(): Boolean {
        return code in 0x0600..0x06FF || code in 0x0750..0x077F
    }

    private fun Char.isCyrillic(): Boolean {
        return code in 0x0400..0x04FF
    }

    private fun Char.isCjkUnifiedIdeograph(): Boolean {
        return code in 0x4E00..0x9FFF
    }

    private fun Char.isBasicLatinLetter(): Boolean {
        return this in 'A'..'Z' || this in 'a'..'z'
    }
}
