package com.android.core.common.recyclerview

import android.content.Context
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 * 可在滑动状态中响应点击事件的RecyclerView
 */
open class ScrollClickableRecyclerView(ctx: Context) : RecyclerView(ctx) {
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val requestCancelDisallowInterceptTouchEvent = scrollState == SCROLL_STATE_SETTLING
        val consumed = super.onInterceptTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> if (requestCancelDisallowInterceptTouchEvent) {
                parent.requestDisallowInterceptTouchEvent(false)
                stopScroll()
                return false
            }
        }
        return consumed
    }
}