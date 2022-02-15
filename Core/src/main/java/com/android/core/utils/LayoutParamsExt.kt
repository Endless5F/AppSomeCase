package com.android.core.utils

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout

val wrapLayoutParam by lazy(LazyThreadSafetyMode.NONE) {
    ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
}

val matchLayoutParam by lazy(LazyThreadSafetyMode.NONE) {
    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
}

val wrapLinearLayoutParam by lazy(LazyThreadSafetyMode.NONE) {
    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
}

val matchLinearLayoutParam by lazy(LazyThreadSafetyMode.NONE) {
    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
}

val wrapFrameLayoutParam by lazy(LazyThreadSafetyMode.NONE) {
    FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
}
val matchFrameLayoutParam by lazy(LazyThreadSafetyMode.NONE) {
    FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
}

val wrapRelativeLayoutParam by lazy(LazyThreadSafetyMode.NONE) {
    RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
}
val matchRelativeLayoutParam by lazy(LazyThreadSafetyMode.NONE) {
    RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
}