package com.test.span.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import java.lang.ref.WeakReference

class CenteredImageSpan @JvmOverloads constructor(
    drawable: Drawable,
    verticalAlignment: Int = DynamicDrawableSpan.ALIGN_BOTTOM,
) : ImageSpan(drawable, verticalAlignment) {
    private var initialDescent = 0
    private var extraSpace = 0
    private var drawableRef: WeakReference<Drawable>? = null
    private var displayOffset: Int = 0
    private var label: Label? = null

    fun setDisplayOffset(offset: Int = 0) {
        this.displayOffset = offset
    }

    fun setLabel(label: Label) {
        this.label = label
    }

    override fun draw(
        canvas: Canvas, text: CharSequence,
        start: Int, end: Int,
        x: Float, top: Int,
        y: Int, bottom: Int,
        paint: Paint
    ) {
        canvas.save()
        var transY = 0f
        paint.fontMetrics?.let {
            val centerY = (bottom - top) / 2f
            drawable?.bounds?.let { rect ->
                val height = (rect.bottom - rect.top)
                transY = centerY - height / 2
                canvas.translate(x + displayOffset, transY)
            }
        }
        drawable.draw(canvas)
        canvas.restore()

        label?.run {
            if (!content.isNullOrEmpty()) {
                if (contentStyle == TYPE_BOLD) {
                    paint.setTypeface(Typeface.DEFAULT_BOLD)
                }
                paint.textSize = contentFontSize.toFloat()
                paint.color = Color.parseColor(contentColor)
                val textX = x + contentFontSize * 2f + padding
                canvas.drawText(content, textX, y.toFloat() - (bottom - top) / 2 + (drawable.bounds.bottom - drawable.bounds.top) / 2, paint)
            }
        }
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fontMetrics: Paint.FontMetricsInt?
    ): Int {
        getCachedDrawable()?.let {
            val rect = it.bounds
            fontMetrics?.let { fm ->
                if (rect.bottom - (fm.descent - fm.ascent) >= 0) {
                    // Stores the initial descent and computes the margin available
                    initialDescent = fm.descent
                    extraSpace = rect.bottom - (fm.descent - fm.ascent)
                }

                fm.descent = extraSpace / 2 + initialDescent
                fm.bottom = fm.descent

                fm.ascent = -rect.bottom + fm.descent
                fm.top = fm.ascent
            }
            return rect.right
        }

        return super.getSize(paint, text, start, end, fontMetrics)
    }

    private fun getCachedDrawable(): Drawable? {
        var result: Drawable? = drawableRef?.get()

        if (result == null) {
            result = drawable
            drawableRef = WeakReference(drawable)
        }
        return result
    }
}