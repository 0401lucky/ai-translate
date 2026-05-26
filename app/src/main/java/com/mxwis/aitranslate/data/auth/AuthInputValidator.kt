package com.mxwis.aitranslate.data.auth

object AuthInputValidator {
    fun normalizeUsername(value: String): String = value.trim().lowercase()

    fun validateUsername(value: String): String? {
        val username = normalizeUsername(value)
        if (username.length !in 3..32) return "账号长度需要在 3 到 32 个字符之间"
        if (!Regex("^[a-z0-9_]+$").matches(username)) return "账号只能包含小写字母、数字和下划线"
        return null
    }

    fun validatePassword(value: String): String? {
        if (value.length !in 6..72) return "密码长度需要在 6 到 72 个字符之间"
        return null
    }

    fun normalizeEmail(value: String): String = value.trim().lowercase()

    fun validateEmail(value: String): String? {
        val email = normalizeEmail(value)
        if (email.length !in 6..254) return "邮箱格式不正确"
        if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email)) return "邮箱格式不正确"
        return null
    }

    fun validateVerificationCode(value: String): String? {
        if (!Regex("^\\d{6}$").matches(value.trim())) return "验证码需要是 6 位数字"
        return null
    }

    fun validateRegistration(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        verificationCode: String,
    ): String? {
        validateUsername(username)?.let { return it }
        validateEmail(email)?.let { return it }
        validatePassword(password)?.let { return it }
        if (password != confirmPassword) return "两次输入的密码不一致"
        validateVerificationCode(verificationCode)?.let { return it }
        return null
    }
}
