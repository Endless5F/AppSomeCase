package com.android.customwidget.ext

import android.R
import android.app.Activity
import android.view.View
import android.view.ViewGroup

class ActivityExt(private val activity: Activity) {
    /**
     * Activity中真实的根布局为DecorView(FrameLayout子类)
     * DecorView包含一个线性布局LinearLayout，LinearLayout其分为上下两部分：titleBar和mContentParent。
     * 而mContentParent实际上就是我们在布局文件中绘制布局显示的区域。mContentParent的id即android.R.id.content
     */
    private val contentParent: ViewGroup = activity.findViewById(R.id.content)

    fun addContentView(view: View?, params: ViewGroup.LayoutParams?) {
        contentParent.addView(view, params)
    }

    fun removeContentView(view: View?) {
        contentParent.removeView(view)
    }
}