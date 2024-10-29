package io.github.ttypic.swiftklib.gradle

import com.autonomousapps.kit.GradleBuilder.build
import com.autonomousapps.kit.truth.TestKitTruth.Companion.assertThat
import io.github.ttypic.swiftklib.gradle.fixture.KotlinSource
import io.github.ttypic.swiftklib.gradle.fixture.SwiftKlibTestFixture
import io.github.ttypic.swiftklib.gradle.fixture.SwiftSource
import org.junit.jupiter.api.Test

class CinteropModulesTest {

    @Test
    fun `build with imported UIKit framework is successful`() {
        assumeMacos()

        // Given
        val fixture = SwiftKlibTestFixture.builder()
            .withSwiftSources(
                SwiftSource.of(
                    content = """
                    import UIKit
                    @objc public class TestView: UIView {}
                    """.trimIndent()
                )
            )
            .withKotlinSources(
                KotlinSource.of(
                    content = """
                    package test
                    import test.TestView
                    val view = TestView()
                    """.trimIndent()
                )
            )
            .build()

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
    }

    @Test
    fun `build on linux results in warning about unsupported OS`() {
        assumeLinux()

        // Given
        val fixture = SwiftKlibTestFixture.builder()
            .withSwiftSources(
                SwiftSource.of(
                    content = """
                    import Foundation
                    @objc public class TestClass: NSObject {}
                    """.trimIndent()
                )
            )
            .build()

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).output().contains("Current host OS is not macOS. Disabling SwiftKlib plugin")
    }
}
