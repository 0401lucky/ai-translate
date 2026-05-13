package com.mxwis.aitranslate.speech

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import com.mxwis.aitranslate.domain.LanguageOption
import com.mxwis.aitranslate.domain.SpeechLocaleResolver
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TtsStatus {
    CHECKING,
    READY,
    SPEAKING,
    NO_ENGINE,
    INIT_FAILED,
    VOICE_MISSING,
    ERROR,
    SHUTDOWN,
}

data class TtsRuntimeState(
    val status: TtsStatus = TtsStatus.CHECKING,
    val message: String = "正在检测系统朗读",
    val engineLabel: String? = null,
    val enginePackage: String? = null,
    val engineCount: Int = 0,
    val locale: Locale? = null,
) {
    val canSpeak: Boolean
        get() = status == TtsStatus.READY || status == TtsStatus.SPEAKING

    val isRepairable: Boolean
        get() = status == TtsStatus.NO_ENGINE ||
            status == TtsStatus.INIT_FAILED ||
            status == TtsStatus.VOICE_MISSING ||
            status == TtsStatus.ERROR
}

data class TtsSpeakResult(
    val accepted: Boolean,
    val message: String? = null,
)

class SystemTextSpeaker(
    context: Context,
) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val _state = MutableStateFlow(TtsRuntimeState())
    val state: StateFlow<TtsRuntimeState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var hasInitFailed = false
    private var engines: List<TtsEngineSummary> = emptyList()

    init {
        refresh()
    }

    override fun onInit(status: Int) {
        val engine = tts
        isReady = status == TextToSpeech.SUCCESS && engine != null
        hasInitFailed = !isReady

        if (isReady && engine != null) {
            engine.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            engine.setOnUtteranceProgressListener(createProgressListener())
            val currentEngine = engine.defaultEngine ?: defaultEnginePackage()
            val summary = findEngineSummary(currentEngine)
            _state.value = TtsRuntimeState(
                status = TtsStatus.READY,
                message = "系统朗读可用",
                engineLabel = summary?.label ?: currentEngine,
                enginePackage = currentEngine,
                engineCount = engines.size,
            )
        } else {
            _state.value = TtsRuntimeState(
                status = TtsStatus.INIT_FAILED,
                message = "默认朗读引擎初始化失败",
                engineLabel = bestEngineLabel(),
                enginePackage = defaultEnginePackage(),
                engineCount = engines.size,
            )
        }
    }

    fun refresh() {
        shutdownEngine()
        isReady = false
        hasInitFailed = false
        engines = queryTtsEngines()

        if (engines.isEmpty()) {
            _state.value = TtsRuntimeState(
                status = TtsStatus.NO_ENGINE,
                message = "未检测到系统朗读引擎",
                engineCount = 0,
            )
            return
        }

        _state.value = TtsRuntimeState(
            status = TtsStatus.CHECKING,
            message = "正在初始化系统朗读",
            engineLabel = bestEngineLabel(),
            enginePackage = defaultEnginePackage(),
            engineCount = engines.size,
        )
        tts = TextToSpeech(appContext, this)
    }

    fun speak(text: String, language: LanguageOption): TtsSpeakResult {
        val content = text.trim()
        if (content.isBlank()) {
            return TtsSpeakResult(accepted = false, message = "暂无可朗读文本")
        }

        if (engines.isEmpty()) {
            refresh()
            return TtsSpeakResult(
                accepted = false,
                message = repairMessage("未检测到系统朗读引擎"),
            )
        }

        val engine = tts
        return when {
            engine == null || hasInitFailed -> {
                TtsSpeakResult(
                    accepted = false,
                    message = repairMessage("默认朗读引擎初始化失败"),
                )
            }
            !isReady -> {
                TtsSpeakResult(accepted = false, message = "朗读正在准备中")
            }
            else -> speakReadyText(engine, content, language)
        }
    }

    fun stop() {
        tts?.stop()
        if (isReady) {
            _state.value = _state.value.copy(
                status = TtsStatus.READY,
                message = "朗读已停止",
            )
        }
    }

    fun shutdown() {
        shutdownEngine()
        _state.value = _state.value.copy(
            status = TtsStatus.SHUTDOWN,
            message = "系统朗读已关闭",
        )
    }

    fun installVoiceDataIntent(): Intent {
        return Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun systemSettingsIntents(): List<Intent> {
        return listOf(
            Intent("com.android.settings.TTS_SETTINGS"),
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
            Intent(Settings.ACTION_SETTINGS),
        ).map { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    }

    private fun speakReadyText(
        engine: TextToSpeech,
        text: String,
        language: LanguageOption,
    ): TtsSpeakResult {
        val candidates = SpeechLocaleResolver.resolveCandidates(
            language = language,
            text = text,
            defaultLocale = Locale.getDefault(),
        )
        val locale = candidates.firstOrNull { candidate ->
            engine.isLanguageAvailable(candidate) >= TextToSpeech.LANG_AVAILABLE
        }

        if (locale == null) {
            _state.value = _state.value.copy(
                status = TtsStatus.VOICE_MISSING,
                message = "设备未安装对应朗读语音",
            )
            return TtsSpeakResult(
                accepted = false,
                message = repairMessage("设备未安装对应朗读语音"),
            )
        }

        val languageResult = engine.setLanguage(locale)
        if (languageResult < TextToSpeech.LANG_AVAILABLE) {
            _state.value = _state.value.copy(
                status = TtsStatus.VOICE_MISSING,
                message = "设备未安装对应朗读语音",
                locale = locale,
            )
            return TtsSpeakResult(
                accepted = false,
                message = repairMessage("设备未安装对应朗读语音"),
            )
        }

        val utteranceId = "ai-translate-${System.nanoTime()}"
        val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) {
            _state.value = _state.value.copy(
                status = TtsStatus.ERROR,
                message = "朗读启动失败",
                locale = locale,
            )
            return TtsSpeakResult(
                accepted = false,
                message = repairMessage("朗读启动失败"),
            )
        }

        _state.value = _state.value.copy(
            status = TtsStatus.SPEAKING,
            message = "正在朗读",
            locale = locale,
        )
        return TtsSpeakResult(accepted = true)
    }

    private fun createProgressListener(): UtteranceProgressListener {
        return object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _state.value = _state.value.copy(
                    status = TtsStatus.SPEAKING,
                    message = "正在朗读",
                )
            }

            override fun onDone(utteranceId: String?) {
                _state.value = _state.value.copy(
                    status = TtsStatus.READY,
                    message = "朗读完成",
                )
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                notifyPlaybackError()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                notifyPlaybackError()
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                _state.value = _state.value.copy(
                    status = TtsStatus.READY,
                    message = if (interrupted) "朗读已中断" else "朗读已停止",
                )
            }
        }
    }

    private fun notifyPlaybackError() {
        _state.value = _state.value.copy(
            status = TtsStatus.ERROR,
            message = "朗读播放失败",
        )
        mainHandler.post {
            Toast.makeText(appContext, repairMessage("朗读播放失败"), Toast.LENGTH_SHORT).show()
        }
    }

    private fun shutdownEngine() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun queryTtsEngines(): List<TtsEngineSummary> {
        val intent = Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE)
        val services = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.packageManager.queryIntentServices(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            appContext.packageManager.queryIntentServices(intent, PackageManager.MATCH_ALL)
        }
        return services.mapNotNull { resolveInfo ->
            val serviceInfo = resolveInfo.serviceInfo ?: return@mapNotNull null
            val label = serviceInfo.loadLabel(appContext.packageManager)?.toString()
                ?.takeIf { it.isNotBlank() }
            TtsEngineSummary(
                packageName = serviceInfo.packageName,
                label = label ?: serviceInfo.packageName,
            )
        }.distinctBy { it.packageName }
    }

    private fun defaultEnginePackage(): String? {
        return Settings.Secure.getString(appContext.contentResolver, "tts_default_synth")
            ?.takeIf { it.isNotBlank() }
    }

    private fun findEngineSummary(packageName: String?): TtsEngineSummary? {
        if (packageName == null) return null
        return engines.firstOrNull { it.packageName == packageName }
    }

    private fun bestEngineLabel(): String? {
        val defaultEngine = defaultEnginePackage()
        return findEngineSummary(defaultEngine)?.label ?: engines.firstOrNull()?.label
    }

    private fun repairMessage(reason: String): String {
        return "$reason，请到设置 > 文本朗读修复"
    }
}

private data class TtsEngineSummary(
    val packageName: String,
    val label: String,
)
