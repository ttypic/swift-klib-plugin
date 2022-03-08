plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.7.22"
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0")
    runtimeOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0")
}

gradlePlugin {
    plugins {
        create("swiftklib") {
            id = "com.ttypic.swiftklib"
            displayName = "SwiftKlib Gradle Plugin"
            description = "SwiftKlib Gradle Plugin"
            implementationClass = "com.ttypic.swiftklib.gradle.SwiftKlibPlugin"
        }
    }
}

repositories {
    mavenCentral()
}
