package com.android.customwidget.activity

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.customwidget.R
import com.android.customwidget.adapter.HomePageAdapter
import com.android.customwidget.data.HomeData
import com.android.customwidget.ext.setPaddingStatusBarHeight
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val title = findViewById<TextView>(R.id.title)
        setPaddingStatusBarHeight(title)
        rl_demo_list.layoutManager = LinearLayoutManager(this)
        val homePageAdapter = HomePageAdapter(this, HomeData.addDevTotalRes)
        rl_demo_list.adapter = homePageAdapter
    }
}