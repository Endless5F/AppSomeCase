package com.android.core.common.recyclerview.drag

import android.view.View

interface IItemTouchStatus {

    /**
     * 拖动操作移动数据源到指定位置
     *
     * @param fromPosition 移动之前数据源位置
     * @param toPosition 移动之后数据源位置
     * @return true:列表中元素已移动
     */
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    /**
     * 拖动操作删除指定数据源
     *
     * @param position 删除指定数据源位置
     */
    fun onItemRemove(position: Int)

    /**
     * 拖动状态
     * @param itemView 条目视图
     * @param state 状态
     */
    fun onTouchState(itemView: View?, state : Int)

    /**
     * 当用户与item的交互结束，item完成动画时调用
     */
    fun onClearView()
}