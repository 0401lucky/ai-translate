package com.mxwis.aitranslate.data.model

data class MlKitLanguageModelState(
    val languageTag: String,
    val displayName: String,
    val isBuiltIn: Boolean = false,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null,
) {
    val canDownload: Boolean
        get() = !isBuiltIn && !isDownloaded && !isDownloading

    val canDelete: Boolean
        get() = !isBuiltIn && isDownloaded && !isDownloading
}
