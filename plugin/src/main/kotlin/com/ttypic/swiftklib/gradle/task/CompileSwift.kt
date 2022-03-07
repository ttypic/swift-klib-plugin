package com.ttypic.swiftklib.gradle.task

import org.gradle.api.DefaultTask
import com.ttypic.swiftklib.gradle.CompileTarget
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class CompileSwift @Inject constructor(
    @Input val cinteropName: String,
    @Input val compileTarget: CompileTarget,
    @Input val path: File,
    @Input val packageName: String,
) : DefaultTask() {

    @get:Internal
    internal val targetDir: File
        get() {
            return project.buildDir.resolve("swiftklib/$cinteropName/$compileTarget")
        }

    @get:OutputDirectory
    val swiftBuildDir
        get() = File(targetDir, "swiftBuild")

    @get:OutputFile
    val defFile
        get() = File(targetDir, "$cinteropName.def")

    @TaskAction
    fun produce() {
        swiftBuildDir.mkdirs()
        createPackageSwift()
        val (libPath, headerPath) = buildSwift()
        createDefFile(libPath, headerPath)
    }

    private fun createPackageSwift() {
        TODO("Not yet implemented")
    }

    private fun buildSwift(): SwiftBuildResult {
        TODO("Not yet implemented")
    }

    private fun createDefFile(libPath: File, headerPath: File) {
        TODO("Not yet implemented")
    }
}

private data class SwiftBuildResult(
    val libPath: File,
    val headerPath: File,
)
