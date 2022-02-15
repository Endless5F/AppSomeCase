package com.android.core.widget.multiimage

import android.graphics.Rect
import com.android.core.AppRuntime
import com.android.core.util.dip
import com.android.core.util.getScreenWidth
import kotlin.math.min

// 单行显示个数
const val SINGLE_LINE_DISPLAY_MAX_COUNT = 3

// 多图显示策略
abstract class MultiImageStrategy {

    /**
     * 多图显示位置集
     */
    var imageRectList = arrayListOf<Rect>()

    protected var marginSum = 0

    /** 高宽比，仅一张时有效 */
    protected var percent = 1f

    protected var isVideo = false

    protected var itemSpaceSize: Int = AppRuntime.getAppContext().dip(4)
    protected val screenWidth: Int = AppRuntime.getAppContext().getScreenWidth()

    fun setItemSpace(size: Int) {
        itemSpaceSize = size
    }

    /**
     * 可通过此方法在 setImageSize 之后调用，替换宽高比
     * @param isVideo 是否是视频图片(走单独的显示流程)
     */
    fun setShowPercent(percent: Float, isVideo: Boolean = false) {
        this.percent = percent
        this.isVideo = isVideo
    }

    /**
     * @param marginArray 此margin 为整个Layout距离屏幕两边的间距
     */
    fun setMarginArray(marginArray: IntArray = intArrayOf(0, 0)) {
        marginSum = 0
        for (margin in marginArray) {
            marginSum += margin
        }
    }

    abstract fun setImageCount(count: Int)
}

// 多图均分显示策略
class MultiImageAverageStrategy : MultiImageStrategy() {

    private var itemWidth: Int = screenWidth
    private val singleImageWidth = AppRuntime.getAppContext().dip(216)

    override fun setImageCount(count: Int) {
        // reset
        imageRectList.clear()

        if (count > 0) {
            var left: Int
            var top: Int
            var row: Int // 第一行
            var column: Int // 第一列
            val number = min(count, 9)
            // 单行最大显示
            val rowMaxCount = when (number) {
                1 -> 1
                4 -> 2
                else -> SINGLE_LINE_DISPLAY_MAX_COUNT
            }

            if (count == 1) {
                itemWidth = if (isVideo) screenWidth - marginSum else singleImageWidth
                val itemHeight = itemWidth * percent
                imageRectList.add(Rect(0, 0, itemWidth, itemHeight.toInt()))
            } else {
                itemWidth =
                    (screenWidth - marginSum - (SINGLE_LINE_DISPLAY_MAX_COUNT - 1) * itemSpaceSize) / SINGLE_LINE_DISPLAY_MAX_COUNT
                for (i in 0 until number) {
                    row = i / rowMaxCount + 1
                    column = i % rowMaxCount + 1
                    left = (column - 1) * (itemWidth + itemSpaceSize)
                    top = (row - 1) * (itemWidth + itemSpaceSize)
                    imageRectList.add(Rect(left, top, left + itemWidth, top + itemWidth))
                }
            }
        } else {
            throw IllegalAccessException("多图均分策略不允许count小于1")
        }
    }
}