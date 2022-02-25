package com.android.kmmshared.ext.number

actual class DecimalFormat {

    actual fun format(num: Number, maximumFractionDigits: Int): String {
        val df = java.text.DecimalFormat()
        df.isGroupingUsed = false
        df.maximumFractionDigits = maximumFractionDigits
        df.isDecimalSeparatorAlwaysShown = false
        return df.format(num)
    }
}