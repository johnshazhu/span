package com.test.span.ui

data class Label(
    val resId: Int = 0,
    val placeHolder: Int = 0,
    val content: String? = null,
    val contentColor: String? = null,
    var contentFontSize: Int = 8,
    val contentStyle: Int = 0,
    val contentBackgroundResId: Int = 0,
    var spanCharCount: Int = 1,
    var padding: Int = 0,
    val drawByCanvas: Boolean = false
)
