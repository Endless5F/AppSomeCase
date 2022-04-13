package com.android.core.common.recyclerview.common

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * 提供对分页加载的监听的封装
 */
class TriggerLoadNextUtils(private val mRecyclerView: RecyclerView) {
    companion object {
        const val PREVIEW_LOAD_NEXT_COUNT = 3
    }

    private var mTriggerPreCount = PREVIEW_LOAD_NEXT_COUNT // 触发预加载条数
    private var mPreLoadNextEnable: Boolean = true

    fun setTriggerPreCount(triggerPreCont: Int) {
        mTriggerPreCount = triggerPreCont
    }

    fun setPreLoadNextEnable(enable: Boolean) {
        mPreLoadNextEnable = enable
    }

    /**
     * 是否触发预加载下一页
     *
     * @return
     */
    fun isTriggerPreLoadNext(lastItem: Int, itemCount: Int): Boolean {
        return mPreLoadNextEnable && itemCount > 0 && lastItem + mTriggerPreCount >= itemCount
    }

    /**
     * 是否所以item都展示了：正常item，不涉及adapter的footerview
     * 所有item都展示了，则不执行预加载，避免网络不好时，滑动时上下跳动
     *
     * @return
     */
    fun isAllItemVisible(
        lastItem: Int,
        itemCount: Int,
        adapter: RecyclerView.Adapter<*>?
    ): Boolean {
        var footerItem = 0
        if (adapter != null && adapter is RecyclerAdapter) {
            footerItem = adapter.getFooterCount()
        }
        return lastItem >= itemCount - 1 - footerItem
    }

    /**
     * 最底部的元素是否快要露出（仅适用于瀑布流优化预加载机制）
     *
     * @return Boolean
     */
    fun isLatestItemAlmostShow(): Boolean {
        val layoutManager = mRecyclerView.layoutManager
        if (layoutManager is StaggeredGridLayoutManager) {
            val spanCount = layoutManager.spanCount
            val itemCount = mRecyclerView.adapter?.itemCount ?: return false
            for (i in 0 until spanCount) {
                val index = itemCount - i - 1
                val view = layoutManager.findViewByPosition(index)
                // 只要最底部item的View已经被贴在RecyclerView上，即：即将被拉出来的时候（能被LayoutManager找到），就触发预加载
                if (view != null) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 是否触发加载下一页
     *
     * @return
     */
    fun isTriggerLoadNext(lastItem: Int, itemCount: Int): Boolean {
        return itemCount > 0 && lastItem + mTriggerPreCount >= itemCount
    }

    fun getLastVisiblePosition(layoutManager: RecyclerView.LayoutManager): Int {
        return when (layoutManager) {
            is LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
            is GridLayoutManager -> layoutManager.findLastVisibleItemPosition()
            is StaggeredGridLayoutManager -> {
                val lastPositions =
                    layoutManager.findLastVisibleItemPositions(IntArray(layoutManager.spanCount))
                getMaxPosition(lastPositions)
            }
            else -> layoutManager.itemCount - 1
        }
    }

    private fun getMaxPosition(lastPositions: IntArray): Int {
        var max = lastPositions[0]
        for (value in lastPositions) {
            if (value > max) {
                max = value
            }
        }
        return max
    }
}