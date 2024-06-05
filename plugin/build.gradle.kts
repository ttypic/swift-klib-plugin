plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.1"
    kotlin("jvm") version "2.0.0"
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
}

version = "0.5.1"
group = "io.github.ttypic"

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    website = "https://github.com/ttypic/swift-klib-plugin"
    vcsUrl = "https://github.com/ttypic/swift-klib-plugin"

    plugins {
        create("swiftklib") {
            id = "io.github.ttypic.swiftklib"
            displayName = "SwiftKlib Gradle Plugin"
            description = "Gradle Plugin to inject Swift-code for Kotlin Multiplatform iOS target"
            implementationClass = "io.github.ttypic.swiftklib.gradle.SwiftKlibPlugin"
            tags = listOf("kotlin-multiplatform", "swift")
        }
    }
}
