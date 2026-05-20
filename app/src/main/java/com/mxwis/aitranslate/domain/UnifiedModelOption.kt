package com.mxwis.aitranslate.domain

enum class ModelType(val label: String) {
    CLOUD("云端"),
    OFFLINE("离线"),
    AUTO("自动"),
}

enum class OfflineModelType(
    val id: String,
    val displayName: String,
    val providerName: String,
    val defaultSubtitle: String,
) {
    HY_MT(
        id = "hymt",
        displayName = "HY-MT 1.5B",
        providerName = "本地推理",
        defaultSubtitle = "本地大模型 · Cloudflare R2 分片下载",
    ),
    ML_KIT(
        id = "mlkit",
        displayName = "Google ML Kit",
        providerName = "Google ML Kit",
        defaultSubtitle = "设备端离线 · 免费 · 无需 API Key",
    );

    companion object {
        fun fromId(value: String?): OfflineModelType {
            return entries.firstOrNull { it.id == value } ?: HY_MT
        }
    }
}

data class UnifiedModelOption(
    val id: String,
    val displayName: String,
    val type: ModelType,
    val providerName: String,
    val isAvailable: Boolean = true,
    val subtitle: String = "",
    val offlineModelType: OfflineModelType? = null,
)
