package com.mxwis.aitranslate.domain

object ExternalTextInput {
    const val ACTION_PROCESS_TEXT = "android.intent.action.PROCESS_TEXT"
    const val ACTION_SEND = "android.intent.action.SEND"

    fun extractText(
        action: String?,
        mimeType: String?,
        processText: CharSequence?,
        sharedText: CharSequence?,
    ): String? {
        val rawText = when (action) {
            ACTION_PROCESS_TEXT -> processText
            ACTION_SEND -> if (mimeType.isTextMimeType()) sharedText else null
            else -> null
        }

        return rawText
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    fun extractClipboardText(clipboardText: CharSequence?): String? {
        return clipboardText
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun String?.isTextMimeType(): Boolean {
        return this == null || startsWith("text/")
    }
}
