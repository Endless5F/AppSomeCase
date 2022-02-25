package com.android.kmmshared.ext

import co.touchlab.stately.concurrency.AtomicReference

expect class WeakReference<T: Any>(ref: T?) {
    fun get(): T?
}

class AtomicWeakReference<T: Any>() {

    constructor(ref: T?) : this() {
        weakRef = AtomicReference(WeakReference(ref))
    }

    private var weakRef: AtomicReference<WeakReference<T>?>? = null

    fun clear() {
        weakRef?.set(null)
    }

    fun get(): T? = weakRef?.get()?.get()

    fun set(v: T?) = weakRef?.set(WeakReference(v))

}

expect fun callGCCollect()