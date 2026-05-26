package com.mxwis.aitranslate.data.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthInputValidatorTest {
    @Test
    fun `账号会去除空白并转为小写`() {
        assertEquals("alice_01", AuthInputValidator.normalizeUsername("  Alice_01  "))
    }

    @Test
    fun `账号只允许字母数字和下划线`() {
        assertEquals(null, AuthInputValidator.validateUsername("alice_01"))
        assertEquals("账号只能包含小写字母、数字和下划线", AuthInputValidator.validateUsername("alice-01"))
    }

    @Test
    fun `注册会校验两次密码一致`() {
        assertEquals(
            "两次输入的密码不一致",
            AuthInputValidator.validateRegistration("alice_01", "alice@example.com", "123456", "654321", "123456"),
        )
    }

    @Test
    fun `邮箱和验证码格式会参与注册校验`() {
        assertEquals(null, AuthInputValidator.validateEmail("alice@example.com"))
        assertEquals("邮箱格式不正确", AuthInputValidator.validateEmail("not-email"))
        assertEquals(null, AuthInputValidator.validateVerificationCode("123456"))
        assertEquals("验证码需要是 6 位数字", AuthInputValidator.validateVerificationCode("abc456"))
    }
}
