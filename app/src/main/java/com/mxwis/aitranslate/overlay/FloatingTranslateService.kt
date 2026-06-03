package com.mxwis.aitranslate.overlay

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.mxwis.aitranslate.AiTranslateApplication
import com.mxwis.aitranslate.R
import com.mxwis.aitranslate.data.ocr.ImageTextRecognizerContract
import com.mxwis.aitranslate.data.translation.TranslationRepository
import com.mxwis.aitranslate.domain.ExternalTextInput
import com.mxwis.aitranslate.domain.LanguageOption
import com.mxwis.aitranslate.domain.Languages
import com.mxwis.aitranslate.domain.TranslateRequest
import com.mxwis.aitranslate.speech.SystemTextSpeaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FloatingTranslateService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var windowManager: WindowManager
    private lateinit var repository: TranslationRepository
    private lateinit var imageTextRecognizer: ImageTextRecognizerContract
    private lateinit var speaker: SystemTextSpeaker
    private var bubbleView: View? = null
    private var menuView: View? = null
    private var selectionView: ScreenSelectionOverlayView? = null
    private var panelView: View? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WindowManager::class.java)
        val container = (application as AiTranslateApplication).container
        repository = container.repository
        imageTextRecognizer = container.imageTextRecognizer
        speaker = SystemTextSpeaker(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_HIDE -> stopSelf()
            ACTION_TRANSLATE_CLIPBOARD_TEXT -> showPanelAndTranslate(
                clipboardText = intent.getStringExtra(EXTRA_SOURCE_TEXT),
                errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE),
            )
            ACTION_SCREEN_CAPTURE_PERMISSION_GRANTED -> handleScreenCapturePermission(intent)
            ACTION_SCREEN_CAPTURE_PERMISSION_DENIED -> showMessagePanel(
                title = "截图翻译",
                body = "已取消屏幕捕获授权",
                detail = "需要授权后才能截取框选区域。App 不会后台录屏，也不会默认保存截图。",
            )
            else -> showBubble()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeSelection()
        removeMenu()
        removePanel()
        removeBubble()
        speaker.shutdown()
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

        val bubble = FrameLayout(this).apply {
            contentDescription = "悬浮翻译"
            background = floatingBubbleBackground()
            elevation = dp(10).toFloat()
            isClickable = true
            setPadding(dp(5), dp(5), dp(5), dp(5))
            setOnLongClickListener {
                stopSelf()
                true
            }
        }
        bubble.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.ic_floating_bubble_premium)
                scaleType = ImageView.ScaleType.FIT_CENTER
            },
            FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER),
        )

        val params = WindowManager.LayoutParams(
            dp(58),
            dp(58),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = resources.displayMetrics.widthPixels - dp(76)
            y = dp(220)
        }

        bubble.setOnTouchListener(FloatingDragTouchListener(params, ::toggleActionMenu))

        windowManager.addView(bubble, params)
        bubbleView = bubble
    }

    private fun toggleActionMenu() {
        if (menuView == null) {
            showActionMenu()
        } else {
            removeMenu()
        }
    }

    private fun showActionMenu() {
        if (!Settings.canDrawOverlays(this)) return
        removeMenu()

        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = roundedBackground(Color.WHITE, dp(18))
            elevation = dp(12).toFloat()
        }
        menu.addView(menuButton("剪贴板") {
            removeMenu()
            openClipboardBridge()
        })
        menu.addView(menuButton("截图") {
            removeMenu()
            requestScreenCapturePermission()
        })

        val params = WindowManager.LayoutParams(
            dp(136),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = resources.displayMetrics.widthPixels - dp(170)
            y = dp(286)
        }

        windowManager.addView(menu, params)
        menuView = menu
    }

    private fun menuButton(text: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(BODY_TEXT)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
            minHeight = dp(48)
            setPadding(dp(12), 0, dp(12), 0)
            setOnClickListener { onClick() }
        }
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

    private fun requestScreenCapturePermission() {
        val intent = Intent(this, ScreenCaptureBridgeActivity::class.java)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION,
            )
        runCatching { startActivity(intent) }
            .onFailure {
                showMessagePanel(
                    title = "截图翻译",
                    body = "无法打开屏幕捕获授权",
                    detail = "请回到 App 内重试，或检查系统是否限制悬浮窗启动授权页面。",
                )
            }
    }

    private fun handleScreenCapturePermission(intent: Intent) {
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_SCREEN_CAPTURE_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_SCREEN_CAPTURE_DATA)
        }
        if (data == null) {
            showMessagePanel(
                title = "截图翻译",
                body = "屏幕捕获授权无效",
                detail = "请重新点击悬浮球并授权截图翻译。",
            )
            return
        }
        val resultCode = intent.getIntExtra(EXTRA_SCREEN_CAPTURE_RESULT_CODE, 0)
        showSelectionOverlay(resultCode, data)
    }

    private fun showSelectionOverlay(resultCode: Int, projectionData: Intent) {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "悬浮窗权限已关闭", Toast.LENGTH_SHORT).show()
            return
        }
        removePanel()
        removeMenu()
        removeSelection()

        val overlay = ScreenSelectionOverlayView(
            context = this,
            onConfirmSelection = { selection ->
                removeSelection()
                removeBubble()
                serviceScope.launch {
                    captureSelectionAndTranslate(
                        resultCode = resultCode,
                        projectionData = projectionData,
                        selection = selection,
                    )
                }
            },
            onCancelSelection = {
                removeSelection()
                showBubble()
            },
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        windowManager.addView(overlay, params)
        selectionView = overlay
    }

    private suspend fun captureSelectionAndTranslate(
        resultCode: Int,
        projectionData: Intent,
        selection: SelectionBounds,
    ) {
        runCatching {
            startProjectionForeground()
            delay(220)
            captureSelectedBitmap(resultCode, projectionData, selection)
        }.onSuccess { bitmap ->
            stopProjectionForeground()
            showBubble()
            showScreenshotTranslationPanel(bitmap)
        }.onFailure { error ->
            stopProjectionForeground()
            showBubble()
            showMessagePanel(
                title = "截图翻译",
                body = "截图失败",
                detail = screenshotFailureDetail(error),
            )
        }
    }

    private suspend fun captureSelectedBitmap(
        resultCode: Int,
        projectionData: Intent,
        selection: SelectionBounds,
    ): Bitmap {
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val densityDpi = metrics.densityDpi
        val projectionManager = getSystemService(MediaProjectionManager::class.java)
        val projection = projectionManager.getMediaProjection(resultCode, projectionData)
            ?: error("未获得屏幕捕获权限")
        return try {
            val fullBitmap = captureOneFrame(
                projection = projection,
                width = screenWidth,
                height = screenHeight,
                densityDpi = densityDpi,
            )
            val cropRect = ScreenshotSelectionBoundsPolicy.scaleToBitmap(
                selection = selection,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                bitmapWidth = fullBitmap.width,
                bitmapHeight = fullBitmap.height,
            )
            require(ScreenshotSelectionBoundsPolicy.isValid(cropRect)) { "框选区域太小" }
            Bitmap.createBitmap(
                fullBitmap,
                cropRect.left,
                cropRect.top,
                cropRect.width.coerceAtLeast(1),
                cropRect.height.coerceAtLeast(1),
            ).also {
                if (it !== fullBitmap) fullBitmap.recycle()
            }
        } finally {
            projection.stop()
        }
    }

    private suspend fun captureOneFrame(
        projection: MediaProjection,
        width: Int,
        height: Int,
        densityDpi: Int,
    ): Bitmap = withTimeout(4_000L) {
        suspendCancellableCoroutine { continuation ->
            val mainHandler = Handler(Looper.getMainLooper())
            val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            var completed = false
            var callbackRegistered = false
            var virtualDisplay: VirtualDisplay? = null
            lateinit var projectionCallback: MediaProjection.Callback

            fun cleanup() {
                runCatching { virtualDisplay?.release() }
                virtualDisplay = null
                runCatching { imageReader.setOnImageAvailableListener(null, null) }
                runCatching { imageReader.close() }
                if (callbackRegistered) {
                    runCatching { projection.unregisterCallback(projectionCallback) }
                    callbackRegistered = false
                }
            }

            fun completeWithBitmap(bitmap: Bitmap) {
                if (completed) {
                    bitmap.recycle()
                    return
                }
                completed = true
                cleanup()
                continuation.resume(bitmap)
            }

            fun completeWithError(error: Throwable) {
                if (completed) return
                completed = true
                cleanup()
                continuation.resumeWithException(error)
            }

            projectionCallback = object : MediaProjection.Callback() {
                override fun onStop() {
                    completeWithError(IllegalStateException("屏幕捕获已被系统停止，请重新授权后再试。"))
                }
            }

            runCatching {
                projection.registerCallback(projectionCallback, mainHandler)
            }.onSuccess {
                callbackRegistered = true
            }.onFailure { error ->
                imageReader.close()
                continuation.resumeWithException(error)
                return@suspendCancellableCoroutine
            }

            virtualDisplay = runCatching {
                projection.createVirtualDisplay(
                    "ai-translate-screen-capture",
                    width,
                    height,
                    densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.surface,
                    null,
                    null,
                )
            }.getOrElse { error ->
                completeWithError(error)
                return@suspendCancellableCoroutine
            }

            if (virtualDisplay == null) {
                completeWithError(IllegalStateException("无法创建屏幕捕获会话"))
                return@suspendCancellableCoroutine
            }

            imageReader.setOnImageAvailableListener({ reader ->
                if (completed) return@setOnImageAvailableListener
                val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                runCatching {
                    val plane = image.planes.first()
                    val buffer = plane.buffer
                    val pixelStride = plane.pixelStride
                    val rowStride = plane.rowStride
                    val rowPadding = rowStride - pixelStride * width
                    val paddedWidth = width + rowPadding / pixelStride
                    val paddedBitmap = Bitmap.createBitmap(paddedWidth, height, Bitmap.Config.ARGB_8888)
                    paddedBitmap.copyPixelsFromBuffer(buffer)
                    Bitmap.createBitmap(paddedBitmap, 0, 0, width, height).also {
                        paddedBitmap.recycle()
                    }
                }.onSuccess { bitmap ->
                    image.close()
                    completeWithBitmap(bitmap)
                }.onFailure { error ->
                    image.close()
                    completeWithError(error)
                }
            }, mainHandler)
            continuation.invokeOnCancellation {
                completed = true
                cleanup()
            }
        }
    }

    private fun screenshotFailureDetail(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("register a callback", ignoreCase = true) ->
                "系统截图会话启动顺序异常，请重新点击截图翻译并授权。"
            message.contains("Timed out", ignoreCase = true) ->
                "系统没有及时返回截图画面，请重新框选或再授权一次。"
            message.contains("VirtualDisplay", ignoreCase = true) ->
                "系统无法创建截图画面，请重新授权后再试。"
            message.any { it.code > 127 } -> message
            else -> "截图没有启动成功，请重新点击截图翻译并授权后再试。"
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
        val sourceText = scrollableBodyText("正在读取剪贴板...", BODY_TEXT)
        val statusText = bodyText("等待翻译", SUB_TEXT, maxLines = 2)
        val resultText = scrollableBodyText("译文会显示在这里", SUB_TEXT)
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
        panel.addView(scrollableCard(sourceText, maxHeightDp = 112))
        panel.addView(sectionLabel("状态"))
        panel.addView(statusText)
        panel.addView(sectionLabel("译文"))
        panel.addView(scrollableCard(resultText, maxHeightDp = 200))

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

    private fun showScreenshotTranslationPanel(bitmap: Bitmap) {
        removePanel()
        val preview = ImageView(this).apply {
            setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = roundedBackground(Color.rgb(248, 250, 252), dp(8))
        }
        val sourceText = scrollableBodyText("正在识别截图文字...", BODY_TEXT)
        val statusText = bodyText("正在识别", SUB_TEXT, maxLines = 2)
        val resultText = scrollableBodyText("译文会显示在这里", SUB_TEXT)
        val copyButton = actionButton("复制译文").apply { isEnabled = false }
        val speakButton = actionButton("朗读译文").apply { isEnabled = false }
        val reselectButton = actionButton("重新框选")

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
        header.addView(titleBlock("截图翻译", "来自框选区域"), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(actionButton("关闭").apply { setOnClickListener { removePanel() } })

        panel.addView(header)
        panel.addView(sectionLabel("截图预览"))
        panel.addView(preview, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(112)))
        panel.addView(sectionLabel("识别文字"))
        panel.addView(scrollableCard(sourceText, maxHeightDp = 132))
        panel.addView(sectionLabel("状态"))
        panel.addView(statusText)
        panel.addView(sectionLabel("译文"))
        panel.addView(scrollableCard(resultText, maxHeightDp = 192))

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(8), 0, 0)
        }
        actions.addView(reselectButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        actions.addView(speakButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        actions.addView(copyButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        panel.addView(actions)

        reselectButton.setOnClickListener {
            removePanel()
            requestScreenCapturePermission()
        }

        val panelWidth = (resources.displayMetrics.widthPixels - dp(32)).coerceAtMost(dp(420))
        val params = WindowManager.LayoutParams(
            panelWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.CENTER
        }

        windowManager.addView(panel, params)
        panelView = panel
        serviceScope.launch {
            recognizeAndTranslateScreenshot(
                bitmap = bitmap,
                sourceTextView = sourceText,
                statusTextView = statusText,
                resultTextView = resultText,
                speakButton = speakButton,
                copyButton = copyButton,
            )
        }
    }

    private suspend fun recognizeAndTranslateScreenshot(
        bitmap: Bitmap,
        sourceTextView: TextView,
        statusTextView: TextView,
        resultTextView: TextView,
        speakButton: Button,
        copyButton: Button,
    ) {
        runCatching { imageTextRecognizer.recognize(bitmap) }
            .onSuccess { recognizedText ->
                sourceTextView.text = recognizedText
                statusTextView.text = "正在翻译..."
                resultTextView.text = "请稍候"
                translateOverlayText(
                    sourceText = recognizedText,
                    statusTextView = statusTextView,
                    resultTextView = resultTextView,
                    speakButton = speakButton,
                    copyButton = copyButton,
                )
            }
            .onFailure { error ->
                statusTextView.text = error.message ?: "截图文字识别失败"
                sourceTextView.text = "未识别到文字"
                resultTextView.text = "暂无译文"
                resultTextView.setTextColor(SUB_TEXT)
            }
    }

    private suspend fun translateOverlayText(
        sourceText: String,
        statusTextView: TextView,
        resultTextView: TextView,
        speakButton: Button,
        copyButton: Button,
    ) {
        runCatching {
            val settings = repository.settings.first()
            repository.translate(
                request = TranslateRequest(
                    sourceText = sourceText,
                    sourceLanguage = Languages.auto,
                    targetLanguage = Languages.supported.first(),
                ),
                mode = settings.defaultMode,
            )
        }.onSuccess { output ->
            statusTextView.text = "已使用${output.displayModeLabel}翻译"
            resultTextView.text = output.translatedText
            resultTextView.setTextColor(BODY_TEXT)
            speakButton.isEnabled = true
            copyButton.isEnabled = true
            speakButton.setOnClickListener {
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

    private fun showMessagePanel(
        title: String,
        body: String,
        detail: String,
    ) {
        removePanel()
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
        header.addView(titleBlock(title, body), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(actionButton("关闭").apply { setOnClickListener { removePanel() } })
        panel.addView(header)
        panel.addView(sectionLabel("提示"))
        panel.addView(card(bodyText(detail, SUB_TEXT, maxLines = 6)))

        val panelWidth = (resources.displayMetrics.widthPixels - dp(32)).coerceAtMost(dp(420))
        val params = WindowManager.LayoutParams(
            panelWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.CENTER
        }
        windowManager.addView(panel, params)
        panelView = panel
        showBubble()
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
                statusTextView.text = "已使用${output.displayModeLabel}翻译"
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
        val result = speaker.speak(text, language)
        if (!result.accepted && result.message != null) {
            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun titleBlock(): LinearLayout {
        return titleBlock("悬浮翻译", "来自剪贴板")
    }

    private fun titleBlock(title: String, subtitle: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleText(title))
            addView(bodyText(subtitle, SUB_TEXT, maxLines = 1))
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

    private fun scrollableCard(content: TextView, maxHeightDp: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(8), dp(10))
            background = roundedBackground(Color.rgb(248, 250, 252), dp(8))
            addView(
                ScrollView(this@FloatingTranslateService).apply {
                    isFillViewport = false
                    isVerticalScrollBarEnabled = true
                    scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
                    overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
                    addView(
                        content,
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                        ),
                    )
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(maxHeightDp),
                ),
            )
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

    private fun scrollableBodyText(text: String, color: Int): TextView {
        return bodyText(text, color, maxLines = Int.MAX_VALUE).apply {
            ellipsize = null
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

    private fun startProjectionForeground() {
        val notification = buildProjectionNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                SCREEN_CAPTURE_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION,
            )
        } else {
            startForeground(SCREEN_CAPTURE_NOTIFICATION_ID, notification)
        }
    }

    private fun stopProjectionForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun buildProjectionNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SCREEN_CAPTURE_CHANNEL_ID,
                "截图翻译",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "仅在用户主动框选截图翻译时显示"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, SCREEN_CAPTURE_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setSmallIcon(android.R.drawable.ic_menu_crop)
            .setContentTitle("正在截取框选区域")
            .setContentText("截图完成后会立即停止屏幕捕获")
            .setOngoing(true)
            .build()
    }

    private fun removeMenu() {
        menuView?.let { runCatching { windowManager.removeView(it) } }
        menuView = null
    }

    private fun removeSelection() {
        selectionView?.let { runCatching { windowManager.removeView(it) } }
        selectionView = null
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

    private fun floatingBubbleBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.TRANSPARENT)
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
        const val ACTION_SCREEN_CAPTURE_PERMISSION_GRANTED =
            "com.mxwis.aitranslate.overlay.SCREEN_CAPTURE_PERMISSION_GRANTED"
        const val ACTION_SCREEN_CAPTURE_PERMISSION_DENIED =
            "com.mxwis.aitranslate.overlay.SCREEN_CAPTURE_PERMISSION_DENIED"
        const val EXTRA_SOURCE_TEXT = "com.mxwis.aitranslate.overlay.extra.SOURCE_TEXT"
        const val EXTRA_ERROR_MESSAGE = "com.mxwis.aitranslate.overlay.extra.ERROR_MESSAGE"
        const val EXTRA_SCREEN_CAPTURE_RESULT_CODE = "com.mxwis.aitranslate.overlay.extra.SCREEN_CAPTURE_RESULT_CODE"
        const val EXTRA_SCREEN_CAPTURE_DATA = "com.mxwis.aitranslate.overlay.extra.SCREEN_CAPTURE_DATA"
        private const val BODY_TEXT = 0xFF0F172A.toInt()
        private const val SUB_TEXT = 0xFF64748B.toInt()
        private const val SCREEN_CAPTURE_CHANNEL_ID = "screen_capture_translate"
        private const val SCREEN_CAPTURE_NOTIFICATION_ID = 3021
    }
}
