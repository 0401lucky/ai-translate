package com.mxwis.aitranslate.data.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

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
    private val client: OkHttpClient,
) {
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
