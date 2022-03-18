package com.android.app.planetview

import androidx.annotation.DrawableRes

/**
 * 主页 星球豆bean类
 */
data class HomePlanetBean(
        val planetName: String,//行星名字
        @DrawableRes val planetNormalImage: Int,//行星未激活图片
        @DrawableRes val planetActivateImage: Int,// 行星已激活图片
        val isActivated: Boolean, // 是否已激活
)