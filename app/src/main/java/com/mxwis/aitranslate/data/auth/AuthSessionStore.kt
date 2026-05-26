package com.mxwis.aitranslate.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authSessionDataStore by preferencesDataStore(name = "auth_session")

class AuthSessionStore(context: Context) {
    private val dataStore = context.applicationContext.authSessionDataStore

    val session: Flow<AuthSession?> = dataStore.data.map { preferences ->
        val token = preferences[TOKEN].orEmpty()
        val userId = preferences[USER_ID].orEmpty()
        val username = preferences[USERNAME].orEmpty()
        val expiresAt = preferences[EXPIRES_AT] ?: 0L

        if (token.isBlank() || userId.isBlank() || username.isBlank() || expiresAt <= System.currentTimeMillis()) {
            null
        } else {
            AuthSession(
                token = token,
                user = AuthUser(id = userId, username = username),
                expiresAt = expiresAt,
            )
        }
    }

    suspend fun saveSession(session: AuthSession) {
        dataStore.edit { preferences ->
            preferences[TOKEN] = session.token
            preferences[USER_ID] = session.user.id
            preferences[USERNAME] = session.user.username
            preferences[EXPIRES_AT] = session.expiresAt
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN)
            preferences.remove(USER_ID)
            preferences.remove(USERNAME)
            preferences.remove(EXPIRES_AT)
        }
    }

    private companion object {
        val TOKEN = stringPreferencesKey("token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EXPIRES_AT = longPreferencesKey("expires_at")
    }
}
