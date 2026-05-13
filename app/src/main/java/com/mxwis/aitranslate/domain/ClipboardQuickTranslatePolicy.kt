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
    ): Boolean {
        return normalizedText.isNotBlank() &&
            !isMiniTranslatorOpen &&
            !isClipboardSuggestionOpen &&
            normalizedText != lastPromptText &&
            normalizedText != currentSourceText.trim()
    }
}
