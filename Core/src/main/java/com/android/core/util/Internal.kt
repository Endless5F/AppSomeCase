package com.android.core.util

const val NO_GETTER: String = "Property does not have a getter"

fun noGetter(): Nothing = throw AnkoException("Property does not have a getter")