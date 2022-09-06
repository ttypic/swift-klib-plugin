package io.github.ttypic.swiftklib.gradle.task

import io.github.ttypic.swiftklib.gradle.CompileTarget
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.configurationcache.extensions.capitalized
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject


open class CompileSwift @Inject constructor(
    @Input val cinteropName: String,
    @Input val compileTarget: CompileTarget,
    @InputDirectory val pathProperty: Property<File>,
    @Input val packageNameProperty: Property<String>,
    @Optional @Input val minIosProperty: Property<Int>,
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
        val path: File = pathProperty.get()
        val packageName: String = packageNameProperty.get()
        val minIos: Int = minIosProperty.getOrElse(13)

        swiftBuildDir.mkdirs()
        createPackageSwift()
        val (libPath, headerPath) = buildSwift(path, minIos)
        createDefFile(libPath, headerPath, packageName, minIos)
    }

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

    private fun buildSwift(path: File, minIos: Int): SwiftBuildResult {
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

        val sdkPath = stdout.toString()

        path.copyRecursively(File(swiftBuildDir, cinteropName), true)

        project.exec {
            it.executable = "swift"
            it.workingDir = swiftBuildDir
            it.args = listOf(
                "build",
                "--arch",
                compileTarget.arch(),
                "-c",
                "release",
                "-Xswiftc",
                "-sdk",
                "-Xswiftc",
                sdkPath.trim(),
                "-Xswiftc",
                "-target",
                "-Xswiftc",
                "${compileTarget.arch()}-apple-ios${minIos}.0${compileTarget.simulatorSuffix()}",
            )
        }

        return SwiftBuildResult(
            libPath = File(swiftBuildDir, ".build/${compileTarget.arch()}-apple-macosx/release/lib${cinteropName.capitalized()}.a"),
            headerPath = File(swiftBuildDir, ".build/${compileTarget.arch()}-apple-macosx/release/$cinteropName.build/$cinteropName-Swift.h")
        )
    }

    private fun createDefFile(libPath: File, headerPath: File, packageName: String, minIos: Int) {
        val content = """
            package = $packageName
            language = Objective-C
            headers = ${headerPath.absolutePath}

            staticLibraries = ${libPath.name}
            libraryPaths = ${libPath.parentFile.absolutePath}

            linkerOpts = -L/usr/lib/swift -${compileTarget.linkerMinIosVersionName()} $minIos.0 -L/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/${compileTarget.os()}
        """.trimIndent()
        defFile.create(content)
    }
}

private fun CompileTarget.os() = when(this) {
    CompileTarget.iosX64 -> "iphonesimulator"
    CompileTarget.iosArm64 -> "iphoneos"
    CompileTarget.iosSimulatorArm64 -> "iphonesimulator"
}

private fun CompileTarget.arch() = when(this) {
    CompileTarget.iosX64 -> "x86_64"
    CompileTarget.iosArm64 -> "arm64"
    CompileTarget.iosSimulatorArm64 -> "arm64"
}
private fun CompileTarget.simulatorSuffix() = when(this) {
    CompileTarget.iosX64 -> "-simulator"
    CompileTarget.iosArm64 -> ""
    CompileTarget.iosSimulatorArm64 -> "-simulator"
}

private fun CompileTarget.linkerMinIosVersionName() = when(this) {
    CompileTarget.iosX64 -> "ios_simulator_version_min"
    CompileTarget.iosArm64 -> "ios_version_min"
    CompileTarget.iosSimulatorArm64 -> "ios_simulator_version_min"
}

private fun File.create(content: String) {
    bufferedWriter().use {
        it.write(content)
    }
}

private data class SwiftBuildResult(
    val libPath: File,
    val headerPath: File,
)
