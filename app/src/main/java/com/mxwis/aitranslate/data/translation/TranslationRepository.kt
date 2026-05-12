package com.mxwis.aitranslate.data.translation

import com.mxwis.aitranslate.data.history.TranslationHistoryDao
import com.mxwis.aitranslate.data.history.TranslationHistoryEntity
import com.mxwis.aitranslate.data.model.HyMtModelManager
import com.mxwis.aitranslate.data.model.ModelState
import com.mxwis.aitranslate.data.settings.AppSettings
import com.mxwis.aitranslate.data.settings.CloudProviderSettings
import com.mxwis.aitranslate.data.settings.SettingsStore
import com.mxwis.aitranslate.domain.TranslateOutput
import com.mxwis.aitranslate.domain.TranslateRequest
import com.mxwis.aitranslate.domain.TranslationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TranslationRepository(
    private val settingsStore: SettingsStore,
    private val historyDao: TranslationHistoryDao,
    private val modelManager: HyMtModelManager,
    private val cloudEngine: CloudTranslationEngine,
    private val offlineEngine: OfflineTranslationEngine,
) {
    val settings: Flow<AppSettings> = settingsStore.settings
    val history: Flow<List<TranslationHistoryEntity>> = historyDao.observeAll()
    val modelState: Flow<ModelState> = modelManager.state

    suspend fun updateBaseUrl(value: String) = settingsStore.updateBaseUrl(value)
    suspend fun updateApiKey(value: String) = settingsStore.updateApiKey(value)
    suspend fun updateModelName(value: String) = settingsStore.updateModelName(value)
    suspend fun updateCustomModelNames(values: List<String>) = settingsStore.updateCustomModelNames(values)
    suspend fun updateProviderName(value: String) = settingsStore.updateProviderName(value)
    suspend fun selectCloudProvider(providerId: String) = settingsStore.selectCloudProvider(providerId)
    suspend fun addCloudProvider(provider: CloudProviderSettings) = settingsStore.addCloudProvider(provider)
    suspend fun updateDefaultMode(value: TranslationMode) = settingsStore.updateDefaultMode(value)

    suspend fun fetchCloudModels(settings: AppSettings): List<String> = cloudEngine.fetchModels(settings)

    suspend fun translate(request: TranslateRequest, mode: TranslationMode): TranslateOutput {
        require(request.sourceText.isNotBlank()) { "请输入要翻译的文本" }
        require(request.targetLanguage.code != "auto") { "目标语言不能选择自动检测" }

        val settings = settings.first()
        val result = when (mode) {
            TranslationMode.CLOUD -> TranslateOutput(
                translatedText = cloudEngine.translate(request, settings),
                usedMode = TranslationMode.CLOUD,
            )
            TranslationMode.OFFLINE -> TranslateOutput(
                translatedText = offlineEngine.translate(request),
                usedMode = TranslationMode.OFFLINE,
            )
            TranslationMode.AUTO -> translateAutomatically(request, settings)
        }

        historyDao.insert(
            TranslationHistoryEntity(
                sourceText = request.sourceText,
                translatedText = result.translatedText,
                sourceLanguage = request.sourceLanguage.displayName,
                targetLanguage = request.targetLanguage.displayName,
                mode = result.usedMode.label,
                createdAt = System.currentTimeMillis(),
            ),
        )
        return result
    }

    private suspend fun translateAutomatically(
        request: TranslateRequest,
        settings: AppSettings,
    ): TranslateOutput {
        return runCatching {
            TranslateOutput(
                translatedText = cloudEngine.translate(request, settings),
                usedMode = TranslationMode.CLOUD,
            )
        }.getOrElse { cloudError ->
            if (modelManager.isModelAvailable()) {
                runCatching {
                    TranslateOutput(
                        translatedText = offlineEngine.translate(request),
                        usedMode = TranslationMode.OFFLINE,
                    )
                }.getOrElse {
                    error("云端翻译失败，离线内核暂不可用：${cloudError.message}")
                }
            } else {
                error("云端翻译失败，且离线模型未下载：${cloudError.message}")
            }
        }
    }

    suspend fun downloadModel() = modelManager.downloadModel()
    suspend fun deleteModel() = modelManager.deleteModel()
    fun refreshModelState() = modelManager.refresh()
    suspend fun deleteHistory(entity: TranslationHistoryEntity) = historyDao.delete(entity)
    suspend fun clearHistory() = historyDao.clear()
}
