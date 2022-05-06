package com.android.app.planettab

import androidx.annotation.DrawableRes
import com.android.app.R

/**
 * @author jiaochengyun@baidu.com
 * @since 3.0.0
 */
class PlanetItemData(
    val id: String? = null,
    val name: String? = "null",
    val topicId: String? = null,
    @DrawableRes val picture: Int = R.drawable.icon_tide_play_star,
    val afxPlayPath: String = "",
    @DrawableRes val pictureSelect: Int = R.drawable.icon_planet_selected
) {

    companion object {
        var planetCount = 0
        var firstNextAngle: Float = 0f
        var firstPreviousAngle: Float = 0f
        val angleArray = arrayListOf<Float>()

        /** 计算静置状态每个星球静置时的角度 */
        fun calculateAngleArray(
                startAngle: Float,
                count: Int,
                rule: ((index: Int, averageAngle: Float) -> Float)? = null
        ) {
            if (count == 0) return
            angleArray.clear()
            planetCount = count
            /** 每个星球平均的角度 */
            val averageAngle = 360f / count
            if (rule != null) {
                for (i in 0 until count) {
                    angleArray.add(rule.invoke(i, averageAngle))
                }
            } else {
                for (i in 0 until count) {
                    angleArray.add((startAngle + averageAngle * i) % 360)
                }
            }
            angleArray.lastOrNull()?.let { firstPreviousAngle = it }
            angleArray.getOrNull(1)?.let { firstNextAngle = it }
        }

        /** 当前角度是否属于选中(最近)星球的左右范围(index == 0 上一个--下一个之间) */
        fun isSelectAngleRange(angle: Float): Boolean {
            if (angleArray.isEmpty()) return false
            return angle in (firstPreviousAngle)..(firstNextAngle)
//            return angle in (firstPreviousAngle + 0.00005)..(firstNextAngle - 0.00005)
        }
    }

    /** 原始位置 */
    var originalIndex = 0

    /** 当前位置位置 */
    var currentIndex = 0

    /** 当前星球角度，滑动过程中会实时更新 */
    var currentAngle = 0f

    /** 星球是否被选中，即是否最最前面 */
    var isSelected: Boolean = false

    /** 初始化最开始的index */
    fun initIndex(index: Int) {
        this.currentIndex = index
        this.originalIndex = index
    }

    /**
     * 重新设置当前位置
     * @param count 改变位置树，负为左滑减少，正为右滑增加
     */
    fun resetCurrentIndex(count: Int) {
        this.currentIndex = (currentIndex + count + planetCount) % planetCount
    }

    /** 当前星球角度，此角度为星球无动画时处于的固定角度 */
    fun currentAngle(): Float {
        return angleArray.getOrNull(currentIndex) ?: 0f
    }

    /** 下一个星球角度 */
    fun nextAngle(count: Int): Float {
        return angleArray.getOrNull((currentIndex + count) % planetCount) ?: 0f
    }

    /** 上一个星球角度 */
    fun previousAngle(count: Int): Float {
        return angleArray.getOrNull((currentIndex - count + planetCount) % planetCount) ?: 0f
    }
}