package com.android.app.planettab

import android.app.Activity
import android.app.Service
import android.os.Vibrator

object VibrateUtil {
    /**
     * 振动器，manifest中需要振动权限
     *
     *
     * <uses-permission android:name="android.permission.VIBRATE"/>
     */
    private var mVibrator: Vibrator? = null

    /**
     * 振动50ms
     *
     * @param activity
     */
    fun startVibrate(activity: Activity) {
        if (mVibrator == null) {
            mVibrator = activity.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        }
        mVibrator?.vibrate(10)
    }

    /**
     * 释放引用
     */
    fun release() {
        mVibrator = null
    }
}