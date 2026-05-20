package com.mxwis.aitranslate.data.model

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.mxwis.aitranslate.data.translation.MlKitTranslationEngine
import com.mxwis.aitranslate.domain.Languages
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class MlKitLanguageModelManager(
    private val remoteModelManager: RemoteModelManager = RemoteModelManager.getInstance(),
) {
    private val descriptors = buildDescriptors()
    private val downloadingTags = mutableSetOf<String>()
    private val errorsByTag = mutableMapOf<String, String>()

    private val _state = MutableStateFlow(
        descriptors.map { it.toState(isDownloaded = it.isBuiltIn) },
    )
    val state: StateFlow<List<MlKitLanguageModelState>> = _state.asStateFlow()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        runCatching { readDownloadedTags() }
            .onSuccess { downloadedTags -> publish(downloadedTags) }
            .onFailure { error ->
                _state.value = _state.value.map {
                    it.copy(errorMessage = error.message ?: "读取语种包状态失败")
                }
            }
    }

    suspend fun downloadLanguage(languageTag: String) = withContext(Dispatchers.IO) {
        val descriptor = requireRemoteDescriptor(languageTag)
        if (!downloadingTags.add(descriptor.languageTag)) return@withContext
        errorsByTag.remove(descriptor.languageTag)
        publish(readDownloadedTags())

        try {
            remoteModelManager
                .download(descriptor.toRemoteModel(), DownloadConditions.Builder().build())
                .await()
            errorsByTag.remove(descriptor.languageTag)
        } catch (error: Throwable) {
            errorsByTag[descriptor.languageTag] = error.message ?: "语种包下载失败"
        } finally {
            downloadingTags.remove(descriptor.languageTag)
            publish(readDownloadedTags())
        }
    }

    suspend fun deleteLanguage(languageTag: String) = withContext(Dispatchers.IO) {
        val descriptor = requireRemoteDescriptor(languageTag)
        errorsByTag.remove(descriptor.languageTag)

        try {
            remoteModelManager.deleteDownloadedModel(descriptor.toRemoteModel()).await()
        } catch (error: Throwable) {
            errorsByTag[descriptor.languageTag] = error.message ?: "语种包删除失败"
        } finally {
            publish(readDownloadedTags())
        }
    }

    private suspend fun readDownloadedTags(): Set<String> {
        return remoteModelManager
            .getDownloadedModels(TranslateRemoteModel::class.java)
            .await()
            .map { it.language }
            .toSet()
    }

    private fun publish(downloadedTags: Set<String>) {
        _state.value = descriptors.map { descriptor ->
            descriptor.toState(
                isDownloaded = descriptor.isBuiltIn || descriptor.languageTag in downloadedTags,
                isDownloading = descriptor.languageTag in downloadingTags,
                errorMessage = errorsByTag[descriptor.languageTag],
            )
        }
    }

    private fun requireRemoteDescriptor(languageTag: String): MlKitLanguageDescriptor {
        val descriptor = descriptors.firstOrNull { it.languageTag == languageTag }
            ?: error("暂不支持该 ML Kit 语种包：$languageTag")
        require(!descriptor.isBuiltIn) { "英文为 ML Kit 内置语种，无需下载或删除" }
        return descriptor
    }

    companion object {
        internal fun buildDescriptors(): List<MlKitLanguageDescriptor> {
            return Languages.supported
                .mapNotNull { language ->
                    val tag = MlKitTranslationEngine.toMlKitLanguageTag(language)
                        ?: return@mapNotNull null
                    runCatching {
                        MlKitTranslationEngine.requireSupportedLanguageTag(tag, "语种包")
                    }.getOrNull() ?: return@mapNotNull null
                    MlKitLanguageDescriptor(
                        languageTag = tag,
                        displayName = if (tag == "zh") "中文（简体/繁体）" else language.displayName,
                        isBuiltIn = tag == "en",
                    )
                }
                .distinctBy { it.languageTag }
        }
    }
}

internal data class MlKitLanguageDescriptor(
    val languageTag: String,
    val displayName: String,
    val isBuiltIn: Boolean,
) {
    fun toRemoteModel(): TranslateRemoteModel {
        return TranslateRemoteModel.Builder(languageTag).build()
    }

    fun toState(
        isDownloaded: Boolean,
        isDownloading: Boolean = false,
        errorMessage: String? = null,
    ): MlKitLanguageModelState {
        return MlKitLanguageModelState(
            languageTag = languageTag,
            displayName = displayName,
            isBuiltIn = isBuiltIn,
            isDownloaded = isDownloaded,
            isDownloading = isDownloading,
            errorMessage = errorMessage,
        )
    }
}

private suspend fun <T> Task<T>.await(): T {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { error -> continuation.resumeWithException(error) }
        addOnCanceledListener { continuation.cancel() }
    }
}
