package io.github.ttypic.swiftklib.gradle.task

import io.github.ttypic.swiftklib.gradle.CompileTarget
import io.github.ttypic.swiftklib.gradle.EXTENSION_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
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
    @Optional @Input val minMacosProperty: Property<Int>,
    @Optional @Input val minTvosProperty: Property<Int>,
    @Optional @Input val minWatchosProperty: Property<Int>,
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

        prepareBuildDirectory()
        createPackageSwift()
        val (libPath, headerPath) = buildSwift()

        createDefFile(
            libPath = libPath,
            headerPath = headerPath,
            packageName = packageName,
        )
    }

    private val minIos get() = minIosProperty.getOrElse(13)
    private val minMacos get() = minMacosProperty.getOrElse(11)
    private val minTvos get() = minTvosProperty.getOrElse(13)
    private val minWatchos get() = minWatchosProperty.getOrElse(8)

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

    private fun buildSwift(): SwiftBuildResult {
        project.exec {
            it.executable = "xcrun"
            it.workingDir = swiftBuildDir
            val extraArgs = if (compileTarget == CompileTarget.iosArm64) {
                additionalSysrootArgs()
            } else {
                emptyList()
            }
            it.args = generateBuildArgs() + extraArgs
        }

        return SwiftBuildResult(
            libPath = File(
                swiftBuildDir,
                ".build/${compileTarget.arch()}-apple-macosx/release/lib${cinteropName}.a"
            ),
            headerPath = File(
                swiftBuildDir,
                ".build/${compileTarget.arch()}-apple-macosx/release/$cinteropName.build/$cinteropName-Swift.h"
            )
        )
    }

    private fun generateBuildArgs(): List<String> = listOf(
        "swift",
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
        "${compileTarget.archPrefix()}-apple-${operatingSystem(compileTarget)}.0${compileTarget.simulatorSuffix()}",
    )

    /** Workaround for bug in toolchain where the sdk path (via `swiftc -sdk` flag) is not propagated to clang. */
    private fun additionalSysrootArgs(): List<String> = listOf(
        "-Xcc",
        "-isysroot",
        "-Xcc",
        readSdkPath(),
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
    private fun createDefFile(libPath: File, headerPath: File, packageName: String) {
        val content = """
            package = $packageName
            language = Objective-C
            headers = ${headerPath.absolutePath}

            # md5 ${libPath.md5()}
            staticLibraries = ${libPath.name}
            libraryPaths = ${libPath.parentFile.absolutePath}

            linkerOpts = -L/usr/lib/swift -${compileTarget.linkerMinOsVersionName()} ${minOs(compileTarget)}.0 -L/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/${compileTarget.os()}
        """.trimIndent()
        defFile.create(content)
    }

    private fun operatingSystem(compileTarget: CompileTarget): String =
        when (compileTarget) {
            CompileTarget.iosX64, CompileTarget.iosArm64, CompileTarget.iosSimulatorArm64 -> "ios$minIos"
            CompileTarget.watchosX64, CompileTarget.watchosArm64, CompileTarget.watchosSimulatorArm64 -> "watchos$minWatchos"
            CompileTarget.tvosX64, CompileTarget.tvosArm64, CompileTarget.tvosSimulatorArm64 -> "tvos$minTvos"
            CompileTarget.macosX64, CompileTarget.macosArm64 -> "macosx$minMacos"
        }

    private fun minOs(compileTarget: CompileTarget): Int =
        when (compileTarget) {
            CompileTarget.iosX64, CompileTarget.iosArm64, CompileTarget.iosSimulatorArm64 -> minIos
            CompileTarget.watchosX64, CompileTarget.watchosArm64, CompileTarget.watchosSimulatorArm64 -> minWatchos
            CompileTarget.tvosX64, CompileTarget.tvosArm64, CompileTarget.tvosSimulatorArm64 -> minTvos
            CompileTarget.macosX64, CompileTarget.macosArm64 -> minMacos
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
