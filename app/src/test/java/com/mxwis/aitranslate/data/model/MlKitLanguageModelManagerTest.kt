package com.mxwis.aitranslate.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MlKitLanguageModelManagerTest {
    @Test
    fun `语种包描述会合并中文并标记英文内置`() {
        val descriptors = MlKitLanguageModelManager.buildDescriptors()

        assertTrue(descriptors.any { it.languageTag == "en" && it.isBuiltIn })
        assertEquals(
            1,
            descriptors.count { it.languageTag == "zh" },
        )
        assertEquals(
            "中文（简体/繁体）",
            descriptors.first { it.languageTag == "zh" }.displayName,
        )
    }
}
