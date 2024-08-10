package io.github.ttypic.swiftklib.gradle

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CinteropModulesTest {
    @TempDir
    lateinit var testProjectDir: File
    private lateinit var settingsFile: File
    private lateinit var buildFile: File
    private lateinit var swiftLocation: File
    private lateinit var swiftCodeFile: File
    private lateinit var kotlinLocation: File
    private lateinit var kotlinCodeFile: File
    private lateinit var gradlePropertiesFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        buildFile = File(testProjectDir, "build.gradle.kts")
        swiftLocation = File(testProjectDir, "swift")
        swiftCodeFile = File(swiftLocation, "test.swift")
        kotlinLocation = File(testProjectDir, "src/commonMain/kotlin/test")
        kotlinCodeFile = File(kotlinLocation, "Test.kt")
        gradlePropertiesFile = File(testProjectDir, "gradle.properties")
    }

    @Test
    fun `build with imported UIKit framework is successful`() {
        assumeMacos()

        testBuild(
            swiftCode = """
                import UIKit

                @objc public class TestView: UIView {}
            """.trimIndent(),
            kotlinCode = """
                import test.TestView

                val view = TestView()
            """.trimIndent(),
        ) {
            task(":build")
                .shouldNotBeNull()
                .outcome.shouldBe(TaskOutcome.SUCCESS)
        }
    }


    @Test
    fun `build on linux results in warning about unsupported OS`() {
        assumeLinux()
        testBuild {
            output.shouldContain("Current host OS is not macOS. Disabling SwiftKlib plugin")
        }
    }

    private fun assumeMacos() {
        assumeTrue(OS.MAC.isCurrentOs)
    }

    private fun assumeLinux() {
        assumeTrue(OS.LINUX.isCurrentOs)
    }

    private fun testBuild(
        @Language("swift")
        swiftCode: String? = null,
        @Language("kotlin")
        kotlinCode: String? = null,
        swiftklibName: String = "test",
        swiftklibPackage: String = "test",
        asserter: BuildResult.() -> Unit,
    ) {
        gradlePropertiesFile.writeText(
            """
                kotlin.mpp.enableCInteropCommonization=true
            """.trimIndent()
        )
        @Language("kotlin")
        val settingsKts = """
            pluginManagement {
              includeBuild("..")
            }

            dependencyResolutionManagement {
              repositories {
                mavenCentral()
              }
            }
        """.trimIndent()
        settingsFile.writeText(settingsKts)

        @Language("kotlin")
        val buildKts = """
            plugins {
              embeddedKotlin("multiplatform")
              id("io.github.ttypic.swiftklib")
            }

            kotlin {
              compilerOptions {
                optIn.addAll(
                  "kotlinx.cinterop.ExperimentalForeignApi",
                )
              }

              listOf(
                iosX64(),
                iosArm64(),
                iosSimulatorArm64(),
              ).forEach {
                it.compilations {
                  val main by getting {
                    cinterops.create("$swiftklibName")
                  }
                }
              }
            }

            swiftklib {
              create("$swiftklibName") {
                path = file("${swiftLocation.absolutePath}")
                packageName("$swiftklibPackage")
              }
            }
        """.trimIndent()
        buildFile.writeText(buildKts)

        swiftLocation.mkdirs()
        kotlinLocation.mkdirs()

        if (swiftCode != null) {
            swiftCodeFile.writeText(swiftCode)
        }
        if (kotlinCode != null) {
            kotlinCodeFile.writeText(kotlinCode)
        }

        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("build")
            .withPluginClasspath()
            .build()
            .asserter()
    }
}
