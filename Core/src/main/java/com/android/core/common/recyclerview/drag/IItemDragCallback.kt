package com.android.core.common.recyclerview.drag

import androidx.recyclerview.widget.RecyclerView

interface IItemDragCallback {

    /**
     * 开始拖动
     *
     * @param viewHolder 拖动的元素适配对象
     */
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder): Boolean
}