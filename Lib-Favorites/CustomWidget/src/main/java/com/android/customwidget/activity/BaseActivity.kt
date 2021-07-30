package com.android.customwidget.activity

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
    }

    open fun isImmersiveStatusBar(): Boolean {
        return true
    }

    open fun isLightStatusBar(): Boolean {
        return true
    }
}