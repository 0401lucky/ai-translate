package com.mxwis.aitranslate.ui

import com.mxwis.aitranslate.data.history.TranslationHistoryEntity
import com.mxwis.aitranslate.data.dictionary.DictionaryEntry
import com.mxwis.aitranslate.data.dictionary.DictionaryInflection
import com.mxwis.aitranslate.data.dictionary.DictionaryLookupResult
import com.mxwis.aitranslate.data.dictionary.DictionaryRepositoryContract
import com.mxwis.aitranslate.data.dictionary.DictionaryWordSummary
import com.mxwis.aitranslate.data.model.ModelState
import com.mxwis.aitranslate.data.ocr.ImageTextRecognizerContract
import com.mxwis.aitranslate.data.settings.AppSettings
import com.mxwis.aitranslate.data.settings.CloudProviderSettings
import com.mxwis.aitranslate.data.translation.TranslationRepositoryContract
import com.mxwis.aitranslate.data.update.AppUpdateCheckResult
import com.mxwis.aitranslate.data.update.AppUpdateRelease
import com.mxwis.aitranslate.domain.ModelType
import com.mxwis.aitranslate.domain.TranslateOutput
import com.mxwis.aitranslate.domain.TranslateRequest
import com.mxwis.aitranslate.domain.TranslationMode
import com.mxwis.aitranslate.domain.UnifiedModelOption
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class TranslateViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `确认剪贴板后会打开快速翻译并标记自动翻译`() = runTest {
        val viewModel = TranslateViewModel(FakeTranslationRepository())

        viewModel.offerClipboardQuickTranslate("  hello  ")
        viewModel.acceptClipboardQuickTranslate()

        val state = viewModel.uiState.value
        assertTrue(state.isMiniTranslatorOpen)
        assertEquals("剪贴板", state.miniSourceLabel)
        assertEquals("hello", state.miniSourceText)
        assertTrue(state.shouldAutoTranslateMini)
        assertFalse(state.isClipboardSuggestionOpen)
    }

    @Test
    fun `检查应用更新发现新版本时写入下载信息`() = runTest {
        val downloadedFile = File("build/tmp/test-update.apk")
        val repository = FakeTranslationRepository(
            appUpdateResult = AppUpdateCheckResult.Available(
                AppUpdateRelease(
                    versionCode = 2,
                    versionName = "1.1.0",
                    required = false,
                    apkUrl = "https://download.204152.xyz/releases/app-release.apk",
                    sha256 = "abc",
                    sizeBytes = 1024L,
                    notes = listOf("新增应用内更新入口"),
                ),
            ),
        )
        val viewModel = TranslateViewModel(repository, currentVersionCode = 1)

        viewModel.checkAppUpdate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCheckingAppUpdate)
        assertEquals("发现新版本 1.1.0", state.appUpdateMessage)
        assertEquals("https://download.204152.xyz/releases/app-release.apk", state.availableAppUpdate?.apkUrl)
    }

    @Test
    fun `下载应用更新成功后标记待安装路径`() = runTest {
        val downloadedFile = File("build/tmp/test-update.apk")
        val repository = FakeTranslationRepository(
            appUpdateResult = AppUpdateCheckResult.Available(
                AppUpdateRelease(
                    versionCode = 2,
                    versionName = "1.0.1",
                    required = false,
                    apkUrl = "https://download.204152.xyz/releases/ai-translate-1.0.1-debug.apk",
                    sha256 = "abc",
                    sizeBytes = 100L,
                    notes = emptyList(),
                ),
            ),
            downloadedUpdateFile = downloadedFile,
        )
        val viewModel = TranslateViewModel(repository, currentVersionCode = 1)

        viewModel.checkAppUpdate()
        advanceUntilIdle()
        viewModel.downloadAppUpdate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDownloadingAppUpdate)
        assertEquals(downloadedFile.absolutePath, state.downloadedAppUpdatePath)
        assertEquals(downloadedFile.absolutePath, state.pendingAppUpdateInstallPath)
    }

    @Test
    fun `选择云端统一模型后会同步默认模式并按云端翻译`() = runTest {
        val repository = FakeTranslationRepository(
            initialSettings = AppSettings(
                modelName = "gpt-4o-mini",
                defaultMode = TranslationMode.OFFLINE,
            ),
        )
        val viewModel = TranslateViewModel(repository)
        advanceUntilIdle()

        viewModel.selectUnifiedModel(
            UnifiedModelOption(
                id = "cloud-current-gpt-4o-mini",
                displayName = "gpt-4o-mini",
                type = ModelType.CLOUD,
                providerName = "OpenAI",
                subtitle = "OpenAI · 云端模型",
            ),
        )
        advanceUntilIdle()
        viewModel.updateSourceText("hello")
        viewModel.translate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(TranslationMode.CLOUD, state.selectedMode)
        assertEquals(TranslationMode.CLOUD, state.settings.defaultMode)
        assertEquals(ModelType.CLOUD, state.selectedUnifiedModel?.type)
        assertEquals(TranslationMode.CLOUD, repository.updatedDefaultMode)
        assertEquals("gpt-4o-mini", repository.updatedModelName)
        assertEquals(TranslationMode.CLOUD, repository.lastTranslateMode)
    }

    @Test
    fun `默认离线时统一模型状态不会误指向云端模型名`() = runTest {
        val viewModel = TranslateViewModel(
            FakeTranslationRepository(
                initialSettings = AppSettings(
                    modelName = "gpt-4o-mini",
                    defaultMode = TranslationMode.OFFLINE,
                ),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(TranslationMode.OFFLINE, state.selectedMode)
        assertEquals(ModelType.OFFLINE, state.selectedUnifiedModel?.type)
        assertEquals("HY-MT 1.5B", state.selectedUnifiedModel?.displayName)
    }

    @Test
    fun `图片OCR成功后写入识别文本`() = runTest {
        val viewModel = TranslateViewModel(
            repository = FakeTranslationRepository(),
            imageTextRecognizer = FakeImageTextRecognizer("hello\n你好"),
        )

        viewModel.openImageTranslator("content://test/image", "相册导入")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isImageTranslatorOpen)
        assertFalse(state.isImageRecognizing)
        assertEquals("hello\n你好", state.imageRecognizedText)
        assertEquals("已识别图片文字，可编辑后翻译", state.imageInfoMessage)
    }

    @Test
    fun `图片OCR为空时显示错误且不会触发翻译`() = runTest {
        val repository = FakeTranslationRepository()
        val viewModel = TranslateViewModel(
            repository = repository,
            imageTextRecognizer = FakeImageTextRecognizer("   "),
        )

        viewModel.openImageTranslator("content://test/blank", "拍照翻译")
        advanceUntilIdle()
        viewModel.translateImageText()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("请先识别或输入要翻译的图片文字", state.imageErrorMessage)
        assertEquals(null, repository.lastTranslateMode)
    }

    @Test
    fun `图片翻译使用当前语言和模型模式`() = runTest {
        val repository = FakeTranslationRepository(
            initialSettings = AppSettings(defaultMode = TranslationMode.CLOUD),
        )
        val viewModel = TranslateViewModel(
            repository = repository,
            imageTextRecognizer = FakeImageTextRecognizer("hi"),
        )
        advanceUntilIdle()

        viewModel.openImageTranslator("content://test/photo", "相册导入")
        advanceUntilIdle()
        viewModel.translateImageText()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("你好", state.imageTranslatedText)
        assertEquals(TranslationMode.CLOUD, repository.lastTranslateMode)
        assertEquals("hi", repository.lastTranslateRequest?.sourceText)
        assertEquals("中文（简体）", repository.lastTranslateRequest?.targetLanguage?.displayName)
    }

    @Test
    fun `图片翻译带入首页会同步原文和译文`() = runTest {
        val viewModel = TranslateViewModel(
            repository = FakeTranslationRepository(),
            imageTextRecognizer = FakeImageTextRecognizer("hi"),
        )

        viewModel.openImageTranslator("content://test/photo", "相册导入")
        advanceUntilIdle()
        viewModel.translateImageText()
        advanceUntilIdle()
        viewModel.bringImageTranslationToHome()

        val state = viewModel.uiState.value
        assertFalse(state.isImageTranslatorOpen)
        assertEquals("hi", state.sourceText)
        assertEquals("你好", state.translatedText)
        assertEquals("已带入首页", state.infoMessage)
    }

    @Test
    fun `词典精确查询会写入单词详情`() = runTest {
        val viewModel = TranslateViewModel(
            repository = FakeTranslationRepository(),
            dictionaryRepository = FakeDictionaryRepository(),
        )

        viewModel.updateDictionaryQuery("reason")
        viewModel.lookupDictionary()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("reason", state.dictionaryEntry?.word)
        assertEquals("n. 原因；理由；理性", state.dictionaryEntry?.translations?.first())
        assertEquals("reasons", state.dictionaryEntry?.inflections?.first()?.value)
        assertEquals(null, state.dictionaryErrorMessage)
    }

    @Test
    fun `词典查询会忽略大小写`() = runTest {
        val viewModel = TranslateViewModel(
            repository = FakeTranslationRepository(),
            dictionaryRepository = FakeDictionaryRepository(),
        )

        viewModel.updateDictionaryQuery("Reason")
        viewModel.lookupDictionary()
        advanceUntilIdle()

        assertEquals("reason", viewModel.uiState.value.dictionaryEntry?.word)
    }

    @Test
    fun `词典未命中时显示建议词`() = runTest {
        val viewModel = TranslateViewModel(
            repository = FakeTranslationRepository(),
            dictionaryRepository = FakeDictionaryRepository(),
        )

        viewModel.updateDictionaryQuery("rea")
        viewModel.lookupDictionary()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(null, state.dictionaryEntry)
        assertEquals("未找到该单词，试试相近词", state.dictionaryMessage)
        assertEquals(listOf("reason", "real"), state.dictionarySuggestions.map { it.word })
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeTranslationRepository(
    initialSettings: AppSettings = AppSettings(),
    private val appUpdateResult: AppUpdateCheckResult = AppUpdateCheckResult.UpToDate("1.0.0"),
    private val downloadedUpdateFile: File = File("build/tmp/fake-update.apk"),
) : TranslationRepositoryContract {
    private val settingsFlow = MutableStateFlow(initialSettings)
    override val settings: Flow<AppSettings> = settingsFlow
    override val history: Flow<List<TranslationHistoryEntity>> = MutableStateFlow(emptyList())
    override val modelState: Flow<ModelState> = MutableStateFlow(ModelState())
    var updatedDefaultMode: TranslationMode? = null
        private set
    var updatedModelName: String? = null
        private set
    var lastTranslateMode: TranslationMode? = null
        private set
    var lastTranslateRequest: TranslateRequest? = null
        private set

    override suspend fun updateBaseUrl(value: String) = Unit
    override suspend fun updateApiKey(value: String) = Unit
    override suspend fun updateModelName(value: String) {
        updatedModelName = value
        settingsFlow.value = settingsFlow.value.copy(modelName = value)
    }
    override suspend fun updateCustomModelNames(values: List<String>) = Unit
    override suspend fun updateProviderName(value: String) = Unit
    override suspend fun selectCloudProvider(providerId: String) = Unit
    override suspend fun addCloudProvider(provider: CloudProviderSettings) = Unit
    override suspend fun updateDefaultMode(value: TranslationMode) {
        updatedDefaultMode = value
        settingsFlow.value = settingsFlow.value.copy(defaultMode = value)
    }
    override suspend fun fetchCloudModels(settings: AppSettings): List<String> = emptyList()
    override suspend fun checkAppUpdate(currentVersionCode: Int): AppUpdateCheckResult = appUpdateResult
    override suspend fun downloadAppUpdate(
        release: AppUpdateRelease,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): File {
        onProgress(release.sizeBytes, release.sizeBytes)
        return downloadedUpdateFile
    }
    override suspend fun translate(request: TranslateRequest, mode: TranslationMode): TranslateOutput {
        lastTranslateMode = mode
        lastTranslateRequest = request
        return TranslateOutput(translatedText = "你好", usedMode = mode)
    }
    override suspend fun downloadModel() = Unit
    override suspend fun deleteModel() = Unit
    override fun refreshModelState() = Unit
    override suspend fun deleteHistory(entity: TranslationHistoryEntity) = Unit
    override suspend fun clearHistory() = Unit
}

private class FakeImageTextRecognizer(
    private val text: String,
) : ImageTextRecognizerContract {
    override suspend fun recognize(uriString: String): String = text
}

private class FakeDictionaryRepository : DictionaryRepositoryContract {
    private val entries = listOf(
        DictionaryEntry(
            word = "reason",
            phonetic = "'ri:zn",
            definitions = listOf("a cause, explanation, or justification"),
            translations = listOf("n. 原因；理由；理性"),
            partOfSpeech = "n.",
            tags = listOf("CET4", "考研"),
            collins = 5,
            oxford = true,
            bncRank = 310,
            frequencyRank = 359,
            inflections = listOf(DictionaryInflection("复数", "reasons")),
        ),
        DictionaryEntry(
            word = "real",
            phonetic = "ri:l",
            definitions = listOf("being or occurring in fact"),
            translations = listOf("a. 真实的；实际的"),
            partOfSpeech = "a.",
            tags = listOf("CET4"),
            collins = 5,
            oxford = true,
            bncRank = 200,
            frequencyRank = 180,
            inflections = emptyList(),
        ),
    )

    override suspend fun lookup(query: String): DictionaryLookupResult {
        val normalized = query.trim().lowercase()
        val entry = entries.firstOrNull { it.word == normalized }
        return DictionaryLookupResult(
            query = normalized,
            entry = entry,
            suggestions = suggest(normalized, 8).filterNot { it.word == entry?.word },
        )
    }

    override suspend fun suggest(prefix: String, limit: Int): List<DictionaryWordSummary> {
        val normalized = prefix.trim().lowercase()
        return entries
            .filter { normalized.isBlank() || it.word.startsWith(normalized) }
            .take(limit)
            .map { DictionaryWordSummary(it.word, it.translations.first()) }
    }
}
