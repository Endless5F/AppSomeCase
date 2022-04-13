package com.android.core.common.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.widget.NestedScrollView
import kotlin.math.abs

/**
 * 支持内嵌下拉刷新的 NestedScrollView
 */
class PullNestedScrollView : NestedScrollView {

    var isNeedScroll = true
    private var isResetTouchEvent = false
    private var isInterceptUpEvent = false
    private var downX = 0f
    private var downY = 0f
    private var mTouchSlop = 0
    private var currentEvent = MotionEvent.INVALID_POINTER_ID

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        currentEvent = ev?.action ?: MotionEvent.INVALID_POINTER_ID
        when (ev?.action) {
            MotionEvent.ACTION_MOVE -> {
                if (isResetTouchEvent) {
                    isResetTouchEvent = false
                    isInterceptUpEvent = true
                    // 重新复制down事件，主要是为了再触发一遍生效一遍 onInterceptTouchEvent事件
                    ev.action = MotionEvent.ACTION_DOWN
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isInterceptUpEvent) {
                    isInterceptUpEvent = false
                    // 防止上述MOVE事件更改为DOWN事件 导致点击事件的触发
                    ev.action = MotionEvent.ACTION_CANCEL
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val curX = ev.x
                val curY = ev.y

                val xDiff = abs(curX - downX)
                val yDiff = abs(curY - downY)
                if (xDiff > mTouchSlop || yDiff > mTouchSlop) {
                    return if (xDiff > yDiff) {
                        super.onInterceptTouchEvent(ev)
                    } else {
                        isNeedScroll
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    fun resetTouchEvent() {
        isResetTouchEvent = currentEvent == MotionEvent.ACTION_MOVE
    }
}