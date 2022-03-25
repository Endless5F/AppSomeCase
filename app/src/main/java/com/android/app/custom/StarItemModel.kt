package com.android.app.custom

import androidx.annotation.DrawableRes

class StarItemModel(val name: String? = null, @DrawableRes val icon: Int) {

    companion object {
        val angleArray = arrayListOf<Float>()
        var firstNextAngle: Float = 0f
        var firstPreviousAngle: Float = 0f

        /** 计算每个星球静置时的角度 */
        fun calculateAngleArray(
            startAngle: Float,
            count: Int,
            rule: ((index: Int, averageAngle: Float) -> Float)? = null
        ) {
            angleArray.clear()
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
            firstNextAngle = angleArray[1]
            firstPreviousAngle = angleArray[angleArray.size - 1]
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
        val all = angleArray.size
        this.currentIndex = (currentIndex + count + all) % all
    }

    /** 上一个星球角度 */
    fun previousAngle(count: Int): Float {
        val all = angleArray.size
        val previous = (currentIndex - count + all) % all
        return angleArray[previous]
    }

    /** 下一个星球角度 */
    fun nextAngle(count: Int): Float {
        val next = (currentIndex + count) % angleArray.size
        return angleArray[next]
    }

    /** 当前星球角度，此角度为星球无动画时处于的固定角度 */
    fun currentAngle(): Float {
        return if (currentIndex >= 0 && currentIndex < angleArray.size) angleArray[currentIndex] else 0f
    }
}