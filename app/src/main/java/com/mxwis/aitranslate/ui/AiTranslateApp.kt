package com.mxwis.aitranslate.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.mxwis.aitranslate.BuildConfig
import com.mxwis.aitranslate.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mxwis.aitranslate.data.history.TranslationHistoryEntity
import com.mxwis.aitranslate.data.settings.CloudProviderSettings
import com.mxwis.aitranslate.domain.LanguageOption
import com.mxwis.aitranslate.domain.Languages
import com.mxwis.aitranslate.domain.TranslationMode
import com.mxwis.aitranslate.overlay.FloatingTranslateService
import com.mxwis.aitranslate.speech.TtsRuntimeState
import com.mxwis.aitranslate.speech.TtsSpeakResult
import com.mxwis.aitranslate.speech.TtsStatus
import com.mxwis.aitranslate.ui.theme.AiTranslateTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AiTranslateApp(viewModel: TranslateViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AiTranslateTheme {
        AiTranslateContent(
            state = uiState,
            onSectionSelected = viewModel::selectSection,
            onSourceTextChanged = viewModel::updateSourceText,
            onClearInput = viewModel::clearInput,
            onModeSelected = viewModel::selectMode,
            onOpenLanguagePicker = viewModel::openLanguagePicker,
            onCloseLanguagePicker = viewModel::closeLanguagePicker,
            onChooseLanguage = viewModel::chooseLanguage,
            onSwapLanguages = viewModel::swapLanguages,
            onTranslate = viewModel::translate,
            onBaseUrlChanged = viewModel::updateBaseUrl,
            onApiKeyChanged = viewModel::updateApiKey,
            onProviderNameChanged = viewModel::updateProviderName,
            onSelectCloudProvider = viewModel::selectCloudProvider,
            onAddCloudProvider = viewModel::addCloudProvider,
            onFetchCloudModels = viewModel::fetchCloudModels,
            onOpenModelPicker = viewModel::openModelPicker,
            onCloseModelPicker = viewModel::closeModelPicker,
            onModelSearchChanged = viewModel::updateModelSearchQuery,
            onModelToAddChanged = viewModel::updateModelToAdd,
            onSelectCloudModel = viewModel::selectCloudModel,
            onAddCustomModel = viewModel::addCustomModel,
            onDefaultModeChanged = viewModel::updateDefaultMode,
            onDownloadModel = viewModel::downloadModel,
            onDeleteModel = viewModel::deleteModel,
            onDeleteHistory = viewModel::deleteHistory,
            onClearHistory = viewModel::clearHistory,
            onOpenHistoryDetail = viewModel::openHistoryDetail,
            onCloseHistoryDetail = viewModel::closeHistoryDetail,
            onCloseMiniTranslator = viewModel::closeMiniTranslator,
            onTranslateMini = viewModel::translateMini,
            onConsumeMiniAutoTranslateRequest = viewModel::consumeMiniAutoTranslateRequest,
            onOpenFullTranslateFromMini = viewModel::openFullTranslateFromMini,
            onAcceptClipboardQuickTranslate = viewModel::acceptClipboardQuickTranslate,
            onDismissClipboardQuickTranslate = viewModel::dismissClipboardQuickTranslate,
            onCheckAppUpdate = viewModel::checkAppUpdate,
            onDownloadAppUpdate = viewModel::downloadAppUpdate,
            onConsumeAppUpdateInstallRequest = viewModel::consumeAppUpdateInstallRequest,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiTranslateContent(
    state: TranslateUiState,
    onSectionSelected: (AppSection) -> Unit,
    onSourceTextChanged: (String) -> Unit,
    onClearInput: () -> Unit,
    onModeSelected: (TranslationMode) -> Unit,
    onOpenLanguagePicker: (LanguagePickerTarget) -> Unit,
    onCloseLanguagePicker: () -> Unit,
    onChooseLanguage: (LanguageOption) -> Unit,
    onSwapLanguages: () -> Unit,
    onTranslate: () -> Unit,
    onBaseUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onProviderNameChanged: (String) -> Unit,
    onSelectCloudProvider: (String) -> Unit,
    onAddCloudProvider: () -> Unit,
    onFetchCloudModels: () -> Unit,
    onOpenModelPicker: () -> Unit,
    onCloseModelPicker: () -> Unit,
    onModelSearchChanged: (String) -> Unit,
    onModelToAddChanged: (String) -> Unit,
    onSelectCloudModel: (String) -> Unit,
    onAddCustomModel: () -> Unit,
    onDefaultModeChanged: (TranslationMode) -> Unit,
    onDownloadModel: () -> Unit,
    onDeleteModel: () -> Unit,
    onDeleteHistory: (TranslationHistoryEntity) -> Unit,
    onClearHistory: () -> Unit,
    onOpenHistoryDetail: (TranslationHistoryEntity) -> Unit,
    onCloseHistoryDetail: () -> Unit,
    onCloseMiniTranslator: () -> Unit,
    onTranslateMini: () -> Unit,
    onConsumeMiniAutoTranslateRequest: () -> Unit,
    onOpenFullTranslateFromMini: () -> Unit,
    onAcceptClipboardQuickTranslate: () -> Unit,
    onDismissClipboardQuickTranslate: () -> Unit,
    onCheckAppUpdate: () -> Unit,
    onDownloadAppUpdate: () -> Unit,
    onConsumeAppUpdateInstallRequest: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val speaker = rememberTextSpeaker()
    val ttsState by speaker.state.collectAsStateWithLifecycle()

    fun speakText(text: String, language: LanguageOption) {
        showTtsSpeakResult(context, speaker.speak(text, language))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI 翻译",
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            AppBottomBar(
                current = state.currentSection,
                onSelected = onSectionSelected,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state.currentSection) {
                AppSection.TRANSLATE -> TranslateScreen(
                    state = state,
                    onSourceTextChanged = onSourceTextChanged,
                    onClearInput = onClearInput,
                    onModeSelected = onModeSelected,
                    onOpenLanguagePicker = onOpenLanguagePicker,
                    onSwapLanguages = onSwapLanguages,
                    onTranslate = onTranslate,
                    onSpeakSource = { speakText(state.sourceText, state.sourceLanguage) },
                    onSpeakTranslation = { speakText(state.translatedText, state.targetLanguage) },
                )
                AppSection.HISTORY -> HistoryScreen(
                    histories = state.histories,
                    onDeleteHistory = onDeleteHistory,
                    onClearHistory = onClearHistory,
                    onOpenHistoryDetail = onOpenHistoryDetail,
                )
                AppSection.MODEL -> ModelScreen(
                    state = state,
                    onDownloadModel = onDownloadModel,
                    onDeleteModel = onDeleteModel,
                )
                AppSection.SETTINGS -> SettingsScreen(
                    state = state,
                    onBaseUrlChanged = onBaseUrlChanged,
                    onApiKeyChanged = onApiKeyChanged,
                    onProviderNameChanged = onProviderNameChanged,
                    onSelectCloudProvider = onSelectCloudProvider,
                    onAddCloudProvider = onAddCloudProvider,
                    onOpenModelPicker = onOpenModelPicker,
                    onOpenModelManager = { onSectionSelected(AppSection.MODEL) },
                    onDefaultModeChanged = onDefaultModeChanged,
                    onClearHistory = onClearHistory,
                    onCheckAppUpdate = onCheckAppUpdate,
                    onDownloadAppUpdate = onDownloadAppUpdate,
                    ttsState = ttsState,
                    onRefreshTts = speaker::refresh,
                    onInstallTtsData = {
                        openFirstResolvableIntent(
                            context = context,
                            intents = listOf(speaker.installVoiceDataIntent()),
                            failureMessage = "无法打开语音包安装页",
                        )
                    },
                    onOpenTtsSettings = {
                        openFirstResolvableIntent(
                            context = context,
                            intents = speaker.systemSettingsIntents(),
                            failureMessage = "无法打开系统朗读设置",
                        )
                    },
                    onTestTts = {
                        speakText("你好，欢迎使用 AI 翻译文本朗读。", Languages.supported.first())
                    },
                )
            }
        }
    }

    state.pendingAppUpdateInstallPath?.let { apkPath ->
        LaunchedEffect(apkPath) {
            installAppUpdate(context, apkPath)
            onConsumeAppUpdateInstallRequest()
        }
    }

    if (state.languagePickerTarget != null) {
        ModalBottomSheet(onDismissRequest = onCloseLanguagePicker) {
            LanguagePickerSheet(
                target = state.languagePickerTarget,
                sourceLanguage = state.sourceLanguage,
                targetLanguage = state.targetLanguage,
                onChooseLanguage = onChooseLanguage,
            )
        }
    }

    if (state.isModelPickerOpen) {
        ModalBottomSheet(onDismissRequest = onCloseModelPicker) {
            ModelPickerSheet(
                state = state,
                onSearchChanged = onModelSearchChanged,
                onModelToAddChanged = onModelToAddChanged,
                onSelectModel = onSelectCloudModel,
                onAddCustomModel = onAddCustomModel,
                onFetchCloudModels = onFetchCloudModels,
            )
        }
    }

    if (state.selectedHistory != null) {
        ModalBottomSheet(onDismissRequest = onCloseHistoryDetail) {
            HistoryDetailSheet(
                entity = state.selectedHistory,
                onCopyTranslation = { text ->
                    clipboard.setText(AnnotatedString(text))
                    Toast.makeText(context, "已复制译文", Toast.LENGTH_SHORT).show()
                },
                onSpeakSource = {
                    speakText(
                        state.selectedHistory.sourceText,
                        Languages.byDisplayNameOrAuto(state.selectedHistory.sourceLanguage),
                    )
                },
                onSpeakTranslation = {
                    speakText(
                        state.selectedHistory.translatedText,
                        Languages.byDisplayNameOrAuto(state.selectedHistory.targetLanguage),
                    )
                },
                onDelete = {
                    onDeleteHistory(state.selectedHistory)
                    onCloseHistoryDetail()
                },
            )
        }
    }

    if (state.isClipboardSuggestionOpen) {
        Dialog(onDismissRequest = onDismissClipboardQuickTranslate) {
            ClipboardQuickTranslateCard(
                clipboardText = state.clipboardCandidateText,
                onAccept = onAcceptClipboardQuickTranslate,
                onDismiss = onDismissClipboardQuickTranslate,
            )
        }
    }

    if (state.isMiniTranslatorOpen) {
        if (state.shouldAutoTranslateMini && !state.isMiniTranslating && state.miniSourceText.isNotBlank()) {
            LaunchedEffect(state.miniSourceText, state.shouldAutoTranslateMini) {
                onConsumeMiniAutoTranslateRequest()
                onTranslateMini()
            }
        }
        Dialog(onDismissRequest = onCloseMiniTranslator) {
            MiniTranslateCard(
                state = state,
                onModeSelected = onModeSelected,
                onTranslate = onTranslateMini,
                onCopyTranslation = { text ->
                    clipboard.setText(AnnotatedString(text))
                    Toast.makeText(context, "已复制译文", Toast.LENGTH_SHORT).show()
                },
                onSpeakSource = { speakText(state.miniSourceText, state.sourceLanguage) },
                onSpeakTranslation = { speakText(state.miniTranslatedText, state.targetLanguage) },
                onOpenFullTranslate = onOpenFullTranslateFromMini,
                onClose = onCloseMiniTranslator,
            )
        }
    }
}

@Composable
private fun AppBottomBar(
    current: AppSection,
    onSelected: (AppSection) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = current == AppSection.TRANSLATE,
            onClick = { onSelected(AppSection.TRANSLATE) },
            icon = { Icon(Icons.Default.Translate, contentDescription = null) },
            label = { Text("翻译") },
        )
        NavigationBarItem(
            selected = current == AppSection.HISTORY,
            onClick = { onSelected(AppSection.HISTORY) },
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("历史") },
        )
        NavigationBarItem(
            selected = current == AppSection.MODEL,
            onClick = { onSelected(AppSection.MODEL) },
            icon = { Icon(Icons.Default.Storage, contentDescription = null) },
            label = { Text("模型") },
        )
        NavigationBarItem(
            selected = current == AppSection.SETTINGS,
            onClick = { onSelected(AppSection.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("设置") },
        )
    }
}

