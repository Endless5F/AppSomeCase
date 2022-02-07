package com.android.core.widget.multiimage

import android.graphics.Rect
import com.android.core.AppRuntime
import com.android.core.util.dip
import com.android.core.util.getScreenWidth
import kotlin.math.ceil
import kotlin.math.min

// 单行显示个数
const val SINGLE_LINE_DISPLAY_MAX_COUNT = 3

// 多图显示策略
abstract class MultiImageStrategy {

    /**
     * 多图显示位置集
     */
    var imageRectList = arrayListOf<Rect>()

    protected var width = 0
    protected var height = 0
    protected var marginSum = 0

    /** 宽高比 */
    protected var percent = 3f / 2
    protected var singleLineDisplayCount = SINGLE_LINE_DISPLAY_MAX_COUNT

    protected var itemSpaceSize: Int = AppRuntime.getAppContext().dip(4)
    protected val screenWidth: Int = AppRuntime.getAppContext().getScreenWidth()

    fun setItemSpace(size: Int) {
        itemSpaceSize = size
    }

    fun setImageSize(width: Int, height: Int) {
        this.width = width
        this.height = height

        // 设置宽高比
        percent = when {
            width != 0 && width == height -> 1f
            height > width -> 3f / 4
            else -> 3f / 2
        }
    }

    /**
     * 可通过此方法在 setImageSize 之后调用，替换宽高比
     */
    fun setShowPercent(percent: Float) {
        this.percent = percent
    }

    /**
     * @param marginArray 此margin 为整个Layout距离屏幕两边的间距
     */
    fun setMarginArray(marginArray: IntArray = intArrayOf(0, 0)) {
        for (margin in marginArray) {
            marginSum += margin
        }
    }

    abstract fun setImageCount(count: Int)
}

// 多图均分显示策略
class MultiImageAverageStrategy : MultiImageStrategy() {

    private var itemWidth: Int = screenWidth / SINGLE_LINE_DISPLAY_MAX_COUNT

    override fun setImageCount(count: Int) {
        imageRectList.clear()
        if (count > 0) {
            // 单行最大显示
            singleLineDisplayCount = when (count) {
                1 -> 1
                4 -> 2
                else -> SINGLE_LINE_DISPLAY_MAX_COUNT
            }
            // 设置item宽度，只在 width == 0 时生效
            itemWidth = if (singleLineDisplayCount == 1) {
                screenWidth - marginSum
            } else {
                (screenWidth - marginSum - (SINGLE_LINE_DISPLAY_MAX_COUNT - 1) * itemSpaceSize) / SINGLE_LINE_DISPLAY_MAX_COUNT
            }

            var left = 0
            var top = 0
            var right = itemWidth
            var bottom = itemWidth

            when (val number = min(count, 9)) {
                1 -> {
                    if (width > 0) {
                        imageRectList.add(Rect(left, top, width, (width / percent).toInt()))
                    } else {
                        imageRectList.add(Rect(left, top, itemWidth, (itemWidth / percent).toInt()))
                    }
                }
                4 -> {
                    for (i in 0 until number) {
                        val index = i % singleLineDisplayCount
                        ceil((i / 2).toDouble())
                        left = index * (itemWidth + itemSpaceSize)
                        right = (index + 1) * itemWidth + index * itemSpaceSize
                        when {
                            i > 1 -> {
                                top = itemWidth + itemSpaceSize
                                bottom = 2 * itemWidth + itemSpaceSize
                            }
                        }
                        imageRectList.add(Rect(left, top, right, bottom))
                    }
                }
                else -> {
                    for (i in 0 until number) {
                        val index = i % singleLineDisplayCount
                        left = index * (itemWidth + itemSpaceSize)
                        right = (index + 1) * itemWidth + index * itemSpaceSize
                        when {
                            i > 5 -> {
                                top = 2 * (itemWidth + itemSpaceSize)
                                bottom = 3 * itemWidth + 2 * itemSpaceSize
                            }
                            i > 2 -> {
                                top = itemWidth + itemSpaceSize
                                bottom = 2 * itemWidth + itemSpaceSize
                            }
                        }
                        imageRectList.add(Rect(left, top, right, bottom))
                    }
                }
            }
        } else {
            throw IllegalAccessException("多图均分策略不允许count小于1")
        }
    }
}