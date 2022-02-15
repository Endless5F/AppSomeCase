package com.android.core.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import java.lang.Exception

/**
 * 状态栏文字和图标颜色帮助类，这里小米，魅族区别对待。
 */
fun setLightStatusBarContentColor(activity: Activity, dark: Boolean) {
    when {
        TextUtils.equals(Build.MANUFACTURER, "Xiaomi") -> {
            setMIUIStatusBarLightMode(activity, dark)
        }
        TextUtils.equals(Build.MANUFACTURER, "Meizu") -> {
            setFlymeLightStatusBar(activity, dark)
        }
        else -> {
            setAndroidNativeLightStatusBar(activity, dark)
        }
    }
}

@SuppressLint("PrivateApi")
fun setMIUIStatusBarLightMode(activity: Activity, dark: Boolean): Boolean {
    var result = false
    val window = activity.window
    if (window != null) {
        val clazz: Class<*> = window.javaClass
        try {
            var darkModeFlag = 0
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod(
                "setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            if (dark) {
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag) //状态栏透明且黑色字体
            } else {
                extraFlagField.invoke(window, 0, darkModeFlag) //清除黑色字体
            }
            result = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                if (dark) {
                    activity.window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    activity.window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return result
}

fun setFlymeLightStatusBar(activity: Activity?, dark: Boolean): Boolean {
    var result = false
    if (activity != null) {
        try {
            val lp = activity.window.attributes
            val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
            val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
            darkFlag.isAccessible = true
            meizuFlags.isAccessible = true
            val bit = darkFlag.getInt(null)
            var value = meizuFlags.getInt(lp)
            value = if (dark) value or bit else value and bit.inv()
            meizuFlags.setInt(lp, value)
            activity.window.attributes = lp
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return result
}

fun setAndroidNativeLightStatusBar(activity: Activity, dark: Boolean) {
    val decor = activity.window.decorView
    if (dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decor.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    } else {
        decor.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}
