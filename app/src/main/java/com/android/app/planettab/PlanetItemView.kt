package com.android.app.planettab

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.android.app.R

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

        setOnClickListener {
            clickListener?.invoke(planetBean)
        }
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

    fun setNameAlpha(alpha: Float, threshold: Float) {
        // 2倍速渐变：1f - (1f - alpha) * 2，(1f - alpha) * 2 属于0..1f
        val diff2x = (1f - alpha) * 2
        val alpha2x = 1f - if (diff2x < 0) 0f else if (diff2x > 1f) 1f else diff2x

        nameView?.alpha = alpha2x
        iconView?.alpha = alpha2x
        iconSelectView?.alpha = 1f - alpha
    }

    override fun setScaleX(scaleX: Float) {
        super.setScaleX(scaleX)
        nameView?.scaleX = 1f / 2
    }

    override fun setScaleY(scaleY: Float) {
        super.setScaleY(scaleY)
        nameView?.scaleY = 1f / 2
    }

    override fun toString(): String {
        return planetBean?.name ?: ""
    }
}
