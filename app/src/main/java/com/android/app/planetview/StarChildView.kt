package com.android.app.planetview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.app.R

@SuppressLint("ViewConstructor")
class StarChildView(planetBean: HomePlanetBean, context: Context) : FrameLayout(context) {

    init {
        setWillNotDraw(false)
        LayoutInflater.from(context).inflate(R.layout.layout_planet_view, this)
        val nameView = findViewById<TextView>(R.id.planet_name)
        val iconView = findViewById<ImageView>(R.id.planet_icon)
        planetBean.let {
            nameView.text = it.planetName
            if (it.isActivated) {
                iconView.setImageResource(it.planetActivateImage)
            } else {
                iconView.setImageResource(it.planetNormalImage)
            }

            findViewById<TextView>(R.id.button)?.setOnClickListener {
                Toast.makeText(context, planetBean.planetName, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
