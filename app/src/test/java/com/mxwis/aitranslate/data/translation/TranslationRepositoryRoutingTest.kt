package com.mxwis.aitranslate.data.translation

import com.mxwis.aitranslate.domain.OfflineModelType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationRepositoryRoutingTest {
    @Test
    fun `HY MT 回退需要本地模型已下载`() {
        assertFalse(
            TranslationRepository.canAttemptOffline(
                offlineModelType = OfflineModelType.HY_MT,
                isHyMtAvailable = false,
            ),
        )
        assertTrue(
            TranslationRepository.canAttemptOffline(
                offlineModelType = OfflineModelType.HY_MT,
                isHyMtAvailable = true,
            ),
        )
    }

    @Test
    fun `ML Kit 回退由 SDK 按需下载不依赖 HY MT 文件`() {
        assertTrue(
            TranslationRepository.canAttemptOffline(
                offlineModelType = OfflineModelType.ML_KIT,
                isHyMtAvailable = false,
            ),
        )
    }
}
