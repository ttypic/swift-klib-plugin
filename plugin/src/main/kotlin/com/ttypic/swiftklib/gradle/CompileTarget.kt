package com.ttypic.swiftklib.gradle

@Suppress("EnumEntryName")
enum class CompileTarget {
    iosX64,
    iosArm64,
    iosSimulatorArm64,
    ;

    companion object  {
        fun byKonanName(konanName: String): CompileTarget? {
            return when(konanName) {
                "ios_x64" -> iosX64
                "ios_arm64" -> iosArm64
                "ios_simulator_arm64" -> iosSimulatorArm64
                else -> null
            }
        }
    }
}
