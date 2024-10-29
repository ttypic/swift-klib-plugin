package io.github.ttypic.swiftklib.gradle

import com.autonomousapps.kit.GradleBuilder.build
import com.autonomousapps.kit.GradleBuilder.buildAndFail
import com.autonomousapps.kit.truth.TestKitTruth.Companion.assertThat
import io.github.ttypic.swiftklib.gradle.fixture.SwiftPackageFixture
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class SwiftPackageModulesTest {

    @Test
    fun `build with remote SPM dependency using exact version is successful`() {
        // Given
        val fixture = SwiftPackageFixture(
            swiftCode = """
                import Foundation
                import KeychainAccess

                @objc public class KeychainManager: NSObject {
                    private let keychain = Keychain(service: "test-service")

                    @objc public func save(value: String, forKey key: String) throws {
                        try keychain.set(value, key: key)
                    }
                }
            """.trimIndent(),
            swiftklibConfig = """
                dependencies {
                    remote("KeychainAccess") {
                        github("kishikawakatsumi", "KeychainAccess")
                        exactVersion("4.2.2")
                    }
                }
            """.trimIndent()
        )

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
        val packageResolved = fixture.getPackageResolvedFile()
        assertTrue(packageResolved.exists())
        assertTrue(packageResolved.readText().contains("KeychainAccess"))
    }

    @Test
    fun `build with remote SPM dependency using version range is successful`() {
        // Given
        val fixture = SwiftPackageFixture(
            swiftCode = """
                import Foundation
                import KeychainAccess

                @objc public class KeychainManager: NSObject {
                    private let keychain = Keychain(service: "test-service")
                }
            """.trimIndent(),
            swiftklibConfig = """
                dependencies {
                    remote("KeychainAccess") {
                        github("kishikawakatsumi", "KeychainAccess")
                        versionRange("4.0.0", "5.0.0")
                    }
                }
            """.trimIndent()
        )

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
        val packageResolved = fixture.getPackageResolvedFile()
        assertTrue(packageResolved.exists())
        assertTrue(packageResolved.readText().contains("KeychainAccess"))
    }

    @Test
    fun `build with remote SPM dependency using from version is successful`() {
        // Given
        val fixture = SwiftPackageFixture(
            swiftCode = """
                import Foundation
                import KeychainAccess

                @objc public class KeychainManager: NSObject {
                    private let keychain = Keychain(service: "test-service")
                }
            """.trimIndent(),
            swiftklibConfig = """
                dependencies {
                    remote("KeychainAccess") {
                        github("kishikawakatsumi", "KeychainAccess")
                        fromVersion("4.0.0")
                    }
                }
            """.trimIndent()
        )

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
        val packageResolved = fixture.getPackageResolvedFile()
        assertTrue(packageResolved.exists())
        assertTrue(packageResolved.readText().contains("KeychainAccess"))
    }

    @Test
    fun `build with remote SPM dependency using branch is successful`() {
        // Given
        val fixture = SwiftPackageFixture(
            swiftCode = """
                import Foundation
                import KeychainAccess

                @objc public class KeychainManager: NSObject {
                    private let keychain = Keychain(service: "test-service")
                }
            """.trimIndent(),
            swiftklibConfig = """
                dependencies {
                    remote("KeychainAccess") {
                        github("kishikawakatsumi", "KeychainAccess")
                        branch("master")
                    }
                }
            """.trimIndent()
        )

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
        val packageResolved = fixture.getPackageResolvedFile()
        assertTrue(packageResolved.exists())
        assertTrue(packageResolved.readText().contains("KeychainAccess"))
    }

    @Test
    fun `build with local SPM dependency is successful`() {
        // Given
        val fixture = SwiftPackageFixture(
            swiftCode = """
                import Foundation
                import LocalPackage

                @objc public class VersionProvider: NSObject {
                    @objc public class func getLocalVersion() -> String {
                        return LocalHelper.getVersion()
                    }
                }
            """.trimIndent(),
            swiftklibConfig = """
            """.trimIndent(),
            withLocalPackage = true
        )

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
    }

    @Test
    fun `build with multiple dependencies is successful`() {
        // Given
        val fixture = SwiftPackageFixture(
            swiftCode = """
                import Foundation
                import KeychainAccess
                import SwiftyJSON

                @objc public class DataManager: NSObject {
                    private let keychain = Keychain(service: "test-service")

                    @objc public func processJson(jsonString: String) throws -> String {
                        let json = try JSON(parseJSON: jsonString)
                        return json.description
                    }
                }
            """.trimIndent(),
            swiftklibConfig = """
                dependencies {
                    remote("KeychainAccess") {
                        github("kishikawakatsumi", "KeychainAccess")
                        exactVersion("4.2.2")
                    }
                    remote("SwiftyJSON") {
                        github("SwiftyJSON", "SwiftyJSON")
                        versionRange("5.0.0", "6.0.0")
                    }
                }
            """.trimIndent()
        )

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()

        val packageResolved = fixture.getPackageResolvedFile()
        assertTrue(packageResolved.exists(), "Package.resolved file not found")

        val content = packageResolved.readText()
        assertTrue(
            content.contains("\"identity\" : \"KeychainAccess\"", ignoreCase = true),
            "KeychainAccess dependency not found"
        )
        assertTrue(
            content.contains("\"identity\" : \"SwiftyJSON\"", ignoreCase = true),
            "SwiftyJSON dependency not found"
        )
    }

    @Test
    fun `build fails with blank package name`() {
        // Given
        val fixture = SwiftPackageFixture(
            swiftCode = """
                import Foundation
                @objc public class TestClass: NSObject {}
            """.trimIndent(),
            swiftklibConfig = """
                dependencies {
                    remote("") {
                        github("example", "test")
                        exactVersion("1.0.0")
                    }
                }
            """.trimIndent()
        )

        // When
        val result = buildAndFail(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).output().contains("Package name cannot be blank")
    }

    @Test
    fun `build fails with blank version`() {
        // Given
        val fixture = SwiftPackageFixture(
            swiftCode = """
                import Foundation
                @objc public class TestClass: NSObject {}
            """.trimIndent(),
            swiftklibConfig = """
                dependencies {
                    remote("Test") {
                        github("example", "test")
                        exactVersion("")
                    }
                }
            """.trimIndent()
        )

        // When
        val result = buildAndFail(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).output().contains("Version cannot be blank")
    }

    @Test
    fun `build fails with missing version specification`() {
        // Given
        val fixture = SwiftPackageFixture(
            swiftCode = """
                import Foundation
                @objc public class TestClass: NSObject {}
            """.trimIndent(),
            swiftklibConfig = """
                dependencies {
                    remote("Test") {
                        github("example", "test")
                    }
                }
            """.trimIndent()
        )

        // When
        val result = buildAndFail(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).output().contains("No version specification provided for remote package Test")
    }

    @Test
    fun `build fails with nonexistent local package`() {
        // Given
        val fixture = SwiftPackageFixture(
            swiftCode = """
                import Foundation
                @objc public class TestClass: NSObject {}
            """.trimIndent(),
            swiftklibConfig = """
                dependencies {
                    local("LocalPackage", file("nonexistent/path"))
                }
            """.trimIndent()
        )

        // When
        val result = buildAndFail(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).output().contains("Package path must exist")
    }
}

