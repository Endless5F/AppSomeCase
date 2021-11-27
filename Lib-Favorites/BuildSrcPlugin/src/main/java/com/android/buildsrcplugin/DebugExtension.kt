package com.android.buildsrcplugin

const val DEBUG_CONFIG = "debugConfig"

/**
 * 必须是open
 */
open class DebugExtension {
    // 是否使用此功能
    var enable: Boolean = true

    // 是否开启transform的增量编译
    var enableIncremental: Boolean = false
}