package com.mxwis.aitranslate.data.translation

import com.mxwis.aitranslate.data.auth.RemoteHistorySync
import com.mxwis.aitranslate.data.history.TranslationHistoryDao
import com.mxwis.aitranslate.data.history.TranslationHistoryEntity
import com.mxwis.aitranslate.data.model.HyMtModelManager
import com.mxwis.aitranslate.data.model.MlKitLanguageModelManager
import com.mxwis.aitranslate.data.model.MlKitLanguageModelState
import com.mxwis.aitranslate.data.model.ModelState
import com.mxwis.aitranslate.data.settings.AppSettings
import com.mxwis.aitranslate.data.settings.CloudProviderSettings
import com.mxwis.aitranslate.data.settings.SettingsStore
import com.mxwis.aitranslate.data.update.AppUpdateCheckResult
import com.mxwis.aitranslate.data.update.AppUpdateManager
import com.mxwis.aitranslate.data.update.AppUpdateRelease
import com.mxwis.aitranslate.domain.OfflineModelType
import com.mxwis.aitranslate.domain.TranslateOutput
import com.mxwis.aitranslate.domain.TranslateRequest
import com.mxwis.aitranslate.domain.TranslationMode
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

interface TranslationRepositoryContract {
    val settings: Flow<AppSettings>
    val history: Flow<List<TranslationHistoryEntity>>
    val modelState: Flow<ModelState>
    val mlKitLanguageModels: Flow<List<MlKitLanguageModelState>>

    suspend fun updateBaseUrl(value: String)
    suspend fun updateApiKey(value: String)
    suspend fun updateModelName(value: String)
    suspend fun updateCustomModelNames(values: List<String>)
    suspend fun updateProviderName(value: String)
    suspend fun selectCloudProvider(providerId: String)
    suspend fun addCloudProvider(provider: CloudProviderSettings)
    suspend fun updateDefaultMode(value: TranslationMode)
    suspend fun updateOfflineModelType(value: OfflineModelType)
    suspend fun fetchCloudModels(settings: AppSettings): List<String>
    suspend fun checkAppUpdate(currentVersionCode: Int): AppUpdateCheckResult
    suspend fun downloadAppUpdate(
        release: AppUpdateRelease,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): File
    suspend fun translate(request: TranslateRequest, mode: TranslationMode): TranslateOutput
    suspend fun downloadModel()
    suspend fun deleteModel()
    fun refreshModelState()
    suspend fun refreshMlKitLanguageModels()
    suspend fun downloadMlKitLanguageModel(languageTag: String)
    suspend fun deleteMlKitLanguageModel(languageTag: String)
    suspend fun deleteHistory(entity: TranslationHistoryEntity)
    suspend fun clearHistory()
}

