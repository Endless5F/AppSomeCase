package com.android.kmmshared.ext.datetime

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


/**
 * 获取当前时间戳
 *
 * @return 毫秒
 */
expect fun getCurrentTimeStamp(): Long

/**
 * 毫秒时间戳转换
 *
 * @param formatter 由 createFormat 方法创建
 */
expect fun Long.millisecondToString(formatter: TimeStampFormatter): String

/**
 * 秒时间戳转换
 *
 * @param format 由 createFormat 方法创建
 */
fun Long.secondToString(format: TimeStampFormatter): String = (this * 1000).millisecondToString(format)

/**
 * 创建时间戳格式化实例
 *
 * @param format 时间格式，如：yyyy-MM-dd
 */
expect fun createTimeFormat(format: String): TimeStampFormatter

/**
 * 包装 SimpleDataFormat 或 NSDateFormatter
 */
data class TimeStampFormatter(val format: Any?)

/**
 * 判断是否是在同一天
 *
 * @param instant
 */
fun Instant.isSameDayTo(instant: Instant): Boolean {
    val date1 = this.toLocalDateTime(TimeZone.currentSystemDefault())
    val date2 = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return (date1.date == date2.date)
}

/**
 * 判断是否是在同一年
 *
 * @param instant
 */
fun Instant.isSameYearTo(instant: Instant): Boolean {
    val date1 = this.toLocalDateTime(TimeZone.currentSystemDefault())
    val date2 = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return (date1.year == date2.year)
}

/**
 * 判断是否是在同一月
 *
 * @param instant
 */
fun Instant.isSameMonthTo(instant: Instant): Boolean {
    val date1 = this.toLocalDateTime(TimeZone.currentSystemDefault())
    val date2 = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return (date1.month == date2.month)
}