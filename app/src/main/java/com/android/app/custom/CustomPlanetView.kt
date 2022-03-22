package com.android.app.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.android.app.R

class CustomPlanetView : FrameLayout {
    private var nameView: TextView? = null
    private var iconView: ImageView? = null
    private var planetBean: PlanetItem? = null

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
    }

    fun setPlanetBean(planetBean: PlanetItem) {
        this.planetBean = planetBean
        planetBean.let {
            nameView?.text = it.name
            iconView?.setImageResource(it.planetImage)
        }
    }

    override fun toString(): String {
        return planetBean?.name ?: ""
    }
}
