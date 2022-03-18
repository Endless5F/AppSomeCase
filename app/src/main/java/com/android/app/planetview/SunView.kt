package com.android.app.planetview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.android.app.R

/**
 * 太阳
 */
class SunView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_sun_view,this)
        findViewById<AppCompatImageView>(R.id.iv_sun_light)?.startAnimation(AnimationUtils.loadAnimation(context, R.anim.sun_anim))
    }
}
