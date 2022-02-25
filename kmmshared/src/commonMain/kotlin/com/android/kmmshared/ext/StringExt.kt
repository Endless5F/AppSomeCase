package com.android.kmmshared.ext


/**
 * 使用正则表达式去掉多余的.与0，需先将float转成string
 * 1 -> 1
 * 10 -> 10
 * 1.0 -> 1
 * 1.010 -> 1.01
 * 1.01 -> 1.01
 */
fun String?.subZeroAndDot(): String? {
    var s = this ?: return null
    if (s.indexOf(".") > 0) {
        s = s.replace(Regex("0+?$"), "")
        s = s.replace(Regex("[.]$"), "")
    }
    return s
}