package com.android.kmmshared.ext.datetime

import platform.Foundation.*

actual fun getCurrentTimeStamp(): Long = (NSDate.date().timeIntervalSince1970 * 1000).toLong()

actual fun createTimeFormat(format: String): TimeStampFormatter {
    return TimeStampFormatter(NSDateFormatter.new()?.apply { dateFormat = format })
}

actual fun Long.millisecondToString(formatter: TimeStampFormatter): String {
    val date = NSDate.dateWithTimeIntervalSince1970((this / 1000.0))
    return (formatter.format as? NSDateFormatter)?.stringFromDate(date) ?: ""
}