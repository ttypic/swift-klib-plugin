package io.github.ttypic.swiftklib.gradle

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

    private fun testBuild(
        @Language("swift")
        swiftCode: String,
        @Language("kotlin")
        kotlinCode: String = "",
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

        swiftCodeFile.writeText(swiftCode)
        kotlinCodeFile.writeText(kotlinCode)

        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("build")
            .withPluginClasspath()
            .build()
            .asserter()
    }
}
