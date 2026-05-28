package com.mxwis.aitranslate.domain

object ClipboardQuickTranslatePolicy {
    fun normalize(text: String): String {
        return text.trim()
    }

    fun shouldOffer(
        normalizedText: String,
        lastPromptText: String?,
        currentSourceText: String,
        isMiniTranslatorOpen: Boolean,
        isClipboardSuggestionOpen: Boolean,
        isImageTranslatorOpen: Boolean = false,
    ): Boolean {
        return normalizedText.isNotBlank() &&
            !isMiniTranslatorOpen &&
            !isClipboardSuggestionOpen &&
            !isImageTranslatorOpen &&
            normalizedText != lastPromptText &&
            normalizedText != currentSourceText.trim()
    }

    fun isFreshClipboard(timestampMillis: Long, nowMillis: Long, maxAgeMillis: Long): Boolean {
        return timestampMillis > 0L &&
            timestampMillis <= nowMillis &&
            nowMillis - timestampMillis <= maxAgeMillis
    }
}
