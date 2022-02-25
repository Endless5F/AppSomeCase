package com.android.kmmshared.ext.number

import kotlinx.cinterop.convert
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter

actual class DecimalFormat {
    actual fun format(num: Number, maximumFractionDigits: Int): String {
        val formatter = NSNumberFormatter()
        formatter.minimumFractionDigits = 0u
        formatter.maximumFractionDigits = maximumFractionDigits.convert()
        formatter.numberStyle = 1u
        return formatter.stringFromNumber(NSNumber(num.toDouble()))!!
    }
}