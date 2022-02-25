@file:JvmName("KmmLog")
package com.android.kmmshared.ext

import android.util.Log

actual fun printLog(tag: String, msg: String) {
    if (!LOG_ENABLE) {
        return
    }
    Log.i(tag, msg)
}