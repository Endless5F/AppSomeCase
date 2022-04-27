package com.android.app.planettab

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.android.app.R
import kotlin.math.abs

/**
 * @author jiaochengyun@baidu.com
 * @since 3.0.0
 */
class PlanetItemView : FrameLayout {
    private var nameView: TextView? = null
    private var iconView: ImageView? = null
    private var iconSelectView: ImageView? = null
    private var planetBean: PlanetItemData? = null

    var clickListener: ((PlanetItemData?) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_planet_view, this)
        nameView = findViewById(R.id.planet_name)
        iconView = findViewById(R.id.planet_icon)
        iconSelectView = findViewById(R.id.planet_icon_select)
    }

    fun setPlanetBean(planetBean: PlanetItemData) {
        this.planetBean = planetBean
        planetBean.let {
            nameView?.text = it.name
            iconView?.setImageResource(it.picture)
            iconSelectView?.alpha = 0f
            iconSelectView?.setImageResource(it.pictureSelect)
        }
    }

    fun getSelectedIcon(): View? {
        return iconSelectView
    }

    fun setItemViewAlpha(alpha: Float) {
        // 2倍速渐变：1f - (1f - alpha) * 2，(1f - alpha) * 2 属于0..1f
        val diff2x = (1f - alpha) * 2
        val alpha2x = 1f - if (diff2x < 0) 0f else if (diff2x > 1f) 1f else diff2x

        nameView?.alpha = alpha2x
        iconView?.alpha = alpha2x
        iconSelectView?.alpha = 1f - alpha
    }

    /** 手势处理 */
    private var downX = 0f
    private var downY = 0f
    /** 系统所认为的最小滑动距离TouchSlop */
    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x ?: 0f
        val y = event?.y ?: 0f
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = downX - x
                val dy = downY - y
                if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                    return super.onTouchEvent(event)
                }
            }
            MotionEvent.ACTION_UP -> {
                val dx = downX - x
                val dy = downY - y
                if (abs(dx) < touchSlop && abs(dy) < touchSlop) {
                    clickListener?.invoke(planetBean)
                }
            }
        }
        return true
    }

    override fun toString(): String {
        return planetBean?.name ?: ""
    }
}
