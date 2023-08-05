package com.test.span.ui

import android.content.Context
import android.view.View


/**
 * Created by csl on 2019/1/24.
 */
object Utils {
    private const val RLM = '\u200F'

    const val RLM_STRING = RLM.toString()

    // 是否阿语地区
    fun isSupportRTL(context: Context): Boolean {
        return context.resources?.configuration?.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }
}