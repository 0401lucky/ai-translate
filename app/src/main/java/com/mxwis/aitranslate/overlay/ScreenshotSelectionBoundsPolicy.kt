package com.mxwis.aitranslate.overlay

data class SelectionBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val width: Int
        get() = right - left

    val height: Int
        get() = bottom - top

    val isEmpty: Boolean
        get() = width <= 0 || height <= 0
}

object ScreenshotSelectionBoundsPolicy {
    const val MIN_SELECTION_SIZE_PX = 80

    fun normalize(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        screenWidth: Int,
        screenHeight: Int,
    ): SelectionBounds {
        val normalizedLeft = minOf(left, right).coerceIn(0, screenWidth)
        val normalizedRight = maxOf(left, right).coerceIn(0, screenWidth)
        val normalizedTop = minOf(top, bottom).coerceIn(0, screenHeight)
        val normalizedBottom = maxOf(top, bottom).coerceIn(0, screenHeight)
        return SelectionBounds(normalizedLeft, normalizedTop, normalizedRight, normalizedBottom)
    }

    fun isValid(bounds: SelectionBounds, minSizePx: Int = MIN_SELECTION_SIZE_PX): Boolean {
        return bounds.width >= minSizePx && bounds.height >= minSizePx
    }

    fun scaleToBitmap(
        selection: SelectionBounds,
        screenWidth: Int,
        screenHeight: Int,
        bitmapWidth: Int,
        bitmapHeight: Int,
    ): SelectionBounds {
        if (screenWidth <= 0 || screenHeight <= 0 || bitmapWidth <= 0 || bitmapHeight <= 0) {
            return SelectionBounds(0, 0, 0, 0)
        }
        val scaleX = bitmapWidth.toFloat() / screenWidth.toFloat()
        val scaleY = bitmapHeight.toFloat() / screenHeight.toFloat()
        return SelectionBounds(
            (selection.left * scaleX).toInt().coerceIn(0, bitmapWidth),
            (selection.top * scaleY).toInt().coerceIn(0, bitmapHeight),
            (selection.right * scaleX).toInt().coerceIn(0, bitmapWidth),
            (selection.bottom * scaleY).toInt().coerceIn(0, bitmapHeight),
        )
    }
}
