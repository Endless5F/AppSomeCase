package com.android.kmmshared.ext.async

import platform.Foundation.NSThread
import platform.darwin.*
import kotlin.native.concurrent.freeze

@SharedImmutable
private val serialQueue by lazy {
    dispatch_queue_create(
        "com.kmm.SerialWorker",
        DISPATCH_QUEUE_SERIAL as dispatch_queue_attr_t
    )
}

/**
 * 加入到异步线程串行工作队列中，并执行（支持delay）
 *
 * @param task Kotlin闭包
 * @param delayedSec 延迟时间，默认0s
 */
actual fun bgSerialWork(task: (() -> Unit), delayedSec: Float) {
    task.freeze()
    val delay = delayedSec * NSEC_PER_SEC.toFloat()
    val delayTime = dispatch_time(DISPATCH_TIME_NOW, delay.toLong())
    dispatch_after(delayTime, serialQueue, task)
}

/**
 * 异步线程串行执行（立即执行，不delay）
 *
 * @param task Kotlin闭包
 */
actual fun bgSerialWork(task: (() -> Unit)) {
    task.freeze()
    dispatch_async(serialQueue, task)
}

/**
 * 加入到异步线程并发工作队列中，并执行（支持delay）
 *
 * @param task Kotlin闭包
 * @param delayedSec 延迟时间，默认0s
 */
actual fun bgConcurrentWork(
    task: (() -> Unit),
    delayedSec: Float
) {
    task.freeze()
    val delay = delayedSec * NSEC_PER_SEC.toFloat()
    val delayTime = dispatch_time(DISPATCH_TIME_NOW, delay.toLong())
    dispatch_after(delayTime, dispatch_get_global_queue(0, 0), task)
}

/**
 * 异步线程并发执行（立即执行，不delay）
 *
 * @param task Kotlin闭包
 */
actual fun bgConcurrentWork(task: (() -> Unit)) {
    task.freeze()
    dispatch_async(dispatch_get_global_queue(0, 0), task)
}

/**
 * 加入到主线程中，并执行（支持Delay）
 *
 * @param task Kotlin闭包
 * @param delayedSec 延迟时间，默认0s
 */
actual fun mainWork(task: (() -> Unit), delayedSec: Float) {
    if (!NSThread.isMainThread) {
        task.freeze()
    }
    val delay = delayedSec * NSEC_PER_SEC.toFloat()
    val delayTime = dispatch_time(DISPATCH_TIME_NOW, delay.toLong())
    dispatch_after(delayTime, dispatch_get_main_queue(), task)
}

/**
 * 加入到主线程中，并立即执行（不支持Delay和取消）
 *
 * @param task Kotlin闭包
 */
actual fun mainWork(task: (() -> Unit)) {
    if (!NSThread.isMainThread) {
        task.freeze()
    }
    dispatch_async(dispatch_get_main_queue(), task)
}

actual fun sync(task: () -> Unit) {
    task.freeze()
    dispatch_sync(serialQueue, task)
}

actual fun freezeObj(obj: Any?): Any? {
    return obj?.freeze()
}