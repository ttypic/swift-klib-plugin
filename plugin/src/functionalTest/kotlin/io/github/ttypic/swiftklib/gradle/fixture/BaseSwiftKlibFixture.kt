package io.github.ttypic.swiftklib.gradle.fixture

import com.autonomousapps.kit.AbstractGradleProject
import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.RootProject
import com.autonomousapps.kit.Source
import com.autonomousapps.kit.Subproject
import com.autonomousapps.kit.gradle.Plugin
import org.intellij.lang.annotations.Language

abstract class BaseSwiftKlibFixture(
    protected val swiftklibName: String = "test",
    protected val swiftklibPackage: String = "test"
) : AbstractGradleProject() {

    protected val pluginVersion = System.getProperty("com.autonomousapps.plugin-under-test.version")
    private var _gradleProject: GradleProject? = null

    protected val swiftklibSrcPath = "src/main/swift/$swiftklibName"

    val gradleProject: GradleProject
        get() = _gradleProject ?: buildProject().also { _gradleProject = it }

    protected abstract fun buildProject(): GradleProject

    protected fun Subproject.Builder.withSwiftSource(source: SwiftSource) {
        withFile("$swiftklibSrcPath/${source.filename}", source.content)
    }

    protected fun Subproject.Builder.withSwiftSources(vararg sources: SwiftSource) {
        sources.forEach { withSwiftSource(it) }
    }

    protected fun RootProject.Builder.withPackageSwift(source: PackageSource) {
        withFile("$swiftklibName/Package.swift", source.content)
    }

    protected fun Subproject.Builder.withKotlinSource(source: KotlinSource): Source =
        Source.kotlin(source.content)
            .withPath(source.packageName, source.className)
            .build().apply {
                sources.add(this)
            }

    protected fun Subproject.Builder.withDefaultKotlinSource(): Source =
        withKotlinSource(KotlinSource.default())

    protected fun Subproject.Builder.withBaseGradleSetup(
        @Language("kotlin") additionalConfig: String = "",
        @Language("kotlin") swiftklibConfig: String = ""
    ) {
        withBuildScript {
            plugins(
                Plugin.kotlinMultiplatform,
                Plugin("io.github.ttypic.swiftklib", pluginVersion)
            )
            withKotlin(
                """
                @file:OptIn(io.github.ttypic.swiftklib.gradle.api.ExperimentalSwiftklibApi::class)
                kotlin {
                    compilerOptions {
                        optIn.addAll("kotlinx.cinterop.ExperimentalForeignApi")
                    }

                    listOf(
                        iosX64(),
                        iosArm64(),
                        iosSimulatorArm64()
                    ).forEach {
                        it.compilations {
                            val main by getting {
                                cinterops.create("$swiftklibName")
                            }
                        }
                    }

                    $additionalConfig
                }

                swiftklib {
                    create("$swiftklibName") {
                        path = file("$swiftklibSrcPath")
                        packageName("$swiftklibPackage")

                        $swiftklibConfig
                    }
                }
                """.trimIndent()
            )
        }
    }

    companion object {
        val Plugin.Companion.kotlinMultiplatform
            get() = Plugin.of("org.jetbrains.kotlin.multiplatform", "2.0.21")
    }
}
