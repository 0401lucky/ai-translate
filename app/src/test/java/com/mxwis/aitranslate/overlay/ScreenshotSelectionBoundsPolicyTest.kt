package com.mxwis.aitranslate.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotSelectionBoundsPolicyTest {
    @Test
    fun `反向拖拽会被规范成左上到右下矩形`() {
        val rect = ScreenshotSelectionBoundsPolicy.normalize(
            left = 900,
            top = 700,
            right = 100,
            bottom = 200,
            screenWidth = 1000,
            screenHeight = 800,
        )

        assertRect(100, 200, 900, 700, rect)
    }

    @Test
    fun `过小框选区域无效`() {
        assertFalse(ScreenshotSelectionBoundsPolicy.isValid(SelectionBounds(0, 0, 40, 120)))
        assertFalse(ScreenshotSelectionBoundsPolicy.isValid(SelectionBounds(0, 0, 120, 40)))
        assertTrue(ScreenshotSelectionBoundsPolicy.isValid(SelectionBounds(0, 0, 120, 120)))
    }

    @Test
    fun `屏幕选区会按比例映射到截图尺寸`() {
        val rect = ScreenshotSelectionBoundsPolicy.scaleToBitmap(
            selection = SelectionBounds(100, 200, 500, 600),
            screenWidth = 1000,
            screenHeight = 800,
            bitmapWidth = 2000,
            bitmapHeight = 1600,
        )

        assertRect(200, 400, 1000, 1200, rect)
    }

    private fun assertRect(left: Int, top: Int, right: Int, bottom: Int, bounds: SelectionBounds) {
        assertEquals(left, bounds.left)
        assertEquals(top, bounds.top)
        assertEquals(right, bounds.right)
        assertEquals(bottom, bounds.bottom)
    }
}
