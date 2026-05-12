package com.mxwis.aitranslate.domain

data class LanguageOption(
    val code: String,
    val displayName: String,
    val promptName: String,
)

object Languages {
    val auto = LanguageOption("auto", "自动检测", "auto-detect")

    val supported = listOf(
        LanguageOption("zh-CN", "中文（简体）", "Simplified Chinese"),
        LanguageOption("zh-TW", "中文（繁体）", "Traditional Chinese"),
        LanguageOption("en", "英文（English）", "English"),
        LanguageOption("ja", "日文（日本語）", "Japanese"),
        LanguageOption("ko", "韩文（한국어）", "Korean"),
        LanguageOption("fr", "法文（Français）", "French"),
        LanguageOption("de", "德文（Deutsch）", "German"),
        LanguageOption("es", "西班牙文（Español）", "Spanish"),
        LanguageOption("ru", "俄文（Русский）", "Russian"),
        LanguageOption("ar", "阿拉伯文（العربية）", "Arabic"),
        LanguageOption("it", "意大利文（Italiano）", "Italian"),
        LanguageOption("pt", "葡萄牙文（Português）", "Portuguese"),
    )

    fun byCode(code: String): LanguageOption {
        if (code == auto.code) return auto
        return supported.firstOrNull { it.code == code } ?: supported.first()
    }
}
