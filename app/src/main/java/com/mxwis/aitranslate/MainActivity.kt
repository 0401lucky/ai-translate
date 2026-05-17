package com.mxwis.aitranslate

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mxwis.aitranslate.domain.ExternalTextInput
import com.mxwis.aitranslate.ui.AiTranslateApp
import com.mxwis.aitranslate.ui.TranslateViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TranslateViewModel by viewModels {
        val repository = (application as AiTranslateApplication).container.repository
        viewModelFactory {
            initializer {
                TranslateViewModel(
                    repository = repository,
                    imageTextRecognizer = (application as AiTranslateApplication).container.imageTextRecognizer,
                    dictionaryRepository = (application as AiTranslateApplication).container.dictionaryRepository,
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleExternalTextIntent(intent)
        setContent {
            AiTranslateApp(viewModel = viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleExternalTextIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        offerClipboardText()
    }

    private fun handleExternalTextIntent(intent: Intent?) {
        val text = ExternalTextInput.extractText(
            action = intent?.action,
            mimeType = intent?.type,
            processText = intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT),
            sharedText = intent?.getCharSequenceExtra(Intent.EXTRA_TEXT),
        ) ?: return

        viewModel.openMiniTranslator(text)
    }

    private fun offerClipboardText() {
        val clipboard = getSystemService(ClipboardManager::class.java) ?: return
        val description = clipboard.primaryClipDescription ?: return
        val isText = description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
            description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
        if (!isText) return

        val primaryClip = clipboard.primaryClip?.takeIf { it.itemCount > 0 } ?: return
        val text = ExternalTextInput.extractClipboardText(
            primaryClip.getItemAt(0)?.coerceToText(this),
        ) ?: return

        viewModel.offerClipboardQuickTranslate(text)
    }
}
