package com.android.customwidget.widget.scroll

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent

/**
 * ScrollView 滑动状态改变监听
 * @author jiaochengyun
 * @version
 * @since 2021/10/21
 */
abstract class ScrollStateChangeListener {
    val SCROLL_STATE_IDLE = 0
    val SCROLL_STATE_DRAGGING = 1
    val SCROLL_STATE_SETTLING = 2 // 自动滚动

    private var initX = 0
    private var initY = 0
    private var lastX = 0
    private var lastY = 0
    private var currentState = SCROLL_STATE_IDLE

    private var pointerState = MotionEvent.ACTION_CANCEL
    private val handler = Handler(Looper.getMainLooper())

    private val touchRunnable = Runnable {
        if (currentState == SCROLL_STATE_DRAGGING) {
            currentState = SCROLL_STATE_IDLE

            onScrollEnd(lastX, lastY, lastY > initY)
        }
    }
    private val stateRunnable = Runnable {
        if (currentState == SCROLL_STATE_SETTLING) {
            currentState = SCROLL_STATE_IDLE
            onScrollEnd(lastX, lastY, lastY > initY)
        }
    }

    fun onTouch(event: MotionEvent?) {
        pointerState = event?.action ?: MotionEvent.ACTION_CANCEL
        when (event?.action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                pointerState = MotionEvent.ACTION_CANCEL
                processScrollEndEvent(touchRunnable)
            }
        }
    }

    /**
     * 动画背景：根据ScrollView的滑动进度，对底Bar 做位移，以及改变底Bar 内的元素透明度 和 底Bar中心文本的大小显示多少
     * 问题背景：内容区域可能是写死的高度，导致退化态时，内容的高度也不会改变，内容无法展示更多
     * 问题方案：动画执行过程中，改变内容高度，此时内容部分就会刷新，可以根据动画的进度来动态显示内容的多少
     * 问题情况：若内容滑动到最底部，然后下滑使底Bar恢复为正常态，然后再快速上滑，会导致内容部分抖动 继而也会导致 底Bar动画的抖动。
     * 问题原因：内容滑动到最底部时，内容高度发生变化，此时onScrollChanged 被回调，而onScrollChanged被回调又会导致内容高度再次变化
     * 解决方案：1. 判断内容是否满足两屏时，满足才响应此退化态，不满足则不响应 2. 滑动到底部时则不响应 onScrollChange回调
     *
     * 解决方案2-滑动到底部时的条件：内容高度(包括未滑动部分) - 内容View的高度 - 底Bar向下位移最大高度
     */
    fun onScrollChange(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        if (currentState == SCROLL_STATE_IDLE) {
            initX = oldScrollX
            initY = oldScrollY
            currentState = SCROLL_STATE_DRAGGING
            onScrollStart(oldScrollX, oldScrollY, scrollY > oldScrollY)
        }
        if (pointerState == MotionEvent.ACTION_CANCEL) {
            currentState = SCROLL_STATE_SETTLING
            processScrollEndEvent(stateRunnable)
        }
        lastX = scrollX
        lastY = scrollY

        onScrolling(scrollX, scrollY, oldScrollX, oldScrollY)
    }

    private fun processScrollEndEvent(runnable: Runnable) {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 50)
    }

    abstract fun onScrollStart(initX: Int, initY: Int, up: Boolean)

    /**
     * @param up 滑动方向，是否是上滑
     */
    abstract fun onScrolling(
        scrollX: Int,
        scrollY: Int,
        oldScrollX: Int,
        oldScrollY: Int
    )

    abstract fun onScrollEnd(lastX: Int, lastY: Int, up: Boolean)
}