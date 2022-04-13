package com.android.core.common.recyclerview.common

import androidx.recyclerview.widget.RecyclerView

/**
 * 配置RecyclerView的分页加载监听
 */
class DataLoader {

    companion object {
        @JvmStatic
        fun create(list: RecyclerView, loadNextPageCallback: () -> Unit) = DataLoader().apply {
            bind(list, loadNextPageCallback)
        }
    }

    var isLoading = false // 是否正在加载中

    var canLoadNext = true // 是否可以加载下一页

    private lateinit var triggerLoadNextUtil: TriggerLoadNextUtils

    private fun bind(list: RecyclerView, loadNextPageCallback: () -> Unit) {
        triggerLoadNextUtil = TriggerLoadNextUtils(list)
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = list.layoutManager ?: return
                val lastItem = triggerLoadNextUtil.getLastVisiblePosition(layoutManager)
                val totalItemCount = recyclerView.adapter?.itemCount ?: return
                when (newState) {
                    RecyclerView.SCROLL_STATE_SETTLING, RecyclerView.SCROLL_STATE_IDLE -> {
                        checkLoadNext(lastItem, totalItemCount, loadNextPageCallback)
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = list.layoutManager ?: return
                // 预加载下一页逻辑
                if (dy > 0 && canLoadNext && !isLoading) {
                    val lastItem = triggerLoadNextUtil.getLastVisiblePosition(layoutManager)
                    val itemCount = list.adapter?.itemCount ?: return
                    checkLoadNext(lastItem, itemCount, loadNextPageCallback)
                }
            }
        })
    }

    private fun checkLoadNext(lastItem: Int, itemCount: Int, loadNextPageCallback: () -> Unit) {
        val isPreLoad = triggerLoadNextUtil.isTriggerPreLoadNext(lastItem, itemCount)
        val isLastItemAlmostShow = triggerLoadNextUtil.isLatestItemAlmostShow()
        if ((isPreLoad || isLastItemAlmostShow) && canLoadNext && !isLoading) {
            loadNextPageCallback()
        }
    }
}