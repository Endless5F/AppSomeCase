package com.android.customwidget.data

import java.util.*

/**
 * Created by jcy on 2018/8/8.
 */
object HomeData {
    var addDevTotalRes: MutableList<ItemView> = ArrayList()
    private fun addDeviceItem(icon: String, desc: String) {
        addDevTotalRes.add(ItemView(icon, desc))
    }

    class ItemView(var icon: String, var desc: String)

    init {
        addDeviceItem("", "自定义View demo")
        addDeviceItem("&#xe620;", "Canves draw 基本用法")
        addDeviceItem("&#xe620;", "Paint 画笔的基本用法")
        addDeviceItem("&#xe620;", "DrawText 绘制文字的基本用法")
        addDeviceItem("&#xe620;", "Clip和Matrix对绘制的辅助")
        addDeviceItem("&#xe620;", "View的绘制顺序")
        addDeviceItem("&#xe620;", "Animation（上手篇）")
        addDeviceItem("&#xe620;", "Animation（进阶篇）")
        addDeviceItem("&#xe620;", "自定义布局onMeasure基础")
        addDeviceItem("&#xe620;", "动态可变高度ViewPager")
    }
}