package com.mxwis.aitranslate.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class ScreenSelectionOverlayView(
    context: Context,
    private val onConfirmSelection: (SelectionBounds) -> Unit,
    private val onCancelSelection: () -> Unit,
) : FrameLayout(context) {
    private val selectionCanvas = SelectionCanvasView(context)

    init {
        setWillNotDraw(false)
        addView(
            selectionCanvas,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT),
        )
        addView(topLabel(), topLabelParams())
        addView(actionBar(), actionBarParams())
    }

    fun selectionRect(): SelectionBounds {
        return selectionCanvas.selectionRect()
    }

    private fun topLabel(): TextView {
        return TextView(context).apply {
            text = "拖拽框选要翻译的区域"
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(8), dp(14), dp(8))
            background = roundedPill(0x99000000.toInt())
        }
    }

    private fun actionBar(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = roundedPill(0xDDFFFFFF.toInt())
            addView(actionButton("取消", false).apply {
                setOnClickListener { onCancelSelection() }
            })
            addView(actionButton("确认", true).apply {
                setOnClickListener {
                    val rect = selectionRect()
                    if (ScreenshotSelectionBoundsPolicy.isValid(rect)) {
                        onConfirmSelection(rect)
                    } else {
                        selectionCanvas.showInvalidHint()
                    }
                }
            })
        }
    }

    private fun actionButton(text: String, primary: Boolean): Button {
        return Button(context).apply {
            this.text = text
            textSize = 14f
            minWidth = dp(104)
            minHeight = dp(44)
            setTextColor(if (primary) Color.WHITE else BODY_TEXT)
            background = roundedPill(if (primary) TEAL else Color.WHITE)
        }
    }

    private fun topLabelParams(): LayoutParams {
        return LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
            .apply { topMargin = dp(34) }
    }

    private fun actionBarParams(): LayoutParams {
        return LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
            .apply { bottomMargin = dp(34) }
    }

    private fun roundedPill(color: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(24).toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private inner class SelectionCanvasView(context: Context) : android.view.View(context) {
        private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xBB0F172A.toInt()
        }
        private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
        }
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEAL
            style = Paint.Style.STROKE
            strokeWidth = dp(2).toFloat()
        }
        private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = dp(14).toFloat()
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        private var startX = 0f
        private var startY = 0f
        private var endX = 0f
        private var endY = 0f
        private var invalidHintUntil = 0L

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            if (selectionRect().isEmpty) {
                startX = w * 0.12f
                startY = h * 0.28f
                endX = w * 0.88f
                endY = h * 0.55f
            }
        }

        override fun onDraw(canvas: Canvas) {
            val layer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
            val rect = selectionRectF()
            canvas.drawRoundRect(rect, dp(8).toFloat(), dp(8).toFloat(), clearPaint)
            canvas.restoreToCount(layer)

            canvas.drawRoundRect(rect, dp(8).toFloat(), dp(8).toFloat(), borderPaint)
            drawHandle(canvas, rect.left, rect.top)
            drawHandle(canvas, rect.right, rect.top)
            drawHandle(canvas, rect.left, rect.bottom)
            drawHandle(canvas, rect.right, rect.bottom)

            if (System.currentTimeMillis() < invalidHintUntil) {
                canvas.drawText("框选区域太小", width / 2f, height - dp(112).toFloat(), hintPaint)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    endX = event.x
                    endY = event.y
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    endX = event.x
                    endY = event.y
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    endX = event.x
                    endY = event.y
                    invalidate()
                    return true
                }
            }
            return true
        }

        fun selectionRect(): SelectionBounds {
            return ScreenshotSelectionBoundsPolicy.normalize(
                left = startX.toInt(),
                top = startY.toInt(),
                right = endX.toInt(),
                bottom = endY.toInt(),
                screenWidth = width,
                screenHeight = height,
            )
        }

        fun showInvalidHint() {
            invalidHintUntil = System.currentTimeMillis() + 1_400L
            invalidate()
            postDelayed({ invalidate() }, 1_450L)
        }

        private fun selectionRectF(): RectF {
            val bounds = selectionRect()
            return RectF(
                bounds.left.toFloat(),
                bounds.top.toFloat(),
                bounds.right.toFloat(),
                bounds.bottom.toFloat(),
            )
        }

        private fun drawHandle(canvas: Canvas, x: Float, y: Float) {
            canvas.drawCircle(x, y, dp(7).toFloat(), handlePaint)
            canvas.drawCircle(x, y, dp(7).toFloat(), borderPaint)
        }
    }

    companion object {
        private const val TEAL = 0xFF0F9F96.toInt()
        private const val BODY_TEXT = 0xFF0F172A.toInt()
    }
}
