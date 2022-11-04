plugins {
    kotlin("multiplatform") version "1.6.0"
    id("io.github.ttypic.swiftklib")
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.compilations {
            val main by getting {
                cinterops {
                    create("HelloSwift")
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

swiftklib {
    create("HelloSwift") {
        path = file("native/HelloSwift")
        packageName("com.ttypic.objclibs.greeting")
    }
}
