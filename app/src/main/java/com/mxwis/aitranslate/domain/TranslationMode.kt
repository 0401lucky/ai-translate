package com.mxwis.aitranslate.domain

enum class TranslationMode(val label: String) {
    CLOUD("云端"),
    OFFLINE("离线"),
    AUTO("自动"),
}

data class TranslateRequest(
    val sourceText: String,
    val sourceLanguage: LanguageOption,
    val targetLanguage: LanguageOption,
)

data class TranslateOutput(
    val translatedText: String,
    val usedMode: TranslationMode,
)
