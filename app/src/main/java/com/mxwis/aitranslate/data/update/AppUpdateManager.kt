package com.mxwis.aitranslate.data.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

data class AppUpdateRelease(
    val versionCode: Int,
    val versionName: String,
    val required: Boolean,
    val apkUrl: String,
    val sha256: String,
    val sizeBytes: Long,
    val notes: List<String>,
)

sealed class AppUpdateCheckResult {
    data class Available(val release: AppUpdateRelease) : AppUpdateCheckResult()
    data class UpToDate(val latestVersionName: String) : AppUpdateCheckResult()
    data class PackageUnavailable(val release: AppUpdateRelease) : AppUpdateCheckResult()
}

class AppUpdateManager(
    context: Context,
    private val client: OkHttpClient,
) {
    private val updateDir = File(context.cacheDir, "updates")

    suspend fun checkForUpdate(currentVersionCode: Int): AppUpdateCheckResult = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(RELEASE_MANIFEST_URL).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("更新清单请求失败：HTTP ${response.code}")
            }

            val body = response.body?.string().orEmpty()
            val release = parseRelease(body)
            when {
                release.versionCode <= currentVersionCode -> AppUpdateCheckResult.UpToDate(release.versionName)
                release.apkUrl.isBlank() -> AppUpdateCheckResult.PackageUnavailable(release)
                else -> AppUpdateCheckResult.Available(release)
            }
        }
    }

    suspend fun downloadUpdate(
        release: AppUpdateRelease,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        require(release.apkUrl.isNotBlank()) { "更新包地址为空" }
        updateDir.mkdirs()
        val outputFile = File(updateDir, "ai-translate-${release.versionName}-debug.apk")
        val tempFile = File(updateDir, "${outputFile.name}.part")

        runCatching {
            val request = Request.Builder().url(release.apkUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("更新包下载失败：HTTP ${response.code}")
                }
                val body = response.body ?: error("更新包下载失败：响应为空")
                val totalBytes = release.sizeBytes.takeIf { it > 0L } ?: body.contentLength().coerceAtLeast(0L)
                var downloadedBytes = 0L
                tempFile.outputStream().use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            downloadedBytes += read
                            onProgress(downloadedBytes, totalBytes)
                        }
                    }
                }
            }

            if (release.sizeBytes > 0L && tempFile.length() != release.sizeBytes) {
                error("更新包大小校验失败")
            }
            if (release.sha256.isNotBlank() && !tempFile.sha256().equals(release.sha256, ignoreCase = true)) {
                error("更新包 SHA256 校验失败")
            }

            if (outputFile.exists()) outputFile.delete()
            check(tempFile.renameTo(outputFile)) { "更新包保存失败" }
            onProgress(outputFile.length(), release.sizeBytes.takeIf { it > 0L } ?: outputFile.length())
            outputFile
        }.getOrElse { error ->
            tempFile.delete()
            throw error
        }
    }

    companion object {
        const val RELEASE_MANIFEST_URL = "https://download.204152.xyz/releases/latest.json"

        internal fun parseRelease(value: String): AppUpdateRelease {
            val root = JSONObject(value)
            val android = root.getJSONObject("android")
            val notesJson = android.optJSONArray("notes")
            val notes = if (notesJson == null) {
                emptyList()
            } else {
                (0 until notesJson.length())
                    .map { notesJson.optString(it).trim() }
                    .filter { it.isNotBlank() }
            }

            return AppUpdateRelease(
                versionCode = android.optInt("versionCode", 0),
                versionName = android.optString("versionName", "未知版本"),
                required = android.optBoolean("required", false),
                apkUrl = android.optString("apkUrl").trim(),
                sha256 = android.optString("sha256").trim(),
                sizeBytes = android.optLong("sizeBytes", 0L),
                notes = notes,
            )
        }
    }
}

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
