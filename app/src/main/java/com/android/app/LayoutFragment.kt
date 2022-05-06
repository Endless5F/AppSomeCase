package com.android.app

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout

class LayoutFragment : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)
        for (index in 0 until childCount) {
            val child = getChildAt(index)

            val lp = child.layoutParams as LayoutParams
            val width = child.measuredWidth
            val height = child.measuredHeight
            var childLeft: Int
            var childTop: Int
            var gravity = lp.gravity
            if (gravity == -1) {
                gravity = Gravity.TOP or Gravity.START
            }
            val layoutDirection = layoutDirection
            val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
            val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK

            val parentLeft: Int = paddingLeft
            val parentRight: Int = right - left - parentLeft
            val parentTop: Int = paddingTop
            val parentBottom: Int = bottom - top - paddingBottom

//            childLeft = parentLeft + lp.leftMargin
//            childTop = parentTop + lp.topMargin

            when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.CENTER_HORIZONTAL -> childLeft =
                    parentLeft + (parentRight - parentLeft - width) / 2 +
                            lp.leftMargin - lp.rightMargin
                Gravity.RIGHT -> {
                    childLeft = parentRight - width - lp.rightMargin
                    break
                }
                Gravity.LEFT -> childLeft = parentLeft + lp.leftMargin
                else -> childLeft = parentLeft + lp.leftMargin
            }

            childTop = when (verticalGravity) {
                Gravity.TOP -> parentTop + lp.topMargin
                Gravity.CENTER_VERTICAL -> parentTop + (parentBottom - parentTop - height) / 2 +
                        lp.topMargin - lp.bottomMargin
                Gravity.BOTTOM -> parentBottom - height - lp.bottomMargin
                else -> parentTop + lp.topMargin
            }

            child.layout(childLeft, childTop, childLeft + width, childTop + height)

//            child.layout(paddingLeft, paddingTop, child.measuredWidth, child.measuredHeight)
        }
    }
}