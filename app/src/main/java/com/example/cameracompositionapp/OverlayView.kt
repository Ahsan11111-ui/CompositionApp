package com.example.cameracompositionapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class CompositionMode {
        CENTRAL, RULE_OF_THIRDS, DIAGONAL, GOLDEN_RATIO
    }

    private var compositionMode: CompositionMode = CompositionMode.CENTRAL
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    fun setCompositionMode(mode: CompositionMode) {
        compositionMode = mode
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (compositionMode) {
            CompositionMode.CENTRAL -> drawCentralGuide(canvas)
            CompositionMode.RULE_OF_THIRDS -> drawRuleOfThirds(canvas)
            CompositionMode.DIAGONAL -> drawDiagonalGuides(canvas)
            CompositionMode.GOLDEN_RATIO -> drawGoldenSpiralLandscape(canvas) // ✅ landscape only
        }
    }

    private fun drawCentralGuide(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        canvas.drawLine(centerX, 0f, centerX, height.toFloat(), paint)
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, paint)
    }

    private fun drawRuleOfThirds(canvas: Canvas) {
        val thirdWidth = width / 3f
        val thirdHeight = height / 3f
        canvas.drawLine(thirdWidth, 0f, thirdWidth, height.toFloat(), paint)
        canvas.drawLine(2 * thirdWidth, 0f, 2 * thirdWidth, height.toFloat(), paint)
        canvas.drawLine(0f, thirdHeight, width.toFloat(), thirdHeight, paint)
        canvas.drawLine(0f, 2 * thirdHeight, width.toFloat(), 2 * thirdHeight, paint)
    }

    private fun drawDiagonalGuides(canvas: Canvas) {
        canvas.drawLine(0f, 0f, width.toFloat(), height.toFloat(), paint)
        canvas.drawLine(width.toFloat(), 0f, 0f, height.toFloat(), paint)
    }

    /**
     * Golden Ratio Spiral (Landscape only)
     */
    private fun drawGoldenSpiralLandscape(canvas: Canvas) {
        // Fibonacci sequence
        val fibs = mutableListOf(1, 1)
        for (i in 0 until 5) { // more terms = larger spiral
            fibs.add(fibs[fibs.size - 1] + fibs[fibs.size - 2])
        }

        // Fit spiral across full LANDSCAPE width
        val maxFib = fibs.maxOrNull() ?: 1
        val scale = (width.toFloat() * 1.15f) / (maxFib.toFloat() * 1.03f)

        // Center vertically
        val offsetY = (height - (maxFib * scale))

        // Paints
        val rectPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val spiralPaint = Paint(paint).apply {
            color = Color.YELLOW
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }

        // Save + translate
        canvas.save()
        canvas.translate(270f, 1400f)

        // Draw Fibonacci rectangles + spiral
        for (i in 0 until fibs.size) {
            val fibSize = fibs[i] * scale

            // Rectangle grid
            canvas.drawRect(0f, 0f, fibSize, fibSize, rectPaint)

            // Spiral arc
            val arcRect = RectF(0f, -fibSize, 2 * fibSize, fibSize)
            canvas.drawArc(arcRect, 90f, 90f, false, spiralPaint)

            // Shift + rotate
            canvas.translate(fibSize, fibSize)
            canvas.rotate(-90f)
        }

        canvas.restore()
    }
}
