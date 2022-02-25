package com.android.kmmshared.ext.number

import com.android.kmmshared.ext.subZeroAndDot

/**
 * 只保留一位小数
 */
fun Number?.saveOneDecimal(): String {
    this ?: return "0"
    val df = DecimalFormat()
    return df.format(this, 1)
}

/**
 * 只保留一位小数，若小数位是0，则去掉小数点和0
 */
fun Number?.saveOneDecimalAndSubZero(): String {
    return this.saveOneDecimal().subZeroAndDot() ?: ""
}

fun Number?.saveTwoDecimal(): String {
    this ?: return "0"
    val df = DecimalFormat()
    return df.format(this, 2)
}

/**
 * 保留两位小数，若小数位是0，则去掉小数点和0
 */
fun Number?.saveTwoDecimalAndSubZero(): String {
    return this.saveTwoDecimal().subZeroAndDot() ?: ""
}

expect class DecimalFormat() {
    fun format(num: Number, maximumFractionDigits: Int = 2): String
}