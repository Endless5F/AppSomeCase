package com.android.core.common.recyclerview.link

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 联动效果RecyclerView，依托Behavior
 */
class LinkRecycleView : RecyclerView {

    /**
     * 此View用于底部占位，高度为底部浮层整体弹出后的高度，目的是用于底部联动浮层整体上滑
     * 需要在数据绑定之前就添加进列表中，默认设置0的高度，当满足联动时再设置为浮层整体的高度
     */
    private lateinit var placeholderView: View

    private var totalDy: Int = 0

    private var lastHolderVisible = false

    var linkBehavior: LinkBottomSheetBehavior<View>? = null

    var scrollByOutsideView = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = LinkAdapter()
        // 滚动监听
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (linkBehavior == null) {
                    return
                }
                totalDy += dy
                val isPlaceholderViewVisible = isPlaceholderViewVisible() // 当前时刻，底部占位视图是否漏出

                // 由可见变为不可见时，说明底部浮层已经恢复到了收缩态，黄金语句，解决了好几个问题！！！
                if (lastHolderVisible && !isPlaceholderViewVisible) {
                    linkBehavior?.setStateInternal(STATE_COLLAPSED)
                }

                if (!scrollByOutsideView) {
                    linkBehavior?.let {
                        it.setSlideByOutsideView(isPlaceholderViewVisible)
                        it.setOutsideView(this@LinkRecycleView)
                        if (isPlaceholderViewVisible) {
                            val slideHeight =
                                measuredHeight - (placeholderView.top - totalDy) + it.getPeekHeight()
                            it.setSlideHeight(slideHeight)
                        }
                    }
                }
                scrollByOutsideView = false
                lastHolderVisible = isPlaceholderViewVisible
            }
        })
    }

    /**
     * 添加内容控件，在绑定联动行为之前添加
     */
    fun addContentView(view: View) {
        val firstChildView = getChildAt(0) as? ViewGroup
        firstChildView?.addView(view)
    }

    /**
     * 绑定联动行为
     */
    fun bindBottomSheetBehavior(linkBehavior: LinkBottomSheetBehavior<View>) {
        if (this.linkBehavior != null) {
            return
        }

        this.linkBehavior = linkBehavior
        placeholderView = View(context)
        placeholderView.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
        val firstChildView = getChildAt(0) as? ViewGroup
        firstChildView?.addView(placeholderView)
    }

    /**
     * 根据是否需要联动行为来设置占位控件的高度
     */
    fun updateHolderViewHeight(needLink: Boolean) {
        if (linkBehavior == null) {
            return
        }
        placeholderView.layoutParams?.let {
            it.height = if (needLink) {
                linkBehavior?.collapsedOffset ?: 0
            } else {
                0
            }
            placeholderView.layoutParams = it
        }
    }

    private fun isPlaceholderViewVisible(): Boolean {
        val scrollBounds = Rect()
        getHitRect(scrollBounds)
        return placeholderView.getLocalVisibleRect(scrollBounds)
    }

}