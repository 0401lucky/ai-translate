package com.mxwis.aitranslate.data.translation

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.mxwis.aitranslate.domain.LanguageOption
import com.mxwis.aitranslate.domain.TranslateRequest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class MlKitTranslationEngine {
    suspend fun translate(request: TranslateRequest): String = withContext(Dispatchers.IO) {
        require(request.sourceText.isNotBlank()) { "请输入要翻译的文本" }
        val targetLanguage = requireSupportedLanguage(request.targetLanguage, role = "目标语言")
        val sourceLanguage = resolveSourceLanguage(request)

        if (sourceLanguage == targetLanguage) {
            return@withContext request.sourceText.trim()
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()
        val translator = Translation.getClient(options)

        try {
            // ML Kit 官方模型由 SDK 内部下载和缓存，不能像 HY-MT 一样切换到 R2 分发。
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
            translator.translate(request.sourceText).await().trim().also {
                require(it.isNotBlank()) { "ML Kit 离线翻译结果为空" }
            }
        } catch (error: Throwable) {
            throw IllegalStateException("ML Kit 离线翻译失败：${error.message ?: error.javaClass.simpleName}", error)
        } finally {
            translator.close()
        }
    }

    private suspend fun resolveSourceLanguage(request: TranslateRequest): String {
        return if (request.sourceLanguage.code == "auto") {
            detectSourceLanguage(request.sourceText)
        } else {
            requireSupportedLanguage(request.sourceLanguage, role = "源语言")
        }
    }

    private suspend fun detectSourceLanguage(text: String): String {
        val languageIdentifier = LanguageIdentification.getClient()
        return try {
            val detected = languageIdentifier.identifyLanguage(text).await()
            if (detected == "und") {
                error("ML Kit 无法识别源语言，请手动选择源语言后重试")
            }
            requireSupportedLanguageTag(detected, role = "识别到的源语言")
        } finally {
            languageIdentifier.close()
        }
    }

    companion object {
        internal fun requireSupportedLanguage(
            language: LanguageOption,
            role: String,
        ): String {
            return requireSupportedLanguageTag(toMlKitLanguageTag(language), role)
        }

        internal fun requireSupportedLanguageTag(
            languageTag: String?,
            role: String,
        ): String {
            val normalized = normalizeLanguageTag(languageTag)
                ?: error("$role 暂不支持 ML Kit 离线翻译")
            return TranslateLanguage.fromLanguageTag(normalized)
                ?: error("$role 暂不支持 ML Kit 离线翻译：$languageTag")
        }

        internal fun toMlKitLanguageTag(language: LanguageOption): String? {
            return normalizeLanguageTag(language.code)
        }

        internal fun normalizeLanguageTag(languageTag: String?): String? {
            val normalized = languageTag
                ?.trim()
                ?.replace("_", "-")
                ?.lowercase()
                ?: return null

            return when {
                normalized.isBlank() || normalized == "auto" -> null
                normalized == "zh" || normalized.startsWith("zh-") -> "zh"
                else -> normalized.substringBefore("-")
            }
        }
    }
}

private suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { error -> continuation.resumeWithException(error) }
        addOnCanceledListener { continuation.cancel() }
    }
}