@Composable
private fun TranslateScreen(
    state: TranslateUiState,
    onSourceTextChanged: (String) -> Unit,
    onClearInput: () -> Unit,
    onModeSelected: (TranslationMode) -> Unit,
    onOpenLanguagePicker: (LanguagePickerTarget) -> Unit,
    onSwapLanguages: () -> Unit,
    onTranslate: () -> Unit,
    onSpeakSource: () -> Unit,
    onSpeakTranslation: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ModeSelector(
                selected = state.selectedMode,
                onSelected = onModeSelected,
            )
        }
        item {
            LanguageRow(
                source = state.sourceLanguage,
                target = state.targetLanguage,
                onOpenSource = { onOpenLanguagePicker(LanguagePickerTarget.SOURCE) },
                onOpenTarget = { onOpenLanguagePicker(LanguagePickerTarget.TARGET) },
                onSwap = onSwapLanguages,
            )
        }
        item {
            OutlinedCard(shape = RoundedCornerShape(8.dp)) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("输入原文", style = MaterialTheme.typography.titleSmall)
                        Row {
                            IconButton(
                                onClick = onSpeakSource,
                                enabled = state.sourceText.isNotBlank(),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "朗读原文")
                            }
                            IconButton(
                                onClick = onClearInput,
                                enabled = state.sourceText.isNotEmpty(),
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "清空")
                            }
                        }
                    }
                    OutlinedTextField(
                        value = state.sourceText,
                        onValueChange = onSourceTextChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        placeholder = { Text("在此输入或粘贴要翻译的内容...") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                        maxLines = 8,
                    )
                    Text(
                        text = "${state.sourceText.length}/5000",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }
        }
        item {
            Button(
                onClick = onTranslate,
                enabled = !state.isTranslating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Translate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.isTranslating) "正在翻译" else "翻译")
            }
        }
        item {
            MessageBanner(
                error = state.errorMessage,
                info = state.infoMessage,
            )
        }
        item {
            OutlinedCard(shape = RoundedCornerShape(8.dp)) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("译文", style = MaterialTheme.typography.titleSmall)
                        Row {
                            IconButton(
                                onClick = onSpeakTranslation,
                                enabled = state.translatedText.isNotBlank(),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "朗读译文")
                            }
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(state.translatedText))
                                    Toast.makeText(context, "已复制译文", Toast.LENGTH_SHORT).show()
                                },
                                enabled = state.translatedText.isNotBlank(),
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                            }
                        }
                    }
                    if (state.isTranslating) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = "翻译中，请稍候...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = state.translatedText.ifBlank { "译文将显示在这里" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (state.translatedText.isBlank()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.height(140.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniTranslateCard(
    state: TranslateUiState,
    onModeSelected: (TranslationMode) -> Unit,
    onTranslate: () -> Unit,
    onCopyTranslation: (String) -> Unit,
    onSpeakSource: () -> Unit,
    onSpeakTranslation: () -> Unit,
    onOpenFullTranslate: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .widthIn(max = 430.dp)
            .fillMaxWidth(0.92f),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "快速翻译",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "来自${state.miniSourceLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Clear, contentDescription = "关闭")
                }
            }

            FloatingTextSection(
                title = "原文",
                text = state.miniSourceText,
                placeholder = "",
                maxLines = 5,
                action = {
                    IconButton(
                        onClick = onSpeakSource,
                        enabled = state.miniSourceText.isNotBlank(),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "朗读原文")
                    }
                },
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Translate, contentDescription = null)
                    Text(
                        text = "${state.sourceLanguage.displayName} → ${state.targetLanguage.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Text("翻译模式", fontWeight = FontWeight.Bold)
            ModeSelector(
                selected = state.selectedMode,
                onSelected = onModeSelected,
            )

            if (state.miniErrorMessage != null || state.miniInfoMessage != null) {
                MessageBanner(
                    error = state.miniErrorMessage,
                    info = state.miniInfoMessage,
                )
            }

            Button(
                onClick = onTranslate,
                enabled = !state.isMiniTranslating && state.miniSourceText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Translate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.isMiniTranslating) "正在翻译" else "翻译")
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "译文",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row {
                            IconButton(
                                onClick = onSpeakTranslation,
                                enabled = state.miniTranslatedText.isNotBlank(),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "朗读译文")
                            }
                            IconButton(
                                onClick = { onCopyTranslation(state.miniTranslatedText) },
                                enabled = state.miniTranslatedText.isNotBlank(),
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "复制译文")
                            }
                        }
                    }
                    if (state.isMiniTranslating) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = "翻译中，请稍候...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = state.miniTranslatedText.ifBlank { "译文会显示在这里" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (state.miniTranslatedText.isBlank()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.heightIn(min = 80.dp),
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onOpenFullTranslate,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("转到完整翻译页")
            }
        }
    }
}

