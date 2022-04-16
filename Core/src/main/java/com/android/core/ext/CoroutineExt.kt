package com.android.core.ext

import android.os.Build
import android.view.View
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// 创建一个Job，并用这个Job来管理CoroutineScope的所有子协程
val job = Job()
val coroutineContext: CoroutineContext = Dispatchers.Main + job
// 方法三，自行通过 CoroutineContext 创建一个 CoroutineScope 对象
//                                    👇 需要一个类型为 CoroutineContext 的参数
val coroutineScope = CoroutineScope(coroutineContext)

// 开始执行协程
fun startCoroutine(block: suspend CoroutineScope.() -> Unit) {
    coroutineScope.launch(Dispatchers.Main) {
        // 切换到 IO 线程，并在执行完成后切回 UI 线程
        withContext(Dispatchers.IO, block)
    }
}

// 模仿 Jetpack Compose 创建协程域
inline fun createCoroutineScope(
    getContext: () -> CoroutineContext = { EmptyCoroutineContext }
): CoroutineScope {
    return CoroutineScope(job + getContext())
}

fun endAllCoroutine() {
    // 结束所有子协程
    job.cancel()
}




// ===========================================================================

/**
 * start counting down from [duration] to 0 in a background thread and invoking the [onCountdown] every [interval] in main thread
 */
fun <T> countdown2(duration: Long, interval: Long, context: CoroutineContext = Dispatchers.Default,onCountdown: suspend (Long) -> T): Flow<T> =
    flow { (duration - interval downTo 0 step interval).forEach { emit(it) } }
        .onEach { delay(interval) }
        .onStart { emit(duration) }
        .flatMapMerge { flow { emit(onCountdown(it)) } }
        .flowOn(context)


/**
 * avoid memory leak for View and activity when activity has finished while coroutine is still running
 */
fun Job.autoDispose(view: View?): Job {
    view ?: return this

    val listener = object : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(v: View?) {
            cancel()
            v?.removeOnAttachStateChangeListener(this)
        }

        override fun onViewAttachedToWindow(v: View?) = Unit
    }

    view.addOnAttachStateChangeListener(listener)
    invokeOnCompletion {
        view.removeOnAttachStateChangeListener(listener)
    }
    return this
}

/**
 * avoid memory leak
 */
fun <T> SendChannel<T>.autoDispose(view: View?): SendChannel<T> {
    view ?: return this

    val isAttached = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && view.isAttachedToWindow || view.windowToken != null
    val listener = object : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(v: View?) {
            close()
            v?.removeOnAttachStateChangeListener(this)
        }

        override fun onViewAttachedToWindow(v: View?) = Unit
    }

    view.addOnAttachStateChangeListener(listener)
    invokeOnClose {
        view.removeOnAttachStateChangeListener(listener)
    }
    return this
}
