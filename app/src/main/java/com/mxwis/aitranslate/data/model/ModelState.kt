package com.mxwis.aitranslate.data.model

data class ModelState(
    val isAvailable: Boolean = false,
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val filePath: String = "",
    val errorMessage: String? = null,
)
