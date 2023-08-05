package com.test.span.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs

const val ONE_SPACE_PLACEHOLDER = " "
const val TWO_SPACE_PLACEHOLDER = ONE_SPACE_PLACEHOLDER.plus(ONE_SPACE_PLACEHOLDER)
const val THREE_SPACE_PLACEHOLDER = ONE_SPACE_PLACEHOLDER.plus(TWO_SPACE_PLACEHOLDER)
const val TYPE_BOLD = 1

class SpanInformation(
    val imageSpanPlaceHolder: String = TWO_SPACE_PLACEHOLDER
) {
    var start: Int = 0
    var totalPlaceholderCharCount: Int = 0
    var prefix: String = ""
    val spanPlaceHolderLen: Int = imageSpanPlaceHolder.length
    var contentPlaceholderCount: Int = 0
    var foregroundColorSpan: ForegroundColorSpan? = null
    var foregroundColorContent: String? = null
    var styleSpan: StyleSpan? = null
    var sizeSpan: AbsoluteSizeSpan? = null
}

fun computeGradeComposeSpan(
    view: TextView,
    label: Label,
    spanInfo: SpanInformation,
    spaceText: String = ONE_SPACE_PLACEHOLDER
): Int {
    val resources = view.context.resources
    val gradeBackgroundDrawable = ResourcesCompat.getDrawable(resources, label.contentBackgroundResId, null)
    val gradeDrawable = if (label.resId == 0) null else {
        ResourcesCompat.getDrawable(resources, label.resId, null)
    }

    if (gradeBackgroundDrawable != null) {
        val spaceWidth = view.paint.measureText(spaceText)
        val old = view.paint.typeface
        if (label.contentStyle == TYPE_BOLD) {
            view.paint.typeface = Typeface.DEFAULT_BOLD
        }
        val gradeWidth = view.paint.measureText(label.content)
        view.paint.typeface = old

        spanInfo.prefix = spanInfo.prefix.plus(THREE_SPACE_PLACEHOLDER)
        spanInfo.foregroundColorContent = label.content ?: ""
        spanInfo.prefix = spanInfo.prefix.plus(spanInfo.foregroundColorContent)
        if (spaceWidth > 0) {
            val gradeBackgroundWidth = gradeBackgroundDrawable.intrinsicWidth
            val placeholderSpaceCharCount =
                ((gradeBackgroundWidth - (gradeDrawable?.intrinsicWidth ?: 0) - gradeWidth.toInt()) / spaceWidth).toInt()
            if (placeholderSpaceCharCount > 0) {
                for (i in 0 until placeholderSpaceCharCount) {
                    spanInfo.prefix = spanInfo.prefix.plus(spaceText)
                }
                spanInfo.contentPlaceholderCount = placeholderSpaceCharCount
            }
        }
    }

    return gradeBackgroundDrawable?.intrinsicWidth ?: 0
}

private fun isSupportRTL(context: Context): Boolean {
    return context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
}

fun setIdentityGradeSpan(
    msgBody: String = "",
    view: TextView,
    labels: ArrayList<Label>
) {
    val spanInfo = SpanInformation()
    val labelBackgroundWidthList = arrayOfNulls<Int>(labels.size)

    labels.forEachIndexed { index, it ->
        labelBackgroundWidthList[index] = 0
        if (it.resId > 0 || it.contentBackgroundResId > 0) {
            spanInfo.prefix = spanInfo.prefix.plus(spanInfo.imageSpanPlaceHolder)
            if (it.contentBackgroundResId > 0 && it.resId > 0) {
                labelBackgroundWidthList[index] = computeGradeComposeSpan(view, it, spanInfo)
            } else if (!it.content.isNullOrEmpty() && !it.drawByCanvas) {
                spanInfo.prefix = spanInfo.prefix.plus(it.content)
            }
        } else if (!it.content.isNullOrEmpty()) {
            spanInfo.prefix = spanInfo.prefix.plus(it.content)
        }
        it.padding = (4 * view.resources.displayMetrics.density).toInt()
        it.contentFontSize = (it.contentFontSize * view.resources.displayMetrics.density).toInt()
    }

    val str = "${spanInfo.prefix} : $msgBody"
    val spanColor = SpannableString(str)
    labels.forEachIndexed { index, it ->
        checkSpan(
            view,
            spanColor,
            if (it.placeHolder > 0) it.placeHolder else it.resId,
            label = it,
            spanInfo = spanInfo
        )

        if (it.contentBackgroundResId > 0) {
            checkSpan(
                view,
                spanColor,
                it.contentBackgroundResId,
                updateTotalPlaceholderCount = false,
                placeholderWithSpace = false,
                label = it,
                spanInfo = spanInfo
            )
        }

        if (!it.content.isNullOrEmpty() && it.contentBackgroundResId > 0 && !it.drawByCanvas) {
            spanInfo.foregroundColorContent = it.content
            spanInfo.foregroundColorSpan = ForegroundColorSpan(Color.parseColor(it.contentColor))
            spanInfo.styleSpan = StyleSpan(if (it.contentStyle == TYPE_BOLD) Typeface.BOLD else Typeface.NORMAL)
            spanInfo.sizeSpan = AbsoluteSizeSpan(it.contentFontSize, false)
            checkSpan(
                view,
                spanColor,
                it.resId,
                displayOffset = -labelBackgroundWidthList[index]!!,
                updateTotalPlaceholderCount = false,
                placeholderWithSpace = false,
                label = it,
                spanInfo = spanInfo
            )
            spanInfo.totalPlaceholderCharCount += (spanInfo.foregroundColorContent.orEmpty().length + spanInfo.contentPlaceholderCount + spanInfo.spanPlaceHolderLen)
        }

        if (it.drawByCanvas)  {
            spanInfo.start += 1
        }
    }

    view.text = spanColor
}

