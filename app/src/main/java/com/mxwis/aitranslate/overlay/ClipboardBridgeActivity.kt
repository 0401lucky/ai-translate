package com.mxwis.aitranslate.overlay

import android.app.Activity
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.mxwis.aitranslate.domain.ExternalTextInput

class ClipboardBridgeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        translateClipboardFromForeground()
    }

    private fun translateClipboardFromForeground() {
        val text = readClipboardText()
        val intent = Intent(this, FloatingTranslateService::class.java).apply {
            action = FloatingTranslateService.ACTION_TRANSLATE_CLIPBOARD_TEXT
            if (text == null) {
                putExtra(
                    FloatingTranslateService.EXTRA_ERROR_MESSAGE,
                    "剪贴板为空或系统限制读取，请复制文本后再点悬浮球。",
                )
            } else {
                putExtra(FloatingTranslateService.EXTRA_SOURCE_TEXT, text)
            }
        }
        startService(intent)
        if (text == null) {
            Toast.makeText(this, "未读取到剪贴板文本", Toast.LENGTH_SHORT).show()
        }
        finish()
        overridePendingTransition(0, 0)
    }

    private fun readClipboardText(): String? {
        return runCatching {
            val clipboard = getSystemService(ClipboardManager::class.java) ?: return null
            val description = clipboard.primaryClipDescription ?: return null
            val isText = description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
                description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
            if (!isText) return null

            val clip = clipboard.primaryClip?.takeIf { it.itemCount > 0 } ?: return null
            ExternalTextInput.extractClipboardText(clip.getItemAt(0).coerceToText(this))
        }.getOrNull()
    }
}
