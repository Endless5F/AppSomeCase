package com.android.app

import android.content.Context
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

class Anim {
    fun anim(context: Context) {
        val  btree = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f)

    }
}