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
            AuthInputValidator.validateRegistration("alice_01", "123456", "654321"),
        )
    }
}
