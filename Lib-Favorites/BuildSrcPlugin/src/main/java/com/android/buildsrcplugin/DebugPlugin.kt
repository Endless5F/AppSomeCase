package com.android.buildsrcplugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class DebugPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        println("************start apply DebugPlugin************")
//        project.extensions.create(DEBUG_CONFIG, DebugExtension::class.java)

        val android = project.extensions.getByType(AppExtension::class.java)
        if (android is AppExtension) {
//            android.registerTransform(DebugTransform(project))
            android.registerTransform(ToastTransform(project))
        } else {
            println("************end apply DebugPlugin, error: android !is AppExtension ************")
        }

//        if (!project.plugins.hasPlugin("com.android.application")) {
//            throw ProjectConfigurationException("此插件只能在 com.android.application 模块应用", null)
//        }
    }
}