package io.github.ttypic.swiftklib.gradle.task

import io.github.ttypic.swiftklib.gradle.CompileTarget
import io.github.ttypic.swiftklib.gradle.EXTENSION_NAME
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.konan.target.Platform
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.IllegalStateException
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

    @get:OutputFile
    val defFile
        get() = File(targetDir, "$cinteropName.def")

    @TaskAction
    fun produce() {
        if(!Os.isFamily(Os.FAMILY_MAC))
        {
            project.logger.warn("Not running on MacOS. Skipping Swift Klib generation")
            return
        }
        val packageName: String = packageNameProperty.get()

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

    private val xcodeVersion: Int by lazy {
        readXcodeMajorVersion()
    }

    private fun buildSwift(): SwiftBuildResult {
        val swiftBuildDir = pathProperty.get()

        project.exec {
            it.executable = "xcrun"
            it.workingDir = swiftBuildDir
            val extraArgs = if (xcodeVersion >= 15 && compileTarget in SDKLESS_TARGETS) {
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

    private fun readXcodeMajorVersion(): Int {
        val stdout = ByteArrayOutputStream()

        project.exec {
            it.executable = "xcodebuild"
            it.args = listOf("-version")
            it.standardOutput = stdout
        }

        val output = stdout.toString().trim()
        val (_, majorVersion) = "Xcode (\\d+)\\..*".toRegex().find(output)?.groupValues
            ?: throw IllegalStateException("Can't find Xcode")

        return majorVersion.toInt()
    }

    private fun readXcodePath(): String {
        val stdout = ByteArrayOutputStream()

        project.exec {
            it.executable = "xcode-select"
            it.args = listOf("--print-path")
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
        val xcodePath = readXcodePath()

        val linkerPlatformVersion =
            if (xcodeVersion >= 15) compileTarget.linkerPlatformVersionName()
            else compileTarget.linkerMinOsVersionName()

        val content = """
            package = $packageName
            language = Objective-C
            headers = ${headerPath.absolutePath}

            # md5 ${libPath.md5()}
            staticLibraries = ${libPath.name}
            libraryPaths = ${libPath.parentFile.absolutePath}

            linkerOpts = -L/usr/lib/swift -$linkerPlatformVersion ${minOs(compileTarget)}.0 ${minOs(compileTarget)}.0 -L${xcodePath}/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/${compileTarget.os()}
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

val SDKLESS_TARGETS = listOf(
    CompileTarget.iosArm64,
    CompileTarget.watchosArm64,
    CompileTarget.watchosX64,
    CompileTarget.watchosSimulatorArm64,
    CompileTarget.tvosArm64,
    CompileTarget.tvosX64,
    CompileTarget.tvosSimulatorArm64,
)

private fun File.create(content: String) {
    bufferedWriter().use {
        it.write(content)
    }
}

private fun File.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(readBytes()))
    .toString(16)
    .padStart(32, '0')
