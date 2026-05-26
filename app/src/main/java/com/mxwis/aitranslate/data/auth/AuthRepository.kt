package com.mxwis.aitranslate.data.auth

import com.mxwis.aitranslate.data.history.TranslationHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface AuthRepositoryContract {
    val session: Flow<AuthSession?>
    suspend fun sendRegistrationCode(username: String, email: String)
    suspend fun register(username: String, email: String, password: String, verificationCode: String): AuthSession
    suspend fun login(username: String, password: String): AuthSession
    suspend fun logout()
}

interface RemoteHistorySync {
    suspend fun syncHistory(entity: TranslationHistoryEntity): Result<Unit>
}

class AuthRepository(
    private val sessionStore: AuthSessionStore,
    private val apiClient: AuthApiClient,
) : AuthRepositoryContract, RemoteHistorySync {
    override val session: Flow<AuthSession?> = sessionStore.session

    override suspend fun sendRegistrationCode(username: String, email: String) {
        apiClient.sendRegistrationCode(
            username = AuthInputValidator.normalizeUsername(username),
            email = AuthInputValidator.normalizeEmail(email),
        )
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        verificationCode: String,
    ): AuthSession {
        val result = apiClient.register(
            username = AuthInputValidator.normalizeUsername(username),
            email = AuthInputValidator.normalizeEmail(email),
            password = password,
            verificationCode = verificationCode.trim(),
        )
        return saveResult(result)
    }

    override suspend fun login(username: String, password: String): AuthSession {
        val result = apiClient.login(
            username = AuthInputValidator.normalizeUsername(username),
            password = password,
        )
        return saveResult(result)
    }

    override suspend fun logout() {
        sessionStore.clearSession()
    }

    override suspend fun syncHistory(entity: TranslationHistoryEntity): Result<Unit> {
        val currentSession = session.first() ?: return Result.success(Unit)
        return runCatching {
            apiClient.syncHistory(currentSession.token, entity)
        }
    }

    private suspend fun saveResult(result: AuthResult): AuthSession {
        val session = AuthSession(
            token = result.token,
            user = result.user,
            expiresAt = result.expiresAt,
        )
        sessionStore.saveSession(session)
        return session
    }
}