@Composable
private fun ClipboardQuickTranslateCard(
    clipboardText: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .widthIn(max = 410.dp)
            .fillMaxWidth(0.92f),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "剪贴板快捷翻译",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "检测到剪贴板文本",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Clear, contentDescription = "忽略")
                }
            }

            FloatingTextSection(
                title = "剪贴板内容",
                text = clipboardText,
                placeholder = "",
                maxLines = 5,
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text(
                        text = "仅在前台读取，确认后才翻译",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("忽略")
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Default.Translate, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("快速翻译")
                }
            }
        }
    }
}

@Composable
private fun FloatingTextSection(
    title: String,
    text: String,
    placeholder: String,
    maxLines: Int,
    action: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                action?.invoke()
            }
            Text(
                text = text.ifBlank { placeholder },
                style = MaterialTheme.typography.bodyLarge,
                color = if (text.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ModeSelector(
    selected: TranslationMode,
    onSelected: (TranslationMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TranslationMode.entries.forEach { mode ->
            FilterChip(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                label = { Text(mode.label) },
                leadingIcon = {
                    when (mode) {
                        TranslationMode.CLOUD -> Icon(Icons.Default.Cloud, contentDescription = null)
                        TranslationMode.OFFLINE -> Icon(Icons.Default.Storage, contentDescription = null)
                        TranslationMode.AUTO -> Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LanguageRow(
    source: LanguageOption,
    target: LanguageOption,
    onOpenSource: () -> Unit,
    onOpenTarget: () -> Unit,
    onSwap: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onOpenSource,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(source.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onSwap) {
            Icon(Icons.Default.SwapHoriz, contentDescription = "交换语言")
        }
        OutlinedButton(
            onClick = onOpenTarget,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(target.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MessageBanner(
    error: String?,
    info: String?,
) {
    val message = error ?: info ?: return
    val isError = error != null
    Surface(
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Warning else Icons.Default.Check,
                contentDescription = null,
                tint = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LanguagePickerSheet(
    target: LanguagePickerTarget,
    sourceLanguage: LanguageOption,
    targetLanguage: LanguageOption,
    onChooseLanguage: (LanguageOption) -> Unit,
) {
    val options = if (target == LanguagePickerTarget.SOURCE) {
        listOf(Languages.auto) + Languages.supported
    } else {
        Languages.supported
    }
    val selected = if (target == LanguagePickerTarget.SOURCE) sourceLanguage else targetLanguage

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = if (target == LanguagePickerTarget.SOURCE) "选择源语言" else "选择目标语言",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        OutlinedTextField(
            value = "",
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("搜索语言（后续版本开放）") },
        )
        options.forEach { language ->
            TextButton(
                onClick = { onChooseLanguage(language) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(language.displayName)
                    if (language.code == selected.code) {
                        Icon(Icons.Default.Check, contentDescription = "已选择")
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ModelPickerSheet(
    state: TranslateUiState,
    onSearchChanged: (String) -> Unit,
    onModelToAddChanged: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onAddCustomModel: () -> Unit,
    onFetchCloudModels: () -> Unit,
) {
    val candidates = buildModelCandidates(
        currentModel = state.settings.modelName,
        customModels = state.settings.customModelNames,
        fetchedModels = state.availableModels,
    )
    val query = state.modelSearchQuery.trim()
    val filtered = if (query.isBlank()) {
        candidates
    } else {
        candidates.filter { it.name.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "选择翻译模型",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onFetchCloudModels,
                enabled = !state.isFetchingModels,
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(if (state.isFetchingModels) "获取中" else "获取模型")
            }
        }
        if (state.isFetchingModels) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (state.modelFetchError != null || state.modelFetchMessage != null) {
            MessageBanner(
                error = state.modelFetchError,
                info = state.modelFetchMessage,
            )
        }
        OutlinedTextField(
            value = state.modelSearchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (state.modelSearchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "清空搜索")
                    }
                }
            },
            label = { Text("搜索模型") },
            singleLine = true,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = "没有匹配模型，可以在下方手动添加。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            } else {
                items(filtered, key = { it.name }) { candidate ->
                    TextButton(
                        onClick = { onSelectModel(candidate.name) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ModelIconBadge(modelName = candidate.name)
                            Text(
                                text = candidate.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            AssistChip(
                                onClick = { onSelectModel(candidate.name) },
                                label = { Text(candidate.source.label) },
                            )
                            if (candidate.name == state.settings.modelName) {
                                Icon(Icons.Default.Check, contentDescription = "已选择")
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.modelToAdd,
                onValueChange = onModelToAddChanged,
                modifier = Modifier.weight(1f),
                label = { Text("添加模型名称") },
                singleLine = true,
            )
            Button(
                onClick = onAddCustomModel,
                enabled = state.modelToAdd.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("添加并使用")
            }
        }
        Text(
            text = "已获取 ${state.availableModels.size} 个模型，已添加 ${state.settings.customModelNames.size} 个",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun HistoryScreen(
    histories: List<TranslationHistoryEntity>,
    onDeleteHistory: (TranslationHistoryEntity) -> Unit,
    onClearHistory: () -> Unit,
    onOpenHistoryDetail: (TranslationHistoryEntity) -> Unit,
) {
    if (histories.isEmpty()) {
        EmptyState(
            title = "暂无历史记录",
            body = "开始翻译后，历史记录会显示在这里。",
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(onClick = onClearHistory) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("清空")
                }
            }
        }
        items(histories, key = { it.id }) { entity ->
            HistoryItem(
                entity = entity,
                onClick = { onOpenHistoryDetail(entity) },
                onDelete = { onDeleteHistory(entity) },
            )
        }
    }
}

@Composable
private fun HistoryItem(
    entity: TranslationHistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    OutlinedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${entity.sourceLanguage} → ${entity.targetLanguage}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = entity.mode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = entity.sourceText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = entity.translatedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = formatTime(entity.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

@Composable
private fun HistoryDetailSheet(
    entity: TranslationHistoryEntity,
    onCopyTranslation: (String) -> Unit,
    onSpeakSource: () -> Unit,
    onSpeakTranslation: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "翻译详情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            AssistChip(
                onClick = {},
                label = { Text(entity.mode) },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${entity.sourceLanguage} → ${entity.targetLanguage}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = formatTime(entity.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedCard(shape = RoundedCornerShape(8.dp)) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "原文",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(onClick = onSpeakSource) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "朗读原文")
                    }
                }
                Text(
                    text = entity.sourceText,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        OutlinedCard(shape = RoundedCornerShape(8.dp)) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "译文",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row {
                        IconButton(onClick = onSpeakTranslation) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "朗读译文")
                        }
                        IconButton(onClick = { onCopyTranslation(entity.translatedText) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "复制译文")
                        }
                    }
                }
                Text(
                    text = entity.translatedText,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        OutlinedButton(
            onClick = onDelete,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("删除这条记录")
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ModelScreen(
    state: TranslateUiState,
    onDownloadModel: () -> Unit,
    onDeleteModel: () -> Unit,
) {
    val modelState = state.modelState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "离线模型下载",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        OutlinedCard(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (modelState.isAvailable) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = if (modelState.isAvailable) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (modelState.isAvailable) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                    Column {
                        Text(
                            text = if (modelState.isAvailable) "模型已下载" else "模型未下载",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "HY-MT1.5-1.8B-Q4_K_M GGUF，约 1.13GB",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = "下载后可在无网络时进入离线翻译流程。该版本使用标准 Q4_K_M GGUF，体积较大但更适合当前公开推理内核。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (modelState.isDownloading) {
                    LinearProgressIndicator(
                        progress = { modelState.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${formatBytes(modelState.downloadedBytes)} / ${formatBytes(modelState.totalBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                modelState.errorMessage?.let {
                    MessageBanner(error = it, info = null)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDownloadModel,
                        enabled = !modelState.isDownloading,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (modelState.isAvailable) "重新下载" else "下载模型")
                    }
                    OutlinedButton(
                        onClick = onDeleteModel,
                        enabled = modelState.isAvailable && !modelState.isDownloading,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("删除")
                    }
                }
            }
        }
        OutlinedCard(shape = RoundedCornerShape(8.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("本地文件", fontWeight = FontWeight.Bold)
                Text(
                    text = modelState.filePath.ifBlank { "尚未创建模型路径" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: TranslateUiState,
    onBaseUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onProviderNameChanged: (String) -> Unit,
    onSelectCloudProvider: (String) -> Unit,
    onAddCloudProvider: () -> Unit,
    onOpenModelPicker: () -> Unit,
    onOpenModelManager: () -> Unit,
    onDefaultModeChanged: (TranslationMode) -> Unit,
    onClearHistory: () -> Unit,
    onCheckAppUpdate: () -> Unit,
    onDownloadAppUpdate: () -> Unit,
    ttsState: TtsRuntimeState,
    onRefreshTts: () -> Unit,
    onInstallTtsData: () -> Unit,
    onOpenTtsSettings: () -> Unit,
    onTestTts: () -> Unit,
) {
    var showProviderSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var overlayPermissionGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    val isCloudConfigured = state.settings.baseUrl.isNotBlank() && state.settings.apiKey.isNotBlank()
    val selectedProvider = state.settings.selectedProvider
    val modelCount = state.availableModels.size + state.settings.customModelNames.size

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                overlayPermissionGranted = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "设置",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        if (state.modelFetchError != null || state.modelFetchMessage != null) {
            item {
                MessageBanner(
                    error = state.modelFetchError,
                    info = state.modelFetchMessage,
                )
            }
        }
        item {
            SettingsModule(
                title = "AI 模型服务",
                icon = {
                    ProviderIconBadge(
                        name = selectedProvider.name,
                        baseUrl = selectedProvider.baseUrl,
                        modifier = Modifier.size(38.dp),
                    )
                },
            ) {
                CurrentProviderSummary(
                    provider = selectedProvider,
                    modelName = state.settings.modelName,
                    modelCount = modelCount,
                )
                if (state.isFetchingModels) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                SettingsActionRow(
                    title = "供应商配置",
                    body = if (isCloudConfigured) {
                        "${selectedProvider.name} · Base URL / API Key 已配置"
                    } else {
                        "${selectedProvider.name} · 待填写 Base URL / API Key"
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    onClick = { showProviderSheet = true },
                )
                SettingsActionRow(
                    title = "翻译模型",
                    body = if (state.settings.modelName.isBlank()) "选择或搜索可用模型" else state.settings.modelName,
                    icon = { Icon(Icons.Default.Storage, contentDescription = null) },
                    onClick = onOpenModelPicker,
                )
            }
        }
        item {
            SettingsModule(
                title = "离线模型",
                icon = { Icon(Icons.Default.Storage, contentDescription = null) },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("HY-MT Q4_K_M", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (state.modelState.isAvailable) {
                                "本地可用 · ${formatBytes(state.modelState.downloadedBytes)}"
                            } else {
                                "未下载 · 约 1.13GB"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenModelManager,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("管理模型")
                    }
                }
            }
        }
        item {
            SettingsModule(
                title = "翻译偏好",
                icon = { Icon(Icons.Default.Translate, contentDescription = null) },
            ) {
                Text("默认翻译模式", fontWeight = FontWeight.Bold)
                ModeSelector(
                    selected = state.settings.defaultMode,
                    onSelected = onDefaultModeChanged,
                )
                SettingsStaticRow("默认源语言", "自动检测")
                SettingsStaticRow("默认目标语言", "简体中文")
                SettingsStaticRow("提示词风格", "简洁（仅输出译文）")
            }
        }
        item {
            SettingsModule(
                title = "文本朗读",
                icon = { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null) },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("系统朗读", fontWeight = FontWeight.Bold)
                    Text(
                        text = ttsStatusText(ttsState),
                        color = when (ttsState.status) {
                            TtsStatus.READY, TtsStatus.SPEAKING -> MaterialTheme.colorScheme.primary
                            TtsStatus.CHECKING -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.error
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
                ttsState.engineLabel?.let { label ->
                    SettingsStaticRow("朗读引擎", label)
                }
                Text(
                    text = ttsHelperText(ttsState),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (ttsState.status == TtsStatus.CHECKING || ttsState.status == TtsStatus.SPEAKING) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onRefreshTts,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    ) {
                        Text("重新检测", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = onInstallTtsData,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    ) {
                        Text("安装语音包", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = onOpenTtsSettings,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    ) {
                        Text("打开系统设置", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Button(
                    onClick = onTestTts,
                    enabled = ttsState.canSpeak,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("测试朗读")
                }
            }
        }
        item {
            SettingsModule(
                title = "网络与性能",
                icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
            ) {
                SettingsStaticRow("请求超时", "60 秒")
                SettingsStaticRow("失败重试", "由服务端或网络层处理")
                SettingsStaticRow("输出策略", "仅输出译文")
                Text(
                    text = "后续可在这里加入温度、最大输出长度、重试次数等可调参数。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            SettingsModule(
                title = "悬浮翻译",
                icon = { Icon(Icons.Default.Translate, contentDescription = null) },
            ) {
                SettingsStaticRow("悬浮窗权限", if (overlayPermissionGranted) "已授权" else "未授权")
                Text(
                    text = "复制文本后点悬浮球，直接在当前 App 上方打开迷你翻译窗。不会自动监听剪贴板。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (!overlayPermissionGranted) {
                        Button(
                            onClick = { openOverlayPermissionSettings(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("授权悬浮窗")
                        }
                    } else {
                        Button(
                            onClick = {
                                context.startService(
                                    Intent(context, FloatingTranslateService::class.java)
                                        .setAction(FloatingTranslateService.ACTION_SHOW),
                                )
                                Toast.makeText(context, "已开启悬浮球", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("开启悬浮球")
                        }
                        OutlinedButton(
                            onClick = {
                                context.startService(
                                    Intent(context, FloatingTranslateService::class.java)
                                        .setAction(FloatingTranslateService.ACTION_HIDE),
                                )
                                Toast.makeText(context, "已关闭悬浮球", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("关闭悬浮球")
                        }
                    }
                }
            }
        }
        item {
            SettingsModule(
                title = "历史与数据",
                icon = { Icon(Icons.Default.History, contentDescription = null) },
            ) {
                SettingsStaticRow("历史记录", "${state.histories.size} 条")
                OutlinedButton(
                    onClick = onClearHistory,
                    enabled = state.histories.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("清空历史记录")
                }
            }
        }
        item {
            SettingsModule(
                title = "关于与许可",
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
            ) {
                SettingsStaticRow("应用版本", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                SettingsActionRow(
                    title = "应用更新",
                    body = appUpdateStatusText(state),
                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                    onClick = onCheckAppUpdate,
                )
                if (state.isCheckingAppUpdate) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                if (state.isDownloadingAppUpdate) {
                    LinearProgressIndicator(
                        progress = { state.appUpdateDownloadProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (state.appUpdateError != null || state.appUpdateMessage != null) {
                    MessageBanner(
                        error = state.appUpdateError,
                        info = state.appUpdateMessage,
                    )
                }
                state.availableAppUpdate?.let { release ->
                    Text(
                        text = buildString {
                            append("最新版本：${release.versionName}")
                            if (release.sizeBytes > 0L) append(" · ${formatBytes(release.sizeBytes)}")
                            if (release.required) append(" · 必须更新")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    release.notes.take(3).forEach { note ->
                        Text(
                            text = "• $note",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = onDownloadAppUpdate,
                        enabled = release.apkUrl.isNotBlank() && !state.isDownloadingAppUpdate,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            when {
                                state.isDownloadingAppUpdate -> "正在下载"
                                state.downloadedAppUpdatePath != null -> "安装更新"
                                else -> "下载并安装"
                            },
                        )
                    }
                }
                SettingsStaticRow("开源与许可", "正式发布前补充 Hy-MT License")
                SettingsStaticRow("隐私政策", "本地配置仅保存在设备内")
            }
        }
    }

    if (showProviderSheet) {
        ModalBottomSheet(onDismissRequest = { showProviderSheet = false }) {
            ProviderConfigSheet(
                state = state,
                onProviderNameChanged = onProviderNameChanged,
                onBaseUrlChanged = onBaseUrlChanged,
                onApiKeyChanged = onApiKeyChanged,
                onSelectCloudProvider = onSelectCloudProvider,
                onAddCloudProvider = onAddCloudProvider,
            )
        }
    }
}

private fun openOverlayPermissionSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun installAppUpdate(context: Context, apkPath: String) {
    val apkFile = File(apkPath)
    if (!apkFile.exists()) {
        Toast.makeText(context, "更新包不存在，请重新下载", Toast.LENGTH_SHORT).show()
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(context, "请在系统设置中允许安装未知应用", Toast.LENGTH_SHORT).show()
            }
        return
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        apkFile,
    )
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, "application/vnd.android.package-archive")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(context, "无法打开系统安装器", Toast.LENGTH_SHORT).show()
        }
}

private fun showTtsSpeakResult(context: Context, result: TtsSpeakResult) {
    if (!result.accepted && result.message != null) {
        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
    }
}

private fun openFirstResolvableIntent(
    context: Context,
    intents: List<Intent>,
    failureMessage: String,
) {
    for (intent in intents) {
        val opened = runCatching {
            context.startActivity(intent)
        }.isSuccess
        if (opened) return
    }
    Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
}

private fun appUpdateStatusText(state: TranslateUiState): String {
    return when {
        state.isCheckingAppUpdate -> "正在检查 R2 更新清单"
        state.isDownloadingAppUpdate -> "正在下载更新包"
        state.downloadedAppUpdatePath != null -> "更新包已下载，可继续安装"
        state.availableAppUpdate != null -> "发现 ${state.availableAppUpdate.versionName}，可下载更新"
        state.appUpdateError != null -> state.appUpdateError
        state.appUpdateMessage != null -> state.appUpdateMessage
        else -> "检查 R2 更新清单"
    }
}

private fun ttsStatusText(state: TtsRuntimeState): String {
    return when (state.status) {
        TtsStatus.CHECKING -> "检测中"
        TtsStatus.READY -> "可用"
        TtsStatus.SPEAKING -> "朗读中"
        TtsStatus.NO_ENGINE -> "未安装引擎"
        TtsStatus.INIT_FAILED -> "引擎不可用"
        TtsStatus.VOICE_MISSING -> "语音包缺失"
        TtsStatus.ERROR -> "朗读失败"
        TtsStatus.SHUTDOWN -> "已关闭"
    }
}

private fun ttsHelperText(state: TtsRuntimeState): String {
    return when (state.status) {
        TtsStatus.READY -> "系统文字转语音服务已准备好。"
        TtsStatus.SPEAKING -> "正在播放朗读内容。"
        TtsStatus.CHECKING -> "正在检测系统文字转语音服务。"
        TtsStatus.NO_ENGINE -> "请安装或启用系统文字转语音服务。"
        TtsStatus.INIT_FAILED -> "默认朗读引擎初始化失败，可重新检测或打开系统设置切换引擎。"
        TtsStatus.VOICE_MISSING -> "当前语言缺少语音包，可安装语音包后重新检测。"
        TtsStatus.ERROR -> "朗读播放失败，可重新检测或打开系统设置修复。"
        TtsStatus.SHUTDOWN -> "朗读服务已随页面关闭。"
    }.let { "${state.message}。$it" }
}

@Composable
private fun CurrentProviderSummary(
    provider: CloudProviderSettings,
    modelName: String,
    modelCount: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProviderIconBadge(
                    name = provider.name,
                    baseUrl = provider.baseUrl,
                    modifier = Modifier.size(44.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = provider.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (provider.isConfigured) "已配置 · $modelCount 个模型" else "待配置 · $modelCount 个模型",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = if (provider.isConfigured) Icons.Default.Check else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (provider.isConfigured) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ModelIconBadge(modelName = modelName, modifier = Modifier.size(32.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "当前翻译模型",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = modelName.ifBlank { "未选择" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderCardList(
    providers: List<CloudProviderSettings>,
    selectedProviderId: String,
    onSelectProvider: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "选择供应商",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        providers.forEach { provider ->
            ProviderRow(
                provider = provider,
                selected = provider.id == selectedProviderId,
                onClick = { onSelectProvider(provider.id) },
            )
        }
    }
}

@Composable
private fun ProviderRow(
    provider: CloudProviderSettings,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProviderIconBadge(
                name = provider.name,
                baseUrl = provider.baseUrl,
                modifier = Modifier.size(44.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "OpenAI Compatible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = if (provider.isConfigured) "已配置" else "未配置",
                style = MaterialTheme.typography.bodySmall,
                color = if (provider.isConfigured) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = "当前供应商")
            }
        }
    }
}

@Composable
private fun ProviderIconBadge(
    name: String,
    baseUrl: String,
    modifier: Modifier = Modifier,
) {
    val visual = resolveBrandVisual("$name $baseUrl")
    BrandBadge(
        label = visual.label,
        container = visual.container,
        content = visual.content,
        iconResId = visual.iconResId,
        modifier = modifier,
    )
}

@Composable
private fun ModelIconBadge(
    modelName: String,
    modifier: Modifier = Modifier.size(36.dp),
) {
    val visual = resolveBrandVisual(modelName)
    BrandBadge(
        label = visual.label,
        container = visual.container,
        content = visual.content,
        iconResId = visual.iconResId,
        modifier = modifier,
    )
}

@Composable
private fun BrandBadge(
    label: String,
    container: Color,
    content: Color,
    iconResId: Int? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = container,
        contentColor = content,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (iconResId != null) {
                Image(
                    painter = painterResource(iconResId),
                    contentDescription = label,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                )
            } else {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class BrandVisual(
    val label: String,
    val container: Color,
    val content: Color = Color.White,
    val iconResId: Int? = null,
)

private fun resolveBrandVisual(value: String): BrandVisual {
    val key = value.lowercase(Locale.ROOT)
    return when {
        key.contains("deepseek") -> BrandVisual("DS", Color.White, iconResId = R.drawable.lobe_deepseek)
        key.contains("openrouter") -> BrandVisual("OR", Color.White, iconResId = R.drawable.lobe_openrouter)
        key.contains("claude") || key.contains("anthropic") -> BrandVisual("C", Color.White, iconResId = R.drawable.lobe_claude)
        key.contains("gemini") || key.contains("google") -> BrandVisual("G", Color.White, iconResId = R.drawable.lobe_gemini)
        key.contains("qwen") || key.contains("通义") -> BrandVisual("Q", Color.White, iconResId = R.drawable.lobe_qwen)
        key.contains("hunyuan") || key.contains("hy-mt") || key.contains("tencent") -> BrandVisual("HY", Color.White, iconResId = R.drawable.lobe_hunyuan)
        key.contains("kimi") || key.contains("moonshot") -> BrandVisual("K", Color(0xFF0F766E))
        key.contains("glm") || key.contains("zhipu") -> BrandVisual("GL", Color(0xFF7C3AED))
        key.contains("ollama") -> BrandVisual("OL", Color(0xFF111827))
        key.contains("gpt") || key.contains("openai") -> BrandVisual("O", Color.White, iconResId = R.drawable.lobe_openai)
        key.contains("custom") || key.contains("自定义") -> BrandVisual("{}", Color(0xFF6D5DF6))
        else -> BrandVisual("AI", Color(0xFF64748B))
    }
}

@Composable
private fun SettingsModule(
    title: String,
    icon: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(shape = RoundedCornerShape(8.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        icon()
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    body: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(34.dp),
            ) {
                icon()
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = ">",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun SettingsStaticRow(
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontWeight = FontWeight.Bold)
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun ProviderConfigSheet(
    state: TranslateUiState,
    onProviderNameChanged: (String) -> Unit,
    onBaseUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onSelectCloudProvider: (String) -> Unit,
    onAddCloudProvider: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "供应商配置",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "选择一个供应商后配置接口。API Key 仅保存在本机 DataStore。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ProviderCardList(
            providers = state.settings.cloudProviders,
            selectedProviderId = state.settings.selectedProviderId,
            onSelectProvider = onSelectCloudProvider,
        )
        OutlinedButton(
            onClick = onAddCloudProvider,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("添加供应商")
        }
        OutlinedTextField(
            value = state.settings.selectedProvider.name,
            onValueChange = onProviderNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("供应商名称") },
            singleLine = true,
            leadingIcon = {
                ProviderIconBadge(
                    name = state.settings.selectedProvider.name,
                    baseUrl = state.settings.selectedProvider.baseUrl,
                    modifier = Modifier.size(28.dp),
                )
            },
        )
        OutlinedTextField(
            value = state.settings.baseUrl,
            onValueChange = onBaseUrlChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Base URL") },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.settings.apiKey,
            onValueChange = onApiKeyChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("API Key") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun EmptyState(
    title: String,
    body: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val mb = bytes / 1024.0 / 1024.0
    return String.format(Locale.US, "%.1f MB", mb)
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private data class ModelCandidate(
    val name: String,
    val source: ModelCandidateSource,
)

private enum class ModelCandidateSource(val label: String) {
    CURRENT("当前配置"),
    CUSTOM("手动添加"),
    FETCHED("接口返回"),
}

private fun buildModelCandidates(
    currentModel: String,
    customModels: List<String>,
    fetchedModels: List<String>,
): List<ModelCandidate> {
    val candidates = linkedMapOf<String, ModelCandidate>()

    fun add(name: String, source: ModelCandidateSource) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        candidates.putIfAbsent(
            trimmed.lowercase(),
            ModelCandidate(name = trimmed, source = source),
        )
    }

    add(currentModel, ModelCandidateSource.CURRENT)
    customModels.forEach { add(it, ModelCandidateSource.CUSTOM) }
    fetchedModels.forEach { add(it, ModelCandidateSource.FETCHED) }

    return candidates.values.toList()
}
