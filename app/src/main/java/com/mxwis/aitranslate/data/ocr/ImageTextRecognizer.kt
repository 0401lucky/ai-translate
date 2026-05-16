package com.mxwis.aitranslate.data.ocr

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

interface ImageTextRecognizerContract {
    suspend fun recognize(uriString: String): String
}

class MlKitImageTextRecognizer(
    context: Context,
) : ImageTextRecognizerContract {
    private val appContext = context.applicationContext
    private val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val chineseRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    override suspend fun recognize(uriString: String): String = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        val image = InputImage.fromFilePath(appContext, uri)
        val chineseText = chineseRecognizer.process(image).await()
        val latinText = latinRecognizer.process(image).await()
        mergeRecognizedText(chineseText, latinText).ifBlank {
            error("未识别到文字，请换一张更清晰的图片")
        }
    }

    private fun mergeRecognizedText(vararg results: Text): String {
        return results
            .flatMap { result ->
                result.textBlocks.flatMap { block -> block.lines.map { it.text.trim() } }
            }
            .filter { it.isNotBlank() }
            .distinctBy { it.replace("\\s+".toRegex(), " ").lowercase() }
            .joinToString("\n")
    }
}

private suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { error -> continuation.resumeWithException(error) }
        addOnCanceledListener { continuation.cancel() }
    }
}
