package com.mxwis.aitranslate.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mxwis.aitranslate.domain.LanguageOption
import com.mxwis.aitranslate.domain.SpeechLocaleResolver

@Composable
fun rememberTextSpeaker(): TextSpeaker {
    val context = LocalContext.current
    val speaker = remember(context) {
        TextSpeaker(context.applicationContext)
    }
    DisposableEffect(speaker) {
        onDispose { speaker.shutdown() }
    }
    return speaker
}

class TextSpeaker(
    private val appContext: Context,
) : TextToSpeech.OnInitListener {
    private var engine: TextToSpeech? = TextToSpeech(appContext, this)
    private var isReady = false
    private var hasInitFailed = false

    override fun onInit(status: Int) {
        isReady = status == TextToSpeech.SUCCESS
        hasInitFailed = !isReady
    }

    fun speak(text: String, language: LanguageOption) {
        val content = text.trim()
        if (content.isBlank()) {
            Toast.makeText(appContext, "暂无可朗读文本", Toast.LENGTH_SHORT).show()
            return
        }

        val tts = engine
        when {
            tts == null || hasInitFailed -> {
                Toast.makeText(appContext, "系统朗读不可用", Toast.LENGTH_SHORT).show()
            }
            !isReady -> {
                Toast.makeText(appContext, "朗读正在准备中", Toast.LENGTH_SHORT).show()
            }
            else -> speakReadyText(tts, content, language)
        }
    }

    fun shutdown() {
        engine?.stop()
        engine?.shutdown()
        engine = null
    }

    private fun speakReadyText(
        tts: TextToSpeech,
        text: String,
        language: LanguageOption,
    ) {
        val locale = SpeechLocaleResolver.resolve(language = language, text = text)
        val availability = tts.isLanguageAvailable(locale)
        if (availability < TextToSpeech.LANG_AVAILABLE) {
            Toast.makeText(appContext, "设备未安装对应朗读语音", Toast.LENGTH_SHORT).show()
            return
        }

        tts.language = locale
        val result = tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "ai-translate-${System.nanoTime()}",
        )
        if (result == TextToSpeech.ERROR) {
            Toast.makeText(appContext, "朗读启动失败", Toast.LENGTH_SHORT).show()
        }
    }
}
