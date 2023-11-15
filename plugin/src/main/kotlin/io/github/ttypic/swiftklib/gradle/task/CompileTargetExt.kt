package io.github.ttypic.swiftklib.gradle.task

import io.github.ttypic.swiftklib.gradle.CompileTarget

internal fun CompileTarget.os() = when(this) {
    CompileTarget.iosX64 -> "iphonesimulator"
    CompileTarget.iosArm64 -> "iphoneos"
    CompileTarget.iosSimulatorArm64 -> "iphonesimulator"
    CompileTarget.watchosX64 -> "watchsimulator"
    CompileTarget.watchosArm64 -> "watchos"
    CompileTarget.watchosSimulatorArm64 -> "watchsimulator"
    CompileTarget.tvosX64 -> "appletvsimulator"
    CompileTarget.tvosArm64 -> "appletvos"
    CompileTarget.tvosSimulatorArm64 -> "appletvsimulator"
    CompileTarget.macosX64 -> "macosx"
    CompileTarget.macosArm64 -> "macosx"
}

internal fun CompileTarget.arch() = when(this) {
    CompileTarget.iosX64 -> "x86_64"
    CompileTarget.iosArm64 -> "arm64"
    CompileTarget.iosSimulatorArm64 -> "arm64"
    CompileTarget.watchosX64 -> "x86_64"
    CompileTarget.watchosArm64 -> "arm64"
    CompileTarget.watchosSimulatorArm64 -> "arm64"
    CompileTarget.tvosX64 -> "x86_64"
    CompileTarget.tvosArm64 -> "arm64"
    CompileTarget.tvosSimulatorArm64 -> "arm64"
    CompileTarget.macosX64 -> "x86_64"
    CompileTarget.macosArm64 -> "arm64"
}

internal fun CompileTarget.archPrefix() = when(this) {
    CompileTarget.watchosArm64 -> "arm64_32"
    else -> arch()
}

internal fun CompileTarget.simulatorSuffix() = when(this) {
    CompileTarget.iosX64 -> "-simulator"
    CompileTarget.iosArm64 -> ""
    CompileTarget.iosSimulatorArm64 -> "-simulator"
    CompileTarget.watchosX64 -> "-simulator"
    CompileTarget.watchosArm64 -> ""
    CompileTarget.watchosSimulatorArm64 -> "-simulator"
    CompileTarget.tvosX64 -> "-simulator"
    CompileTarget.tvosArm64 -> ""
    CompileTarget.tvosSimulatorArm64 -> "-simulator"
    CompileTarget.macosX64 -> ""
    CompileTarget.macosArm64 -> ""
}

internal fun CompileTarget.linkerPlatformVersionName() = when(this) {
    CompileTarget.iosArm64 -> "platform_version ios"
    CompileTarget.iosX64, CompileTarget.iosSimulatorArm64 -> "platform_version ios-simulator"
    CompileTarget.watchosArm64 -> "platform_version watchos"
    CompileTarget.watchosX64, CompileTarget.watchosSimulatorArm64 -> "platform_version watchos-simulator"
    CompileTarget.tvosArm64 -> "platform_version tvos"
    CompileTarget.tvosX64, CompileTarget.tvosSimulatorArm64 -> "platform_version tvos-simulator"
    CompileTarget.macosX64, CompileTarget.macosArm64 -> "platform_version macosx"
}
