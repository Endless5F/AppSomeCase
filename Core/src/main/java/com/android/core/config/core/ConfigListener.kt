package com.android.core.config.core

interface ConfigListener {
    fun onConfigUpdate(configMap: Map<String, Any>)
}