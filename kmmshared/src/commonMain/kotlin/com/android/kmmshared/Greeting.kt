package com.android.kmmshared

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}