package com.test.span.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

const val ONE_SPACE_PLACEHOLDER = " "
const val TWO_SPACE_PLACEHOLDER = ONE_SPACE_PLACEHOLDER.plus(ONE_SPACE_PLACEHOLDER)
const val TYPE_BOLD = 1

class SpanInformation(
    val imageSpanPlaceHolder: String = TWO_SPACE_PLACEHOLDER
) {
    var start: Int = 0
    var prefix: String = ""
    val spanPlaceHolderLen: Int = imageSpanPlaceHolder.length
    var foregroundColorSpan: ForegroundColorSpan? = null
    var styleSpan: StyleSpan? = null
    var sizeSpan: AbsoluteSizeSpan? = null
}

fun setIdentityGradeSpan(
    msgBody: String = "",
    view: TextView,
    labels: ArrayList<Label>
) {
    val spanInfo = SpanInformation()

    labels.forEach {
        if (it.resId > 0 || it.contentBackgroundResId > 0) {
            spanInfo.prefix = spanInfo.prefix.plus(spanInfo.imageSpanPlaceHolder)
        } else {
            spanInfo.prefix = spanInfo.prefix.plus(it.content)
        }
        it.padding = (4 * view.resources.displayMetrics.density).toInt()
        it.contentFontSize = (it.contentFontSize * view.resources.displayMetrics.density).toInt()
    }

    val str = "${spanInfo.prefix} : $msgBody"
    val spanString = SpannableString(str)
    labels.forEach {
        checkSpan(
            view,
            spanString,
            it,
            spanInfo = spanInfo
        )
    }

    view.text = spanString
}

fun checkSpan(
    view: TextView,
    spannableString: SpannableString,
    label: Label,
    spanInfo: SpanInformation
) {
    val resources = view.context.resources
    // 纯文本Span
    if (!label.content.isNullOrEmpty() && label.resId == 0 && label.contentBackgroundResId == 0) {
        spanInfo.foregroundColorSpan = ForegroundColorSpan(Color.parseColor(label.contentColor))
        spanInfo.styleSpan = StyleSpan(if (label.contentStyle == TYPE_BOLD) Typeface.BOLD else Typeface.NORMAL)
        spanInfo.sizeSpan = AbsoluteSizeSpan(label.contentFontSize, false)
        val list = arrayListOf(spanInfo.foregroundColorSpan, spanInfo.styleSpan, spanInfo.sizeSpan)
        list.forEach {
            spannableString.setSpan(it,
                spanInfo.start,
                spanInfo.start + label.content.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        spanInfo.start += label.content.length + 1
        return
    }

    val imageSpanRes = if (label.contentBackgroundResId > 0) label.contentBackgroundResId else label.resId
    if (imageSpanRes > 0) {
        ResourcesCompat.getDrawable(
            resources,
            imageSpanRes,
            null
        )?.let { drawable ->
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            val imageSpan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CenteredImageSpan(drawable, ImageSpan.ALIGN_CENTER)
            } else {
                CenteredImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
            }
            // 带背景的ImageSpan，传入label信息，通过canvas画上面的图片和文本信息
            if (imageSpanRes == label.contentBackgroundResId) {
                imageSpan.setLabel(label, view.resources)
            }
            spannableString.setSpan(
                imageSpan,
                spanInfo.start,
                spanInfo.start + 1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spanInfo.start += spanInfo.spanPlaceHolderLen
        }
    }
}