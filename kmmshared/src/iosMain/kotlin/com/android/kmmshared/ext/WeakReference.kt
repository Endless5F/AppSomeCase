package com.android.kmmshared.ext

import kotlin.native.internal.GC
import kotlin.native.ref.WeakReference as KNWeakRef

actual class WeakReference<T: Any> actual constructor(ref: T?) {

    private var _ref: KNWeakRef<T>? = null

    actual fun get(): T? {
        return _ref?.get()
    }

    init {
        if (ref != null) {
            _ref = KNWeakRef(ref)
        }
    }
}

// TODO 使用 kmm的New Memory 后此项调用将失效
actual fun callGCCollect() {
    try {
        GC.collectCyclic()
    } catch (e: Exception) {
    }
}