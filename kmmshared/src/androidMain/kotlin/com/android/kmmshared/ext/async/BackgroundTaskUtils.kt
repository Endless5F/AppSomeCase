package com.android.kmmshared.ext.async

import android.os.Handler
import android.os.Looper

private val lockObj = object {}

/**
 * 异步线程串行执行（立即执行，不delay）
 *
 * @param task Kotlin闭包
 */
actual fun bgSerialWork(task: (() -> Unit)) {
    dispatchSerialWork(task)
}

/**
 * 加入到异步线程串行工作队列中，并执行（支持delay）
 *
 * @param task Kotlin闭包
 * @param delayedSec 延迟时间，默认0s
 */
actual fun bgSerialWork(task: (() -> Unit), delayedSec: Float) {
    dispatchSerialWork(task, (delayedSec * 1000L).toLong())
}

/**
 * 异步线程并发执行（立即执行，不delay）
 *
 * @param task Kotlin闭包
 */
actual fun bgConcurrentWork(task: (() -> Unit)) {
    dispatchConcurrentWork(task)
}

/**
 * 加入到异步线程并发工作队列中，并执行（支持delay）
 *
 * @param task Kotlin闭包
 * @param delayedSec 延迟时间，默认0s
 */
actual fun bgConcurrentWork(task: (() -> Unit), delayedSec: Float) {
    dispatchConcurrentWork(task, (delayedSec * 1000L).toLong())
}


/**
 * 加入到主线程中，并执行（支持Delay）
 *
 * @param task Kotlin闭包
 * @param delayedSec 延迟时间，默认0s
 */
actual fun mainWork(task: (() -> Unit), delayedSec: Float) {
    mainHandler.postDelayed(task, (delayedSec * 1000L).toLong())
}

/**
 * 加入到主线程中，并立即执行（不支持Delay和取消）
 *
 * @param task Kotlin闭包
 */
actual fun mainWork(task: (() -> Unit)) {
    mainHandler.post(task)
}

actual fun sync(task: (() -> Unit)) {
    synchronized(lockObj, task)
}

actual fun freezeObj(obj: Any?): Any? {
    return obj
}