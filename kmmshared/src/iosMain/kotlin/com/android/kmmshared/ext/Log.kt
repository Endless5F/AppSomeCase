package com.android.kmmshared.ext

import kotlin.native.concurrent.freeze

actual fun printLog(tag: String, msg: String) {
    if (!LOG_ENABLE) {
        return
    }
    tag.freeze()
    msg.freeze()
    println("$tag: $msg")
}