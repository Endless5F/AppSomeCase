package com.android.app.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.app.R
import com.android.core.utils.wrapFrameLayoutParam

/**
 * 组件编辑View
 */
class ComponentEditView: FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val recyclerView = RecyclerView(context).apply {

            layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        addView(recyclerView)

        addView(ImageView(context).apply {
            setImageResource(R.drawable.ic_launcher_round)
            layoutParams = wrapFrameLayoutParam.apply {
                gravity = Gravity.CENTER
            }
        })
    }


}