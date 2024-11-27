plugins {
    kotlin("multiplatform") version "2.0.21"
    id("io.github.ttypic.swiftklib")
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        tvosX64(),
        tvosArm64(),
        tvosSimulatorArm64(),
        watchosX64(),
        watchosArm64(),
        watchosSimulatorArm64(),
        macosArm64(),
        macosX64(),
    ).forEach {
        it.compilations {
            val main by getting {
                cinterops {
                    create("HelloSwift")
                }
            }
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
    }
}

swiftklib {
    create("HelloSwift") {
        path = file("native/HelloSwift")
        packageName("com.ttypic.objclibs.greeting")

        dependencies {
            remote("KeychainAccess") {
                github("kishikawakatsumi", "KeychainAccess")
                versionRange("4.0.0", "5.0.0")
            }
        }
    }
}
