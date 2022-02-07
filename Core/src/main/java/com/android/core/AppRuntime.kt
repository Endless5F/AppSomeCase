package com.android.core

import android.app.Application
import android.content.Context

object AppRuntime {
    private lateinit var app: Application

    fun init(app: Application) {
        this.app = app
    }

    fun getAppContext(): Context {
        return app
    }

    fun getApplication(): Application {
        return app
    }
}