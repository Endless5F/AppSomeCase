package com.android.core.utils

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt

fun View.removeFromParent() {
    (this.parent as? ViewGroup)?.removeView(this)
}

fun View.getXYOnScreen(): Pair<Int, Int> {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    return Pair(location[0], location[1])
}

fun View.getUnDisplayViewSize(): Pair<Int, Int> {
    val width = View.MeasureSpec.makeMeasureSpec(
        0,
        View.MeasureSpec.UNSPECIFIED
    )
    val height = View.MeasureSpec.makeMeasureSpec(
        0,
        View.MeasureSpec.UNSPECIFIED
    )
    measure(width, height)
    return Pair(measuredWidth, measuredHeight)
}

inline var View.marginStart: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin ?: 0
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin = value
    }

inline var View.marginTop: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = value
    }

inline var View.marginEnd: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin ?: 0
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin = value
    }

inline var View.marginBottom: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin = value
    }

inline var View.marginLeft: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin ?: 0
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin = value
    }

/**
 * 创建圆角矩形背景颜色
 *
 * @param color       填充色
 * @param strokeColor 线条颜色
 * @param strokeWidth 线条宽度  单位px
 * @param radius      角度  px
 */
fun createRectDrawable(
    @ColorInt color: Int,
    radius: Float,
    @ColorInt strokeColor: Int = 0xFFFFFF,
    strokeWidth: Int = 0
): GradientDrawable? {
    return try {
        val radiusBg = GradientDrawable()
        //设置Shape类型
        radiusBg.shape = GradientDrawable.RECTANGLE
        //设置填充颜色
        radiusBg.setColor(color)
        //设置线条粗心和颜色,px
        if (strokeWidth > 0) {
            radiusBg.setStroke(strokeWidth, strokeColor)
        }
        //设置圆角角度,如果每个角度都一样,则使用此方法
        radiusBg.cornerRadius = radius
        radiusBg
    } catch (e: Exception) {
        GradientDrawable()
    }
}

/**
 * 创建圆角矩形背景颜色
 *
 * @param color       填充色
 * @param strokeColor 线条颜色
 * @param strokeWidth 线条宽度  单位px
 * @param radius      角度  px,长度为4,分别表示左上,右上,右下,左下的角度
 */
fun createRectDrawable(
    @ColorInt color: Int,
    radius: FloatArray?,
    @ColorInt strokeColor: Int = 0xFFFFFF,
    strokeWidth: Int = 0
): GradientDrawable? {
    return try {
        val radiusBg = GradientDrawable()
        //设置Shape类型
        radiusBg.shape = GradientDrawable.RECTANGLE
        //设置填充颜色
        radiusBg.setColor(color)
        //设置线条粗心和颜色,px
        if (strokeWidth > 0) {
            radiusBg.setStroke(strokeWidth, strokeColor)
        }
        //每连续的两个数值表示是一个角度,四组:左上,右上,右下,左下
        if (radius != null && radius.size == 4) {
            radiusBg.cornerRadii = floatArrayOf(
                radius[0], radius[0],
                radius[1], radius[1],
                radius[2], radius[2],
                radius[3], radius[3]
            )
        }
        radiusBg
    } catch (e: java.lang.Exception) {
        GradientDrawable()
    }
}