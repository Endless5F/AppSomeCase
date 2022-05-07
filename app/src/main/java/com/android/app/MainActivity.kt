package com.android.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.android.app.planettab.PlanetItemView
import com.android.app.planettab.PlanetTabView
import com.android.app.planettab.PlanetTabViewAnim
import com.android.app.planettab.initData
import com.android.app.viewpager.IconPageAdapter
import com.android.app.viewpager.PAGE_WIDTH
import com.android.core.common.recyclerview.common.RecyclerAdapter
import com.android.core.common.recyclerview.layoutmanager.LaneLayoutManager
import com.android.core.ext.countdown
import com.android.core.utils.dip
import com.android.core.utils.wrapLayoutParam
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlin.math.abs

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
            startActivity(Intent(this@MainActivity, PlanetActivity::class.java).apply {
                putExtra("isDemo", false)
            })
        }
        findViewById<PlanetTabView>(R.id.planet_tab)?.let {
            it.initPlanetListData(initData())
        }

        val mMinScale = 0.75f
        findViewById<ViewPager>(R.id.vp2)?.let {
            it.pageMargin = dip(-10)
            it.adapter = IconPageAdapter()
            it.setCurrentItem(0, false)
            it.setPageTransformer(true) { view, position ->
                when {
                    position < -PAGE_WIDTH -> { // 看不到的一页 *
                        view.scaleX = mMinScale
                        view.scaleY = mMinScale
                    }
                    position <= 1 -> {
                        when {
                            position < 0 -> { // 滑出的页 0.0 ~ -PAGE_WIDTH *
                                val scaleFactor = (1 - mMinScale) * (0 - position) / PAGE_WIDTH
                                view.scaleX = 1 - scaleFactor
                                view.scaleY = 1 - scaleFactor
                            }
                            position < PAGE_WIDTH -> { // 滑进的页 PAGE_WIDTH ~ 0.0 *
                                val scaleFactor =
                                    (1 - mMinScale) * (PAGE_WIDTH - position) / PAGE_WIDTH
                                view.scaleX = mMinScale + scaleFactor
                                view.scaleY = mMinScale + scaleFactor
                            }
                            else -> { // 能看到但是为未选中的
                                val diff: Float = abs(position) % PAGE_WIDTH
                                val scaleFactor = diff / PAGE_WIDTH
                                view.scaleX = mMinScale
                                view.scaleY = mMinScale
//                                view.translationX = -dip(10) * scaleFactor
                                Log.e(
                                    "jcy",
                                    "setPageTransformer: 111 PAGE_WIDTH=$PAGE_WIDTH diff=$diff scaleFactor=$scaleFactor"
                                )
                            }
                        }
                    }
                    else -> { // 看不到的另一页 *
                        val diff: Float = abs(position) % PAGE_WIDTH
                        val scaleFactor = diff / PAGE_WIDTH
                        view.scaleX = mMinScale
                        view.scaleY = mMinScale
                        Log.e(
                            "jcy",
                            "setPageTransformer: 222 PAGE_WIDTH=$PAGE_WIDTH diff=$diff scaleFactor=$scaleFactor"
                        )
                    }
                }
            }
        }


//        findViewById<RecyclerView>(R.id.rv)?.let { rv ->
//            rv.adapter = LaneAdapter()
//            rv.layoutManager = LaneLayoutManager()
//
//            countdown(Long.MAX_VALUE, 100) {
//                rv.smoothScrollBy(dip(10), 0, LinearInterpolator(), 120)
//            }.launchIn(MainScope())
//        }
    }

    private inner class LaneAdapter : RecyclerAdapter() {
        private val dataList = listOf(
            LaneBean("aaa0"),
            LaneBean("bbb1"),
            LaneBean("ccc2"),
            LaneBean("ddd3"),
            LaneBean("111111111cccdkfjsdlfjdslkfjlsdkfjlkdsfjksdl"),
            LaneBean("dddidddddddddddddddddddd"),
        )

        override fun getInnerItemCount(): Int {
            return Int.MAX_VALUE
        }

        override fun getInnerViewType(position: Int): Int {
            return 0
        }

        fun getIndex(position: Int): Int = position % dataList.size

        override fun onCreateInnerViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            return RecyclerViewHolder(TextView(parent.context).apply {
                layoutParams = wrapLayoutParam.apply {
                    val padding = context.dip(3)
                    setPadding(padding, padding, padding, padding)
                }
            })
        }

        override fun onBindInnerViewHolder(holder: RecyclerViewHolder, position: Int) {
            val index = getIndex(position)
            if (holder.itemView is TextView) {
                (holder.itemView as TextView).text = dataList[index].text
            }
        }
    }
}

data class LaneBean(var text: String)