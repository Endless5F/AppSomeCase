package com.android.core

import android.app.Application
import android.content.Context
import com.android.core.log.HiConsolePrinter
import com.android.core.log.HiFilePrinter
import com.android.core.log.HiLogConfig
import com.android.core.log.HiLogManager
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig


class App: Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        AppRuntime.init(this)
    }

    override fun onCreate() {
        super.onCreate()
        initLog()
        initFresco()
    }

    private fun initLog() {
        HiLogManager.init(object : HiLogConfig() {
            override fun injectJsonParser(): JsonParser? {
                return null
//                return (src) -> new Gson().toJson(src);
            }

            override fun includeThread(): Boolean {
                return true
            }
        }, HiConsolePrinter(), HiFilePrinter.getInstance(cacheDir.absolutePath, 0))
    }

    private fun initFresco() {
        val cacheConfig = DiskCacheConfig
            .newBuilder(this)
            .setBaseDirectoryName("cache_images")
            .setBaseDirectoryPath(cacheDir)
            .build()
        val imagePipelineConfig = ImagePipelineConfig
            .newBuilder(this)
            .setMainDiskCacheConfig(cacheConfig)
            .build()
        Fresco.initialize(this, imagePipelineConfig)
    }
}