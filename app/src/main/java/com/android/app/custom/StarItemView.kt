package com.android.app.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.android.app.R
import android.widget.ImageView
import android.widget.TextView

class StarItemView : FrameLayout {
    private var nameView: TextView? = null
    private var iconView: ImageView? = null
    private var planetBean: StarItemModel? = null

    var clickListener: ((isSelected: Boolean, currentIndex: Int) -> Unit)? = null

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

        setOnClickListener {
            clickListener?.invoke(planetBean?.isSelected ?: false, planetBean?.currentIndex ?: 0)
        }
    }

    fun setPlanetBean(planetBean: StarItemModel) {
        this.planetBean = planetBean
        planetBean.let {
            nameView?.text = it.name
            isSelected = it.isSelected
            iconView?.setImageResource(planetBean.icon)
        }
    }

    fun setNameAlpha(alpha: Float) {
        nameView?.alpha = alpha
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
