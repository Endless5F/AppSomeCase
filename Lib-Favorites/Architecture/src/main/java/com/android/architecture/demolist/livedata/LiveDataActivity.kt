package com.android.architecture.demolist.livedata

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.android.architecture.R
import kotlinx.android.synthetic.main.activity_live_data.*

class LiveDataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_data)
        val viewModel = ViewModelProviders.of(this).get(LiveDataTimerViewModel::class.java)
        // LiveData数据改变的监听
        viewModel.elapsedTime.observe(this,
            { t -> textView3.text = t?.toString() })
    }
}
