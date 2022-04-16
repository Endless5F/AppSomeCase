package com.android.app.viewpager

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.android.app.banner.DataBean
import com.android.core.utils.matchLayoutParam

const val PAGE_WIDTH = 110 / 342f

class IconPageAdapter: PagerAdapter() {

    private val data = DataBean.getTestData2()
    private val realCount = data.size

    override fun getPageWidth(position: Int): Float {
        return PAGE_WIDTH
    }

    override fun getCount(): Int {
        return Int.MAX_VALUE
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(container.context).apply {
            layoutParams = matchLayoutParam
//            setPadding(context.dip(-10), 0, 0, 0)
            scaleType = ImageView.ScaleType.FIT_XY
            cropToPadding = true
            val res = data[position % realCount].imageRes
            setImageResource(res)
        }
        container.addView(imageView)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}