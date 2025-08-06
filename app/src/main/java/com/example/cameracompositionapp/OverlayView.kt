package com.example.cameracompositionapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

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
        invalidate() // redraw view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (compositionMode) {
            CompositionMode.CENTRAL -> drawCentralGuide(canvas)
            CompositionMode.RULE_OF_THIRDS -> drawRuleOfThirds(canvas)
            CompositionMode.DIAGONAL -> drawDiagonalGuides(canvas)
            CompositionMode.GOLDEN_RATIO -> drawGoldenSpiral(canvas) // ✅ connected
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

    // ✅ Your Golden Spiral function integrated
    private fun drawGoldenSpiral(canvas: Canvas) {
        // Generate Fibonacci sequence
        val fibs = mutableListOf(1, 1)
        for (i in 0 until 8) { // Fewer iterations for better visibility
            fibs.add(fibs[fibs.size - 1] + fibs[fibs.size - 2])
        }

        // Scale factor
        val scale = min(width, height) * 0.025f

        // Position spiral in lower-left area (like your reference image)
        val startX = width * 0.3f
        val startY = height * 0.7f

        // Save canvas state
        canvas.save()

        // Start from our custom position
        canvas.translate(startX, startY)

        // Set paint properties for rectangles
        val rectPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        // Set paint properties for spiral
        paint.color = Color.YELLOW
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true

        // Draw the spiral with visible segments
        for (i in 0 until fibs.size) {
            val fibSize = fibs[i] * scale

            // Draw the Fibonacci rectangle outline to show segmentation
            canvas.drawRect(0f, 0f, fibSize, fibSize, rectPaint)

            // Draw the quarter circle arc inside the rectangle
            val arcRect = RectF(0f, -fibSize, 2 * fibSize, fibSize)
            canvas.drawArc(arcRect, 90f, 90f, false, paint)

            // Move and rotate for next segment
            canvas.translate(fibSize, fibSize)
            canvas.rotate(-90f)

            // Stop if getting too large
            if (fibSize > min(width, height) * 0.3f) break
        }

        // Restore canvas state
        canvas.restore()

        // Optional: Add Fibonacci numbers as labels
        canvas.save()
        canvas.translate(startX, startY)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = scale * 0.8f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        for (i in 0 until minOf(fibs.size, 6)) { // Label first few rectangles
            val fibSize = fibs[i] * scale

            // Draw the Fibonacci number in the center of each rectangle
            canvas.drawText(
                fibs[i].toString(),
                fibSize / 2,
                fibSize / 2 + textPaint.textSize / 3,
                textPaint
            )

            canvas.translate(fibSize, fibSize)
            canvas.rotate(-90f)

            if (fibSize > min(width, height) * 0.3f) break
        }

        canvas.restore()
    }

}
