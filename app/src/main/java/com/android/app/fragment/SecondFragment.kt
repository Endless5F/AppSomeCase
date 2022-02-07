package com.android.app.fragment

import android.view.View
import android.widget.ImageView
import com.android.app.R
import com.android.core.widget.multiimage.ImageEntity
import com.android.core.widget.multiimage.RecMultiImageLayout

class SecondFragment: LazyFragment() {
    override val layout: Int
        get() = R.layout.fragment_layout

    override fun initView(view: View?) {
        val iv = view?.findViewById<ImageView>(R.id.iv_image)
        iv?.setImageResource(R.drawable.icon_2)

//        val multiImageLayout = view?.findViewById<RecMultiImageLayout>(R.id.iv_image)
//        val dataList = arrayListOf<ImageEntity>()
//        for (i in 0..9) {
//            dataList.add(ImageEntity("http://inews.gtimg.com/newsapp_match/0/11697869775/0.jpg"))
//        }
    }
}