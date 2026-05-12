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
import java.security.MessageDigest

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
            tempFile.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var downloaded = 0L

                // R2 Wrangler 单文件上传限制为 300MiB，模型按分片分发，下载后重新拼接为 GGUF。
                MODEL_PARTS.forEachIndexed { index, part ->
                    val request = Request.Builder().url(part.url).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            error("模型分片下载失败：part${index.toString().padStart(2, '0')} HTTP ${response.code}")
                        }
                        val body = response.body ?: error("模型分片下载失败：响应为空")
                        var partDownloaded = 0L
                        body.byteStream().use { input ->
                            while (true) {
                                val read = input.read(buffer)
                                if (read == -1) break
                                output.write(buffer, 0, read)
                                partDownloaded += read
                                downloaded += read
                                _state.value = ModelState(
                                    isAvailable = false,
                                    isDownloading = true,
                                    progress = (downloaded.toFloat() / EXPECTED_MODEL_BYTES.toFloat()).coerceIn(0f, 1f),
                                    downloadedBytes = downloaded,
                                    totalBytes = EXPECTED_MODEL_BYTES,
                                    filePath = modelFile.absolutePath,
                                )
                            }
                        }
                        if (partDownloaded != part.sizeBytes) {
                            error("模型分片大小校验失败：part${index.toString().padStart(2, '0')}")
                        }
                    }
                }
            }

            if (tempFile.length() != EXPECTED_MODEL_BYTES) {
                tempFile.delete()
                error("模型文件大小校验失败")
            }
            if (!tempFile.sha256().equals(EXPECTED_MODEL_SHA256, ignoreCase = true)) {
                tempFile.delete()
                error("模型文件 SHA256 校验失败")
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
        private const val MODEL_BASE_URL = "https://pub-e16b86eab02f4594aaa4fd358cf6151e.r2.dev"
        const val EXPECTED_MODEL_BYTES = 1_133_080_512L
        const val MIN_VALID_MODEL_BYTES = 1_000L * 1024L * 1024L
        const val EXPECTED_MODEL_SHA256 =
            "4383AC0C3C8E476DE98FF979C2A3F069F8C4FB385E7860CF2D28DA896CC477C7"

        private val MODEL_PARTS = listOf(
            ModelPart("$MODEL_BASE_URL/models/parts/$MODEL_FILE_NAME.part00", 209_715_200L),
            ModelPart("$MODEL_BASE_URL/models/parts/$MODEL_FILE_NAME.part01", 209_715_200L),
            ModelPart("$MODEL_BASE_URL/models/parts/$MODEL_FILE_NAME.part02", 209_715_200L),
            ModelPart("$MODEL_BASE_URL/models/parts/$MODEL_FILE_NAME.part03", 209_715_200L),
            ModelPart("$MODEL_BASE_URL/models/parts/$MODEL_FILE_NAME.part04", 209_715_200L),
            ModelPart("$MODEL_BASE_URL/models/parts/$MODEL_FILE_NAME.part05", 84_504_512L),
        )
    }
}

private data class ModelPart(
    val url: String,
    val sizeBytes: Long,
)

private fun File.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read == -1) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02X".format(it) }
}
