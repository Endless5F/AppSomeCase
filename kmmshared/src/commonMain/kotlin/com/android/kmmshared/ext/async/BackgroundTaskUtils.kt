@file:JvmName("BackgroundTaskUtils")

package com.android.kmmshared.ext.async

import com.android.kmmshared.common.CommonCallback
import kotlin.jvm.JvmName

/**
 * 加入到异步线程串行工作队列中，并执行（支持delay）
 *
 * @param task Kotlin闭包
 * @param delayedSec 延迟时间，默认0s
 */
expect fun bgSerialWork(task: (() -> Unit), delayedSec: Float = 0f)

/**
 * 异步线程串行执行（立即执行，不delay）
 *
 * @param task Kotlin闭包
 */
expect fun bgSerialWork(task: (() -> Unit))

/**
 * 加入到异步线程并发工作队列中，并执行（支持delay）
 *
 * @param task Kotlin闭包
 * @param delayedSec 延迟时间，默认0s
 */
expect fun bgConcurrentWork(
    task: (() -> Unit),
    delayedSec: Float = 0f
)

/**
 * 异步线程并发执行（立即执行，不delay）
 *
 * @param task Kotlin闭包
 */
expect fun bgConcurrentWork(task: (() -> Unit))

/**
 * 加入到主线程中，并执行（支持Delay）
 *
 * @param task Kotlin闭包
 * @param delayedSec 延迟时间，默认0s
 */
expect fun mainWork(task: (() -> Unit), delayedSec: Float = 0f)

/**
 * 加入到主线程中，并立即执行（不支持Delay和取消）
 *
 * @param task Kotlin闭包
 */
expect fun mainWork(task: (() -> Unit))

expect fun sync(task: () -> Unit)

expect fun freezeObj(obj: Any?): Any?


/**
 * 加入到异步线程串行工作队列中，并以 try-catch 执行（支持delay）
 *
 * @param task Kotlin闭包
 * @param delayedSec 延迟时间，默认0s
 */
fun bgSerialWorkWithCatching(task: (() -> Unit), delayedSec: Float = 0f, exceptionCallback: CommonCallback? = null): CancelableTask {
    val cancelableTask = CancelableTask(task)
    bgSerialWork({
        runCatching {
            cancelableTask.execute()
        }.exceptionOrNull()?.apply {
            printStackTrace()
            exceptionCallback?.invoke()
        }
    }, delayedSec)
    return cancelableTask
}

/**
 * 异步线程串行以 try-catch 执行（立即执行，不delay）
 *
 * @param task Kotlin闭包
 */
fun bgSerialWorkWithCatching(task: (() -> Unit), exceptionCallback: CommonCallback? = null) = bgSerialWorkWithCatching(task, 0f, exceptionCallback)

fun bgSerialWorkWithCatching(task: (() -> Unit)) = bgSerialWorkWithCatching(task, null)