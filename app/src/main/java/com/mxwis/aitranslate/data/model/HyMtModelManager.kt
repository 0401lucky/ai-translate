package com.mxwis.aitranslate.data.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class HyMtModelManager(
    context: Context,
    private val client: OkHttpClient,
) {
    private val modelDir = File(context.filesDir, "models")
    private val modelFile = File(modelDir, MODEL_FILE_NAME)

    private val _state = MutableStateFlow(readState())
    val state: StateFlow<ModelState> = _state.asStateFlow()

    fun isModelAvailable(): Boolean = modelFile.exists() && modelFile.length() >= MIN_VALID_MODEL_BYTES

    fun requireModelPath(): String {
        if (!isModelAvailable()) {
            error("离线模型未下载，请先到模型页下载 Hy-MT")
        }
        return modelFile.absolutePath
    }

    fun refresh() {
        _state.value = readState()
    }

    suspend fun downloadModel() = withContext(Dispatchers.IO) {
        if (_state.value.isDownloading) return@withContext
        modelDir.mkdirs()
        val tempFile = File(modelDir, "$MODEL_FILE_NAME.part")
        _state.value = readState().copy(isDownloading = true, errorMessage = null)

        try {
            val request = Request.Builder().url(MODEL_URL).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("模型下载失败：HTTP ${response.code}")
                }
                val body = response.body ?: error("模型下载失败：响应为空")
                val total = body.contentLength().takeIf { it > 0L } ?: EXPECTED_MODEL_BYTES
                tempFile.outputStream().use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var downloaded = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            _state.value = ModelState(
                                isAvailable = false,
                                isDownloading = true,
                                progress = (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f),
                                downloadedBytes = downloaded,
                                totalBytes = total,
                                filePath = modelFile.absolutePath,
                            )
                        }
                    }
                }
            }

            if (tempFile.length() < MIN_VALID_MODEL_BYTES) {
                tempFile.delete()
                error("模型文件过小，校验未通过")
            }
            if (modelFile.exists()) modelFile.delete()
            tempFile.renameTo(modelFile)
            _state.value = readState()
        } catch (error: Throwable) {
            tempFile.delete()
            _state.value = readState().copy(errorMessage = error.message ?: "模型下载失败")
        }
    }

    suspend fun deleteModel() = withContext(Dispatchers.IO) {
        if (modelFile.exists()) modelFile.delete()
        File(modelDir, "$MODEL_FILE_NAME.part").delete()
        _state.value = readState()
    }

    private fun readState(): ModelState {
        val available = isModelAvailable()
        return ModelState(
            isAvailable = available,
            progress = if (available) 1f else 0f,
            downloadedBytes = if (modelFile.exists()) modelFile.length() else 0L,
            totalBytes = EXPECTED_MODEL_BYTES,
            filePath = modelFile.absolutePath,
        )
    }

    companion object {
        const val MODEL_FILE_NAME = "HY-MT1.5-1.8B-Q4_K_M.gguf"
        const val MODEL_URL =
            "https://huggingface.co/tencent/HY-MT1.5-1.8B-GGUF/resolve/main/HY-MT1.5-1.8B-Q4_K_M.gguf?download=true"
        const val EXPECTED_MODEL_BYTES = 1_133_080_512L
        const val MIN_VALID_MODEL_BYTES = 1_000L * 1024L * 1024L
    }
}
