package io.github.ttypic.swiftklib.gradle

import com.autonomousapps.kit.GradleBuilder.build
import com.autonomousapps.kit.GradleBuilder.buildAndFail
import com.autonomousapps.kit.truth.TestKitTruth.Companion.assertThat
import io.github.ttypic.swiftklib.gradle.fixture.SwiftKlibTestFixture
import io.github.ttypic.swiftklib.gradle.fixture.SwiftSource
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class SwiftPackageModulesTest {

    @Test
    fun `build with remote SPM dependency using exact version is successful`() {
        // Given
        val fixture = SwiftKlibTestFixture.builder()
            .withSwiftSources(
                SwiftSource.of(content = """
                    import Foundation
                    import KeychainAccess

                    @objc public class KeychainManager: NSObject {
                        private let keychain = Keychain(service: "test-service")

                        @objc public func save(value: String, forKey key: String) throws {
                            try keychain.set(value, key: key)
                        }
                    }
                """.trimIndent())
            )
            .withConfiguration {
                dependencies {
                    remote("KeychainAccess") {
                        github("kishikawakatsumi", "KeychainAccess")
                        exactVersion("4.2.2")
                    }
                }
            }
            .build()

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
        assertPackageResolved(fixture, "KeychainAccess")
    }

    @Test
    fun `build with remote SPM dependency using version range is successful`() {
        val fixture = SwiftKlibTestFixture.builder()
            .withSwiftSources(
                SwiftSource.of(content = """
                    import Foundation
                    import KeychainAccess

                    @objc public class KeychainManager: NSObject {
                        private let keychain = Keychain(service: "test-service")
                    }
                """.trimIndent())
            )
            .withConfiguration {
                dependencies {
                    remote("KeychainAccess") {
                        github("kishikawakatsumi", "KeychainAccess")
                        versionRange("4.0.0", "5.0.0", true)
                    }
                }
            }
            .build()

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
        assertPackageResolved(fixture, "KeychainAccess")
    }

    @Test
    fun `build with remote SPM dependency using branch is successful`() {
        val fixture = SwiftKlibTestFixture.builder()
            .withSwiftSources(
                SwiftSource.of(content = """
                import Foundation
                import KeychainAccess

                @objc public class KeychainManager: NSObject {
                    private let keychain = Keychain(service: "test-service")
                }
            """.trimIndent())
            )
            .withConfiguration {
                dependencies {
                    remote("KeychainAccess") {
                        github("kishikawakatsumi", "KeychainAccess")
                        branch("master")
                    }
                }
            }
            .build()

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
        assertPackageResolved(fixture, "KeychainAccess")
    }

    @Test
    fun `build with local SPM dependency is successful`() {
        // Given
        val localPackageDir = File(createTempDir(), "LocalPackage").apply {
            mkdirs()
            // Create Package.swift
            File(this, "Package.swift").writeText("""
            // swift-tools-version:5.3
            import PackageDescription

            let package = Package(
                name: "LocalPackage",
                products: [
                    .library(name: "LocalPackage", targets: ["LocalPackage"]),
                ],
                targets: [
                    .target(name: "LocalPackage"),
                ]
            )
        """.trimIndent())

            // Create source files
            File(this, "Sources/LocalPackage").mkdirs()
            File(this, "Sources/LocalPackage/LocalHelper.swift").writeText("""
            import Foundation

            @objc public class LocalHelper: NSObject {
                @objc public class func getVersion() -> String {
                    return "1.0.0"
                }
            }
        """.trimIndent())
        }

        val fixture = SwiftKlibTestFixture.builder()
            .withSwiftSources(
                SwiftSource.of(content = """
                import Foundation
                import LocalPackage

                @objc public class VersionProvider: NSObject {
                    @objc public class func getLocalVersion() -> String {
                        return LocalHelper.getVersion()
                    }
                }
            """.trimIndent())
            )
            .withConfiguration {
                dependencies {
                    local("LocalPackage", localPackageDir)
                }
            }
            .build()

        try {
            // When
            val result = build(fixture.gradleProject.rootDir, "build")

            // Then
            assertThat(result).task(":library:build").succeeded()
        } finally {
            localPackageDir.deleteRecursively()
        }
    }

    @Test
    fun `build with multiple dependencies is successful`() {
        val fixture = SwiftKlibTestFixture.builder()
            .withSwiftSources(
                SwiftSource.of(content = """
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
                """.trimIndent())
            )
            .withConfiguration {
                dependencies {
                    remote("KeychainAccess") {
                        github("kishikawakatsumi", "KeychainAccess")
                        exactVersion("4.2.2")
                    }
                    remote("SwiftyJSON") {
                        github("SwiftyJSON", "SwiftyJSON")
                        versionRange("5.0.0", "6.0.0", true)
                    }
                }
            }
            .build()

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
        assertPackageResolved(fixture, "KeychainAccess", "SwiftyJSON")
    }

    @Test
    fun `build fails with blank package name`() {
        // Given
        val fixture = SwiftKlibTestFixture.builder()
            .withConfiguration {
                dependencies {
                    remote("") {  // Empty package name
                        github("example", "test")
                        exactVersion("1.0.0")
                    }
                }
            }
            .build()

        // When
        val result = buildAndFail(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).output().contains("Package name cannot be blank")
    }

    @Test
    fun `build fails with blank version`() {
        // Given
        val fixture = SwiftKlibTestFixture.builder()
            .withConfiguration {
                dependencies {
                    remote("Test") {
                        github("example", "test")
                        exactVersion("")  // Empty version
                    }
                }
            }
            .build()

        // When
        val result = buildAndFail(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).output().contains("Version cannot be blank")
    }

    @Test
    fun `build fails with missing version specification`() {
        // Given
        val fixture = SwiftKlibTestFixture.builder()
            .withConfiguration {
                dependencies {
                    remote("Test") {
                        github("example", "test")
                        // No version specified
                    }
                }
            }
            .build()

        // When
        val result = buildAndFail(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).output().contains("No version specification provided for remote package Test")
    }

    @Test
    fun `build fails with nonexistent local package`() {
        // Given
        val nonexistentPath = File("nonexistent/path")
        val fixture = SwiftKlibTestFixture.builder()
            .withConfiguration {
                dependencies {
                    local("LocalPackage", nonexistentPath)
                }
            }
            .build()

        // When
        val result = buildAndFail(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).output().contains("Package path must exist")
    }

    @Test
    fun `build with remote SPM dependency using Firebase is successful`() {
        // Given
        val fixture = SwiftKlibTestFixture.builder()
            .withSwiftSources(
                SwiftSource.of(content = """
                import FirebaseAuth
                import Firebase

                 @objc public class FirebaseData: NSObject {
                     @objc public func printVersion() {
                         print(FirebaseVersion())
                         print(ActionCodeOperation.emailLink)
                    }
                }
                """.trimIndent())
            )
            .withConfiguration {
                minIos = "14.0"
                minMacos = "10.15"
                dependencies {
                    remote("FirebaseAuth") {
                        url("https://github.com/firebase/firebase-ios-sdk.git", "firebase-ios-sdk")
                        exactVersion("11.0.0")
                    }
                }
            }
            .build()

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
        assertPackageResolved(fixture, "firebase-ios-sdk")
    }

    @Test
    fun `build with remote SPM dependency using multi product Firebase is successful`() {
        // Given
        val fixture = SwiftKlibTestFixture.builder()
            .withSwiftSources(
                SwiftSource.of(content = """
                import FirebaseAuth
                import Firebase
                import FirebaseRemoteConfig

                 @objc public class FirebaseData: NSObject {
                     @objc public func testLinking() {
                         print(FirebaseVersion())
                         print(ActionCodeOperation.emailLink)
                         print(RemoteConfigSettings())
                    }
                }
                """.trimIndent())
            )
            .withConfiguration {
                minIos = "14.0"
                minMacos = "10.15"
                dependencies {
                    remote(listOf("FirebaseAuth", "FirebaseRemoteConfig")) {
                        url("https://github.com/firebase/firebase-ios-sdk.git", "firebase-ios-sdk")
                        exactVersion("11.0.0")
                    }
                }
            }
            .build()

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
        assertPackageResolved(fixture, "firebase-ios-sdk")
    }

    @Test
    fun `build with complex and mix spm repo`() {
        // Given
        val fixture = SwiftKlibTestFixture.builder()
            .withSwiftSources(
                SwiftSource.of(content = """
                import FirebaseAuth
                import Firebase
                import FirebaseRemoteConfig
                import KeychainAccess
                import SwiftyJSON

                @objc public class FirebaseData: NSObject {
                     @objc public func testLinking() {
                         print(FirebaseVersion())
                         print(ActionCodeOperation.emailLink)
                         print(RemoteConfigSettings())
                    }
                }
                @objc public class DataManager: NSObject {
                    private let keychain = Keychain(service: "test-service")

                    @objc public func processJson(jsonString: String) throws -> String {
                        let json = try JSON(parseJSON: jsonString)
                        return json.description
                    }
                }
                """.trimIndent())
            )
            .withConfiguration {
                minIos = "14.0"
                minMacos = "10.15"
                dependencies {
                    remote(listOf("FirebaseAuth", "FirebaseRemoteConfig")) {
                        url("https://github.com/firebase/firebase-ios-sdk.git", "firebase-ios-sdk")
                        exactVersion("11.0.0")
                    }
                    remote("KeychainAccess") {
                        github("kishikawakatsumi", "KeychainAccess")
                        exactVersion("4.2.2")
                    }
                    remote("SwiftyJSON") {
                        github("SwiftyJSON", "SwiftyJSON")
                        versionRange("5.0.0", "6.0.0", true)
                    }
                }
            }
            .build()

        // When
        val result = build(fixture.gradleProject.rootDir, "build")

        // Then
        assertThat(result).task(":library:build").succeeded()
        assertPackageResolved(fixture, "firebase-ios-sdk")
    }

    private fun assertPackageResolved(fixture: SwiftKlibTestFixture, vararg packageNames: String) {
        val resolvedFile = File(
            fixture.gradleProject.rootDir,
            "library/build/swiftklib/test/iosArm64/swiftBuild/Package.resolved"
        )
        assertTrue(resolvedFile.exists(), "Package.resolved file not found")

        val content = resolvedFile.readText()
        packageNames.forEach { packageName ->
            assertTrue(
                content.contains("\"identity\" : \"$packageName\"", ignoreCase = true),
                "$packageName dependency not found"
            )
        }
    }
}
