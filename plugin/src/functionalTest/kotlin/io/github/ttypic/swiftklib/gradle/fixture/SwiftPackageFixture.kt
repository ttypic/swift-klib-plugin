package io.github.ttypic.swiftklib.gradle.fixture

import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.GradleProject.DslKind
import com.autonomousapps.kit.RootProject
import org.intellij.lang.annotations.Language
import java.io.File

class SwiftPackageFixture(
    @Language("swift") private val swiftCode: String,
    @Language("kotlin") private val swiftklibConfig: String = "",
    swiftklibName: String = "test",
    private val withLocalPackage: Boolean = false,
) : BaseSwiftKlibFixture(swiftklibName) {

    override fun buildProject(): GradleProject {
        return newGradleProjectBuilder(DslKind.KOTLIN)
            .withRootProject {
                if (withLocalPackage) {
                    withLocalPackage()
                }
                withFile(
                    "gradle.properties", """
                    kotlin.mpp.enableCInteropCommonization=true
                """.trimIndent()
                )
            }
            .withSubproject("library") {
                sources.add(withDefaultKotlinSource())
                withSwiftSource(SwiftSource.of(content = swiftCode))
                withBaseGradleSetup(
                    swiftklibConfig = """
                        $swiftklibConfig
                        ${getLocalPackageConfig()}
                    """.trimIndent()
                )
            }
            .write()
    }

    private fun getLocalPackageConfig(): String {
        if (!withLocalPackage) return ""
        return """
            dependencies {
                local("LocalPackage", rootProject.file("LocalPackage"))
            }
        """.trimIndent()
    }

    private fun RootProject.Builder.withLocalPackage() {
        withFile(
            "LocalPackage/Package.swift",
            PackageSource.defaultPackage("LocalPackage").content
        )

        withFile(
            "LocalPackage/Sources/LocalPackage/LocalPackage.swift",
            SwiftSource.of(
                content = """
                import Foundation

                @objc public class LocalHelper: NSObject {
                    @objc public class func getVersion() -> String { return "1.0.0" }
                }
            """.trimIndent()
            ).content
        )
    }

    fun getPackageResolvedFile(): File =
        File(gradleProject.rootDir, "library/build/swiftklib/$swiftklibName/iosArm64/swiftBuild/Package.resolved")
}
