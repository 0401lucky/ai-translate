package com.mxwis.aitranslate.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxwis.aitranslate.BuildConfig
import com.mxwis.aitranslate.data.settings.CloudProviderSettings
import com.mxwis.aitranslate.data.history.TranslationHistoryEntity
import com.mxwis.aitranslate.data.model.ModelState
import com.mxwis.aitranslate.data.settings.AppSettings
import com.mxwis.aitranslate.data.settings.DEFAULT_MODEL_NAME
import com.mxwis.aitranslate.data.translation.TranslationRepositoryContract
import com.mxwis.aitranslate.data.update.AppUpdateCheckResult
import com.mxwis.aitranslate.data.update.AppUpdateRelease
import com.mxwis.aitranslate.domain.ClipboardQuickTranslatePolicy
import com.mxwis.aitranslate.domain.LanguageOption
import com.mxwis.aitranslate.domain.Languages
import com.mxwis.aitranslate.domain.TranslateRequest
import com.mxwis.aitranslate.domain.TranslationMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppSection(val label: String) {
    TRANSLATE("翻译"),
    HISTORY("历史记录"),
    MODEL("模型"),
    SETTINGS("设置"),
}

enum class LanguagePickerTarget {
    SOURCE,
    TARGET,
}

data class TranslateUiState(
    val currentSection: AppSection = AppSection.TRANSLATE,
    val sourceText: String = "",
    val translatedText: String = "",
    val sourceLanguage: LanguageOption = Languages.auto,
    val targetLanguage: LanguageOption = Languages.supported.first(),
    val selectedMode: TranslationMode = TranslationMode.CLOUD,
    val settings: AppSettings = AppSettings(),
    val modelState: ModelState = ModelState(),
    val histories: List<TranslationHistoryEntity> = emptyList(),
    val availableModels: List<String> = emptyList(),
    val isFetchingModels: Boolean = false,
    val isModelPickerOpen: Boolean = false,
    val modelSearchQuery: String = "",
    val modelToAdd: String = "",
    val modelFetchMessage: String? = null,
    val modelFetchError: String? = null,
    val isTranslating: Boolean = false,
    val languagePickerTarget: LanguagePickerTarget? = null,
    val selectedHistory: TranslationHistoryEntity? = null,
    val isMiniTranslatorOpen: Boolean = false,
    val miniSourceLabel: String = "系统划词 / 分享",
    val miniSourceText: String = "",
    val miniTranslatedText: String = "",
    val isMiniTranslating: Boolean = false,
    val miniErrorMessage: String? = null,
    val miniInfoMessage: String? = null,
    val shouldAutoTranslateMini: Boolean = false,
    val isClipboardSuggestionOpen: Boolean = false,
    val clipboardCandidateText: String = "",
    val isCheckingAppUpdate: Boolean = false,
    val availableAppUpdate: AppUpdateRelease? = null,
    val isDownloadingAppUpdate: Boolean = false,
    val appUpdateDownloadProgress: Float = 0f,
    val downloadedAppUpdatePath: String? = null,
    val pendingAppUpdateInstallPath: String? = null,
    val appUpdateMessage: String? = null,
    val appUpdateError: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

class TranslateViewModel(
    private val repository: TranslationRepositoryContract,
    private val currentVersionCode: Int = BuildConfig.VERSION_CODE,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TranslateUiState())
    val uiState: StateFlow<TranslateUiState> = _uiState.asStateFlow()
    private var lastClipboardPromptText: String? = null

    init {
        viewModelScope.launch {
            repository.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        settings = settings,
                        selectedMode = settings.defaultMode,
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.history.collect { histories ->
                _uiState.update { it.copy(histories = histories) }
            }
        }
        viewModelScope.launch {
            repository.modelState.collect { modelState ->
                _uiState.update { it.copy(modelState = modelState) }
            }
        }
        repository.refreshModelState()
    }

    fun selectSection(section: AppSection) {
        _uiState.update { it.copy(currentSection = section, errorMessage = null, infoMessage = null) }
    }

    fun updateSourceText(value: String) {
        _uiState.update { it.copy(sourceText = value, errorMessage = null) }
    }

    fun clearInput() {
        _uiState.update {
            it.copy(sourceText = "", translatedText = "", errorMessage = null, infoMessage = null)
        }
    }

    fun selectMode(mode: TranslationMode) {
        _uiState.update { it.copy(selectedMode = mode, errorMessage = null) }
    }

    fun openLanguagePicker(target: LanguagePickerTarget) {
        _uiState.update { it.copy(languagePickerTarget = target) }
    }

    fun closeLanguagePicker() {
        _uiState.update { it.copy(languagePickerTarget = null) }
    }

    fun chooseLanguage(language: LanguageOption) {
        _uiState.update { state ->
            when (state.languagePickerTarget) {
                LanguagePickerTarget.SOURCE -> state.copy(
                    sourceLanguage = language,
                    languagePickerTarget = null,
                )
                LanguagePickerTarget.TARGET -> state.copy(
                    targetLanguage = if (language.code == Languages.auto.code) Languages.supported.first() else language,
                    languagePickerTarget = null,
                )
                null -> state
            }
        }
    }

    fun swapLanguages() {
        _uiState.update { state ->
            if (state.sourceLanguage.code == Languages.auto.code) {
                state.copy(
                    sourceLanguage = state.targetLanguage,
                    targetLanguage = Languages.supported.first(),
                )
            } else {
                state.copy(
                    sourceLanguage = state.targetLanguage,
                    targetLanguage = state.sourceLanguage,
                    sourceText = state.translatedText.ifBlank { state.sourceText },
                    translatedText = state.sourceText,
                )
            }
        }
    }

    fun translate() {
        val snapshot = _uiState.value
        if (snapshot.sourceText.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入要翻译的文本") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isTranslating = true, errorMessage = null, infoMessage = null)
            }

            val result = runCatching {
                repository.translate(
                    request = TranslateRequest(
                        sourceText = snapshot.sourceText.trim(),
                        sourceLanguage = snapshot.sourceLanguage,
                        targetLanguage = snapshot.targetLanguage,
                    ),
                    mode = snapshot.selectedMode,
                )
            }

            result.onSuccess { output ->
                _uiState.update {
                    it.copy(
                        translatedText = output.translatedText,
                        selectedMode = output.usedMode,
                        isTranslating = false,
                        infoMessage = "已使用${output.usedMode.label}翻译",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isTranslating = false,
                        errorMessage = error.message ?: "翻译失败，请稍后重试",
                    )
                }
            }
        }
    }

    fun openMiniTranslator(
        text: String,
        sourceLabel: String = "系统划词 / 分享",
        autoTranslate: Boolean = false,
    ) {
        val normalized = text.trim()
        if (normalized.isBlank()) return

        _uiState.update {
            it.copy(
                currentSection = AppSection.TRANSLATE,
                isMiniTranslatorOpen = true,
                miniSourceLabel = sourceLabel,
                miniSourceText = normalized,
                miniTranslatedText = "",
                miniErrorMessage = null,
                miniInfoMessage = "已接收${sourceLabel}文本",
                shouldAutoTranslateMini = autoTranslate,
                isClipboardSuggestionOpen = false,
                clipboardCandidateText = "",
                errorMessage = null,
                infoMessage = null,
            )
        }
    }

    fun closeMiniTranslator() {
        _uiState.update {
            it.copy(
                isMiniTranslatorOpen = false,
                miniErrorMessage = null,
                miniInfoMessage = null,
                shouldAutoTranslateMini = false,
            )
        }
    }

    fun offerClipboardQuickTranslate(text: String) {
        val normalized = ClipboardQuickTranslatePolicy.normalize(text)
        if (normalized.isBlank()) return

        _uiState.update { state ->
            if (
                ClipboardQuickTranslatePolicy.shouldOffer(
                    normalizedText = normalized,
                    lastPromptText = lastClipboardPromptText,
                    currentSourceText = state.sourceText,
                    isMiniTranslatorOpen = state.isMiniTranslatorOpen,
                    isClipboardSuggestionOpen = state.isClipboardSuggestionOpen,
                )
            ) {
                    lastClipboardPromptText = normalized
                    state.copy(
                        isClipboardSuggestionOpen = true,
                        clipboardCandidateText = normalized,
                        errorMessage = null,
                        infoMessage = null,
                    )
            } else {
                state
            }
        }
    }

    fun acceptClipboardQuickTranslate() {
        val text = _uiState.value.clipboardCandidateText
        if (text.isBlank()) {
            dismissClipboardQuickTranslate()
            return
        }
        openMiniTranslator(text, sourceLabel = "剪贴板", autoTranslate = true)
    }

    fun dismissClipboardQuickTranslate() {
        _uiState.update {
            it.copy(
                isClipboardSuggestionOpen = false,
                clipboardCandidateText = "",
            )
        }
    }

    fun translateMini() {
        val snapshot = _uiState.value
        if (snapshot.miniSourceText.isBlank()) {
            _uiState.update { it.copy(miniErrorMessage = "没有可翻译的文本") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isMiniTranslating = true,
                    shouldAutoTranslateMini = false,
                    miniErrorMessage = null,
                    miniInfoMessage = null,
                )
            }

            val result = runCatching {
                repository.translate(
                    request = TranslateRequest(
                        sourceText = snapshot.miniSourceText.trim(),
                        sourceLanguage = snapshot.sourceLanguage,
                        targetLanguage = snapshot.targetLanguage,
                    ),
                    mode = snapshot.selectedMode,
                )
            }

            result.onSuccess { output ->
                _uiState.update {
                    it.copy(
                        miniTranslatedText = output.translatedText,
                        selectedMode = output.usedMode,
                        isMiniTranslating = false,
                        miniInfoMessage = "已使用${output.usedMode.label}翻译",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isMiniTranslating = false,
                        miniErrorMessage = error.message ?: "翻译失败，请稍后重试",
                    )
                }
            }
        }
    }

    fun consumeMiniAutoTranslateRequest() {
        _uiState.update { it.copy(shouldAutoTranslateMini = false) }
    }

    fun openFullTranslateFromMini() {
        _uiState.update {
            it.copy(
                currentSection = AppSection.TRANSLATE,
                sourceText = it.miniSourceText,
                translatedText = it.miniTranslatedText,
                isMiniTranslatorOpen = false,
                errorMessage = null,
                infoMessage = "已带入完整翻译页",
            )
        }
    }

    fun updateBaseUrl(value: String) {
        _uiState.update {
            val provider = it.settings.selectedProvider.copy(baseUrl = value)
            it.copy(
                settings = it.settings.withSelectedProvider(provider),
                availableModels = emptyList(),
                modelFetchMessage = null,
                modelFetchError = null,
            )
        }
        viewModelScope.launch { repository.updateBaseUrl(value) }
    }

    fun updateApiKey(value: String) {
        _uiState.update {
            val provider = it.settings.selectedProvider.copy(apiKey = value)
            it.copy(
                settings = it.settings.withSelectedProvider(provider),
                availableModels = emptyList(),
                modelFetchMessage = null,
                modelFetchError = null,
            )
        }
        viewModelScope.launch { repository.updateApiKey(value) }
    }

    fun updateModelName(value: String) {
        _uiState.update {
            val provider = it.settings.selectedProvider.copy(modelName = value)
            it.copy(settings = it.settings.withSelectedProvider(provider))
        }
        viewModelScope.launch { repository.updateModelName(value) }
    }

    fun updateProviderName(value: String) {
        _uiState.update {
            val provider = it.settings.selectedProvider.copy(name = value)
            it.copy(settings = it.settings.withSelectedProvider(provider))
        }
        viewModelScope.launch { repository.updateProviderName(value) }
    }

    fun selectCloudProvider(providerId: String) {
        _uiState.update { state ->
            val provider = state.settings.cloudProviders.firstOrNull { it.id == providerId } ?: return@update state
            state.copy(
                settings = state.settings.withSelectedProvider(provider),
                availableModels = emptyList(),
                modelFetchMessage = "已切换供应商：${provider.name}",
                modelFetchError = null,
            )
        }
        viewModelScope.launch { repository.selectCloudProvider(providerId) }
    }

    fun addCloudProvider() {
        val provider = CloudProviderSettings(
            id = "custom-${System.currentTimeMillis()}",
            name = "自定义供应商",
            baseUrl = "",
            modelName = DEFAULT_MODEL_NAME,
        )
        _uiState.update {
            it.copy(
                settings = it.settings.withSelectedProvider(provider),
                availableModels = emptyList(),
                modelFetchMessage = "已新增供应商，请填写接口地址和 API Key",
                modelFetchError = null,
            )
        }
        viewModelScope.launch { repository.addCloudProvider(provider) }
    }

    fun openModelPicker() {
        _uiState.update {
            it.copy(
                isModelPickerOpen = true,
                modelSearchQuery = "",
                modelToAdd = "",
                modelFetchError = null,
            )
        }
    }

    fun closeModelPicker() {
        _uiState.update { it.copy(isModelPickerOpen = false, modelSearchQuery = "", modelToAdd = "") }
    }

    fun updateModelSearchQuery(value: String) {
        _uiState.update { it.copy(modelSearchQuery = value) }
    }

    fun updateModelToAdd(value: String) {
        _uiState.update { it.copy(modelToAdd = value, modelFetchError = null) }
    }

    fun selectCloudModel(value: String) {
        val model = value.trim()
        if (model.isBlank()) return
        _uiState.update {
            val provider = it.settings.selectedProvider.copy(modelName = model)
            it.copy(
                settings = it.settings.withSelectedProvider(provider),
                isModelPickerOpen = false,
                modelSearchQuery = "",
                modelToAdd = "",
                modelFetchMessage = "已选择模型：$model",
                modelFetchError = null,
            )
        }
        viewModelScope.launch { repository.updateModelName(model) }
    }

    fun addCustomModel() {
        val snapshot = _uiState.value
        val model = snapshot.modelToAdd.trim()
        if (model.isBlank()) {
            _uiState.update { it.copy(modelFetchError = "请输入要添加的模型名称") }
            return
        }

        val nextModels = (snapshot.settings.customModelNames + model)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }

        _uiState.update {
            val provider = it.settings.selectedProvider.copy(
                modelName = model,
                customModelNames = nextModels,
            )
            it.copy(
                settings = it.settings.withSelectedProvider(provider),
                isModelPickerOpen = false,
                modelSearchQuery = "",
                modelToAdd = "",
                modelFetchMessage = "已添加并使用：$model",
                modelFetchError = null,
            )
        }

        viewModelScope.launch {
            repository.updateCustomModelNames(nextModels)
            repository.updateModelName(model)
        }
    }

    fun updateDefaultMode(value: TranslationMode) {
        _uiState.update { it.copy(settings = it.settings.copy(defaultMode = value)) }
        viewModelScope.launch { repository.updateDefaultMode(value) }
    }

    fun fetchCloudModels() {
        val snapshot = _uiState.value.settings
        if (snapshot.baseUrl.isBlank()) {
            _uiState.update { it.copy(modelFetchError = "请先填写接口地址") }
            return
        }
        if (snapshot.apiKey.isBlank()) {
            _uiState.update { it.copy(modelFetchError = "请先填写 API Key") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isFetchingModels = true,
                    modelFetchMessage = null,
                    modelFetchError = null,
                )
            }

            runCatching { repository.fetchCloudModels(snapshot) }
                .onSuccess { models ->
                    val firstModel = models.firstOrNull()
                    if (_uiState.value.settings.modelName.isBlank() && firstModel != null) {
                        repository.updateModelName(firstModel)
                    }
                    _uiState.update {
                        it.copy(
                            availableModels = models,
                            isFetchingModels = false,
                            modelFetchMessage = "已获取 ${models.size} 个模型",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            availableModels = emptyList(),
                            isFetchingModels = false,
                            modelFetchError = error.message ?: "获取模型失败，请检查配置",
                        )
                    }
                }
        }
    }

    fun checkAppUpdate() {
        if (_uiState.value.isCheckingAppUpdate) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCheckingAppUpdate = true,
                    availableAppUpdate = null,
                    downloadedAppUpdatePath = null,
                    pendingAppUpdateInstallPath = null,
                    appUpdateMessage = null,
                    appUpdateError = null,
                )
            }

            runCatching { repository.checkAppUpdate(currentVersionCode) }
                .onSuccess { result ->
                    _uiState.update { state ->
                        when (result) {
                            is AppUpdateCheckResult.Available -> state.copy(
                                isCheckingAppUpdate = false,
                                availableAppUpdate = result.release,
                                appUpdateMessage = "发现新版本 ${result.release.versionName}",
                            )
                            is AppUpdateCheckResult.PackageUnavailable -> state.copy(
                                isCheckingAppUpdate = false,
                                availableAppUpdate = null,
                                appUpdateMessage = "更新清单已连接，但安装包还没有发布",
                            )
                            is AppUpdateCheckResult.UpToDate -> state.copy(
                                isCheckingAppUpdate = false,
                                availableAppUpdate = null,
                                appUpdateMessage = "当前已是最新版本 ${result.latestVersionName}",
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isCheckingAppUpdate = false,
                            availableAppUpdate = null,
                            appUpdateError = error.message ?: "检查更新失败，请稍后重试",
                        )
                    }
                }
        }
    }

    fun downloadAppUpdate() {
        val release = _uiState.value.availableAppUpdate
        if (release == null) {
            _uiState.update { it.copy(appUpdateError = "请先检查更新") }
            return
        }
        if (_uiState.value.isDownloadingAppUpdate) return

        val downloadedPath = _uiState.value.downloadedAppUpdatePath
        if (downloadedPath != null) {
            _uiState.update { it.copy(pendingAppUpdateInstallPath = downloadedPath) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDownloadingAppUpdate = true,
                    appUpdateDownloadProgress = 0f,
                    appUpdateMessage = "正在下载更新包",
                    appUpdateError = null,
                )
            }

            runCatching {
                repository.downloadAppUpdate(release) { downloadedBytes, totalBytes ->
                    _uiState.update {
                        it.copy(
                            appUpdateDownloadProgress = if (totalBytes > 0L) {
                                (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                            } else {
                                0f
                            },
                        )
                    }
                }
            }.onSuccess { file ->
                _uiState.update {
                    it.copy(
                        isDownloadingAppUpdate = false,
                        appUpdateDownloadProgress = 1f,
                        downloadedAppUpdatePath = file.absolutePath,
                        pendingAppUpdateInstallPath = file.absolutePath,
                        appUpdateMessage = "更新包已下载并校验，准备安装",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isDownloadingAppUpdate = false,
                        appUpdateDownloadProgress = 0f,
                        downloadedAppUpdatePath = null,
                        pendingAppUpdateInstallPath = null,
                        appUpdateError = error.message ?: "更新包下载失败",
                    )
                }
            }
        }
    }

    fun consumeAppUpdateInstallRequest() {
        _uiState.update { it.copy(pendingAppUpdateInstallPath = null) }
    }

    fun downloadModel() {
        viewModelScope.launch { repository.downloadModel() }
    }

    fun deleteModel() {
        viewModelScope.launch { repository.deleteModel() }
    }

    fun deleteHistory(entity: TranslationHistoryEntity) {
        _uiState.update {
            if (it.selectedHistory?.id == entity.id) it.copy(selectedHistory = null) else it
        }
        viewModelScope.launch { repository.deleteHistory(entity) }
    }

    fun openHistoryDetail(entity: TranslationHistoryEntity) {
        _uiState.update { it.copy(selectedHistory = entity) }
    }

    fun closeHistoryDetail() {
        _uiState.update { it.copy(selectedHistory = null) }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }

    fun clearMessage() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }
}
