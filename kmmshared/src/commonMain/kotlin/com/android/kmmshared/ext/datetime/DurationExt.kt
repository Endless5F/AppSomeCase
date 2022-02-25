package com.android.kmmshared.ext.datetime

import kotlin.native.concurrent.SharedImmutable
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@SharedImmutable
private val DURATION_FMT by lazy { createTimeFormat("mm:ss") }

/**
 * 毫秒转为两段式时长字符串
 * 例：24:55（24分55秒），01:00:15（1小时15秒）
 *
 * @return
 */
fun Long.millisecondToDurationString(): String {
    val duration = this.toDuration(DurationUnit.MILLISECONDS)
    return toDurationString(duration)
}

/**
 * 秒转为两段式时长字符串
 *
 * @return
 */
fun Long.secondToDurationString(): String {
    val duration = this.toDuration(DurationUnit.SECONDS)
    return toDurationString(duration)
}

private fun toDurationString(duration: Duration): String {
    val ms = duration.toLong(DurationUnit.MILLISECONDS)
    return getHourString(duration) + ms.millisecondToString(DURATION_FMT)
}

private fun getHourString(duration: Duration, sep: String = ":"): String {
    val hours = duration.toInt(DurationUnit.HOURS)
    if (hours > 0) {
        if (hours < 10) {
            return "0${hours}${sep}"
        }
        return "$hours${sep}"
    }
    return ""
}