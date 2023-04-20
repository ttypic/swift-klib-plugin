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
internal fun CompileTarget.simulatorSuffix() = when(this) {
    CompileTarget.iosX64 -> "-simulator"
    CompileTarget.iosArm64 -> ""
    CompileTarget.iosSimulatorArm64 -> "-simulator"
}

internal fun CompileTarget.linkerMinIosVersionName() = when(this) {
    CompileTarget.iosX64 -> "ios_simulator_version_min"
    CompileTarget.iosArm64 -> "ios_version_min"
    CompileTarget.iosSimulatorArm64 -> "ios_simulator_version_min"
}
