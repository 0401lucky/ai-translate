package com.mxwis.aitranslate.ui

import com.mxwis.aitranslate.data.history.TranslationHistoryEntity
import com.mxwis.aitranslate.data.model.ModelState
import com.mxwis.aitranslate.data.settings.AppSettings
import com.mxwis.aitranslate.data.settings.CloudProviderSettings
import com.mxwis.aitranslate.data.translation.TranslationRepositoryContract
import com.mxwis.aitranslate.data.update.AppUpdateCheckResult
import com.mxwis.aitranslate.data.update.AppUpdateRelease
import com.mxwis.aitranslate.domain.TranslateOutput
import com.mxwis.aitranslate.domain.TranslateRequest
import com.mxwis.aitranslate.domain.TranslationMode
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
    private val appUpdateResult: AppUpdateCheckResult = AppUpdateCheckResult.UpToDate("1.0.0"),
) : TranslationRepositoryContract {
    override val settings: Flow<AppSettings> = MutableStateFlow(AppSettings())
    override val history: Flow<List<TranslationHistoryEntity>> = MutableStateFlow(emptyList())
    override val modelState: Flow<ModelState> = MutableStateFlow(ModelState())

    override suspend fun updateBaseUrl(value: String) = Unit
    override suspend fun updateApiKey(value: String) = Unit
    override suspend fun updateModelName(value: String) = Unit
    override suspend fun updateCustomModelNames(values: List<String>) = Unit
    override suspend fun updateProviderName(value: String) = Unit
    override suspend fun selectCloudProvider(providerId: String) = Unit
    override suspend fun addCloudProvider(provider: CloudProviderSettings) = Unit
    override suspend fun updateDefaultMode(value: TranslationMode) = Unit
    override suspend fun fetchCloudModels(settings: AppSettings): List<String> = emptyList()
    override suspend fun checkAppUpdate(currentVersionCode: Int): AppUpdateCheckResult = appUpdateResult
    override suspend fun translate(request: TranslateRequest, mode: TranslationMode): TranslateOutput {
        return TranslateOutput(translatedText = "你好", usedMode = mode)
    }
    override suspend fun downloadModel() = Unit
    override suspend fun deleteModel() = Unit
    override fun refreshModelState() = Unit
    override suspend fun deleteHistory(entity: TranslationHistoryEntity) = Unit
    override suspend fun clearHistory() = Unit
}
