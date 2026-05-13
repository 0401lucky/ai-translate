package com.mxwis.aitranslate.data.translation

import com.mxwis.aitranslate.data.history.TranslationHistoryDao
import com.mxwis.aitranslate.data.history.TranslationHistoryEntity
import com.mxwis.aitranslate.data.model.HyMtModelManager
import com.mxwis.aitranslate.data.model.ModelState
import com.mxwis.aitranslate.data.settings.AppSettings
import com.mxwis.aitranslate.data.settings.CloudProviderSettings
import com.mxwis.aitranslate.data.settings.SettingsStore
import com.mxwis.aitranslate.data.update.AppUpdateCheckResult
import com.mxwis.aitranslate.data.update.AppUpdateManager
import com.mxwis.aitranslate.domain.TranslateOutput
import com.mxwis.aitranslate.domain.TranslateRequest
import com.mxwis.aitranslate.domain.TranslationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface TranslationRepositoryContract {
    val settings: Flow<AppSettings>
    val history: Flow<List<TranslationHistoryEntity>>
    val modelState: Flow<ModelState>

    suspend fun updateBaseUrl(value: String)
    suspend fun updateApiKey(value: String)
    suspend fun updateModelName(value: String)
    suspend fun updateCustomModelNames(values: List<String>)
    suspend fun updateProviderName(value: String)
    suspend fun selectCloudProvider(providerId: String)
    suspend fun addCloudProvider(provider: CloudProviderSettings)
    suspend fun updateDefaultMode(value: TranslationMode)
    suspend fun fetchCloudModels(settings: AppSettings): List<String>
    suspend fun checkAppUpdate(currentVersionCode: Int): AppUpdateCheckResult
    suspend fun translate(request: TranslateRequest, mode: TranslationMode): TranslateOutput
    suspend fun downloadModel()
    suspend fun deleteModel()
    fun refreshModelState()
    suspend fun deleteHistory(entity: TranslationHistoryEntity)
    suspend fun clearHistory()
}

class TranslationRepository(
    private val settingsStore: SettingsStore,
    private val historyDao: TranslationHistoryDao,
    private val modelManager: HyMtModelManager,
    private val cloudEngine: CloudTranslationEngine,
    private val offlineEngine: OfflineTranslationEngine,
    private val appUpdateManager: AppUpdateManager,
) : TranslationRepositoryContract {
    override val settings: Flow<AppSettings> = settingsStore.settings
    override val history: Flow<List<TranslationHistoryEntity>> = historyDao.observeAll()
    override val modelState: Flow<ModelState> = modelManager.state

    override suspend fun updateBaseUrl(value: String) = settingsStore.updateBaseUrl(value)
    override suspend fun updateApiKey(value: String) = settingsStore.updateApiKey(value)
    override suspend fun updateModelName(value: String) = settingsStore.updateModelName(value)
    override suspend fun updateCustomModelNames(values: List<String>) = settingsStore.updateCustomModelNames(values)
    override suspend fun updateProviderName(value: String) = settingsStore.updateProviderName(value)
    override suspend fun selectCloudProvider(providerId: String) = settingsStore.selectCloudProvider(providerId)
    override suspend fun addCloudProvider(provider: CloudProviderSettings) = settingsStore.addCloudProvider(provider)
    override suspend fun updateDefaultMode(value: TranslationMode) = settingsStore.updateDefaultMode(value)

    override suspend fun fetchCloudModels(settings: AppSettings): List<String> = cloudEngine.fetchModels(settings)
    override suspend fun checkAppUpdate(currentVersionCode: Int): AppUpdateCheckResult {
        return appUpdateManager.checkForUpdate(currentVersionCode)
    }

    override suspend fun translate(request: TranslateRequest, mode: TranslationMode): TranslateOutput {
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

    override suspend fun downloadModel() = modelManager.downloadModel()
    override suspend fun deleteModel() = modelManager.deleteModel()
    override fun refreshModelState() = modelManager.refresh()
    override suspend fun deleteHistory(entity: TranslationHistoryEntity) = historyDao.delete(entity)
    override suspend fun clearHistory() = historyDao.clear()
}
