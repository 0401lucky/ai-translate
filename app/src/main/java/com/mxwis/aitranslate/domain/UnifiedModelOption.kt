package com.mxwis.aitranslate.domain

enum class ModelType(val label: String) {
    CLOUD("云端"),
    OFFLINE("离线"),
    AUTO("自动"),
}

data class UnifiedModelOption(
    val id: String,
    val displayName: String,
    val type: ModelType,
    val providerName: String,
    val isAvailable: Boolean = true,
    val subtitle: String = "",
)
