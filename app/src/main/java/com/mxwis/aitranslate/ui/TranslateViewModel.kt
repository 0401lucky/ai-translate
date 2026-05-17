package com.mxwis.aitranslate.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxwis.aitranslate.BuildConfig
import com.mxwis.aitranslate.data.dictionary.DictionaryEntry
import com.mxwis.aitranslate.data.dictionary.DictionaryRepositoryContract
import com.mxwis.aitranslate.data.dictionary.DictionaryWordSummary
import com.mxwis.aitranslate.data.history.TranslationHistoryEntity
import com.mxwis.aitranslate.data.model.ModelState
import com.mxwis.aitranslate.data.ocr.ImageTextRecognizerContract
import com.mxwis.aitranslate.data.settings.AppSettings
import com.mxwis.aitranslate.data.settings.CloudProviderSettings
import com.mxwis.aitranslate.data.settings.DEFAULT_MODEL_NAME
import com.mxwis.aitranslate.data.translation.TranslationRepositoryContract
import com.mxwis.aitranslate.data.update.AppUpdateCheckResult
import com.mxwis.aitranslate.data.update.AppUpdateRelease
import com.mxwis.aitranslate.domain.ClipboardQuickTranslatePolicy
import com.mxwis.aitranslate.domain.LanguageOption
import com.mxwis.aitranslate.domain.Languages
import com.mxwis.aitranslate.domain.ModelType
import com.mxwis.aitranslate.domain.TranslateRequest
import com.mxwis.aitranslate.domain.TranslationMode
import com.mxwis.aitranslate.domain.UnifiedModelOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppSection(val label: String) {
    TRANSLATE("翻译"),
    DICTIONARY("词典"),
    HISTORY("历史"),
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
    val selectedUnifiedModel: UnifiedModelOption? = null,
    val unifiedModelList: List<UnifiedModelOption> = emptyList(),
    val isUnifiedModelPickerOpen: Boolean = false,
    val isImageTranslatorOpen: Boolean = false,
    val imageSourceLabel: String = "图片翻译",
    val imageUri: String? = null,
    val imageRecognizedText: String = "",
    val imageTranslatedText: String = "",
    val isImageRecognizing: Boolean = false,
    val isImageTranslating: Boolean = false,
    val imageErrorMessage: String? = null,
    val imageInfoMessage: String? = null,
    val dictionaryQuery: String = "",
    val dictionaryEntry: DictionaryEntry? = null,
    val dictionarySuggestions: List<DictionaryWordSummary> = emptyList(),
    val isDictionaryLoading: Boolean = false,
    val dictionaryMessage: String? = null,
    val dictionaryErrorMessage: String? = null,
)

