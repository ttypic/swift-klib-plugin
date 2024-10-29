plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.publish)
    alias(libs.plugins.autonomousapps.testkit)
}

dependencies {
    implementation(gradleApi())
    implementation(libs.plugin.kotlin)

    functionalTestImplementation(libs.test.junit.jupiter)
    functionalTestImplementation(libs.test.kotest.assertions)
    functionalTestImplementation(project(":plugin"))
    functionalTestRuntimeOnly(libs.test.junit.jupiter.launcher)
}

gradleTestKitSupport {
    withSupportLibrary()
    withTruthLibrary()
}

tasks.named<Test>("functionalTest") {
    useJUnitPlatform()
    systemProperty("com.autonomousapps.test.versions.kotlin", libs.versions.kotlin.get())

    beforeTest(closureOf<TestDescriptor> {
        logger.lifecycle("Running test: $this")
    })
}

version = "0.7.0-SNAPSHOT"
group = "io.github.ttypic"

kotlin {
    jvmToolchain(17)
    compilerOptions {
        optIn.addAll(
            "io.github.ttypic.swiftklib.gradle.api.ExperimentalSwiftklibApi"
        )
    }
}

@Suppress("UnstableApiUsage")
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
