package com.android.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.app.custom.PlanetItem

class MainActivity : AppCompatActivity() {

    private val dataArray = arrayListOf<PlanetItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val multiImageLayout = findViewById<RecMultiImageLayout>(R.id.iv_image)
//        val dataList = arrayListOf<ImageEntity>()
//        dataList.add(ImageEntity("http://inews.gtimg.com/newsapp_match/0/11697869775/0.jpg"))
//        dataList.add(ImageEntity("http://inews.gtimg.com/newsapp_match/0/11697869775/0.jpg"))
//        dataList.add(ImageEntity("http://inews.gtimg.com/newsapp_match/0/11697869775/0.jpg"))
//        dataList.add(ImageEntity("http://inews.gtimg.com/newsapp_match/0/11697869775/0.jpg"))
//        dataList.add(ImageEntity("http://inews.gtimg.com/newsapp_match/0/11697869775/0.jpg"))
//        dataList.add(ImageEntity("http://inews.gtimg.com/newsapp_match/0/11697869775/0.jpg"))
//        dataList.add(ImageEntity("https://c-ssl.duitang.com/uploads/blog/202105/09/20210509225323_2c0c6.jpeg"))
//        dataList.add(ImageEntity("http://inews.gtimg.com/newsapp_match/0/11697869775/0.jpg"))
//        dataList.add(ImageEntity("http://inews.gtimg.com/newsapp_match/0/11697869775/0.jpg"))
//        dataList.add(ImageEntity("http://inews.gtimg.com/newsapp_match/0/11697869775/0.jpg"))
//        multiImageLayout.setMarginArray(intArrayOf(dip(10), dip(10)))
//        multiImageLayout.showImages(dataList)
//        multiImageLayout.showVideo("http://inews.gtimg.com/newsapp_match/0/11697869775/0.jpg")

        findViewById<Button>(R.id.button)?.setOnClickListener {
            startActivity(Intent(this@MainActivity, PlanetActivity::class.java))
        }
        findViewById<Button>(R.id.button2)?.setOnClickListener {
            startActivity(Intent(this@MainActivity, PlanetActivity::class.java).apply{
                putExtra("isDemo",false)
            })
        }

//        findViewById<CustomPlanetGroup>(R.id.planet_group).let {
//            dataArray.add(PlanetItem(0, 0, "地球", R.drawable.planet_diqiu_activated))
//            dataArray.add(PlanetItem(1, 1, "水星", R.drawable.planet_shuixing_activated))
//            dataArray.add(PlanetItem(2, 1, "金星", R.drawable.planet_jinxing_activated))
//            dataArray.add(PlanetItem(3, 2, "火星", R.drawable.planet_huoxing_normal))
//            dataArray.add(PlanetItem(4, 2, "土星", R.drawable.planet_tuxing_activated))
//            dataArray.add(PlanetItem(5, 3, "木星", R.drawable.planet_tianwangxing_normal))
//            dataArray.add(PlanetItem(6, 3, "海王星", R.drawable.planet_haiwagnxing_activated))
//
//            for (i in dataArray.indices) {
//                val child = CustomPlanetView(this).apply {
//                    setPlanetBean(dataArray[i])
//                }
//                it.addView(child, FrameLayout.LayoutParams(dip(80), dip(80)))
//            }
//        }
    }
}