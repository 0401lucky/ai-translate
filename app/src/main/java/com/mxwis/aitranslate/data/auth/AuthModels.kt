package com.mxwis.aitranslate.data.auth

data class AuthUser(
    val id: String,
    val username: String,
)

data class AuthSession(
    val token: String,
    val user: AuthUser,
    val expiresAt: Long,
)

data class AuthResult(
    val token: String,
    val user: AuthUser,
    val expiresAt: Long,
)

enum class AuthMode {
    LOGIN,
    REGISTER,
}
