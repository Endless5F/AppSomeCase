package com.example.kotlinlib

import kotlin.math.roundToInt

fun main() {
    println(System.currentTimeMillis())
    for (i in 0..10) {
        first()
    }
    println("")
    println(System.currentTimeMillis())
}

fun first() {
    try {
        val index:Float = -0/0f
        if (index != (Float.NaN as Number)) {
            print("1111")
            index.roundToInt()
        }
    } catch (e: Exception) {
//        e.printStackTrace()
    }

    print("aaaa")
}