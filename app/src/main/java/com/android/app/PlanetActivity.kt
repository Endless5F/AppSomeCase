package com.android.app

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.android.app.planet.PlanetGroupView
import com.android.app.planet.PlanetView
import com.android.app.planetview.HomePlanetBean

/**
 * @author guolong
 * @since 2019/8/21
 *
 *
 * https://www.jianshu.com/p/2954f2ef8ea5
 */
class PlanetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isDemo = intent.getBooleanExtra("isDemo", true)
        if (isDemo) {
            setContentView(R.layout.activity_land)
            val sunView = findViewById<View>(R.id.sun_view)
            sunView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.sun_anim))
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            setContentView(R.layout.activity_land_star)
            initData()
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    private fun initData() {
        val result = ArrayList<HomePlanetBean>()
        val moon = HomePlanetBean(
            "月球",
            R.drawable.planet_yueqiu_mormal,
            R.drawable.planet_yueqiu_activated,
            true
        )
        val shui = HomePlanetBean(
            "水星",
            R.drawable.planet_shuixing_normal,
            R.drawable.planet_shuixing_activated,
            true
        )
        val jin = HomePlanetBean(
            "金星",
            R.drawable.planet_jinxing_normal,
            R.drawable.planet_jinxing_activated,
            true
        )
        val earth = HomePlanetBean(
            "地球",
            R.drawable.planet_diqiu_normal,
            R.drawable.planet_diqiu_activated,
            true
        )
        val fire = HomePlanetBean(
            "火星",
            R.drawable.planet_huoxing_normal,
            R.drawable.planet_huoxing_activated,
            true
        )
        val wood = HomePlanetBean(
            "木星",
            R.drawable.planet_muxing_normal,
            R.drawable.planet_muxing_activated,
            true
        )
        val soil = HomePlanetBean(
            "土星",
            R.drawable.planet_tuxing_normal,
            R.drawable.planet_tuxing_activated,
            true
        )
        val gold = HomePlanetBean(
            "天王星",
            R.drawable.planet_tianwangxing_normal,
            R.drawable.planet_tianwangxing_activated,
            true
        )
        val ocean = HomePlanetBean(
            "海王星",
            R.drawable.planet_haiwangxing_normal,
            R.drawable.planet_haiwagnxing_activated,
            false
        )
//        result.add(moon)
//        result.add(shui)
        result.add(jin)
        result.add(earth)
        result.add(fire)
        result.add(wood)
        result.add(soil)
        result.add(gold)
        result.add(ocean)
        val starGroupView = findViewById<PlanetGroupView>(R.id.starGroupView)
        for (i in result.indices) {
            val child = PlanetView(this).apply {
                setPlanetBean(result[i])
            }
            starGroupView.addView(child, FrameLayout.LayoutParams(-2, -2))
        }
        starGroupView.requestLayout()
    }
}