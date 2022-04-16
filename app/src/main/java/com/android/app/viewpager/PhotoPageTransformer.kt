package com.android.app.viewpager

import android.view.View
import androidx.viewpager.widget.ViewPager

class PhotoPageTransformer : ViewPager.PageTransformer {
    private val mMinAlpha = 0.3f
    private val leftScaleY = 0.9f
    private val rightScaleY = 0.5f
    private val leftTransY = 0.05f //(1-0.9)/2
    private val rightTransY = 0.25f
    override fun transformPage(view: View, position: Float) {
        val pageHeight = view.height
        if (position < -1) { // [-Infinity,-1)  This page is way off-screen to the left.
            view.alpha = mMinAlpha
            view.scaleY = leftScaleY
            view.translationY = pageHeight * leftTransY
        } else if (position <= 1) {
            if (position <= 0) { //[-1，0) 左滑  a页滑动至b页：a页从 0.0~-1，b页从1 ~ 0.0
                //1到mMinAlpha
                val factor = mMinAlpha + (1 - mMinAlpha) * (1 + position)
                view.alpha = factor
                view.scaleY = leftScaleY + (1 - leftScaleY) * (1 + position)
                //0到leftTransY的变化
                view.translationY = -pageHeight * (leftTransY * position)
            } else  //[0,1]  1~0
            {
                //minAlpha到1的变化
                val factor = mMinAlpha + (1 - mMinAlpha) * (1 - position)
                view.alpha = factor
                view.scaleY = rightScaleY + (1 - rightScaleY) * (1 - position)
                //rightTransY到0的变化
                view.translationY = pageHeight * (rightTransY * position)
            }
        } else {  // (1,+Infinity]
            // This page is way off-screen to the right.
            view.alpha = mMinAlpha
            view.scaleY = rightScaleY
            view.translationY = pageHeight * rightTransY
        }
    }
}