fun checkSpan(
    view: TextView,
    spannableString: SpannableString,
    resId: Int,
    displayOffset: Int = 0,
    updateTotalPlaceholderCount: Boolean = true,
    placeholderWithSpace: Boolean = true,
    label: Label,
    spanInfo: SpanInformation
) {
    val resources = view.context.resources
    if (!label.content.isNullOrEmpty() && resId == 0 && label.contentBackgroundResId == 0) {
        spanInfo.foregroundColorSpan = ForegroundColorSpan(Color.parseColor(label.contentColor))
        spanInfo.styleSpan = StyleSpan(if (label.contentStyle == TYPE_BOLD) Typeface.BOLD else Typeface.NORMAL)
        spanInfo.sizeSpan = AbsoluteSizeSpan(label.contentFontSize, false)
        spannableString.setSpan(
            spanInfo.foregroundColorSpan,
            spanInfo.start,
            spanInfo.start + label.content.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        spanInfo.styleSpan.run {
            spannableString.setSpan(
                this,
                spanInfo.start,
                spanInfo.start + label.content.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        spanInfo.sizeSpan.run {
            spannableString.setSpan(
                this,
                spanInfo.start,
                spanInfo.start + label.content.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        spanInfo.start += label.content.length + 1
        spanInfo.foregroundColorSpan = null
        spanInfo.styleSpan = null
        return
    }

    if (resId > 0) {
        ResourcesCompat.getDrawable(
            resources,
            resId,
            null
        )?.let { drawable ->
            var adjustGap = 0
            if (displayOffset != 0) {
                adjustGap = (resources.displayMetrics.density * 3).toInt()
            }
            drawable.setBounds(
                displayOffset + adjustGap,
                0,
                displayOffset + adjustGap + drawable.intrinsicWidth,
                drawable.intrinsicHeight
            )
            val imageSpan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CenteredImageSpan(drawable, ImageSpan.ALIGN_CENTER)
            } else {
                CenteredImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
            }
            if (displayOffset != 0) {
                imageSpan.setDisplayOffset(if (isSupportRTL(view.context)) abs(displayOffset) else 0)
            }
            if (updateTotalPlaceholderCount) {
                spanInfo.totalPlaceholderCharCount += spanInfo.spanPlaceHolderLen
            }
            if (label.drawByCanvas) {
                imageSpan.setLabel(label)
            }

            spannableString.setSpan(
                imageSpan,
                spanInfo.start,
                spanInfo.start + 1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            if (placeholderWithSpace && (resId != label.placeHolder)) {
                spanInfo.start += spanInfo.spanPlaceHolderLen
            } else {
                spanInfo.start += 1
            }
        }
    }
    if (!spanInfo.foregroundColorContent.isNullOrEmpty()) {
        val foregroundColorContentLen = spanInfo.foregroundColorContent?.length ?: 0
        val start = spanInfo.start
        val end = start + foregroundColorContentLen + 1
        spanInfo.foregroundColorSpan?.run {
            spannableString.setSpan(
                this,
                start,
                end,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spanInfo.start = end + spanInfo.contentPlaceholderCount
        }

        spanInfo.styleSpan?.run {
            spannableString.setSpan(
                this,
                start,
                end,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        spanInfo.sizeSpan.run {
            spannableString.setSpan(
                this,
                start,
                end,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        spanInfo.foregroundColorSpan = null
        spanInfo.styleSpan = null
    }
}