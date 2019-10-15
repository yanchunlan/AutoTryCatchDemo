package com.example.autotrycatchlibrary

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class PathPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.logger.debug("================自定义插件成功！==========")

        // 注册自定义 transform
        def android = project.extensions.findByType(AppExtension)
        android.registerTransform(new PathTransform(project))
    }
}