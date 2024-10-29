package io.github.ttypic.swiftklib.gradle.fixture

import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.GradleProject.DslKind
import com.autonomousapps.kit.Source

class CInteropFixture(
    private val swiftSource: SwiftSource? = null,
    private val kotlinSource: KotlinSource? = null,
    swiftklibName: String = "test",
    swiftklibPackage: String = "test"
) : BaseSwiftKlibFixture(swiftklibName, swiftklibPackage) {

    override fun buildProject(): GradleProject {
        return newGradleProjectBuilder(DslKind.KOTLIN)
            .withRootProject {
                withFile(
                    "gradle.properties", """
                    kotlin.mpp.enableCInteropCommonization=true
                """.trimIndent()
                )
            }
            .withSubproject("library") {
                withSources()
                withBaseGradleSetup()
            }
            .write()
    }

    private fun com.autonomousapps.kit.Subproject.Builder.withSources() {
        val sources = mutableListOf<Source>()

        swiftSource?.let {
            withSwiftSource(it)
        }

        if (kotlinSource != null) {
            sources.add(withKotlinSource(kotlinSource))
        } else {
            sources.add(withDefaultKotlinSource())
        }

        this.sources.addAll(sources)
    }
}
