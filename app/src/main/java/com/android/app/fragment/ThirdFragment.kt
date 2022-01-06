package com.android.app.fragment

import android.view.View
import android.widget.ImageView
import com.android.app.R

class ThirdFragment: LazyFragment() {
    override val layout: Int
        get() = R.layout.fragment_layout

    override fun initView(view: View?) {
        val iv = view?.findViewById<ImageView>(R.id.iv_image)
        iv?.setImageResource(R.drawable.icon_3)
    }
}