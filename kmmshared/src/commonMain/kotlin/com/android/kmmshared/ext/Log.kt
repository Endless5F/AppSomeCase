package com.android.kmmshared.ext

internal const val LOG_ENABLE = true

/**
 * 打印Log
 *
 * @param tag
 * @param msg
 */
expect fun printLog(tag: String, msg: String)