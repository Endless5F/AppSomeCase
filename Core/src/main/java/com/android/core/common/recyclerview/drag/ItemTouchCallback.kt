package com.android.core.common.recyclerview.drag

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/** NONE */
const val STATE_NONE = 0

/** 开始拖动 */
const val STATE_START_DRAG = 1

/** 拖动中 */
const val STATE_DRAGGING = 3

/**
 * 拖动操作形为实现类
 *
 * @param itemTouchStatus itemHolder拖动回调
 */
class ItemTouchCallback(private var itemTouchStatus: IItemTouchStatus?) : ItemTouchHelper.Callback() {

    /** 手指抬起标记位 */
    private var actionUp = false

    override fun isLongPressDragEnabled(): Boolean {
        // 禁用系统内部长按触发拖动操作，由业务自己定制实现长按触发
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        // 禁用系统侧滑删除元素操作
        return false
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // 那设拖动行为和方向，禁用侧滑删除行为
        val dragFlags: Int =
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // 交换在数据源中相应数据源的位置
        return itemTouchStatus?.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            ?: false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
        // 从数据源中移除相应的数据
        itemTouchStatus?.onItemRemove(viewHolder.adapterPosition)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        itemTouchStatus?.onTouchState(viewHolder.itemView, STATE_DRAGGING)
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        // itemView被选中
        if (ItemTouchHelper.ACTION_STATE_DRAG == actionState) {
            itemTouchStatus?.onTouchState(viewHolder?.itemView, STATE_START_DRAG)
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun getAnimationDuration(
        recyclerView: RecyclerView,
        animationType: Int,
        animateDx: Float,
        animateDy: Float
    ): Long {
        // 拖动手指松开
        actionUp = true
        return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
    }

    /**
     * 当用户与item的交互结束清除所有状态
     *
     * @param recyclerView 列表实例
     * @param viewHolder 操作的itemHolder
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        itemTouchStatus?.onTouchState(viewHolder.itemView, STATE_NONE)
        actionUp = false
        itemTouchStatus?.onClearView()
    }
}