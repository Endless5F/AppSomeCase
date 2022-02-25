package com.android.kmmshared.ext

import java.lang.ref.WeakReference

actual class WeakReference<T: Any> actual constructor(ref: T?) {

    private var _ref: WeakReference<T>? = null

    actual fun get(): T? {
        return _ref?.get()
    }

    init {
        _ref = WeakReference(ref)
    }
}

actual fun callGCCollect() {
    Runtime.getRuntime().gc()
}