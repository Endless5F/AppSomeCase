package com.android.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.app.banner.DataBean
import com.android.app.banner.ImageAdapter
import com.android.app.planettab.PlanetTabView
import com.android.app.planettab.initData
import com.android.core.widget.banner2.Banner
import com.android.core.widget.banner2.indicator.CircleIndicator

class MainActivity : AppCompatActivity() {

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
        findViewById<PlanetTabView>(R.id.planet_tab)?.let {
            it.initPlanetListData(initData())
        }
        findViewById<Banner<DataBean, ImageAdapter>>(R.id.banner)?.let {
            /**
             * 画廊效果
             */
            it.setAdapter(ImageAdapter(DataBean.getTestData2()))
            it.indicator = CircleIndicator(this)
            // 添加画廊效果
            it.setBannerGalleryEffect(50, 10)
            // (可以和其他PageTransformer组合使用，比如AlphaPageTransformer，注意但和其他带有缩放的PageTransformer会显示冲突)
            // 添加透明效果(画廊配合透明效果更棒)
            // it.addPageTransformer(new AlphaPageTransformer());
        }
    }
}