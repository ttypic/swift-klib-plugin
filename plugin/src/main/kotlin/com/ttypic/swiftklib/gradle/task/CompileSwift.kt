package com.ttypic.swiftklib.gradle.task

import org.gradle.api.DefaultTask
import com.ttypic.swiftklib.gradle.CompileTarget
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class  CompileSwift @Inject constructor(
    @Input val cinteropName: String,
    @Input val compileTarget: CompileTarget
) : DefaultTask() {

    @TaskAction
    fun produce() {
        println("\n\n\n\n\n\n\n$compileTarget\n\n$cinteropName\n\n\n\n")
    }
}