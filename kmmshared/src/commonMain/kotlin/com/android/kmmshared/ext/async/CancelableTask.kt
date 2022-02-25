package com.android.kmmshared.ext.async

import co.touchlab.stately.concurrency.AtomicBoolean

/**
 * 可取消任务
 *
 * @param task 任务闭包
 * @author yuanguozheng
 * @since 2.11
 */
class CancelableTask(private val task: () -> Unit) {

    private var isCanceled = AtomicBoolean(false)

    /**
     * 取消任务
     */
    fun cancel() {
        isCanceled.value = true
    }

    /**
     * 执行任务
     */
    fun execute() {
        if (!isCanceled.value) {
            task()
        }
    }
}