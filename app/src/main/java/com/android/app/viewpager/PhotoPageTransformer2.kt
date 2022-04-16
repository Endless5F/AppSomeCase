package com.android.app.viewpager

import android.view.View
import androidx.viewpager.widget.ViewPager

class PhotoPageTransformer2 : ViewPager.PageTransformer {
    private val mMinAlpha = 0.3f
    private val leftscaleX = 0.8f
    private val rightscaleX = 0.8f
    private val leftTransY = 0.05f //(1-0.9)/2
    private val rightTransY = 0.25f
    override fun transformPage(view: View, position: Float) {
        val pageHeight = view.width
        if (position < -1) { // [-Infinity,-1)  This page is way off-screen to the left.
            view.alpha = mMinAlpha
            view.scaleX = leftscaleX
            view.scaleY = leftscaleX
            view.translationX = -pageHeight * leftTransY
        } else if (position <= 1) {
            if (position <= 0) { //[-1，0) 左滑  a页滑动至b页：a页从 0.0~-1，b页从1 ~ 0.0
                //1到mMinAlpha
                val factor = mMinAlpha + (1 - mMinAlpha) * (1 + position)
                view.alpha = factor
                view.scaleX = leftscaleX + (1 - leftscaleX) * (1 + position)
                view.scaleY= leftscaleX + (1 - leftscaleX) * (1 + position)
                //0到leftTransY的变化
                view.translationX = -pageHeight * (leftTransY * position)
            } else  //[0,1]  1~0
            {
                //minAlpha到1的变化
                val factor = mMinAlpha + (1 - mMinAlpha) * (1 - position)
                view.alpha = factor
                view.scaleX = rightscaleX + (1 - rightscaleX) * (1 - position)
                view.scaleY = rightscaleX + (1 - rightscaleX) * (1 - position)
                //rightTransY到0的变化
//                view.translationX = -pageHeight * (rightTransY * position)
            }
        } else {  // (1,+Infinity]
            // This page is way off-screen to the right.
            view.alpha = mMinAlpha
            view.scaleX = rightscaleX
            view.scaleY = rightscaleX
            view.translationX = -pageHeight * rightTransY * 2/4
        }
    }
}