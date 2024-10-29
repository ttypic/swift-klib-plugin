package io.github.ttypic.swiftklib.gradle.fixture

import org.intellij.lang.annotations.Language

class PackageSource private constructor(
    @Language("Swift") val content: String
) {
    companion object {
        fun packageSwift(@Language("Swift") content: String): PackageSource =
            PackageSource(content)

        fun defaultPackage(name: String) = packageSwift("""
            // swift-tools-version:5.6
            import PackageDescription

            let package = Package(
                name: "$name",
                products: [
                    .library(name: "$name", targets: ["$name"]),
                ],
                targets: [
                    .target(name: "$name")
                ]
            )
        """.trimIndent())
    }
}
