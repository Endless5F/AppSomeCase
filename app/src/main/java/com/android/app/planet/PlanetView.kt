package com.android.app.planet

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.app.R
import com.android.app.planetview.HomePlanetBean

@SuppressLint("ViewConstructor")
class PlanetView : FrameLayout {
    private var nameView: TextView? = null
    private var iconView: ImageView? = null
    private var planetBean: HomePlanetBean? = null
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        setWillNotDraw(false)
        LayoutInflater.from(context).inflate(R.layout.layout_planet_view, this)
        nameView = findViewById(R.id.planet_name)
        iconView = findViewById(R.id.planet_icon)
    }

    fun setPlanetBean(planetBean: HomePlanetBean) {
        this.planetBean = planetBean
        planetBean.let {
            nameView?.text = it.planetName
            if (it.isActivated) {
                iconView?.setImageResource(it.planetActivateImage)
            } else {
                iconView?.setImageResource(it.planetNormalImage)
            }

            findViewById<TextView>(R.id.button)?.setOnClickListener {
                Toast.makeText(context, planetBean.planetName, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun toString(): String {
        return planetBean?.planetName ?: ""
    }
}
