@file:JvmName("TimeUtils")
package com.android.kmmshared.ext.datetime

import java.text.SimpleDateFormat
import java.util.*

actual fun getCurrentTimeStamp(): Long = System.currentTimeMillis()

actual fun Long.millisecondToString(formatter: TimeStampFormatter): String {
    val sdf = formatter.format
    if (sdf is SimpleDateFormat) {
        return sdf.format(Date(this))
    }
    return ""
}

actual fun createTimeFormat(format: String): TimeStampFormatter {
    var sdf: SimpleDateFormat? = null
    try {
        sdf = SimpleDateFormat(format, Locale.CHINA)
    } catch (e: Exception) {
    }
    return TimeStampFormatter(sdf)
}