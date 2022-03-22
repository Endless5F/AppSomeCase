package com.android.app.custom

import androidx.annotation.DrawableRes

class PlanetItem {
    /** 原始位置 */
    var originalIndex = 0

    /** 当前位置位置 */
    var currentIndex = 0

    /** 当前星球角度 */
    var currentAngle = 0f

    /** 上一个星球角度 */
    var previousAngle = 0f

    /** 下一个星球角度 */
    var nextAngle = 0f

    /** 临时星球角度量，用于随手势滑动 */
    var tempAngle = 0f

    /** 星球名称 */
    var name: String? = null

    /** 行星图片 */
    @DrawableRes
    var planetImage: Int = -1

    constructor(originalIndex: Int, name: String?, @DrawableRes planetImage: Int) {
        this.originalIndex = originalIndex
        this.currentIndex = originalIndex
        this.name = name
        this.planetImage = planetImage
    }
}