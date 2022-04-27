package com.android.core.common.recyclerview.layoutmanager

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * 弹幕泳道 LayoutManager
 * https://juejin.cn/post/7010521583894659103
 *
 * demo：
 * findViewById<RecyclerView>(R.id.rv)?.let { rv ->
 *     rv.adapter = LaneAdapter()
 *     rv.layoutManager = LaneLayoutManager()

 *     countdown(Long.MAX_VALUE, 100) {
 *         // 120 > 100 ,  防止动画执行完下一帧滚动尚未调用导致些许卡顿
 *         rv.smoothScrollBy(dip(10), 0, LinearInterpolator(), 120)
 *     }.launchIn(MainScope())
 * }
 */
class LaneLayoutManager : RecyclerView.LayoutManager() {
    /**
     * [layoutView]的返回值，表示将视图填充到车道应该结束
     */
    private val LAYOUT_FINISH = -1

    /**
     * 与adapter中的数据相关的索引
     */
    private var adapterIndex = 0

    private var lastLaneEndView: View? = null

    /**
     * RecyclerView 中的所有 [Lane]
     */
    private var lanes = mutableListOf<Lane>()

    /**
     * 评论视图的垂直间隙
     */
    var verticalGap = 5 * 3

    /**
     * 评论视图的水平间隙
     */
    var horizontalGap = 3 * 3

