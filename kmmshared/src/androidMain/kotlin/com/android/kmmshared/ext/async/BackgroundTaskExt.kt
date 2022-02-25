package com.android.kmmshared.ext.async

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 主线程Handler
 */
val mainHandler by lazy { Handler(Looper.getMainLooper()) }

/**
 * 默认串行执行队列
 */
private val backgroundSerialExecutor by lazy {
    Executors.newSingleThreadExecutor(TaskThreadFactory())
}

private val backgroundElasticExecutor by lazy {
    /** 最小池大小 */
    val minPoolSize = 2
    /** 可用核心 */
    val availableCore = Runtime.getRuntime().availableProcessors()
    /** IO 核心池大小 */
    val ioCorePoolSize = 2 * (if (availableCore < minPoolSize) minPoolSize else availableCore) + 1

    ThreadPoolExecutor(
        minPoolSize, ioCorePoolSize, 1, TimeUnit.SECONDS,
        SynchronousQueue(), TaskThreadFactory(),
        ThreadPoolExecutor.DiscardPolicy()
    )
}

private const val DEFAULT_TASK_NAME = "KMM Async Task"

/**
 * 加入到异步线程串行工作队列中，并执行（支持delay）
 *
 * @param task Kotlin闭包
 * @param delayed 延迟时间，默认0ms
 */
fun dispatchSerialWork(task: (() -> Unit), delayed: Long = 0): CommonTask {
    val commonTask = CommonTask()
    val runnable = dispatchMainLoopWork({
        val future = backgroundSerialExecutor.submit {
            task()
        }
        commonTask.future = future
    }, delayed).runnable
    commonTask.runnable = runnable
    return commonTask
}

/**
 * 异步线程串行执行（立即执行，不delay）
 *
 * @param task Kotlin闭包
 */
fun dispatchSerialWork(task: (() -> Unit)): CommonTask {
    val future = backgroundSerialExecutor.submit {
        task()
    }
    return CommonTask(future = future)
}

/**
 * 并行执行任务（使用Kotlin协程）
 *
 * @param task Kotlin闭包
 */
fun dispatchConcurrentWork(task: (() -> Unit)): CommonTask {
    val future = backgroundElasticExecutor.submit {
        task()
    }
    return CommonTask(future = future)
}

/**
 * 并行执行任务（使用Kotlin协程）
 *
 * @param task Kotlin闭包
 */
fun dispatchConcurrentWork(task: (() -> Unit), delayed: Long = 0): CommonTask {
    val commonTask = CommonTask()
    val runnable = dispatchMainLoopWork({
        val future = backgroundElasticExecutor.submit {
            task()
        }
        commonTask.future = future
    }, delayed).runnable
    commonTask.runnable = runnable
    return commonTask
}

/**
 * 加入到主线程中，并执行（支持Delay）
 *
 * @param task Kotlin闭包
 * @param delayed 延迟时间，默认0ms
 */
fun dispatchMainLoopWork(task: (() -> Unit), delayed: Long = 0): CommonTask {
    val runnable = Runnable {
        task()
    }
    mainHandler.postDelayed(runnable, delayed)
    return CommonTask(runnable = runnable)
}

/**
 * 加入到主线程中，并立即执行（不支持Delay和取消）
 *
 * @param task Kotlin闭包
 */
fun dispatchMainLoopWork(task: (() -> Unit)) {
    mainHandler.postDelayed(task, 0L)
}

/**
 * 取消任务
 *
 * @param task 由以上几个方法返回的CommonTask对象
 * @param force 是否强行取消，默认false
 */
fun cancelTask(task: CommonTask?, force: Boolean = false) {
    task ?: return
    task.future?.cancel(force)
    task.runnable?.let {
        mainHandler.removeCallbacks(it)
    }
}

private class TaskThreadFactory : ThreadFactory {

    override fun newThread(r: Runnable): Thread {
        return Thread(r, DEFAULT_TASK_NAME)
    }
}

data class CommonTask(
    var runnable: Runnable? = null,
    var future: Future<*>? = null
)