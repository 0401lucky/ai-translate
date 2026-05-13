package com.mxwis.aitranslate.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mxwis.aitranslate.speech.SystemTextSpeaker

@Composable
fun rememberTextSpeaker(): SystemTextSpeaker {
    val context = LocalContext.current
    val speaker = remember(context) {
        SystemTextSpeaker(context.applicationContext)
    }
    DisposableEffect(speaker) {
        onDispose { speaker.shutdown() }
    }
    return speaker
}