class TranslationRepository(
    private val settingsStore: SettingsStore,
    private val historyDao: TranslationHistoryDao,
    private val modelManager: HyMtModelManager,
    private val mlKitLanguageModelManager: MlKitLanguageModelManager,
    private val cloudEngine: CloudTranslationEngine,
    private val offlineEngine: OfflineTranslationEngine,
    private val mlKitEngine: MlKitTranslationEngine,
    private val appUpdateManager: AppUpdateManager,
    private val remoteHistorySync: RemoteHistorySync? = null,
) : TranslationRepositoryContract {
    override val settings: Flow<AppSettings> = settingsStore.settings
    override val history: Flow<List<TranslationHistoryEntity>> = historyDao.observeAll()
    override val modelState: Flow<ModelState> = modelManager.state
    override val mlKitLanguageModels: Flow<List<MlKitLanguageModelState>> = mlKitLanguageModelManager.state

    override suspend fun updateBaseUrl(value: String) = settingsStore.updateBaseUrl(value)
    override suspend fun updateApiKey(value: String) = settingsStore.updateApiKey(value)
    override suspend fun updateModelName(value: String) = settingsStore.updateModelName(value)
    override suspend fun updateCustomModelNames(values: List<String>) = settingsStore.updateCustomModelNames(values)
    override suspend fun updateProviderName(value: String) = settingsStore.updateProviderName(value)
    override suspend fun selectCloudProvider(providerId: String) = settingsStore.selectCloudProvider(providerId)
    override suspend fun addCloudProvider(provider: CloudProviderSettings) = settingsStore.addCloudProvider(provider)
    override suspend fun updateDefaultMode(value: TranslationMode) = settingsStore.updateDefaultMode(value)
    override suspend fun updateOfflineModelType(value: OfflineModelType) = settingsStore.updateOfflineModelType(value)

    override suspend fun fetchCloudModels(settings: AppSettings): List<String> = cloudEngine.fetchModels(settings)
    override suspend fun checkAppUpdate(currentVersionCode: Int): AppUpdateCheckResult {
        return appUpdateManager.checkForUpdate(currentVersionCode)
    }
    override suspend fun downloadAppUpdate(
        release: AppUpdateRelease,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): File {
        return appUpdateManager.downloadUpdate(release, onProgress)
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
                translatedText = translateOfflineText(request, settings.offlineModelType),
                usedMode = TranslationMode.OFFLINE,
                usedModelName = "${settings.offlineModelType.displayName} 离线",
            )
            TranslationMode.AUTO -> translateAutomatically(request, settings)
        }

        val history = TranslationHistoryEntity(
            sourceText = request.sourceText,
            translatedText = result.translatedText,
            sourceLanguage = request.sourceLanguage.displayName,
            targetLanguage = request.targetLanguage.displayName,
            mode = result.displayModeLabel,
            createdAt = System.currentTimeMillis(),
        )
        historyDao.insert(history)
        runCatching {
            withTimeoutOrNull(3_000) {
                remoteHistorySync?.syncHistory(history)
            }
        }
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
            if (canAttemptOffline(settings.offlineModelType)) {
                runCatching {
                    TranslateOutput(
                        translatedText = translateOfflineText(request, settings.offlineModelType),
                        usedMode = TranslationMode.OFFLINE,
                        usedModelName = "${settings.offlineModelType.displayName} 离线",
                    )
                }.getOrElse {
                    error("云端翻译失败，离线内核暂不可用：${cloudError.message}")
                }
            } else {
                error("云端翻译失败，且离线模型未下载：${cloudError.message}")
            }
        }
    }

    private suspend fun translateOfflineText(
        request: TranslateRequest,
        offlineModelType: OfflineModelType,
    ): String {
        return when (offlineModelType) {
            OfflineModelType.HY_MT -> offlineEngine.translate(request)
            OfflineModelType.ML_KIT -> mlKitEngine.translate(request)
        }
    }

    private fun canAttemptOffline(offlineModelType: OfflineModelType): Boolean {
        return canAttemptOffline(
            offlineModelType = offlineModelType,
            isHyMtAvailable = modelManager.isModelAvailable(),
        )
    }

    override suspend fun downloadModel() = modelManager.downloadModel()
    override suspend fun deleteModel() = modelManager.deleteModel()
    override fun refreshModelState() = modelManager.refresh()
    override suspend fun refreshMlKitLanguageModels() {
        mlKitLanguageModelManager.refresh()
    }
    override suspend fun downloadMlKitLanguageModel(languageTag: String) {
        mlKitLanguageModelManager.downloadLanguage(languageTag)
    }
    override suspend fun deleteMlKitLanguageModel(languageTag: String) {
        mlKitLanguageModelManager.deleteLanguage(languageTag)
    }
    override suspend fun deleteHistory(entity: TranslationHistoryEntity) = historyDao.delete(entity)
    override suspend fun clearHistory() = historyDao.clear()

    companion object {
        internal fun canAttemptOffline(
            offlineModelType: OfflineModelType,
            isHyMtAvailable: Boolean,
        ): Boolean {
            return when (offlineModelType) {
                OfflineModelType.HY_MT -> isHyMtAvailable
                OfflineModelType.ML_KIT -> true
            }
        }
    }
}
