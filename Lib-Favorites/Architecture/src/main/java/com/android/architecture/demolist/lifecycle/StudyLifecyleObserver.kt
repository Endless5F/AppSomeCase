package com.android.architecture.demolist.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import android.content.Context
import com.android.architecture.loge

class StudyLifecyleObserver(val context : Context) : DefaultLifecycleObserver {
    val Tag = "StudyLifecyleObserver"

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        loge(Tag, "onCreate")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loge(Tag, "onStart")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        loge(Tag, "onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        loge(Tag, "onPause")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        loge(Tag, "onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        loge(Tag, "onDestroy")
    }
}