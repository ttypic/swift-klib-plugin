plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.1.0"
    kotlin("jvm") version "1.9.20"
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
}

version = "0.4.0"
group = "io.github.ttypic"

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    website.set("https://github.com/ttypic/swift-klib-plugin")
    vcsUrl.set("https://github.com/ttypic/swift-klib-plugin")

    plugins {
        create("swiftklib") {
            id = "io.github.ttypic.swiftklib"
            displayName = "SwiftKlib Gradle Plugin"
            description = "Gradle Plugin to inject Swift-code for Kotlin Multiplatform iOS target"
            implementationClass = "io.github.ttypic.swiftklib.gradle.SwiftKlibPlugin"
            tags.set(listOf("kotlin-multiplatform", "swift"))
        }
    }
}

repositories {
    mavenCentral()
}
