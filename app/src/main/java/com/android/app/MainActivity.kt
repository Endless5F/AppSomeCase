package com.android.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.core.restful.HiCallback
import com.android.core.restful.HiResponse
import com.android.core.restful.demo.ApiFactory
import com.android.core.restful.demo.TestApi
import com.android.kmmshared.Greeting

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ApiFactory.create(TestApi::class.java).getArticleList(0)
            .enqueue(object : HiCallback<String> {
                override fun onSuccess(response: HiResponse<String>) {
                    Log.e("jcy", "onSuccess: ${response.data}")
                }

                override fun onFailed(throwable: Throwable) {
                    Log.e("jcy", "onFailed: ${throwable.message}")
                }
            })

        Greeting().greeting()
    }
}