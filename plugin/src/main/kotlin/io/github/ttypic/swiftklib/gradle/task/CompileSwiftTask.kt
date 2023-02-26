package io.github.ttypic.swiftklib.gradle.task

import io.github.ttypic.swiftklib.gradle.CompileTarget
import io.github.ttypic.swiftklib.gradle.EXTENSION_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.configurationcache.extensions.capitalized
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import javax.inject.Inject

open class CompileSwiftTask @Inject constructor(
    @Input val cinteropName: String,
    @Input val compileTarget: CompileTarget,
    @InputDirectory val pathProperty: Property<File>,
    @Input val packageNameProperty: Property<String>,
    @Optional @Input val minIosProperty: Property<Int>,
) : DefaultTask() {

    @get:Internal
    internal val targetDir: File
        get() {
            return project.buildDir.resolve("${EXTENSION_NAME}/$cinteropName/$compileTarget")
        }

    @get:OutputDirectory
    val swiftBuildDir
        get() = File(targetDir, "swiftBuild")

    @get:OutputFile
    val defFile
        get() = File(targetDir, "$cinteropName.def")

    @TaskAction
    fun produce() {
        val packageName: String = packageNameProperty.get()
        val minIos: Int = minIosProperty.getOrElse(13)

        prepareBuildDirectory()
        createPackageSwift()
        val (libPath, headerPath) = buildSwift(minIos)

        createDefFile(
            minIos = minIos,
            libPath = libPath,
            headerPath = headerPath,
            packageName = packageName,
        )
    }

    /**
     * Creates build directory or cleans up if it already exists
     * and copies Swift source files to it
     */
    private fun prepareBuildDirectory() {
        val path: File = pathProperty.get()

        if (swiftBuildDir.exists()) swiftBuildDir.deleteRecursively()

        swiftBuildDir.mkdirs()
        path.copyRecursively(File(swiftBuildDir, cinteropName), true)
    }

    /**
     * Creates `Package.Swift` file for the library
     */
    private fun createPackageSwift() {
        val content = """
            // swift-tools-version:5.5
            import PackageDescription

            let package = Package(
            	name: "$cinteropName",
            	products: [
            		.library(
            			name: "$cinteropName",
            			type: .static,
            			targets: ["$cinteropName"])
            	],
            	dependencies: [],
            	targets: [
            		.target(
            			name: "$cinteropName",
            			dependencies: [],
            			path: "$cinteropName")
            	]
            )
        """.trimIndent()

        File(swiftBuildDir, "Package.swift").create(content)
    }

    private fun buildSwift(minIos: Int): SwiftBuildResult {
        project.exec {
            it.executable = "swift"
            it.workingDir = swiftBuildDir
            it.args = generateBuildArgs(minIos)
        }

        return SwiftBuildResult(
            libPath = File(
                swiftBuildDir,
                ".build/${compileTarget.arch()}-apple-macosx/release/lib${cinteropName.capitalized()}.a"
            ),
            headerPath = File(
                swiftBuildDir,
                ".build/${compileTarget.arch()}-apple-macosx/release/$cinteropName.build/$cinteropName-Swift.h"
            )
        )
    }

    private fun generateBuildArgs(minIos: Int): List<String> = listOf(
        "build",
        "--arch",
        compileTarget.arch(),
        "-c",
        "release",
        "-Xswiftc",
        "-sdk",
        "-Xswiftc",
        readSdkPath(),
        "-Xswiftc",
        "-target",
        "-Xswiftc",
        "${compileTarget.arch()}-apple-ios${minIos}.0${compileTarget.simulatorSuffix()}",
    )

    private fun readSdkPath(): String {
        val stdout = ByteArrayOutputStream()

        project.exec {
            it.executable = "xcrun"
            it.args = listOf(
                "--sdk",
                compileTarget.os(),
                "--show-sdk-path",
            )
            it.standardOutput = stdout
        }

        return stdout.toString().trim()
    }

    /**
     * Generates Def-file for Kotlin/Native Cinterop
     *
     * Note: adds lib-file md5 hash to library in order to automatically
     * invalidate connected cinterop task
     */
    private fun createDefFile(libPath: File, headerPath: File, packageName: String, minIos: Int) {
        val content = """
            package = $packageName
            language = Objective-C
            headers = ${headerPath.absolutePath}

            # md5 ${libPath.md5()}
            staticLibraries = ${libPath.name}
            libraryPaths = ${libPath.parentFile.absolutePath}

            linkerOpts = -L/usr/lib/swift -${compileTarget.linkerMinIosVersionName()} $minIos.0 -L/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/${compileTarget.os()}
        """.trimIndent()
        defFile.create(content)
    }
}

private data class SwiftBuildResult(
    val libPath: File,
    val headerPath: File,
)

private fun File.create(content: String) {
    bufferedWriter().use {
        it.write(content)
    }
}

private fun File.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(readBytes()))
    .toString(16)
    .padStart(32, '0')
