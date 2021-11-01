package com.android.customwidget.widget.scroll

import android.animation.ValueAnimator
import android.view.MotionEvent
import java.lang.Math.abs

/**
 * 公共底Bar 随手势滚动动态帮助类
 */
class CommonBottomBarAnimHelper {

    /**
     * 动画响应阈值
     */
    private val animResponseThreshold = 50f
    private var animCallback: ((Float) -> Unit)? = null

    /**
     * 正常态
     */
    private val NORMAL_STATE = 0

    /**
     * 正常态--》退化态
     */
    private val NORMAL_TO_DEGENERATE_STATE = 1

    /**
     * 退化态--》正常态
     */
    private val DEGENERATE_TO_NORMAL_STATE = 2

    /**
     * 退化态
     */
    private val DEGENERATE_STATE = 3

    /**
     * 退化状态，仅显示一行搜索词或链接；反之，为正常态
     */
    private var degenerateSate = NORMAL_STATE

    private var valueAnimator: ValueAnimator? = null

    private var stateChangeListener = object : ScrollStateChangeListener() {
        private var isUp = true
        private var addY = 0f

        override fun onScrollStart(initX: Int, initY: Int, up: Boolean) {
            this.isUp = up
            this.addY = 0f
        }

        override fun onScrolling(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
            val diff = scrollY - oldScrollY
            val direction = diff > 0
            addY = limitMaxSize(addY, animResponseThreshold)

            if (isUp) { // 初始向上滑动
                val lastProgress = abs(addY) / animResponseThreshold
                addY += diff
                if (isUp == direction) { // 和初始是同方向
                    if (degenerateSate == DEGENERATE_STATE) return
                    addY = limitMaxSize(addY, animResponseThreshold)
                    val currentProgress = abs(addY) / animResponseThreshold
                    animAutomatic(lastProgress, currentProgress)
                } else { // 和初始是不同方向
                    addY = if (addY < 0f) 0f else addY
                    val currentProgress = abs(addY) / animResponseThreshold
                    animAutomatic(lastProgress, currentProgress)
                }
            } else { // 初始向下滑动
                val lastProgress = 1f - abs(addY) / animResponseThreshold
                addY += diff

                if (isUp == direction) { // 和初始是同方向
                    if (degenerateSate == NORMAL_STATE) return
                    addY = limitMaxSize(addY, animResponseThreshold)
                    val currentProgress = 1f - abs(addY) / animResponseThreshold
                    animAutomatic(lastProgress, currentProgress)
                } else { // 和初始是不同方向
                    addY = if (addY > 0f) 0f else addY
                    val currentProgress = 1f - abs(addY) / animResponseThreshold
                    animAutomatic(currentProgress, lastProgress)
                }
            }
        }

        override fun onScrollEnd(lastX: Int, lastY: Int, up: Boolean) {
            val lastProgress = if (isUp) abs(addY) / animResponseThreshold else 1 - abs(addY) / animResponseThreshold
            if (abs(addY) < animResponseThreshold) {
                if (degenerateSate == NORMAL_TO_DEGENERATE_STATE) {
                    animAutomatic(lastProgress, 0f)
                } else if (degenerateSate == DEGENERATE_TO_NORMAL_STATE) {
                    animAutomatic(lastProgress, 1f)
                }
            }
        }
    }

    /**
     * 设置动画进度回调
     */
    fun setAnimProgressCallback(callback: (Float) -> Unit) {
        animCallback = callback
    }

    /**
     * 设置触摸事件，用于监听触摸结束事件，确定滑动停止
     */
    fun onTouch(event: MotionEvent) {
        stateChangeListener.onTouch(event)
    }

    /**
     * 设置滚动改变
     */
    fun onScrollChange(l: Int, t: Int, oldl: Int, oldt: Int) {
        stateChangeListener.onScrollChange(l, t, oldl, oldt)
    }

    /**
     * 是否是退化态
     */
    fun isDegenerateSate(): Boolean {
        return degenerateSate == DEGENERATE_STATE
    }

    /**
     * 还原底Bar 为正常态
     */
    fun resetTooBarAnim() {
        animAutomatic(1f, 0f)
    }

    private fun animAutomatic(startValue: Float, endValue: Float) {
        degenerateSate = if (startValue > endValue) {
            DEGENERATE_TO_NORMAL_STATE
        } else {
            NORMAL_TO_DEGENERATE_STATE
        }
        if (degenerateSate == DEGENERATE_STATE && endValue == 1f) {
            return
        }
        if (degenerateSate == NORMAL_STATE && endValue == 0f) {
            return
        }
        valueAnimator = ValueAnimator.ofFloat(startValue, endValue)
        valueAnimator?.duration = 300
        valueAnimator?.addUpdateListener {
            animFromProcess(it.animatedValue as Float)
        }
        valueAnimator?.start()
        if (endValue == 0f) degenerateSate = NORMAL_STATE
        if (endValue == 1f) degenerateSate = DEGENERATE_STATE
    }

    private fun animFromProcess(fl: Float) {
        animCallback?.invoke(fl)
    }

    private fun limitMaxSize(limitValue: Float, targetValue: Float): Float {
        var value = limitValue
        if (limitValue < -targetValue) value = -targetValue
        if (limitValue > targetValue) value = targetValue
        return value
    }
}