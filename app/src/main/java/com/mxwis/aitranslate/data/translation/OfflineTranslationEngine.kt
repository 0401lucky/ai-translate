package com.mxwis.aitranslate.data.translation

import com.mxwis.aitranslate.data.model.HyMtModelManager
import com.mxwis.aitranslate.domain.TranslateRequest
import com.llamatik.library.platform.LlamaBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class OfflineTranslationEngine(
    private val modelManager: HyMtModelManager,
) {
    private val loadMutex = Mutex()
    private var loadedModelPath: String? = null

    suspend fun translate(request: TranslateRequest): String = withContext(Dispatchers.Default) {
        require(request.sourceText.isNotBlank()) { "请输入要翻译的文本" }
        val modelPath = modelManager.requireModelPath()
        ensureModelLoaded(modelPath)

        val prompt = buildHyMtPrompt(request)
        val renderedPrompt = runCatching {
            LlamaBridge.applyChatTemplate(
                messages = listOf("user" to prompt),
                addAssistantPrefix = true,
            )
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: prompt

        val rawOutput = runCatching {
            LlamaBridge.generate(renderedPrompt)
        }.getOrElse { error ->
            throw IllegalStateException("离线翻译失败：${error.readableMessage()}", error)
        }

        cleanOutput(rawOutput, prompt).also {
            require(it.isNotBlank()) { "离线翻译结果为空" }
        }
    }

    private suspend fun ensureModelLoaded(modelPath: String) {
        if (loadedModelPath == modelPath) return
        loadMutex.withLock {
            if (loadedModelPath == modelPath) return

            runCatching {
                LlamaBridge.updateGenerateParams(
                    temperature = 0.7f,
                    maxTokens = 512,
                    topP = 0.6f,
                    topK = 20,
                    repeatPenalty = 1.05f,
                    contextLength = 2048,
                    numThreads = Runtime.getRuntime().availableProcessors().coerceIn(2, 6),
                    useMmap = true,
                    flashAttention = false,
                    batchSize = 512,
                )
                LlamaBridge.initGenerateModel(modelPath)
            }.getOrElse { error ->
                throw IllegalStateException("离线推理库加载失败：${error.readableMessage()}", error)
            }.also { success ->
                check(success) {
                    "模型文件完整，但当前推理库仍未能加载 HY-MT Q4_K_M。请确认已下载标准 Q4_K_M GGUF，或后续改用自编译 llama.cpp Android 内核。"
                }
                loadedModelPath = modelPath
            }
        }
    }

    companion object {
        internal fun buildHyMtPrompt(request: TranslateRequest): String {
            return if (request.targetLanguage.code.startsWith("zh")) {
                """
                    Translate the following segment into ${request.targetLanguage.promptName}, without additional explanation.

                    ${request.sourceText}
                """.trimIndent()
            } else {
                """
                    Translate the following segment into ${request.targetLanguage.promptName}, without additional explanation.

                    ${request.sourceText}
                """.trimIndent()
            }
        }

        internal fun cleanOutput(rawOutput: String, prompt: String): String {
            return rawOutput
                .replace(prompt, "")
                .replace("<|im_end|>", "")
                .replace("<|endoftext|>", "")
                .replace("<end_of_turn>", "")
                .substringAfterLast("<start_of_turn>assistant\n")
                .trim()
                .trim('"')
                .trim()
        }

        private fun Throwable.readableMessage(): String {
            return when (this) {
                is UnsatisfiedLinkError -> "当前 APK 缺少可用 native 推理库，或设备 ABI 不受支持"
                else -> message ?: javaClass.simpleName
            }
        }
    }
}
