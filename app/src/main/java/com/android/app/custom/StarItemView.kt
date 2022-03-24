package com.android.app.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.android.app.R

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
        }
    }

    /** 设置是否选中 */
    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            nameView?.visibility = INVISIBLE
            iconView?.setImageResource(R.drawable.planet_select)
        } else {
            nameView?.visibility = VISIBLE
            iconView?.setImageResource(R.drawable.planet_unselect)
        }
    }

    override fun toString(): String {
        return planetBean?.name ?: ""
    }
}
