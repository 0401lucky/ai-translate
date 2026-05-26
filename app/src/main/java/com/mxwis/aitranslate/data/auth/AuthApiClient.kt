package com.mxwis.aitranslate.data.auth

import com.mxwis.aitranslate.data.history.TranslationHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AuthApiClient(
    private val client: OkHttpClient,
    private val baseUrl: String,
) {
    suspend fun login(username: String, password: String): AuthResult = postAuth(
        path = "/auth/login",
        username = username,
        email = null,
        password = password,
        verificationCode = null,
    )

    suspend fun sendRegistrationCode(username: String, email: String) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("username", username)
            .put("email", email)

        val request = Request.Builder()
            .url(resolveUrl("/auth/send-code"))
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        client.newCall(request).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error(parseErrorMessage(responseText, "验证码发送失败：HTTP ${response.code}"))
            }
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        verificationCode: String,
    ): AuthResult = postAuth(
        path = "/auth/register",
        username = username,
        email = email,
        password = password,
        verificationCode = verificationCode,
    )

    suspend fun syncHistory(token: String, entity: TranslationHistoryEntity) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("id", entity.remoteStableId())
            .put("sourceText", entity.sourceText)
            .put("translatedText", entity.translatedText)
            .put("sourceLanguage", entity.sourceLanguage)
            .put("targetLanguage", entity.targetLanguage)
            .put("mode", entity.mode)
            .put("createdAt", entity.createdAt)

        val request = Request.Builder()
            .url(resolveUrl("/history"))
            .header("Authorization", "Bearer $token")
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        client.newCall(request).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error(parseErrorMessage(responseText, "历史同步失败：HTTP ${response.code}"))
            }
        }
    }

    private suspend fun postAuth(
        path: String,
        username: String,
        email: String?,
        password: String,
        verificationCode: String?,
    ): AuthResult =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("username", username)
                .put("password", password)
            email?.let { body.put("email", it) }
            verificationCode?.let { body.put("verificationCode", it) }

            val request = Request.Builder()
                .url(resolveUrl(path))
                .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    error(parseErrorMessage(responseText, "认证失败：HTTP ${response.code}"))
                }
                parseAuthResult(responseText)
            }
        }

    private fun resolveUrl(path: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        require(trimmed.startsWith("http")) { "认证服务器地址未配置" }
        return "$trimmed$path"
    }

    private fun parseAuthResult(responseText: String): AuthResult {
        val json = JSONObject(responseText)
        val user = json.getJSONObject("user")
        return AuthResult(
            token = json.getString("token"),
            expiresAt = json.optLong("expiresAt"),
            user = AuthUser(
                id = user.getString("id"),
                username = user.getString("username"),
                email = user.optString("email", "")
                    .takeIf { it.isNotBlank() && it != "null" },
            ),
        )
    }

    private fun parseErrorMessage(responseText: String, fallback: String): String {
        return runCatching {
            JSONObject(responseText).optString("message").ifBlank { fallback }
        }.getOrDefault(fallback)
    }

    private fun TranslationHistoryEntity.remoteStableId(): String {
        val hash = "${sourceText}|${translatedText}|$createdAt".hashCode().toUInt().toString(16)
        return "android-$createdAt-$hash"
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
