package com.mxwis.aitranslate.overlay

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mxwis.aitranslate.AiTranslateApplication
import com.mxwis.aitranslate.data.translation.TranslationRepository
import com.mxwis.aitranslate.domain.ExternalTextInput
import com.mxwis.aitranslate.domain.LanguageOption
import com.mxwis.aitranslate.domain.Languages
import com.mxwis.aitranslate.domain.SpeechLocaleResolver
import com.mxwis.aitranslate.domain.TranslateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FloatingTranslateService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var windowManager: WindowManager
    private lateinit var repository: TranslationRepository
    private var bubbleView: View? = null
    private var panelView: View? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var hasTtsFailed = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WindowManager::class.java)
        repository = (application as AiTranslateApplication).container.repository
        tts = TextToSpeech(this) { status ->
            isTtsReady = status == TextToSpeech.SUCCESS
            hasTtsFailed = !isTtsReady
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_HIDE -> stopSelf()
            ACTION_TRANSLATE_CLIPBOARD_TEXT -> showPanelAndTranslate(
                clipboardText = intent.getStringExtra(EXTRA_SOURCE_TEXT),
                errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE),
            )
            else -> showBubble()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removePanel()
        removeBubble()
        tts?.stop()
        tts?.shutdown()
        tts = null
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun showBubble() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先授权悬浮窗权限", Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }
        if (bubbleView != null) return

        val bubble = TextView(this).apply {
            text = "译"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            contentDescription = "悬浮翻译"
            background = roundedBackground(PRIMARY_BLUE, dp(28))
            elevation = dp(8).toFloat()
            setOnLongClickListener {
                stopSelf()
                true
            }
        }

        val params = WindowManager.LayoutParams(
            dp(56),
            dp(56),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = resources.displayMetrics.widthPixels - dp(76)
            y = dp(220)
        }

        bubble.setOnTouchListener(FloatingDragTouchListener(params, ::openClipboardBridge))

        windowManager.addView(bubble, params)
        bubbleView = bubble
    }

    private fun openClipboardBridge() {
        val intent = Intent(this, ClipboardBridgeActivity::class.java)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION,
            )
        runCatching { startActivity(intent) }
            .onFailure {
                showPanelAndTranslate(
                    clipboardText = null,
                    errorMessage = "无法打开剪贴板读取入口，请回到 App 内使用剪贴板快捷翻译。",
                )
            }
    }

    private fun showPanelAndTranslate(
        clipboardText: String?,
        errorMessage: String? = null,
    ) {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "悬浮窗权限已关闭", Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }

        removePanel()
        val sourceText = bodyText("正在读取剪贴板...", BODY_TEXT, maxLines = 4)
        val statusText = bodyText("等待翻译", SUB_TEXT, maxLines = 2)
        val resultText = bodyText("译文会显示在这里", SUB_TEXT, maxLines = 8)
        val sourceSpeakButton = actionButton("朗读原文").apply { isEnabled = false }
        val resultSpeakButton = actionButton("朗读译文").apply { isEnabled = false }
        val copyButton = actionButton("复制译文").apply {
            isEnabled = false
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(16))
            background = roundedBackground(Color.WHITE, dp(18))
            elevation = dp(12).toFloat()
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(titleBlock(), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(actionButton("关闭").apply { setOnClickListener { removePanel() } })

        panel.addView(header)
        panel.addView(sectionLabel("原文"))
        panel.addView(card(sourceText))
        panel.addView(sectionLabel("状态"))
        panel.addView(statusText)
        panel.addView(sectionLabel("译文"))
        panel.addView(card(resultText))

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(8), 0, 0)
        }
        actions.addView(sourceSpeakButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        actions.addView(resultSpeakButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        actions.addView(copyButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        panel.addView(actions)

        val panelWidth = (resources.displayMetrics.widthPixels - dp(32)).coerceAtMost(dp(420))
        val params = WindowManager.LayoutParams(
            panelWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.CENTER
            x = 0
            y = 0
        }

        windowManager.addView(panel, params)
        panelView = panel
        panel.post {
            translateClipboard(
                providedText = clipboardText,
                clipboardError = errorMessage,
                sourceTextView = sourceText,
                statusTextView = statusText,
                resultTextView = resultText,
                sourceSpeakButton = sourceSpeakButton,
                resultSpeakButton = resultSpeakButton,
                copyButton = copyButton,
            )
        }
    }

    private fun translateClipboard(
        providedText: String?,
        clipboardError: String?,
        sourceTextView: TextView,
        statusTextView: TextView,
        resultTextView: TextView,
        sourceSpeakButton: Button,
        resultSpeakButton: Button,
        copyButton: Button,
    ) {
        val clipboardText = ExternalTextInput.extractClipboardText(providedText)
        if (clipboardText == null) {
            sourceTextView.text = "剪贴板为空或系统限制读取"
            statusTextView.text = clipboardError ?: "请复制文本后再点悬浮球；若仍失败，请回到 App 内使用剪贴板快捷翻译。"
            resultTextView.text = "暂无译文"
            resultTextView.setTextColor(SUB_TEXT)
            return
        }

        sourceTextView.text = clipboardText
        sourceSpeakButton.isEnabled = true
        sourceSpeakButton.setOnClickListener {
            speakText(clipboardText, Languages.auto)
        }
        statusTextView.text = "正在翻译..."
        resultTextView.text = "请稍候"

        serviceScope.launch {
            runCatching {
                val settings = repository.settings.first()
                repository.translate(
                    request = TranslateRequest(
                        sourceText = clipboardText,
                        sourceLanguage = Languages.auto,
                        targetLanguage = Languages.supported.first(),
                    ),
                    mode = settings.defaultMode,
                )
            }.onSuccess { output ->
                statusTextView.text = "已使用${output.usedMode.label}翻译"
                resultTextView.text = output.translatedText
                resultTextView.setTextColor(BODY_TEXT)
                copyButton.isEnabled = true
                resultSpeakButton.isEnabled = true
                resultSpeakButton.setOnClickListener {
                    speakText(output.translatedText, Languages.supported.first())
                }
                copyButton.setOnClickListener {
                    val clipboard = getSystemService(ClipboardManager::class.java)
                    clipboard.setPrimaryClip(ClipData.newPlainText("译文", output.translatedText))
                    Toast.makeText(this@FloatingTranslateService, "已复制译文", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { error ->
                statusTextView.text = error.message ?: "翻译失败，请稍后重试"
                resultTextView.text = "暂无译文"
                resultTextView.setTextColor(SUB_TEXT)
            }
        }
    }

    private fun speakText(text: String, language: LanguageOption) {
        val content = text.trim()
        if (content.isBlank()) {
            Toast.makeText(this, "暂无可朗读文本", Toast.LENGTH_SHORT).show()
            return
        }

        val engine = tts
        when {
            engine == null || hasTtsFailed -> {
                Toast.makeText(this, "系统朗读不可用", Toast.LENGTH_SHORT).show()
            }
            !isTtsReady -> {
                Toast.makeText(this, "朗读正在准备中", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val locale = SpeechLocaleResolver.resolve(language, content)
                if (engine.isLanguageAvailable(locale) < TextToSpeech.LANG_AVAILABLE) {
                    Toast.makeText(this, "设备未安装对应朗读语音", Toast.LENGTH_SHORT).show()
                    return
                }
                engine.language = locale
                val result = engine.speak(
                    content,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "ai-translate-overlay-${System.nanoTime()}",
                )
                if (result == TextToSpeech.ERROR) {
                    Toast.makeText(this, "朗读启动失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun titleBlock(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleText("悬浮翻译"))
            addView(bodyText("来自剪贴板", SUB_TEXT, maxLines = 1))
        }
    }

    private fun sectionLabel(text: String): TextView {
        return bodyText(text, SUB_TEXT, maxLines = 1).apply {
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(12), 0, dp(6))
        }
    }

    private fun card(content: TextView): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = roundedBackground(Color.rgb(248, 250, 252), dp(8))
            addView(content)
        }
    }

    private fun titleText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(BODY_TEXT)
        }
    }

    private fun bodyText(text: String, color: Int, maxLines: Int): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(color)
            this.maxLines = maxLines
            ellipsize = android.text.TextUtils.TruncateAt.END
            setLineSpacing(dp(2).toFloat(), 1f)
        }
    }

    private fun actionButton(text: String): Button {
        return Button(this).apply {
            this.text = text
            textSize = 13f
            minHeight = dp(40)
            minWidth = dp(72)
        }
    }

    private fun removePanel() {
        panelView?.let { runCatching { windowManager.removeView(it) } }
        panelView = null
    }

    private fun removeBubble() {
        bubbleView?.let { runCatching { windowManager.removeView(it) } }
        bubbleView = null
    }

    private fun roundedBackground(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private inner class FloatingDragTouchListener(
        private val params: WindowManager.LayoutParams,
        private val onClick: () -> Unit,
    ) : View.OnTouchListener {
        private val touchSlop = ViewConfiguration.get(this@FloatingTranslateService).scaledTouchSlop
        private var startX = 0
        private var startY = 0
        private var downRawX = 0f
        private var downRawY = 0f
        private var moved = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    downRawX = event.rawX
                    downRawY = event.rawY
                    moved = false
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downRawX).toInt()
                    val dy = (event.rawY - downRawY).toInt()
                    if (kotlin.math.abs(dx) > touchSlop || kotlin.math.abs(dy) > touchSlop) {
                        moved = true
                    }
                    params.x = startX + dx
                    params.y = (startY + dy).coerceAtLeast(dp(24))
                    runCatching { windowManager.updateViewLayout(view, params) }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        view.performClick()
                        onClick()
                    }
                    return true
                }
            }
            return false
        }
    }

    companion object {
        const val ACTION_SHOW = "com.mxwis.aitranslate.overlay.SHOW"
        const val ACTION_HIDE = "com.mxwis.aitranslate.overlay.HIDE"
        const val ACTION_TRANSLATE_CLIPBOARD_TEXT = "com.mxwis.aitranslate.overlay.TRANSLATE_CLIPBOARD_TEXT"
        const val EXTRA_SOURCE_TEXT = "com.mxwis.aitranslate.overlay.extra.SOURCE_TEXT"
        const val EXTRA_ERROR_MESSAGE = "com.mxwis.aitranslate.overlay.extra.ERROR_MESSAGE"
        private const val PRIMARY_BLUE = 0xFF2563EB.toInt()
        private const val BODY_TEXT = 0xFF0F172A.toInt()
        private const val SUB_TEXT = 0xFF64748B.toInt()
    }
}
