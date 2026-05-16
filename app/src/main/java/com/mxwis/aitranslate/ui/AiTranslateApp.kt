package com.mxwis.aitranslate.ui

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.mxwis.aitranslate.domain.ModelType
import com.mxwis.aitranslate.domain.TranslationMode
import com.mxwis.aitranslate.domain.UnifiedModelOption
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
            onOpenUnifiedModelPicker = viewModel::openUnifiedModelPicker,
            onCloseUnifiedModelPicker = viewModel::closeUnifiedModelPicker,
            onSelectUnifiedModel = viewModel::selectUnifiedModel,
            onUpdateDefaultUnifiedModel = viewModel::updateDefaultUnifiedModel,
            onOpenImageTranslator = viewModel::openImageTranslator,
            onUpdateImageRecognizedText = viewModel::updateImageRecognizedText,
            onTranslateImageText = viewModel::translateImageText,
            onCloseImageTranslator = viewModel::closeImageTranslator,
            onBringImageTranslationToHome = viewModel::bringImageTranslationToHome,
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
    onOpenUnifiedModelPicker: () -> Unit,
    onCloseUnifiedModelPicker: () -> Unit,
    onSelectUnifiedModel: (UnifiedModelOption) -> Unit,
    onUpdateDefaultUnifiedModel: (UnifiedModelOption) -> Unit,
    onOpenImageTranslator: (String, String) -> Unit,
    onUpdateImageRecognizedText: (String) -> Unit,
    onTranslateImageText: () -> Unit,
    onCloseImageTranslator: () -> Unit,
    onBringImageTranslationToHome: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val speaker = rememberTextSpeaker()
    val ttsState by speaker.state.collectAsStateWithLifecycle()
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isToolSheetOpen by rememberSaveable { mutableStateOf(false) }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = pendingPhotoUri
        if (success && uri != null) {
            onOpenImageTranslator(uri.toString(), "拍照翻译")
        } else {
            Toast.makeText(context, "未获取到照片", Toast.LENGTH_SHORT).show()
        }
    }
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            onOpenImageTranslator(uri.toString(), "相册导入")
        }
    }

    fun launchCamera() {
        runCatching {
            val uri = createPhotoTranslateUri(context)
            pendingPhotoUri = uri
            takePhotoLauncher.launch(uri)
        }.onFailure {
            Toast.makeText(context, "无法打开系统相机", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchGallery() {
        runCatching {
            pickImageLauncher.launch("image/*")
        }.onFailure {
            Toast.makeText(context, "无法打开系统相册", Toast.LENGTH_SHORT).show()
        }
    }

    fun speakText(text: String, language: LanguageOption) {
        showTtsSpeakResult(context, speaker.speak(text, language))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI 翻译",
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    if (state.currentSection == AppSection.TRANSLATE) {
                        Surface(
                            onClick = { isToolSheetOpen = true },
                            modifier = Modifier
                                .padding(end = 14.dp)
                                .size(42.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
                            ),
                            shadowElevation = 0.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BusinessCenter,
                                    contentDescription = "打开工具",
                                    modifier = Modifier.size(21.dp),
                                )
                            }
                        }
                    }
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
                    onOpenLanguagePicker = onOpenLanguagePicker,
                    onSwapLanguages = onSwapLanguages,
                    onTranslate = onTranslate,
                    onSpeakSource = { speakText(state.sourceText, state.sourceLanguage) },
                    onSpeakTranslation = { speakText(state.translatedText, state.targetLanguage) },
                    onOpenUnifiedModelPicker = onOpenUnifiedModelPicker,
                )
                AppSection.HISTORY -> HistoryScreen(
                    histories = state.histories,
                    onDeleteHistory = onDeleteHistory,
                    onClearHistory = onClearHistory,
                    onOpenHistoryDetail = onOpenHistoryDetail,
                )
                AppSection.SETTINGS -> SettingsScreen(
                    state = state,
                    onBaseUrlChanged = onBaseUrlChanged,
                    onApiKeyChanged = onApiKeyChanged,
                    onProviderNameChanged = onProviderNameChanged,
                    onSelectCloudProvider = onSelectCloudProvider,
                    onAddCloudProvider = onAddCloudProvider,
                    onOpenModelPicker = onOpenModelPicker,
                    onDownloadModel = onDownloadModel,
                    onDeleteModel = onDeleteModel,
                    onDefaultModeChanged = onDefaultModeChanged,
                    onUpdateDefaultUnifiedModel = onUpdateDefaultUnifiedModel,
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

    if (state.isUnifiedModelPickerOpen) {
        ModalBottomSheet(onDismissRequest = onCloseUnifiedModelPicker) {
            UnifiedModelPickerSheet(
                state = state,
                onSelectModel = onSelectUnifiedModel,
                onFetchCloudModels = onFetchCloudModels,
                onDownloadModel = onDownloadModel,
            )
        }
    }

    if (isToolSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isToolSheetOpen = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp)
                        .size(width = 54.dp, height = 5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f)),
                )
            },
        ) {
            TranslateToolboxSheet(
                onTakePhoto = {
                    isToolSheetOpen = false
                    launchCamera()
                },
                onPickImage = {
                    isToolSheetOpen = false
                    launchGallery()
                },
            )
        }
    }

    if (state.isImageTranslatorOpen) {
        ModalBottomSheet(onDismissRequest = onCloseImageTranslator) {
            ImageTranslateSheet(
                state = state,
                onRecognizedTextChanged = onUpdateImageRecognizedText,
                onTranslate = onTranslateImageText,
                onPickImage = ::launchGallery,
                onCopyTranslation = { text ->
                    clipboard.setText(AnnotatedString(text))
                    Toast.makeText(context, "已复制译文", Toast.LENGTH_SHORT).show()
                },
                onSpeakTranslation = { speakText(state.imageTranslatedText, state.targetLanguage) },
                onBringToHome = onBringImageTranslationToHome,
                onClose = onCloseImageTranslator,
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
    onOpenLanguagePicker: (LanguagePickerTarget) -> Unit,
    onSwapLanguages: () -> Unit,
    onTranslate: () -> Unit,
    onSpeakSource: () -> Unit,
    onSpeakTranslation: () -> Unit,
    onOpenUnifiedModelPicker: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            primaryColor,
            primaryColor.copy(alpha = 0.7f),
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // 模型选择 - 紧凑的胶囊按钮
        Surface(
            onClick = onOpenUnifiedModelPicker,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val modelType = currentModelType(state)
                Icon(
                    imageVector = when (modelType) {
                        ModelType.CLOUD -> Icons.Default.Cloud
                        ModelType.OFFLINE -> Icons.Default.Storage
                        ModelType.AUTO -> Icons.Default.Refresh
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = currentModelDisplayName(state),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 语言选择栏 - 带浮动交换按钮
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = { onOpenLanguagePicker(LanguagePickerTarget.SOURCE) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.sourceLanguage.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Spacer(Modifier.width(48.dp))
                Surface(
                    onClick = { onOpenLanguagePicker(LanguagePickerTarget.TARGET) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.targetLanguage.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            // 浮动交换按钮
            Surface(
                onClick = onSwapLanguages,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "交换语言",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 输入区域 - 压缩后的主入口卡
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            ),
            tonalElevation = 1.dp,
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onSpeakSource,
                        enabled = state.sourceText.isNotBlank(),
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "朗读原文",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = onClearInput,
                        enabled = state.sourceText.isNotEmpty(),
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "清空",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                BasicTextField(
                    value = state.sourceText,
                    onValueChange = onSourceTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp, max = 180.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 26.sp,
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (state.sourceText.isEmpty()) {
                                Text(
                                    "输入或粘贴要翻译的文本...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${state.sourceText.length}/5000",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 翻译按钮 - 渐变色
        Button(
            onClick = onTranslate,
            enabled = !state.isTranslating && state.sourceText.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            ),
            contentPadding = PaddingValues(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (!state.isTranslating && state.sourceText.isNotBlank()) {
                            gradientBrush
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                ),
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (state.isTranslating) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        Text(
                            "翻译中...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            Icons.Default.Translate,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            "翻译",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // 错误/信息提示
        if (state.errorMessage != null || state.infoMessage != null) {
            Spacer(Modifier.height(12.dp))
            MessageBanner(
                error = state.errorMessage,
                info = state.infoMessage,
            )
        }

        Spacer(Modifier.height(16.dp))

        // 输出区域 - 带左侧色条装饰
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
            ),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // 左侧装饰色条
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .heightIn(min = 120.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                ),
                            ),
                            shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                        ),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = state.targetLanguage.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                            IconButton(
                                onClick = onSpeakTranslation,
                                enabled = state.translatedText.isNotBlank(),
                                modifier = Modifier.size(34.dp),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "朗读译文",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(state.translatedText))
                                    Toast.makeText(context, "已复制译文", Toast.LENGTH_SHORT).show()
                                },
                                enabled = state.translatedText.isNotBlank(),
                                modifier = Modifier.size(34.dp),
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    if (state.isTranslating) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        )
                    }
                    Text(
                        text = state.translatedText.ifBlank { "译文将显示在这里" },
                        style = if (state.translatedText.isNotBlank()) {
                            MaterialTheme.typography.bodyLarge
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        color = if (state.translatedText.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.heightIn(min = 100.dp),
                        lineHeight = 26.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TranslateToolboxSheet(
    onTakePhoto: () -> Unit,
    onPickImage: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "工具",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        ToolboxActionItem(
            title = "拍照翻译",
            subtitle = "拍摄文字，快速识别翻译",
            icon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) },
            container = Color(0xFFEAF8FC),
            content = Color(0xFF159FBE),
            onClick = onTakePhoto,
        )
        ToolboxActionItem(
            title = "相册导入",
            subtitle = "从相册选择图片翻译",
            icon = { Icon(Icons.Default.Image, contentDescription = null) },
            container = Color(0xFFF0F0FF),
            content = Color(0xFF5966E8),
            onClick = onPickImage,
        )
        DashedDivider(modifier = Modifier.padding(top = 4.dp))
        Text(
            text = "更多工具后续加入",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp, bottom = 26.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
        )
    }
}

@Composable
private fun ToolboxActionItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    container: Color,
    content: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = content,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.68f)),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.92f),
                contentColor = content,
                shadowElevation = 1.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    icon()
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DashedDivider(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.88f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
        )
    }
}

@Composable
private fun ModelSelectorChip(
    state: TranslateUiState,
    onClick: () -> Unit,
) {
    val modelName = currentModelDisplayName(state)
    val subtitle = currentModelSubtitle(state)
    val modelType = currentModelType(state)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = when (modelType) {
                    ModelType.CLOUD -> MaterialTheme.colorScheme.primaryContainer
                    ModelType.OFFLINE -> MaterialTheme.colorScheme.tertiaryContainer
                    ModelType.AUTO -> MaterialTheme.colorScheme.secondaryContainer
                },
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = when (modelType) {
                            ModelType.CLOUD -> Icons.Default.Cloud
                            ModelType.OFFLINE -> Icons.Default.Storage
                            ModelType.AUTO -> Icons.Default.Refresh
                        },
                        contentDescription = null,
                        tint = when (modelType) {
                            ModelType.CLOUD -> MaterialTheme.colorScheme.onPrimaryContainer
                            ModelType.OFFLINE -> MaterialTheme.colorScheme.onTertiaryContainer
                            ModelType.AUTO -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modelName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "选择模型",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun currentModelDisplayName(state: TranslateUiState): String {
    val selected = state.selectedUnifiedModel?.takeIf { it.type == currentModelType(state) }
    return when (state.selectedMode) {
        TranslationMode.CLOUD -> selected?.displayName
            ?: state.settings.modelName.ifBlank { "选择云端模型" }
        TranslationMode.OFFLINE -> selected?.displayName ?: "HY-MT 1.5B"
        TranslationMode.AUTO -> selected?.displayName ?: "自动选择"
    }
}

private fun currentModelSubtitle(state: TranslateUiState): String {
    val selected = state.selectedUnifiedModel?.takeIf { it.type == currentModelType(state) }
    return selected?.subtitle ?: when (state.selectedMode) {
        TranslationMode.CLOUD -> "${state.settings.selectedProvider.name} · 云端"
        TranslationMode.OFFLINE -> "本地推理 · 离线"
        TranslationMode.AUTO -> "智能切换 · 自动"
    }
}

private fun currentModelType(state: TranslateUiState): ModelType {
    return state.selectedUnifiedModel
        ?.takeIf { modeMatchesModelType(state.selectedMode, it.type) }
        ?.type
        ?: when (state.selectedMode) {
            TranslationMode.CLOUD -> ModelType.CLOUD
            TranslationMode.OFFLINE -> ModelType.OFFLINE
            TranslationMode.AUTO -> ModelType.AUTO
        }
}

private fun modeMatchesModelType(mode: TranslationMode, type: ModelType): Boolean {
    return when (mode) {
        TranslationMode.CLOUD -> type == ModelType.CLOUD
        TranslationMode.OFFLINE -> type == ModelType.OFFLINE
        TranslationMode.AUTO -> type == ModelType.AUTO
    }
}

@Composable
private fun UnifiedModelPickerSheet(
    state: TranslateUiState,
    onSelectModel: (UnifiedModelOption) -> Unit,
    onFetchCloudModels: () -> Unit,
    onDownloadModel: () -> Unit,
) {
    val offlineModels = state.unifiedModelList.filter { it.type == ModelType.OFFLINE }
    val cloudModels = state.unifiedModelList.filter { it.type == ModelType.CLOUD }
    val autoModels = state.unifiedModelList.filter { it.type == ModelType.AUTO }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (state.isFetchingModels) "获取中" else "获取模型")
            }
        }

        if (state.isFetchingModels) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (offlineModels.isNotEmpty()) {
            Text(
                text = "离线模型",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            offlineModels.forEach { model ->
                UnifiedModelItem(
                    model = model,
                    isSelected = state.selectedUnifiedModel?.id == model.id
                            || (state.selectedUnifiedModel == null && state.selectedMode == TranslationMode.OFFLINE),
                    onSelect = { onSelectModel(model) },
                    trailing = {
                        if (!model.isAvailable) {
                            OutlinedButton(
                                onClick = onDownloadModel,
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("下载", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            Text(
                                text = "已下载",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            }
        }

        if (cloudModels.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Text(
                text = "云端模型",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyColumn(
                modifier = Modifier.heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(cloudModels, key = { it.id }) { model ->
                    UnifiedModelItem(
                        model = model,
                        isSelected = state.selectedUnifiedModel?.id == model.id
                                || (state.selectedUnifiedModel == null && state.selectedMode == TranslationMode.CLOUD && model.displayName == state.settings.modelName),
                        onSelect = { onSelectModel(model) },
                    )
                }
            }
        }

        if (autoModels.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            autoModels.forEach { model ->
                UnifiedModelItem(
                    model = model,
                    isSelected = state.selectedUnifiedModel?.id == model.id
                            || (state.selectedUnifiedModel == null && state.selectedMode == TranslationMode.AUTO),
                    onSelect = { onSelectModel(model) },
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun UnifiedModelItem(
    model: UnifiedModelOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(16.dp),
                color = when (model.type) {
                    ModelType.CLOUD -> MaterialTheme.colorScheme.primaryContainer
                    ModelType.OFFLINE -> MaterialTheme.colorScheme.tertiaryContainer
                    ModelType.AUTO -> MaterialTheme.colorScheme.secondaryContainer
                },
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = when (model.type) {
                            ModelType.CLOUD -> Icons.Default.Cloud
                            ModelType.OFFLINE -> Icons.Default.Storage
                            ModelType.AUTO -> Icons.Default.Refresh
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = when (model.type) {
                            ModelType.CLOUD -> MaterialTheme.colorScheme.onPrimaryContainer
                            ModelType.OFFLINE -> MaterialTheme.colorScheme.onTertiaryContainer
                            ModelType.AUTO -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = model.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (trailing != null) {
                trailing()
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ImageTranslateSheet(
    state: TranslateUiState,
    onRecognizedTextChanged: (String) -> Unit,
    onTranslate: () -> Unit,
    onPickImage: () -> Unit,
    onCopyTranslation: (String) -> Unit,
    onSpeakTranslation: () -> Unit,
    onBringToHome: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "图片翻译",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = state.imageSourceLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Clear, contentDescription = "关闭图片翻译")
            }
        }

        ImagePreviewCard(uri = state.imageUri)

        if (state.isImageRecognizing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (state.imageErrorMessage != null || state.imageInfoMessage != null) {
            MessageBanner(
                error = state.imageErrorMessage,
                info = state.imageInfoMessage,
            )
        }

        OutlinedTextField(
            value = state.imageRecognizedText,
            onValueChange = onRecognizedTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp),
            label = { Text("识别文本") },
            placeholder = { Text("图片中的文字会显示在这里，可手动修改") },
            maxLines = 8,
            shape = RoundedCornerShape(16.dp),
        )

        Button(
            onClick = onTranslate,
            enabled = !state.isImageRecognizing &&
                    !state.isImageTranslating &&
                    state.imageRecognizedText.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.Translate, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (state.isImageTranslating) "正在翻译" else "翻译识别文本")
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "译文",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Row {
                        IconButton(
                            onClick = onSpeakTranslation,
                            enabled = state.imageTranslatedText.isNotBlank(),
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "朗读图片译文")
                        }
                        IconButton(
                            onClick = { onCopyTranslation(state.imageTranslatedText) },
                            enabled = state.imageTranslatedText.isNotBlank(),
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "复制图片译文")
                        }
                    }
                }
                if (state.isImageTranslating) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Text(
                    text = state.imageTranslatedText.ifBlank { "译文将显示在这里" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.imageTranslatedText.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    lineHeight = 24.sp,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onPickImage,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("重新选择")
            }
            Button(
                onClick = onBringToHome,
                enabled = state.imageRecognizedText.isNotBlank() || state.imageTranslatedText.isNotBlank(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("带入首页")
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ImagePreviewCard(uri: String?) {
    val context = LocalContext.current
    val imageBitmap = remember(uri) {
        if (uri == null) {
            null
        } else {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "待识别图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "图片预览",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniTranslateCard(
    state: TranslateUiState,
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
                shape = RoundedCornerShape(16.dp),
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

            Text(
                text = "使用模型：${currentModelDisplayName(state)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Translate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.isMiniTranslating) "正在翻译" else "翻译")
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                shape = RoundedCornerShape(16.dp),
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
                shape = RoundedCornerShape(16.dp),
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
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("忽略")
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(16.dp),
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
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(source.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onSwap) {
            Icon(Icons.Default.SwapHoriz, contentDescription = "交换语言")
        }
        OutlinedButton(
            onClick = onOpenTarget,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(16.dp),
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
                shape = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(16.dp),
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
        OutlinedCard(shape = RoundedCornerShape(16.dp)) {
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
        OutlinedCard(shape = RoundedCornerShape(16.dp)) {
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
            shape = RoundedCornerShape(16.dp),
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
            shape = RoundedCornerShape(16.dp),
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
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (modelState.isAvailable) "重新下载" else "下载模型")
                    }
                    OutlinedButton(
                        onClick = onDeleteModel,
                        enabled = modelState.isAvailable && !modelState.isDownloading,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("删除")
                    }
                }
            }
        }
        OutlinedCard(shape = RoundedCornerShape(16.dp)) {
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
    onDownloadModel: () -> Unit,
    onDeleteModel: () -> Unit,
    onDefaultModeChanged: (TranslationMode) -> Unit,
    onUpdateDefaultUnifiedModel: (UnifiedModelOption) -> Unit,
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
            CollapsibleSettingsSection(
                title = "AI 模型服务",
                icon = {
                    ProviderIconBadge(
                        name = selectedProvider.name,
                        baseUrl = selectedProvider.baseUrl,
                        modifier = Modifier.size(24.dp),
                    )
                },
                initialExpanded = true,
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
            CollapsibleSettingsSection(
                title = "离线模型管理",
                icon = { Icon(Icons.Default.Storage, contentDescription = null) },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("HY-MT 1.5B Q4_K_M", fontWeight = FontWeight.Bold)
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
                }
                if (state.modelState.isDownloading) {
                    LinearProgressIndicator(
                        progress = { state.modelState.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "下载中 ${(state.modelState.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (!state.modelState.isAvailable) {
                        Button(
                            onClick = onDownloadModel,
                            enabled = !state.modelState.isDownloading,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("下载模型")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDeleteModel,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("删除模型")
                        }
                    }
                }
            }
        }
        item {
            CollapsibleSettingsSection(
                title = "默认翻译模型",
                icon = { Icon(Icons.Default.Translate, contentDescription = null) },
                initialExpanded = true,
            ) {
                Text("当前默认模型", fontWeight = FontWeight.Bold)
                val currentDefault = state.selectedUnifiedModel
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = when (currentDefault?.type) {
                                ModelType.OFFLINE -> Icons.Default.Storage
                                ModelType.CLOUD -> Icons.Default.Cloud
                                else -> Icons.Default.SwapHoriz
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentDefault?.displayName ?: "未选择",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Text(
                                text = currentDefault?.subtitle ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
                Text(
                    text = "可在翻译页顶部快速切换模型，此处设置的是每次启动时的默认值。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(state.unifiedModelList) { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateDefaultUnifiedModel(model) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = when (model.type) {
                                    ModelType.OFFLINE -> Icons.Default.Storage
                                    ModelType.CLOUD -> Icons.Default.Cloud
                                    ModelType.AUTO -> Icons.Default.SwapHoriz
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(model.displayName, style = MaterialTheme.typography.bodyMedium)
                                if (model.subtitle.isNotBlank()) {
                                    Text(
                                        model.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            if (model.id == currentDefault?.id) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            CollapsibleSettingsSection(
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
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    ) {
                        Text("重新检测", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = onInstallTtsData,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    ) {
                        Text("安装语音包", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = onOpenTtsSettings,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    ) {
                        Text("打开系统设置", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Button(
                    onClick = onTestTts,
                    enabled = ttsState.canSpeak,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("测试朗读")
                }
            }
        }
        item {
            CollapsibleSettingsSection(
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
            CollapsibleSettingsSection(
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
                            shape = RoundedCornerShape(16.dp),
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
                            shape = RoundedCornerShape(16.dp),
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
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text("关闭悬浮球")
                        }
                    }
                }
            }
        }
        item {
            CollapsibleSettingsSection(
                title = "历史与数据",
                icon = { Icon(Icons.Default.History, contentDescription = null) },
            ) {
                SettingsStaticRow("历史记录", "${state.histories.size} 条")
                OutlinedButton(
                    onClick = onClearHistory,
                    enabled = state.histories.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("清空历史记录")
                }
            }
        }
        item {
            CollapsibleSettingsSection(
                title = "关于与更新",
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
                        shape = RoundedCornerShape(16.dp),
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

private fun createPhotoTranslateUri(context: Context): Uri {
    val photoDir = File(context.cacheDir, "photo_translate").apply { mkdirs() }
    val photoFile = File(photoDir, "photo_translate_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile,
    )
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
        shape = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(16.dp),
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
private fun CollapsibleSettingsSection(
    title: String,
    icon: @Composable () -> Unit,
    initialExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initialExpanded) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "expandArrow",
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = if (expanded) 2.dp else 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.animateContentSize(animationSpec = tween(300))) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        icon()
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(200)),
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    content()
                }
            }
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
            shape = RoundedCornerShape(16.dp),
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