class TranslateViewModel(
    private val repository: TranslationRepositoryContract,
    private val imageTextRecognizer: ImageTextRecognizerContract? = null,
    private val dictionaryRepository: DictionaryRepositoryContract? = null,
    private val currentVersionCode: Int = BuildConfig.VERSION_CODE,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TranslateUiState())
    val uiState: StateFlow<TranslateUiState> = _uiState.asStateFlow()
    private var lastClipboardPromptText: String? = null

    init {
        viewModelScope.launch {
            repository.settings.collect { settings ->
                _uiState.update { state ->
                    val mode = settings.defaultMode
                    val models = buildUnifiedModelList(
                        state = state,
                        settings = settings,
                        modelState = state.modelState,
                    )
                    state.copy(
                        settings = settings,
                        selectedMode = mode,
                        unifiedModelList = models,
                        selectedUnifiedModel = findSelectedUnifiedModel(
                            mode = mode,
                            settings = settings,
                            models = models,
                        ),
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
                _uiState.update { state ->
                    val models = buildUnifiedModelList(
                        state = state,
                        settings = state.settings,
                        modelState = modelState,
                    )
                    state.copy(
                        modelState = modelState,
                        unifiedModelList = models,
                        selectedUnifiedModel = findSelectedUnifiedModel(
                            mode = state.selectedMode,
                            settings = state.settings,
                            models = models,
                        ),
                    )
                }
            }
        }
        repository.refreshModelState()
    }

    fun selectSection(section: AppSection) {
        _uiState.update { it.copy(currentSection = section, errorMessage = null, infoMessage = null) }
        if (section == AppSection.DICTIONARY && _uiState.value.dictionarySuggestions.isEmpty()) {
            refreshDictionarySuggestions()
        }
    }

    fun updateDictionaryQuery(value: String) {
        _uiState.update {
            it.copy(
                dictionaryQuery = value,
                dictionaryErrorMessage = null,
                dictionaryMessage = null,
            )
        }
        refreshDictionarySuggestions()
    }

    fun lookupDictionary(query: String = _uiState.value.dictionaryQuery) {
        val normalized = query.trim()
        val dictionary = dictionaryRepository
        if (dictionary == null) {
            _uiState.update { it.copy(dictionaryErrorMessage = "离线词典模块不可用") }
            return
        }
        if (normalized.isBlank()) {
            _uiState.update {
                it.copy(
                    dictionaryEntry = null,
                    dictionaryErrorMessage = "请输入要查询的英文单词",
                    dictionaryMessage = null,
                )
            }
            refreshDictionarySuggestions()
            return
        }

        _uiState.update {
            it.copy(
                dictionaryQuery = normalized,
                isDictionaryLoading = true,
                dictionaryErrorMessage = null,
                dictionaryMessage = null,
            )
        }
        viewModelScope.launch {
            runCatching { dictionary.lookup(normalized) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            dictionaryQuery = result.entry?.word ?: normalized,
                            dictionaryEntry = result.entry,
                            dictionarySuggestions = result.suggestions,
                            isDictionaryLoading = false,
                            dictionaryMessage = if (result.entry == null) "未找到该单词，试试相近词" else null,
                            dictionaryErrorMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isDictionaryLoading = false,
                            dictionaryEntry = null,
                            dictionaryErrorMessage = error.message ?: "词典查询失败",
                        )
                    }
                }
        }
    }

    fun chooseDictionarySuggestion(word: String) {
        _uiState.update { it.copy(dictionaryQuery = word) }
        lookupDictionary(word)
    }

    private fun refreshDictionarySuggestions() {
        val dictionary = dictionaryRepository ?: return
        val query = _uiState.value.dictionaryQuery
        viewModelScope.launch {
            runCatching { dictionary.suggest(query, limit = 8) }
                .onSuccess { suggestions ->
                    _uiState.update { it.copy(dictionarySuggestions = suggestions) }
                }
        }
    }

    fun updateSourceText(value: String) {
        _uiState.update { it.copy(sourceText = value, errorMessage = null) }
    }

    fun clearInput() {
        _uiState.update {
            it.copy(sourceText = "", translatedText = "", errorMessage = null, infoMessage = null)
        }
    }

    fun openImageTranslator(uri: String, sourceLabel: String = "图片翻译") {
        val recognizer = imageTextRecognizer
        if (recognizer == null) {
            _uiState.update {
                it.copy(
                    isImageTranslatorOpen = true,
                    imageSourceLabel = sourceLabel,
                    imageUri = uri,
                    imageRecognizedText = "",
                    imageTranslatedText = "",
                    imageErrorMessage = "图片识别模块不可用",
                    imageInfoMessage = null,
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isImageTranslatorOpen = true,
                imageSourceLabel = sourceLabel,
                imageUri = uri,
                imageRecognizedText = "",
                imageTranslatedText = "",
                isImageRecognizing = true,
                isImageTranslating = false,
                imageErrorMessage = null,
                imageInfoMessage = "正在识别图片文字",
            )
        }

        viewModelScope.launch {
            runCatching { recognizer.recognize(uri).trim() }
                .onSuccess { text ->
                    if (text.isBlank()) {
                        _uiState.update {
                            it.copy(
                                isImageRecognizing = false,
                                imageErrorMessage = "未识别到文字，请换一张更清晰的图片",
                                imageInfoMessage = null,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isImageRecognizing = false,
                                imageRecognizedText = text,
                                imageErrorMessage = null,
                                imageInfoMessage = "已识别图片文字，可编辑后翻译",
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isImageRecognizing = false,
                            imageErrorMessage = error.message ?: "图片文字识别失败",
                            imageInfoMessage = null,
                        )
                    }
                }
        }
    }

    fun updateImageRecognizedText(value: String) {
        _uiState.update {
            it.copy(
                imageRecognizedText = value,
                imageErrorMessage = null,
                imageInfoMessage = null,
            )
        }
    }

    fun translateImageText() {
        val snapshot = _uiState.value
        val text = snapshot.imageRecognizedText.trim()
        if (text.isBlank()) {
            _uiState.update { it.copy(imageErrorMessage = "请先识别或输入要翻译的图片文字") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isImageTranslating = true,
                    imageErrorMessage = null,
                    imageInfoMessage = null,
                )
            }

            runCatching {
                repository.translate(
                    request = TranslateRequest(
                        sourceText = text,
                        sourceLanguage = snapshot.sourceLanguage,
                        targetLanguage = snapshot.targetLanguage,
                    ),
                    mode = snapshot.selectedMode,
                )
            }.onSuccess { output ->
                _uiState.update {
                    it.copy(
                        imageTranslatedText = output.translatedText,
                        selectedMode = output.usedMode,
                        isImageTranslating = false,
                        imageInfoMessage = "已使用${output.usedMode.label}翻译图片文字",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isImageTranslating = false,
                        imageErrorMessage = error.message ?: "图片文字翻译失败，请稍后重试",
                    )
                }
            }
        }
    }

    fun bringImageTranslationToHome() {
        val snapshot = _uiState.value
        _uiState.update {
            it.copy(
                currentSection = AppSection.TRANSLATE,
                sourceText = snapshot.imageRecognizedText,
                translatedText = snapshot.imageTranslatedText,
                isImageTranslatorOpen = false,
                errorMessage = null,
                infoMessage = "已带入首页",
            )
        }
    }

    fun closeImageTranslator() {
        _uiState.update {
            it.copy(
                isImageTranslatorOpen = false,
                isImageRecognizing = false,
                isImageTranslating = false,
                imageErrorMessage = null,
                imageInfoMessage = null,
            )
        }
    }

    fun selectMode(mode: TranslationMode) {
        _uiState.update { it.copy(selectedMode = mode, errorMessage = null) }
    }

    fun openUnifiedModelPicker() {
        _uiState.update { state ->
            val models = buildUnifiedModelList(state)
            state.copy(
                isUnifiedModelPickerOpen = true,
                unifiedModelList = models,
                selectedUnifiedModel = findSelectedUnifiedModel(
                    mode = state.selectedMode,
                    settings = state.settings,
                    models = models,
                ),
            )
        }
    }

    fun closeUnifiedModelPicker() {
        _uiState.update { it.copy(isUnifiedModelPickerOpen = false) }
    }

    fun selectUnifiedModel(model: UnifiedModelOption) {
        val mode = modeForModel(model)
        when (model.type) {
            ModelType.OFFLINE -> {
                _uiState.update {
                    it.copy(
                        settings = it.settings.copy(defaultMode = mode),
                        selectedMode = mode,
                        selectedUnifiedModel = model,
                        isUnifiedModelPickerOpen = false,
                        infoMessage = "已切换到离线模型：${model.displayName}",
                    )
                }
            }
            ModelType.CLOUD -> {
                _uiState.update {
                    val provider = it.settings.selectedProvider.copy(modelName = model.displayName)
                    val settings = it.settings.withSelectedProvider(provider).copy(defaultMode = mode)
                    it.copy(
                        settings = settings,
                        selectedMode = mode,
                        selectedUnifiedModel = model,
                        isUnifiedModelPickerOpen = false,
                        infoMessage = "已切换到云端模型：${model.displayName}",
                    )
                }
            }
            ModelType.AUTO -> {
                _uiState.update {
                    it.copy(
                        settings = it.settings.copy(defaultMode = mode),
                        selectedMode = mode,
                        selectedUnifiedModel = model,
                        isUnifiedModelPickerOpen = false,
                        infoMessage = "已切换到自动模式",
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.updateDefaultMode(mode)
            if (model.type == ModelType.CLOUD) {
                repository.updateModelName(model.displayName)
            }
        }
    }

    fun updateDefaultUnifiedModel(model: UnifiedModelOption) {
        val mode = modeForModel(model)
        _uiState.update { state ->
            val settings = if (model.type == ModelType.CLOUD) {
                val provider = state.settings.selectedProvider.copy(modelName = model.displayName)
                state.settings.withSelectedProvider(provider).copy(defaultMode = mode)
            } else {
                state.settings.copy(defaultMode = mode)
            }
            val nextState = state.copy(
                settings = settings,
                selectedMode = mode,
                selectedUnifiedModel = model,
                infoMessage = "默认翻译模型已设为：${model.displayName}",
            )
            nextState.copy(unifiedModelList = buildUnifiedModelList(nextState))
        }
        viewModelScope.launch {
            repository.updateDefaultMode(mode)
            if (model.type == ModelType.CLOUD) {
                repository.updateModelName(model.displayName)
            }
        }
    }

    private fun rebuildUnifiedModelList() {
        _uiState.update { state ->
            val models = buildUnifiedModelList(state)
            state.copy(
                unifiedModelList = models,
                selectedUnifiedModel = findSelectedUnifiedModel(
                    mode = state.selectedMode,
                    settings = state.settings,
                    models = models,
                ),
            )
        }
    }

    private fun buildUnifiedModelList(
        state: TranslateUiState,
        settings: AppSettings = state.settings,
        modelState: ModelState = state.modelState,
    ): List<UnifiedModelOption> {
        val list = mutableListOf<UnifiedModelOption>()

        list.add(
            UnifiedModelOption(
                id = "offline-hymt",
                displayName = "HY-MT 1.5B",
                type = ModelType.OFFLINE,
                providerName = "本地推理",
                isAvailable = modelState.isAvailable,
                subtitle = if (modelState.isAvailable) "本地可用 · 无需网络" else "未下载 · 约 1.13GB",
            ),
        )

        val currentModel = settings.modelName
        if (currentModel.isNotBlank()) {
            list.add(
                UnifiedModelOption(
                    id = "cloud-current-$currentModel",
                    displayName = currentModel,
                    type = ModelType.CLOUD,
                    providerName = settings.selectedProvider.name,
                    subtitle = "${settings.selectedProvider.name} · 云端模型",
                ),
            )
        }

        settings.customModelNames
            .filter { it.isNotBlank() && it != currentModel }
            .forEach { model ->
                list.add(
                    UnifiedModelOption(
                        id = "cloud-custom-$model",
                        displayName = model,
                        type = ModelType.CLOUD,
                        providerName = settings.selectedProvider.name,
                        subtitle = "${settings.selectedProvider.name} · 自定义",
                    ),
                )
            }

        state.availableModels
            .filter { it.isNotBlank() && it != currentModel && it !in settings.customModelNames }
            .forEach { model ->
                list.add(
                    UnifiedModelOption(
                        id = "cloud-fetched-$model",
                        displayName = model,
                        type = ModelType.CLOUD,
                        providerName = settings.selectedProvider.name,
                        subtitle = "${settings.selectedProvider.name} · 接口返回",
                    ),
                )
            }

        list.add(
            UnifiedModelOption(
                id = "auto",
                displayName = "自动选择",
                type = ModelType.AUTO,
                providerName = "智能切换",
                subtitle = "优先云端，失败回退离线",
            ),
        )

        return list
    }

    private fun findSelectedUnifiedModel(
        mode: TranslationMode,
        settings: AppSettings,
        models: List<UnifiedModelOption>,
    ): UnifiedModelOption? {
        return when (mode) {
            TranslationMode.OFFLINE -> models.firstOrNull { it.type == ModelType.OFFLINE }
            TranslationMode.AUTO -> models.firstOrNull { it.type == ModelType.AUTO }
            TranslationMode.CLOUD -> models.firstOrNull {
                it.type == ModelType.CLOUD && it.displayName == settings.modelName
            } ?: models.firstOrNull { it.type == ModelType.CLOUD }
        }
    }

    private fun modeForModel(model: UnifiedModelOption): TranslationMode {
        return when (model.type) {
            ModelType.CLOUD -> TranslationMode.CLOUD
            ModelType.OFFLINE -> TranslationMode.OFFLINE
            ModelType.AUTO -> TranslationMode.AUTO
        }
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
        _uiState.update { state ->
            val settings = state.settings.copy(defaultMode = value)
            val models = buildUnifiedModelList(state, settings = settings)
            state.copy(
                settings = settings,
                selectedMode = value,
                unifiedModelList = models,
                selectedUnifiedModel = findSelectedUnifiedModel(
                    mode = value,
                    settings = settings,
                    models = models,
                ),
            )
        }
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
                    _uiState.update { state ->
                        val nextState = state.copy(
                            availableModels = models,
                            isFetchingModels = false,
                            modelFetchMessage = "已获取 ${models.size} 个模型",
                        )
                        val unifiedModels = buildUnifiedModelList(nextState)
                        nextState.copy(
                            unifiedModelList = unifiedModels,
                            selectedUnifiedModel = findSelectedUnifiedModel(
                                mode = nextState.selectedMode,
                                settings = nextState.settings,
                                models = unifiedModels,
                            ),
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
