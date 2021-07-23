package com.debug.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class DebugPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        println("************start apply DebugPlugin************")
        project.extensions.create(DEBUG_CONFIG, DebugExtension::class.java)

        val android = project.extensions.getByType(AppExtension::class.java)
        if (android is AppExtension) {
            print("registerTransform")
            android.registerTransform(DebugTransform(project))
        }
    }
}