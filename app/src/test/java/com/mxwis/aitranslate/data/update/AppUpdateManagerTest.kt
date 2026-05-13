package com.mxwis.aitranslate.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {
    @Test
    fun `解析 R2 更新清单`() {
        val release = AppUpdateManager.parseRelease(
            """
            {
              "schemaVersion": 1,
              "android": {
                "versionCode": 2,
                "versionName": "1.1.0",
                "required": true,
                "apkUrl": " https://download.204152.xyz/releases/app-release.apk ",
                "sha256": " abc ",
                "sizeBytes": 123456,
                "notes": ["新增应用内更新入口", ""]
              }
            }
            """.trimIndent(),
        )

        assertEquals(2, release.versionCode)
        assertEquals("1.1.0", release.versionName)
        assertTrue(release.required)
        assertEquals("https://download.204152.xyz/releases/app-release.apk", release.apkUrl)
        assertEquals("abc", release.sha256)
        assertEquals(123456L, release.sizeBytes)
        assertEquals(listOf("新增应用内更新入口"), release.notes)
    }

    @Test
    fun `清单缺少可选字段时使用安全默认值`() {
        val release = AppUpdateManager.parseRelease("""{"android":{"versionCode":1}}""")

        assertEquals(1, release.versionCode)
        assertEquals("未知版本", release.versionName)
        assertFalse(release.required)
        assertEquals("", release.apkUrl)
        assertEquals(emptyList<String>(), release.notes)
    }
}