    /**
     * define the layout params for child view in RecyclerView
     * override this is a must for customized [RecyclerView.LayoutManager]
     */
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * define how to layout child view in RecyclerView
     * override this is a must for customized [RecyclerView.LayoutManager]
     */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        detachAndScrapAttachedViews(recycler)
        fillLanes(recycler, lanes)
    }

    /**
     * define how to scroll views in RecyclerView
     * override this is a must for customized [RecyclerView.LayoutManager]
     */
    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return scrollBy(dx, recycler)
    }

    /**
     * define whether scrolling horizontally is allowed
     * override this is a must for customized [RecyclerView.LayoutManager]
     */
    override fun canScrollHorizontally(): Boolean {
        return true
    }

    /**
     * 根据位移大小决定填充多少表项
     */
    private fun scrollBy(dx: Int, recycler: RecyclerView.Recycler?): Int {
        // 若列表没有孩子或未发生滚动则返回
        if (childCount == 0 || dx == 0) return 0
        // 在滚动还未开始前，更新泳道信息
        updateLanesEnd(lanes)
        // 获取滚动绝对值
        val absDx = abs(dx)
        // 遍历所有泳道，向其中的枯竭泳道填充弹幕
        // fill new views into lanes after scrolled
        lanes.forEach { lane ->
            if (lane.isDrainOut(absDx)) layoutViewByScroll(recycler, lane)
        }
        // recycle views in lanes after scrolled
        recycleGoneView(lanes, absDx, recycler)
        // make lane move left
        // 滚动列表的落脚点：将表项向手指位移的反方向平移相同的距离
        offsetChildrenHorizontal(-absDx)
        return dx
    }


    // 通过循环填充弹幕
    private fun fillLanes(recycler: RecyclerView.Recycler?, lanes: MutableList<Lane>) {
        lastLaneEndView = null
        // 如果列表垂直方向上还有空间则继续填充弹幕
        while (hasMoreLane(height - lanes.bottom())) {
            // 填充单个弹幕到泳道中
            val consumeSpace = layoutView(recycler, lanes)
            if (consumeSpace == LAYOUT_FINISH) break
        }
    }

    private fun recycleGoneView(lanes: List<Lane>, dx: Int, recycler: RecyclerView.Recycler?) {
        recycler ?: return
        lanes.forEach { lane ->
            getChildAt(lane.startLayoutIndex)?.let { startView ->
                if (isGoneByScroll(startView, dx)) {
                    removeAndRecycleView(startView, recycler)
                    updateLaneIndexAfterRecycle(lanes, lane.startLayoutIndex)
                    lane.startLayoutIndex += lanes.size - 1
                }
            }
        }
    }

    /**
     * 初次填充弹幕也是一个不断在垂直方向上追加泳道的过程，判断是否追加的逻辑如下：列表高度 - 当前最底部泳道的 bottom 值 - 这次填充弹幕消耗的像素值 > 0，其中lanes.bottom()是一个List<Lane>的扩展方法：
     */
    fun List<Lane>.bottom() = lastOrNull()?.getEndView()?.bottom ?: 0

    /**
     * after view is recycled and remove from RecyclerView, the layout index in lane should be minus 1
     */
    private fun updateLaneIndexAfterRecycle(lanes: List<Lane>, recycleIndex: Int) {
        lanes.forEachIndexed { index, lane ->
            if (lane.startLayoutIndex > recycleIndex) {
                lane.startLayoutIndex--
            }
            if (lane.endLayoutIndex > recycleIndex) {
                lane.endLayoutIndex--
            }
        }
    }

    /**
     * 弹幕滚动时填充新弹幕
     * 泳道枯竭的判定依据是：泳道最后一个弹幕的右边向左平移 dx 后是否小于列表宽度。若小于则表示泳道中的弹幕已经全展示完了，此时就要继续填充弹幕：
     * 填充逻辑和初次填充的几乎一样，唯一的区别是，滚动时的填充不可能因为空间不够而提前返回，因为是找准了泳道进行填充的。
     */
    private fun layoutViewByScroll(recycler: RecyclerView.Recycler?, lane: Lane) {
        val view = recycler?.getViewForPosition(adapterIndex)
        view ?: return
        measureChildWithMargins(view, 0, 0)
        addView(view)
        // layout even
        val left = lane.end + horizontalGap
        val top = lane.getEndView()?.top ?: paddingTop
        val right = left + view.measuredWidth
        val bottom = top + view.measuredHeight
        layoutDecorated(view, left, top, right, bottom)
        lane.apply {
            end = right
            endLayoutIndex = childCount - 1
        }
        adapterIndex++
    }

    /**
     * 填充单个弹幕，并记录泳道信息
     */
    private fun layoutView(recycler: RecyclerView.Recycler?, lanes: MutableList<Lane>): Int {
        // 1. 从缓存池中获取表项视图
        // 若缓存未命中，则会触发 onCreateViewHolder() 和 onBindViewHolder()
        val view = recycler?.getViewForPosition(adapterIndex)
        view ?: return LAYOUT_FINISH // 获取表项视图失败，则结束填充

        // 3. 测量表项视图
        measureChildWithMargins(view, 0, 0)
        val verticalMargin =
            (view.layoutParams as? RecyclerView.LayoutParams)?.let { it.topMargin + it.bottomMargin }
                ?: 0
        val consumed =
            getDecoratedMeasuredHeight(view) + if (lastLaneEndView == null) 0 else verticalGap + verticalMargin
        // 若列表垂直方向还可以容纳一条新得泳道，则新建泳道，否则停止填充
        if (height - lanes.bottom() - consumed > 0) {
            lanes.add(emptyLane(adapterIndex))
        } else return LAYOUT_FINISH
        // 2. 将表项视图成为列表孩子
        addView(view)
        // 获取最新追加的泳道
        val lane = lanes.last()
        // 计算弹幕上下左右的边框
        val left = lane.end + horizontalGap
        val top =
            if (lastLaneEndView == null) paddingTop else lastLaneEndView!!.bottom + verticalGap
        val right = left + view.measuredWidth
        val bottom = top + view.measuredHeight
        // 定位弹幕
        layoutDecorated(view, left, top, right, bottom)
        // 更新泳道末尾横坐标及布局索引
        lane.apply {
            end = right
            endLayoutIndex = childCount - 1 // 因为是刚追加的表项，所以其索引值必然是最大的
        }

        adapterIndex++
        lastLaneEndView = view
        return consumed
    }

    /**
     * whether [view] will be invisible in RecyclerView if scrolled [dx] to the left
     */
    private fun isGoneByScroll(view: View, dx: Int): Boolean = getEnd(view) - dx < 0

    /**
     * update every [Lane]'s end to the newest position by next scroll starts,
     * which is the most right pixel of last view in Lane according to the RecyclerView
     * 为什么要在填充枯竭泳道之前更新泳道信息？
     * 因为 RecyclerView 的滚动是一段一段进行的，看似滚动了一丢丢距离，scrollHorizontallyBy()可能要回调十几次，每一次回调，弹幕都会前进一小段，即泳道末尾弹幕的横坐标会发生变化，这变化得同步到Lane结构中。否则泳道枯竭的计算就会出错。
     */
    private fun updateLanesEnd(lanes: MutableList<Lane>) {
        lanes.forEach { lane ->
            lane.getEndView()?.let { lane.end = getEnd(it) }
        }
    }

    /**
     * 获取表项的 right 值
     * 泳道枯竭的判定依据是：泳道最后一个弹幕的右边向左平移 dx 后是否小于列表宽度。若小于则表示泳道中的弹幕已经全展示完了，此时就要继续填充弹幕：layoutViewByScroll
     */
    private fun getEnd(view: View?) =
        if (view == null) Int.MIN_VALUE else getDecoratedRight(view) + (view.layoutParams as RecyclerView.LayoutParams).rightMargin

    /**
     * whether continue to layout child
     */
    private fun hasMoreLane(remainSpace: Int) = remainSpace > 0 && adapterIndex in 0 until itemCount

    /**
     * return the layout index according to the adapter index
     */
    private fun getLayoutIndex(adapterIndex: Int): Int {
        val firstChildIndex =
            getChildAt(0)?.let { (it.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition }
                ?: 0
        return adapterIndex - firstChildIndex
    }

    /**
     * live comment is composed of several [Lane]
     * @param end pixel offset according to the left of RecyclerView, where the layout of follow-up view in the lane should start
     * @param endLayoutIndex the  layout index of the last view in lane
     *
     * 泳道结构包含三个数据，分别是：
     *   1. 泳道末尾弹幕横坐标：它是泳道中最后一个弹幕的 right 值，即它的右侧相对于 RecyclerView 左侧的距离。该值用于判断经过一段位移的滚动后，该泳道是否会枯竭。
     *   2. 泳道末尾弹幕的布局索引：它是泳道中最后一个弹幕的布局索引，记录它是为了方便地通过getChildAt()获取泳道中最后一个弹幕的视图。（布局索引有别于适配器索引，RecyclerView 只会持有有限个表项，所以布局索引的取值范围是[0,x]，x的取值比一屏表项稍多一点，而对于弹幕来说，适配器索引的取值是[0,∞]）
     *   3. 泳道头部弹幕的布局索引：与 2 类似，为了方便地获得泳道第一个弹幕的视图。
     */
    data class Lane(var end: Int, var endLayoutIndex: Int, var startLayoutIndex: Int)

    /**
     * get the right most view in the lane
     * 它获取泳道列表中的最后一个泳道，然后再获取该泳道中最后一条弹幕视图的 bottom 值。其中getEndView()被定义为Lane的扩展方法：
     */
    private fun Lane.getEndView(): View? = getChildAt(endLayoutIndex)

    /**
     * whether [Lane] drains out
     * 泳道是否枯竭
     */
    private fun Lane.isDrainOut(dx: Int): Boolean = getEnd(getEndView()) - dx < width

    /**
     * create an empty [Lane] object
     */
    private fun emptyLane(adapterIndex: Int) =
        Lane(-horizontalGap + width, -1, getLayoutIndex(adapterIndex))
}